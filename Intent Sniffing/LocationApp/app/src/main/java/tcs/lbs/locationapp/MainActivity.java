package tcs.lbs.locationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends AppCompatActivity
{
    MapView mapView = null;
    IMapController mapController;
    MyLocationNewOverlay locationOverlay;

    // Keep a reference so we can unregister in onDestroy
    private MainActivityReceiver mainReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapController = mapView.getController();
        mapController.setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(59.3293, 18.0686);
        mapController.setCenter(startPoint);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), mapView);
        locationOverlay.enableMyLocation();

        // FIX: Register via LocalBroadcastManager instead of the system broadcast bus.
        // LocalBroadcastManager delivers only within this process, so IntentSniffer
        // can no longer intercept intra-app location updates.
        mainReceiver = new MainActivityReceiver();
        IntentFilter filter = new IntentFilter("tcs.lbs.locationapp.MainActivityReceiver");
        LocalBroadcastManager.getInstance(this).registerReceiver(mainReceiver, filter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // Must unregister from LocalBroadcastManager explicitly
        if (mainReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mainReceiver);
        }
    }

    public void toggleForegroundService(android.view.View view)
    {
        if (!ForegroundLocationService.isForegroundServiceRunning)
        {
            Intent intent = new Intent(MainActivity.this, ForegroundLocationService.class);
            intent.setAction(ForegroundLocationService.ACTION_StartForegroundService);
            startService(intent);
            mapView.getOverlays().add(locationOverlay);
        }
        else
        {
            Intent intent = new Intent(MainActivity.this, ForegroundLocationService.class);
            intent.setAction(ForegroundLocationService.ACTION_StopForegroundService);
            startService(intent);
            mapView.getOverlays().remove(locationOverlay);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (ForegroundLocationService.isForegroundServiceRunning) {
            mapView.getOverlays().add(locationOverlay);
        }
        mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (ForegroundLocationService.isForegroundServiceRunning) {
            mapView.getOverlays().remove(locationOverlay);
        }
        mapView.onPause();
    }

    public class MainActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Location location = intent.getParcelableExtra("Location");
            if (location != null)
            {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(startPoint);
            }
        }
    }
}
