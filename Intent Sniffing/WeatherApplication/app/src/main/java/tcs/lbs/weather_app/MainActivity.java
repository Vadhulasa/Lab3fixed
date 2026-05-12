package tcs.lbs.weather_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;


// This is a dummy app
// It serves the functionality of receiving an intra-app broadcast from ForegroundLocationService and shows a Random weather.
public class MainActivity extends AppCompatActivity
{
    WeatherBroadcastReceiver mReceiver;
    TextView textViewWeather;

    static final String RECEIVER_ACTION = "tcs.lbs.weather_app.WeatherBroadcastReceiver";
    String[] weatherType = {"Rainy", "Snowy", "Sunny", "Stormy"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewWeather = findViewById(R.id.textViewWeather);
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(RECEIVER_ACTION);

        mReceiver = new WeatherBroadcastReceiver();
        if (Build.VERSION.SDK_INT >= 33)
        {
            registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
        }
        else
        {
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(mReceiver);
    }


    public class WeatherBroadcastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            textViewWeather.setText(weatherType[new Random().nextInt(weatherType.length)]);
        }
    }
}
