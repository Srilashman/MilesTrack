package com.sp.milestrack;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationRequest;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;

public class LocationTrackingService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double totalDistance = 0.0;
    private Location lastLocation = null;
    boolean isTracking = true;
    private ArrayList<LatLng> routePoints = new ArrayList<>();
    private BroadcastReceiver trackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("PAUSE_TRACKING".equals(intent.getAction())) {
                isTracking = false; // Stop collecting location updates
            } else if ("RESUME_TRACKING".equals(intent.getAction())) {
                isTracking = true; // Resume collecting location updates
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("PAUSE_TRACKING");
        filter.addAction("RESUME_TRACKING");
        LocalBroadcastManager.getInstance(this).registerReceiver(trackingReceiver, filter);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                if (!isTracking) return;

                for (Location location : locationResult.getLocations()) {
                    Log.d("LocationTrackingService", "New location: " + location.getLatitude() + ", " + location.getLongitude());

                    if (lastLocation != null) {
                        float distance = location.distanceTo(lastLocation);
                        totalDistance += distance;
                    }

                    lastLocation = location;
                    routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));

                    // Send broadcast with updated distance and route points
                    Intent intent = new Intent("LOCATION_UPDATE");
                    intent.putExtra("total_distance", totalDistance);
                    intent.putParcelableArrayListExtra("route_points", new ArrayList<>(routePoints));
                    sendBroadcast(intent);
                }
            }
        };

        // Start tracking (without foreground service)
        startTracking();
    }

    private void startTracking() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(2000); // 2 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle permission request here if needed
            return;
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Allow service to restart if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


