package com.android.brancoattendence;

import static com.squareup.okhttp.internal.Internal.instance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import com.android.brancoattendence.databinding.ActivityHomeBinding;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        LocationWorker.scheduleLocationWorker(getApplicationContext());

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile,R.id.nav_attendance, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);



        //go back button enable if profile fragment is visible
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_profile) {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_arrow_back_ios_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> onBackPressed());
            } else if (destination.getId() == R.id.nav_attendance) {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_arrow_back_ios_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> onBackPressed());

            } else {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_menu_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                });
            }
        });

        //logout button
        // logout button
        binding.navView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            performLogout();
            return true;
        });

        //lock the orientation of the screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        //change the color of status bar
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.statusBarColor));

        //hide the title of toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
    }

    private void performLogout() {
        // Clear user session data
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();

        // Navigate back to the login screen
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            intent = new Intent(HomeActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish(); // Finish the current activity (HomeActivity)
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}