package tcs.lbs.intentsniffer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView LatitudeTextView, LongitudeTextView;

    // Single receiver instance shared for both broadcast actions
    private BroadcastReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LatitudeTextView  = findViewById(R.id.LatitudeTextView);
        LongitudeTextView = findViewById(R.id.LongitudeTextView);

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // The Location object is packed as a Parcelable extra with key "Location"
                // in ForegroundLocationService.onLocationChanged()
                Location location = intent.getParcelableExtra("Location");
                if (location != null) {
                    LatitudeTextView.setText(String.valueOf(location.getLatitude()));
                    LongitudeTextView.setText(String.valueOf(location.getLongitude()));
                }
            }
        };

        IntentFilter filter = new IntentFilter();

        // VULNERABILITY: ForegroundLocationService calls sendBroadcast() with no
        // receiverPermission, so any app that registers for these action strings
        // can intercept location updates.

        // Sniff intra-app broadcast (LocationApp → its own MainActivity)
        filter.addAction("tcs.lbs.locationapp.MainActivityReceiver");

        // Sniff inter-app broadcast (LocationApp → WeatherApplication)
        filter.addAction("tcs.lbs.weather_app.WeatherBroadcastReceiver");

        // On API 33+ the flag RECEIVER_EXPORTED is mandatory for dynamic receivers
        // that need to receive broadcasts from other apps/services.
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(locationReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(locationReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Always unregister to avoid memory leaks
        if (locationReceiver != null) {
            unregisterReceiver(locationReceiver);
        }
    }
}
