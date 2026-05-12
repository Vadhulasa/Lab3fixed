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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ForegroundLocationService extends Service implements LocationListener
{
    public static final String ACTION_StartForegroundService = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_StopForegroundService  = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String CHANNEL_ID   = "LOCATION_SERVICE_CHANNEL_ID";
    public static final String CHANNEL_NAME = "LOCATION_SERVICE_CHANNEL";

    // Custom permission that restricts inter-app broadcasts to apps signed with the same key.
    // Declared in AndroidManifest.xml with protectionLevel="signature".
    public static final String PERMISSION_RECEIVE_LOCATION = "tcs.lbs.RECEIVE_LOCATION";

    public static boolean isForegroundServiceRunning = false;

    Intent locationAppIntent, weatherIntent;

    protected LocationManager locationManager;
    private String provider;

    @Override
    public void onCreate()
    {
        super.onCreate();

        locationAppIntent = new Intent();
        weatherIntent     = new Intent();
        weatherIntent.setAction("tcs.lbs.weather_app.WeatherBroadcastReceiver");
        locationAppIntent.setAction("tcs.lbs.locationapp.MainActivityReceiver");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
        {
            String action = intent.getAction();
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

    private void startForegroundService()
    {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(chan);
        }

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText("Location Monitor Service is Running");
        builder.setStyle(bigTextStyle);
        builder.setWhen(System.currentTimeMillis());
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setFullScreenIntent(pendingIntent, true);

        Notification notification = builder.build();
        startForeground(1, notification);
        isForegroundServiceRunning = true;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
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
        stopForeground(true);
        isForegroundServiceRunning = false;
        locationManager.removeUpdates(this);
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location _location)
    {
        locationAppIntent.putExtra("Location", _location);
        weatherIntent.putExtra("Location", _location);

        // FIX (intra-app): LocalBroadcastManager routes the broadcast only within
        // this app's process — no other app can register for it and sniff the location.
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationAppIntent);

        // FIX (inter-app): Pass a required receiverPermission to sendBroadcast.
        // Only apps that declare <uses-permission> for tcs.lbs.RECEIVE_LOCATION AND
        // are signed with the same key (protectionLevel="signature") can receive this.
        sendBroadcast(weatherIntent, PERMISSION_RECEIVE_LOCATION);
    }

    @Override public void onProviderDisabled(String provider) {}
    @Override public void onProviderEnabled(String provider)  {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}

    public ForegroundLocationService() {}

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
