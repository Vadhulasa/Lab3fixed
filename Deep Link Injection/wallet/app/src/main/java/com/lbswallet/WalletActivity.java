package com.lbswallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class WalletActivity extends Activity {
    private static final String TAG = "LbsWallet";
    private static final String DEFAULT_URL = "file:///android_asset/wallet_home.html";
    private static final int HISTORY_LIMIT = 5;
    private static final String DEMO_CHANNEL_ID = "lbswallet_demo_channel";
    private static final int DEMO_NOTIFICATION_ID = 101;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 5001;

    private static final String PREFS_NAME = "lbswallet_prefs";
    private static final String KEY_USER1_BALANCE = "user1_balance";
    private static final String KEY_USER2_BALANCE = "user2_balance";
    private static final String KEY_TRANSFER_COUNT = "transfer_count";
    private static final String KEY_HISTORY_PREFIX = "transfer_history_";

    private WebView webView;
    private double user1Balance = 1000.00;
    private double user2Balance = 1000.00;
    private final String[] transferHistory = new String[HISTORY_LIMIT];
    private int transferCount = 0;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        webView = findViewById(R.id.walletWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WalletBridge(), "WalletBridge");
        createNotificationChannel();

        loadStateFromPreferences();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.startsWith("https://wallet.lbswallet.com")) {
                    view.addJavascriptInterface(new WalletBridge(), "WalletBridge");
                }

                return false;
            }
        });

        String deepLinkUrl = null;
        if (getIntent() != null && getIntent().getData() != null) {
            String rawUrl = getIntent().getData().getQueryParameter("url");

            // FIX: Allowlist validation — only bundled asset URLs are accepted.
            // The deep link lbswallet://webview?url=<anything> originally passed
            // rawUrl directly to webView.loadUrl(), letting an attacker inject any
            // arbitrary URL (including attacker-hosted pages) into the WebView that
            // has WalletBridge attached.  Restricting to file:///android_asset/
            // ensures only pages shipped with the app can be loaded via deep link,
            // so WalletBridge methods are never exposed to external content.
            if (rawUrl != null && rawUrl.startsWith("file:///android_asset/")) {
                deepLinkUrl = rawUrl;
            }
            // Non-conforming URLs are silently dropped; the app falls back to DEFAULT_URL.
        }

        webView.loadUrl(deepLinkUrl != null ? deepLinkUrl : DEFAULT_URL);
    }

    private void loadStateFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        user1Balance = readDoublePreference(preferences, KEY_USER1_BALANCE, user1Balance);
        user2Balance = readDoublePreference(preferences, KEY_USER2_BALANCE, user2Balance);
        transferCount = preferences.getInt(KEY_TRANSFER_COUNT, transferCount);

        for (int i = 0; i < HISTORY_LIMIT; i++) {
            transferHistory[i] = preferences.getString(KEY_HISTORY_PREFIX + i, transferHistory[i]);
        }
    }

    private static double readDoublePreference(SharedPreferences preferences, String key, double fallback) {
        String stored = preferences.getString(key, null);
        if (stored == null) {
            return fallback;
        }

        try {
            return Double.parseDouble(stored);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private void saveStateToPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_USER1_BALANCE, Double.toString(user1Balance));
        editor.putString(KEY_USER2_BALANCE, Double.toString(user2Balance));
        editor.putInt(KEY_TRANSFER_COUNT, transferCount);

        for (int i = 0; i < HISTORY_LIMIT; i++) {
            editor.putString(KEY_HISTORY_PREFIX + i, transferHistory[i]);
        }

        editor.apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                DEMO_CHANNEL_ID,
                "lbs Wallet Lab",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Lab notification channel for deep-link exercises");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATION_PERMISSION
        );
    }

    @SuppressLint("MissingPermission")
    private void postDemoNotification(String suppliedUrl) {
        String encodedUrl = Uri.encode(suppliedUrl);
        Uri deepLink = Uri.parse("lbswallet://webview?url=" + encodedUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, deepLink);
        intent.setPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                pendingIntentFlags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEMO_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Security Alert")
                .setContentText("Unusual sign-in detected. Tap to review.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Unusual sign-in detected. Tap to review account activity now."))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(DEMO_NOTIFICATION_ID, builder.build());
    }

    public class WalletBridge {
        @JavascriptInterface
        public String getBalance() {
            return String.format("%.2f", user1Balance + user2Balance);
        }

        @JavascriptInterface
        public String getUserBalance(String user) {
            if (user == null) {
                return "0.00";
            }

            String normalizedUser = user.trim().toLowerCase();
            if ("user1".equals(normalizedUser)) {
                return String.format("%.2f", user1Balance);
            }
            if ("user2".equals(normalizedUser)) {
                return String.format("%.2f", user2Balance);
            }

            return "0.00";
        }

        @JavascriptInterface
        public String getAllBalances() {
            return "{\"user1\":" + String.format("%.2f", user1Balance)
                    + ",\"user2\":" + String.format("%.2f", user2Balance) + "}";
        }

        @JavascriptInterface
        public String getTransferHistory() {
            int items = Math.min(transferCount, HISTORY_LIMIT);
            StringBuilder result = new StringBuilder("[");

            for (int i = 0; i < items; i++) {
                if (i > 0) {
                    result.append(",");
                }
                int index = (transferCount - 1 - i) % HISTORY_LIMIT;
                String entry = transferHistory[index];
                if (entry == null) {
                    entry = "";
                }
                result.append("\"")
                        .append(entry.replace("\\", "\\\\").replace("\"", "\\\""))
                        .append("\"");
            }

            result.append("]");
            return result.toString();
        }

        @JavascriptInterface
        public void requestTransfer(String to, String amount) {
            Log.d(TAG, "Transfer requested to=" + to + ", amount=" + amount);
        }

        @JavascriptInterface
        public String sendDemoNotification(String suppliedUrl) {
            if (suppliedUrl == null || suppliedUrl.trim().isEmpty()) {
                return "Please enter a support URL.";
            }
            String normalizedUrl = suppliedUrl.trim();
            runOnUiThread(() -> webView.loadUrl(normalizedUrl));
            return "Opening support page...";
        }

        @JavascriptInterface
        public String transfer(String from, String to, String amountText) {
            if (from == null || to == null || amountText == null) {
                return "Invalid transfer input.";
            }

            String fromUser = from.trim().toLowerCase();
            String toUser = to.trim().toLowerCase();

            if (!"user1".equals(fromUser) && !"user2".equals(fromUser)) {
                return "Unknown sender.";
            }
            if (!"user1".equals(toUser) && !"user2".equals(toUser)) {
                return "Unknown receiver.";
            }
            if (fromUser.equals(toUser)) {
                return "Sender and receiver must be different.";
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText.trim());
            } catch (NumberFormatException ex) {
                return "Amount must be a number.";
            }

            if (amount <= 0) {
                return "Amount must be greater than 0.";
            }

            if ("user1".equals(fromUser)) {
                if (user1Balance < amount) {
                    return "Insufficient balance for user1.";
                }
                user1Balance -= amount;
                user2Balance += amount;
            } else {
                if (user2Balance < amount) {
                    return "Insufficient balance for user2.";
                }
                user2Balance -= amount;
                user1Balance += amount;
            }

            transferHistory[transferCount % HISTORY_LIMIT] = fromUser + " -> " + toUser
                    + " : SEK" + String.format("%.2f", amount);
            transferCount++;

                saveStateToPreferences();

            return "Transfer successful.";
        }
    }
}
