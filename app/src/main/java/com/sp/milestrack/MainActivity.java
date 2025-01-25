package com.sp.milestrack;


import static android.graphics.Color.parseColor;
import static androidx.core.app.PendingIntentCompat.getActivity;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.accessibilityservice.AccessibilityService;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_record, R.id.nav_list)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
                    chart = findViewById(R.id.chart);
                    //generateLineChart();
                    navController.navigate(R.id.nav_home);
                } else if (item.getItemId() == R.id.record_ic) {
                    navController.navigate(R.id.nav_record);
                } else if (item.getItemId() == R.id.list_ic) {
                    navController.navigate(R.id.nav_list);
                }
                else return false;
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
    public void generateLineChart() {
        if (chart != null) chart.clear();
        // Sample data for the chart
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(1, 2));
        entries.add(new Entry(2, 3));
        entries.add(new Entry(3, 4));

        LineDataSet dataSet = new LineDataSet(entries, "Sample Data");
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            chart.getAxisLeft().setTextColor(parseColor("#FFFFFF")); // left y-axis
            chart.getXAxis().setTextColor(parseColor("#FFFFFF"));
            chart.getLegend().setTextColor(parseColor("#FFFFFF"));
            chart.getDescription().setTextColor(parseColor("#FFFFFF"));
            dataSet.setValueTextColor(parseColor("#FFFFFF"));
        }
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh chart
    }
}
//superman