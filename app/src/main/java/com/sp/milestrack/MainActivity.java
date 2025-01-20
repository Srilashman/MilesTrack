package com.sp.milestrack;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.sp.milestrack.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public BottomNavigationView bottomNavigationView;
    NavController navController;
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
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home_ic) {
                    navController.navigate(R.id.nav_home);
                } else if (item.getItemId() == R.id.record_ic) {
                    navController.navigate(R.id.nav_record);
                } else if (item.getItemId() == R.id.list_ic) {
                    navController.navigate(R.id.nav_list);
                }
                else return false;
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