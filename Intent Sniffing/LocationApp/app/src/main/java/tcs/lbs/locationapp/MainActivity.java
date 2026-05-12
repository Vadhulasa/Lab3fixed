package tcs.lbs.locationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Some configurations to use OSM (Open Street Map)
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapController = mapView.getController();
        mapController.setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(59.3293, 18.0686);
        mapController.setCenter(startPoint);

        // Location overlay to show users location in OSM
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()),mapView);
        locationOverlay.enableMyLocation();


        // Register MainActivityReceiver class to listen to broadcasts from ForegroundLocationService
        IntentFilter filter = new IntentFilter();
        filter.addAction("tcs.lbs.locationapp.MainActivityReceiver");
        if (android.os.Build.VERSION.SDK_INT >= 33)
        {
            registerReceiver(new MainActivityReceiver(), filter, Context.RECEIVER_NOT_EXPORTED);
        }
        else
        {
            registerReceiver(new MainActivityReceiver(), filter);
        }


        // Check if the app has access to device's Location
        // Also check if the app has access to writing to external storage
        // This is needed for OSM MapView to function correctly
        // All of the tiles downloaded by OSM are stored locally, hence the need for this permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    public void toggleForegroundService(android.view.View view)
    {
        if (!ForegroundLocationService.isForegroundServiceRunning)
        {
            // Starts the ForegroundLocationService !!
            Intent intent = new Intent(MainActivity.this, ForegroundLocationService.class);
            intent.setAction(ForegroundLocationService.ACTION_StartForegroundService);
            startService(intent);

            // Adds location overlay and starts showing user location on MapView
            mapView.getOverlays().add(locationOverlay);
        }
        else
        {
            // Stops the ForegroundLocationService !!
            Intent intent = new Intent(MainActivity.this, ForegroundLocationService.class);
            intent.setAction(ForegroundLocationService.ACTION_StopForegroundService);
            startService(intent);

            // Removes location overlay and stops showing user location on MapView
            mapView.getOverlays().remove(locationOverlay);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (ForegroundLocationService.isForegroundServiceRunning)
        {
            mapView.getOverlays().add(locationOverlay);
        }

        mapView.onResume();
    }

    public void onPause(){
        super.onPause();

        if (ForegroundLocationService.isForegroundServiceRunning)
        {
            mapView.getOverlays().remove(locationOverlay);
        }

        mapView.onPause();
    }


    // BroadcastReceiver class, this class enables MainActivity to receive broadcasts from ForegroundLocationService
    public class MainActivityReceiver extends BroadcastReceiver
    {

        // If a broadcast intent is received, this method will be invoked.
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
