package tcs.lbs.locationapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class ForegroundLocationService extends Service implements LocationListener
{
    public static final String ACTION_StartForegroundService = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_StopForegroundService = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String CHANNEL_ID = "LOCATION_SERVICE_CHANNEL_ID";
    public static final String CHANNEL_NAME = "LOCATION_SERVICE_CHANNEL";


    public static boolean isForegroundServiceRunning = false;

    Intent locationAppIntent, weatherIntent;

    protected LocationManager locationManager;
    private String provider;



    @Override
    public void onCreate()
    {
        super.onCreate();

        locationAppIntent = new Intent();
        weatherIntent = new Intent();
        weatherIntent.setAction("tcs.lbs.weather_app.WeatherBroadcastReceiver");
        locationAppIntent.setAction("tcs.lbs.locationapp.MainActivityReceiver");
        locationAppIntent.setPackage(getPackageName());
        weatherIntent.setPackage("tcs.lbs.weather_app");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();

            // Deciding whether to stop or start the service based on the intent's action
            switch (action)
            {
                case ACTION_StartForegroundService:
                    startForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground Service is Started.", Toast.LENGTH_LONG).show();
                    break;
                case ACTION_StopForegroundService:
                    stopForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground Service is Stopped.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // Used to build and start foreground service.
    private void startForegroundService()
    {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(chan);
        }

        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );

        // Create notification builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        // Make notification show big text.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText("Location Monitor Service is Running");
        // Set big text style.
        builder.setStyle(bigTextStyle);
        builder.setWhen(System.currentTimeMillis());

        // Make the notification max priority.
        builder.setPriority(Notification.PRIORITY_MAX);
        // Make head-up notification.
        builder.setFullScreenIntent(pendingIntent, true);

        // Build the notification.
        Notification notification = builder.build();

        // Start foreground service.
        startForeground(1, notification);
        isForegroundServiceRunning = true;

        //define location service and provider
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);

        // check if the app has permission to read location data
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "I DON'T HAVE Permission To Get Location Data!!", Toast.LENGTH_LONG).show();
        }
    }

    private void stopForegroundService()
    {
        // Stop foreground service and remove the notification.
        stopForeground(true);
        isForegroundServiceRunning = false;

        // Stop Location manager from getting updates
        locationManager.removeUpdates(this);

        // Stop the foreground service.
        stopSelf();
    }


    @Override
    public void onLocationChanged(Location _location)
    {
        // On location update, send a broadcast intent with the location data
        locationAppIntent.putExtra("Location", _location);
        weatherIntent.putExtra("Location", _location);

        // Send intra-app broadcast to MainActivity
        sendBroadcast(locationAppIntent);

        // Send inter-app broadcast to WeatherApp
        sendBroadcast(weatherIntent);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status,
                                Bundle extras)
    {
        // TODO Auto-generated method stub
    }

    public ForegroundLocationService() { }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
