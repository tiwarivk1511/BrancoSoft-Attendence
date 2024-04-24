package com.android.brancoattendence;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.brancoattendence.databinding.ActivityHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_attendance, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_profile || destination.getId() == R.id.nav_attendance) {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_arrow_back_ios_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> onBackPressed());
            } else {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_menu_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> {
                    drawer.openDrawer(GravityCompat.START);
                });
            }
        });

        binding.navView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            performLogOut();
            return true;
        });

        fetchCurrentLocation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.statusBarColor));

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void performLogOut() {
        String baseUrl = HostURL.getBaseUrl();
        String token = retrieveTokenFromSharedPreferences();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter factory
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<Void> call = apiService.logout("Bearer " + token);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    clearTokenFromSharedPreferences();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(HomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("Token: " + preferences.getString("token", null));
        return preferences.getString("token", null);
    }

    private void clearTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void fetchCurrentLocation() {
        //get the location
        if (isUserLoggedIn()) {
            // Permission check
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }

            // Get the fused location provider client
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            // Get the last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            boolean isWithinRange = isWithinCoordinatesRange(latitude, longitude);
                            markAttendance(isWithinRange);
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace());

        } else {
            // User is not logged in, do nothing
            Toast.makeText(this, "Login First", Toast.LENGTH_SHORT).show();
        }

    }

    private void markAttendance(boolean isWithinRange) {
        String currentTime = String.valueOf(Calendar.getInstance().getTime());
        if (isWithinRange){

        } else {

        }

    }


    private boolean isUserLoggedIn() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Check if the token is available in SharedPreferences or any other way you handle login status
        String token = preferences.getString("token", null);
        return token != null;
    }

    private boolean isWithinCoordinatesRange(double latitude, double longitude) {
        // Coordinates of the designated location (e.g., office)
        double officeLatitude = 28.6188512; // Example latitude of office
        double officeLongitude = 77.3911159; // Example longitude of office

        // Radius of the Earth in kilometers
        double earthRadius = 6371;

        // Calculate the differences in latitude and longitude
        double latDistance = Math.toRadians(officeLatitude - latitude);
        double lonDistance = Math.toRadians(officeLongitude - longitude);

        // Calculate the Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(officeLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance in kilometers
        double distance = earthRadius * c;

        // Check if the distance is within the specified range (e.g., 0.5 kilometers)
        return distance <= 0.5; // Specify the range in kilometers
    }
}
