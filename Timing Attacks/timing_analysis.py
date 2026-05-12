#!/usr/bin/env python3
"""
Task 4 – Timing Attack Analysis
================================
Usage:
    1. On the emulator run both Messenger users and send several messages.
    2. Capture the log:   adb logcat -s AppLog > timing_log.txt
    3. Run:               python3 timing_analysis.py timing_log.txt

The script parses the AppLog entries from ConversationActivity, computes
time-to-server and time-to-receiver per message, and matches those values
against the CDN/city RTT tables from the lab spec to infer user locations.
"""

import re
import sys
from datetime import datetime, timedelta
from collections import defaultdict

# ---------------------------------------------------------------------------
# RTT tables from the lab PDF (in ms)
# ---------------------------------------------------------------------------

CDN_RTT = {          # Table 1 — Regional CDN servers
    "Skyrim":     803,
    "Morrowind":  384,
    "Hammerfell": 723,
    "Valenwood":  547,
    "Elsweyr":    427,
    "Cyrodiil":   1052,
}

CITY_RTT = {         # Table 2 — City-to-regional-CDN RTT
    "Skyrim":    {"Riften": 113, "Whiterun": 375, "Windhelm": 342,
                  "Solitude": 312, "Markarth": 149, "Falkreath": 255},
    "Morrowind": {"Vivec": 247, "Mournhold": 345, "Balmora": 307,
                  "Ald'ruhn": 128, "Blacklight": 387, "Narsis": 289},
    "Hammerfell":{"Sentinel": 157, "Rihad": 286, "Taneth": 336,
                  "Elinhir": 305, "Dragonstar": 356, "Hegathe": 220},
    "Valenwood": {"Falinesti": 340, "Elden Root": 244, "Haven": 128,
                  "Silvenar": 72, "Arenthia": 321, "Southpoint": 389},
    "Cyrodiil":  {"Anvil": 209, "Bruma": 305, "Bravil": 260,
                  "Chorrol": 189, "Leyawiin": 245, "Cheydinhal": 326},
    "Elsweyr":   {"Rimmen": 129, "Riverhold": 336, "Orcrest": 302,
                  "Dune": 285, "Senchal": 420, "Torval": 168},
}

# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------

# Log line format produced by ConversationActivity.java:
#   Log.d("AppLog", "User:001 ; Type:new_msg ; Message:hello ; MessageID:1 ; Time:10:30:01.123")
LOG_RE = re.compile(
    r"User:(\S+)\s*;\s*Type:(\S+)\s*;\s*Message:(.*?)\s*;\s*MessageID:(\d+)\s*;\s*Time:(\S+)"
)

TIME_FMT_NANO = "%H:%M:%S.%f"   # LocalTime.now() format; nanoseconds truncated to 6 digits


def parse_time(raw: str) -> datetime:
    """Parse LocalTime string like 10:30:01.123456789 (truncate to microseconds)."""
    # Java LocalTime can produce 9 fractional digits; Python only handles 6
    parts = raw.split(".")
    frac = (parts[1] if len(parts) > 1 else "0")[:6].ljust(6, "0")
    return datetime.strptime(f"{parts[0]}.{frac}", TIME_FMT_NANO)


def parse_log(path: str):
    """Return list of dicts with parsed fields."""
    entries = []
    with open(path) as fh:
        for line in fh:
            m = LOG_RE.search(line)
            if m:
                entries.append({
                    "user":      m.group(1),
                    "type":      m.group(2),
                    "message":   m.group(3),
                    "msg_id":    int(m.group(4)),
                    "time":      parse_time(m.group(5)),
                })
    return entries

# ---------------------------------------------------------------------------
# Timing calculation
# ---------------------------------------------------------------------------

