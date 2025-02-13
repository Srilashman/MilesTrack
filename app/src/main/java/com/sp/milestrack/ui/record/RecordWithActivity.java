package com.sp.milestrack.ui.record;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
import com.sp.milestrack.Database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;



public class RecordWithActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BroadcastReceiver locationReceiver;
    private ArrayList<LatLng> routePoints = new ArrayList<>();
    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView stepsTextView;
    private Polyline routeLine;
    private LatLng latestLocation; // Store the latest location
    TextView timerTextView;
    Button start_stop_btn;
    Button stop_record_btn;
    long startTime = 0;
    long elapsedTime = 0;
    boolean isStarted = false;
    private Database helper = null;
    private double previousDistance = 0.0;
    private long previousTime = 0;
    private double totalCaloriesBurned = 0.0;
    private String activityType = "Running"; // Default activity type
    private static final String TAG = "MileTrack";

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

//                String time = String.format("%d:%02d", minutes, seconds);

                timerTextView.setText("Time: " + String.format("%d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        }
    };

    Handler speedHandler = new Handler();
    Runnable speedRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTracking) {
                long currentTime = System.currentTimeMillis();
                double currentDistance = Double.parseDouble(distanceTextView.getText().toString().replace("Distance: ", "").replace(" km", ""));
                double weightInKg = 70; // Default weight if database doesn't return anything

                // Calculate speed in m/s
                double distanceDelta = (currentDistance - previousDistance) * 1000; // Convert km to m
                double timeDelta = (currentTime - previousTime) / 1000.0; // Convert ms to seconds

                if (timeDelta > 0) {
                    double speedMps = distanceDelta / timeDelta;
                    double speedKmph = speedMps * 3.6;
                    double durationInHours = 5.0 / 3600.0; // Since we calculate every 5 seconds


                    // Update MET based on activity and speed
                    double MET = getMET(activityType, speedKmph);

                    //get user weight
                    Cursor c = helper.getLastRecord();
                    if (c != null && c.moveToLast()) {
                        weightInKg = helper.getWeight(c);
                    }

                    if (distanceDelta > 0) {
                        // Calculate calories burned
                        double caloriesBurned = MET * weightInKg * durationInHours;
                        totalCaloriesBurned += caloriesBurned;
                    }

                    caloriesTextView.setText(String.format("Calories burned: %.2f", totalCaloriesBurned));

                    Log.d("Speed", String.format("Speed: %.2f km/h", speedKmph));
                    Log.d("MET", String.format("MET: %.2f", MET));
                    Log.d("Calories", String.format("Calories this interval: %.2f", totalCaloriesBurned));
                }

                // Update previous values for the next calculation
                previousDistance = currentDistance;
                previousTime = currentTime;

                // Re-run every 5 seconds
                speedHandler.postDelayed(this, 5000);
            }
        }
    };
    private boolean isTracking = true; // Controls whether we add new points

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_with_activity, container, false);

        distanceTextView = view.findViewById(R.id.distance);
        caloriesTextView = view.findViewById(R.id.calories);
        stepsTextView = view.findViewById(R.id.steps);
        TextView sport = view.findViewById(R.id.activity);
        timerTextView = (TextView) view.findViewById(R.id.time);
        start_stop_btn = (Button) view.findViewById(R.id.start_stop_button);
        start_stop_btn.setOnClickListener(start_stop);
        stop_record_btn = (Button) view.findViewById(R.id.stop_btn);
        stop_record_btn.setOnClickListener(stop);
        helper = new Database(requireContext());

        Bundle args = getArguments();
        if (args != null) {
            String sportChoice = getArguments().getString("sportChoice");
            sport.setText("Activity: " + getArguments().getString("sportChoice"));

            // Hide steps for Swimming or Cycling
            if (sportChoice.equalsIgnoreCase("Swimming") || sportChoice.equalsIgnoreCase("Cycling")) {
                stepsTextView.setVisibility(View.GONE);  // Hide the steps TextView
            } else {
                stepsTextView.setVisibility(View.VISIBLE);  // Show steps for other activities
            }

            // Set activityType for calorie calculation
            activityType = sportChoice;
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

    private View.OnClickListener stop = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DateFormat df = new SimpleDateFormat("dd MMM yyyy  h:mm a");
            String date = df.format(Calendar.getInstance().getTime());

            // Print currentTime in the log
            Log.d(TAG, "Current Time: " + date.toString());
            String totalTime = timerTextView.getText().toString().replace("Time: ", "");

            // Get total distance and calories
            double totalDistance = Double.parseDouble(distanceTextView.getText().toString().replace("Distance: ", "").replace(" km", ""));
            double totalCalories = totalCaloriesBurned;

            helper.insertrecord(date, totalDistance, totalTime, totalCalories, activityType);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_list);
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

                    double distanceInMeters = totalDistance * 1000;
                    double strideLength = 0.762; // meters per step
                    double stepsCount = distanceInMeters / strideLength;

                    // Update UI
                    distanceTextView.setText(String.format("Distance: %.2f km", totalDistance));
                    caloriesTextView.setText(String.format("Calories burned: %.2f", totalCaloriesBurned));

                    // Only update steps if the activity is not Swimming or Cycling
                    if (!activityType.equalsIgnoreCase("Swimming") && !activityType.equalsIgnoreCase("Cycling")) {
                        distanceInMeters = totalDistance * 1000;
                        strideLength = 0.762; // meters per step
                        stepsCount = distanceInMeters / strideLength;
                        stepsTextView.setText(String.format("Steps: %.0f", stepsCount));
                    }

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

                                startTime = System.currentTimeMillis();
                                timerHandler.postDelayed(timerRunnable, 0);  // Start timer
                                speedHandler.postDelayed(speedRunnable, 5000);  // Start speed calculation every 5 sec
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

    private double getMET(String activity, double speedKmph) {
        switch (activity) {
            case "Running":
                if (speedKmph < 2) return 0;
                else if (speedKmph <= 4) return 2.9;
                else if (speedKmph <= 5) return 3.5;
                else if (speedKmph <= 6) return 4.5;
                else if (speedKmph <= 8) return 8.3;
                else if (speedKmph <= 10) return 9.8;
                else return 11.0;
            case "Cycling":
                if (speedKmph <= 16) return 4.0;
                else if (speedKmph <= 19) return 6.8;
                else return 8.0;
            case "Swimming":
                if (speedKmph <= 2) return 5.8;  // Light effort
                else if (speedKmph <= 3) return 8.3;  // Moderate
                else return 11.0;  // Vigorous
            case "Hiking":
                if (speedKmph <= 5) return 6.0;
                else return 7.8;
            default:
                return 3.5;  // Default for moderate activity
        }
    }

}
