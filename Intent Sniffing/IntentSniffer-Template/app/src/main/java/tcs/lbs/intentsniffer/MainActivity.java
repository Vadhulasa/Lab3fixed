package tcs.lbs.intentsniffer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;

public class MainActivity extends AppCompatActivity
{
    TextView LatitudeTextView, LongitudeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction("tcs.lbs.locationapp.MainActivityReceiver"); // intra-app
        filter.addAction("tcs.lbs.weather_app.WeatherBroadcastReceiver"); // inter-app

        registerReceiver(locationReceiver, filter, Context.RECEIVER_EXPORTED);

        LatitudeTextView = findViewById(R.id.LatitudeTextView);
        LongitudeTextView = findViewById(R.id.LongitudeTextView);


        // TODO Define a broadcast receiver class
        // TODO Register to receive broadcast messages from ForegroundLocationService of the LocationApp
        // TODO Extract Location coordinates from the broadcast message and show them in LatitudeTextView and LongitudeTextView
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Location location = intent.getParcelableExtra("Location");

            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                LatitudeTextView.setText(String.valueOf(lat));
                LongitudeTextView.setText(String.valueOf(lon));
            }
        }
    };
}
