package com.sp.milestrack;


import static android.graphics.Color.parseColor;
import static androidx.core.app.PendingIntentCompat.getActivity;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.accessibilityservice.AccessibilityService;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.sp.milestrack.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public BottomNavigationView bottomNavigationView;
    NavController navController;
    LineChart chart;
    private static final String TAG = "MileTrack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Add DrawerListener
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                for (int i = 0; i < navigationView.getMenu().size(); i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_record, R.id.nav_list, R.id.nav_indoor)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "Enter Drawer");
                if (item.getItemId() == R.id.nav_exit) {
                    Log.d(TAG, "Exit menu item clicked");
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (item.getItemId() == R.id.nav_edit_info) {
                    navController.navigate(R.id.nav_edit_info);
                }
                // Let NavController handle the rest
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawer.closeDrawer(GravityCompat.START); // Close the drawer after navigation
                }
                return handled; // Indicate whether the event was handled
            }
        });
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            changeSelectedNavIc(); // Update the selected navigation item
        });
        findViewById(R.id.nav_view).setOnClickListener(v -> {
            navController.navigate(R.id.nav_home);
        });
        chart = findViewById(R.id.chart);
        //generateLineChart();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home_ic) {
                    navController.navigate(R.id.nav_home);
                } else if (item.getItemId() == R.id.record_ic) {
                    navController.navigate(R.id.nav_record);
                } else if (item.getItemId() == R.id.list_ic) {
                    navController.navigate(R.id.nav_list);
                } else return false;
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true; // if in one of the if/if-else clauses
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void changeSelectedNavIc() {
        int currentDestinationId = navController.getCurrentDestination().getId();
        MenuItem setNavIc;
        if (currentDestinationId == R.id.nav_home) {
            setNavIc = bottomNavigationView.getMenu().findItem(R.id.home_ic);
            setNavIc.setChecked(true);
        } else if (R.id.nav_record == currentDestinationId) {
            setNavIc = bottomNavigationView.getMenu().findItem(R.id.record_ic);
            setNavIc.setChecked(true);
        } else if (R.id.nav_list == currentDestinationId) {
            setNavIc = bottomNavigationView.getMenu().findItem(R.id.list_ic);
            setNavIc.setChecked(true);
        }
    }
}
//superman