def compute_timings(entries):
    """
    For each (user, msg_id) triple return:
      time_to_server   = server_ack.time  - new_msg.time  (ms)
      time_to_receiver = receiver_ack.time - server_ack.time (ms)
    """
    # Index by (user, msg_id, type)
    index = {}
    for e in entries:
        key = (e["user"], e["msg_id"], e["type"])
        index[key] = e

    results = []
    seen = set()
    for e in entries:
        if e["type"] != "new_msg":
            continue
        uid, mid = e["user"], e["msg_id"]
        if (uid, mid) in seen:
            continue
        seen.add((uid, mid))

        t_sent   = index.get((uid, mid, "new_msg"))
        t_server = index.get((uid, mid, "server_ack"))
        t_rcv    = index.get((uid, mid, "receiver_ack"))

        if not (t_sent and t_server and t_rcv):
            continue  # incomplete triple – skip

        tts = (t_server["time"] - t_sent["time"]) / timedelta(milliseconds=1)
        ttr = (t_rcv["time"]   - t_server["time"]) / timedelta(milliseconds=1)

        results.append({
            "user":             uid,
            "msg_id":           mid,
            "time_to_server":   tts,
            "time_to_receiver": ttr,
        })
    return results

# ---------------------------------------------------------------------------
# Location inference
# ---------------------------------------------------------------------------

def best_match(measured_ms: float, rtt_table: dict) -> tuple:
    """Return (name, rtt) whose RTT is closest to measured_ms."""
    return min(rtt_table.items(), key=lambda kv: abs(kv[1] - measured_ms))


def infer_location(time_to_server_ms: float):
    """
    time_to_server ≈ city RTT + CDN RTT (one-way, so divide total by 2 if
    the server measures round-trip; the lab server simulates one-way latency
    by applying the delay once per hop, so we compare directly).

    Strategy:
      1. Find the CDN region whose RTT is closest to time_to_server.
      2. Within that region, find the city whose city RTT brings the total
         closest to time_to_server.
    """
    region, cdn_rtt = best_match(time_to_server_ms, CDN_RTT)
    cities = CITY_RTT[region]
    residual = time_to_server_ms - cdn_rtt
    city, city_rtt = best_match(residual, cities)
    total_rtt = cdn_rtt + city_rtt
    return region, city, total_rtt

# ---------------------------------------------------------------------------
# Per-user aggregation and report
# ---------------------------------------------------------------------------

def aggregate(timings):
    """Average time_to_server and time_to_receiver per user."""
    sums   = defaultdict(lambda: {"tts": 0.0, "ttr": 0.0, "n": 0})
    for t in timings:
        u = t["user"]
        sums[u]["tts"] += t["time_to_server"]
        sums[u]["ttr"] += t["time_to_receiver"]
        sums[u]["n"]   += 1
    avgs = {}
    for u, s in sums.items():
        avgs[u] = {
            "avg_tts": s["tts"] / s["n"],
            "avg_ttr": s["ttr"] / s["n"],
            "count":   s["n"],
        }
    return avgs


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else "timing_log.txt"
    entries = parse_log(path)
    if not entries:
        print("No AppLog entries found in", path)
        return

    timings = compute_timings(entries)
    if not timings:
        print("Could not pair new_msg/server_ack/receiver_ack triples.")
        return

    user_avgs = aggregate(timings)

    print(f"\n{'='*70}")
    print("  Task 4 – Timing Attack: Location Inference")
    print(f"{'='*70}\n")

    for user, avg in sorted(user_avgs.items()):
        tts = avg["avg_tts"]
        ttr = avg["avg_ttr"]
        n   = avg["count"]

        sender_region, sender_city, _ = infer_location(tts)
        recv_region,   recv_city,   _ = infer_location(ttr)

        print(f"  User {user}  ({n} message(s) averaged)")
        print(f"    Avg time-to-server  : {tts:.1f} ms  → sender ≈ {sender_city}, {sender_region}")
        print(f"    Avg time-to-receiver: {ttr:.1f} ms  → receiver ≈ {recv_city}, {recv_region}")
        print()

    print(f"{'='*70}")
    print("  Per-message detail")
    print(f"{'='*70}")
    for t in timings:
        sr, sc, _ = infer_location(t["time_to_server"])
        rr, rc, _ = infer_location(t["time_to_receiver"])
        print(f"  User:{t['user']}  MsgID:{t['msg_id']:>3}  "
              f"TTS:{t['time_to_server']:>7.1f}ms ({sc}/{sr})  "
              f"TTR:{t['time_to_receiver']:>7.1f}ms ({rc}/{rr})")


if __name__ == "__main__":
    main()
