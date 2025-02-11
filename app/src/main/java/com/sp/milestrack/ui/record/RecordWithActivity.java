package com.sp.milestrack.ui.record;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sp.milestrack.LocationTrackingService;
import com.sp.milestrack.R;

import java.util.ArrayList;


public class RecordWithActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BroadcastReceiver locationReceiver;
    private ArrayList<LatLng> routePoints = new ArrayList<>();
    private TextView distanceTextView;
    private Polyline routeLine;
    private LatLng latestLocation; // Store the latest location
    TextView timerTextView;
    Button start_stop_btn;
    long startTime = 0;
    long elapsedTime = 0;
    boolean isStarted = false;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if(isTracking) {
                long millis = System.currentTimeMillis() - startTime + elapsedTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerTextView.setText("Time: " + String.format("%d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        }
    };
    private boolean isTracking = true; // Controls whether we add new points

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_with_activity, container, false);

        distanceTextView = view.findViewById(R.id.distance);
        TextView sport = view.findViewById(R.id.activity);
        timerTextView = (TextView) view.findViewById(R.id.time);
        start_stop_btn = (Button) view.findViewById(R.id.start_stop_button);
        start_stop_btn.setOnClickListener(start_stop);

        Bundle args = getArguments();
        if (args != null) {
            sport.setText("Activity: " + getArguments().getString("sportChoice"));
        }

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragmentDrawingRoute);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Start the LocationTrackingService
        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);
        requireContext().startService(serviceIntent); // For both Android 12 and below
        return view;
    }
    private View.OnClickListener start_stop = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isTracking) {
                elapsedTime += System.currentTimeMillis() - startTime;
                timerHandler.removeCallbacks(timerRunnable);
                start_stop_btn.setText("start");
                isTracking = false;
                Intent pauseIntent = new Intent("PAUSE_TRACKING");
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(pauseIntent);
            } else {
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
                start_stop_btn.setText("pause");
                isTracking = true;
                Intent resumeIntent = new Intent("RESUME_TRACKING");
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(resumeIntent);
            }
        }
    };
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Move the camera if we already have a location update
        if (latestLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latestLocation, 20));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("LOCATION_UPDATE");

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isTracking) {

                    double totalDistance = intent.getDoubleExtra("total_distance", 0.0) /1000; // convert to km
                    routePoints = intent.getParcelableArrayListExtra("route_points");

                    // Update UI
                    distanceTextView.setText(String.format("Distance: %.2f km", totalDistance));

                    // Update route on the map
                    if (routeLine != null) {
                        routeLine.remove();
                    }
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(routePoints)
                            .width(10)
                            .color(Color.BLUE);
                    routeLine = mMap.addPolyline(polylineOptions);

                    // Store the latest location
                    if (!routePoints.isEmpty()) {
                        latestLocation = routePoints.get(routePoints.size() - 1);

                        // Move the camera only if the map is ready
                        if (mMap != null) {
                            requireActivity().runOnUiThread(() ->
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latestLocation, 20))
                            );
                            // Start the timer
//                                elapsedTime += System.currentTimeMillis() - startTime;
//                                timerHandler.removeCallbacks(timerRunnable);
                            if (!isStarted) {
                                startTime = System.currentTimeMillis();
                                timerHandler.postDelayed(timerRunnable, 0);
                                isStarted = true;
                            }
                        }
                    }
                }
            }
        };

        // Register BroadcastReceiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(locationReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(locationReceiver, filter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationReceiver != null) {
            try {
                requireContext().unregisterReceiver(locationReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); // Receiver was already unregistered
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop the LocationTrackingService when the fragment is destroyed
        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);
        requireContext().stopService(serviceIntent);
    }
}
