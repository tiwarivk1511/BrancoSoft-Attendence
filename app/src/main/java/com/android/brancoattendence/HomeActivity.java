package com.android.brancoattendence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.brancoattendence.databinding.ActivityHomeBinding;
import com.android.brancoattendence.ui.profile.UserDataResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private AttendanceManager mAttendanceManager;

    private String address = "";
    private double latitude;
    private double longitude;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl()) // Update base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService mApiService = retrofit.create(ApiService.class);

        mAttendanceManager = new AttendanceManager(mApiService, this);

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
        String baseUrl = HostURL.getBaseUrl(); // Update base URL
        String token = retrieveTokenFromSharedPreferences();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<Void> call = apiService.logout("Bearer " + token);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
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
        return preferences.getString("token", null);
    }

    private void performCheckIn() {
        String baseUrl = HostURL.getBaseUrl(); // Update base URL
        String token = retrieveTokenFromSharedPreferences();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<AttendanceResponce> call = apiService.checkIn("Bearer " + token, currentTime);
        call.enqueue(new Callback<AttendanceResponce>() {
            @Override
            public void onResponse(@NonNull Call<AttendanceResponce> call, @NonNull Response<AttendanceResponce> response) {
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("Check-in API response: " + response.body());
                    Toast.makeText(HomeActivity.this, "Check-in successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to check-in", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AttendanceResponce> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Failed to check-in", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (isUserLoggedIn()) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request permissions here if not granted
            }

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            new GetAddressTask().execute(latitude, longitude);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });

        } else {
            Toast.makeText(this, "Login First", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetAddressTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];
            return getAddressFromCoordinates(latitude, longitude);
        }

        @Override
        protected void onPostExecute(String result) {
            address = result;
            markAttendance(isWithinCoordinatesRange(latitude, longitude));
        }
    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String currentAddress = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                currentAddress = addresses.get(0).getAddressLine(0);
                System.out.println("Address: " + currentAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentAddress;
    }

    private void markAttendance(boolean isWithinRange) {
        Date currentDate = new Date();
        String date = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(currentDate);
        String time = new SimpleDateFormat("hh:mm", Locale.getDefault()).format(currentDate);

        if (isWithinRange) {
            AttendanceData data = new AttendanceData();
            data.setEmployeeId(UserDataResponse.getEmployeeId());
            data.setDate(date);
            data.setCheckIn(time);
            data.setLocation(address);

            performCheckIn();
        }  else {// Handle when user is not within range

            Toast.makeText(this, "You are not within range", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isUserLoggedIn() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = preferences.getString("token", null);
        return token != null;
    }

    private boolean isWithinCoordinatesRange(double latitude, double longitude) {
        double officeLatitude = 28.6188512;
        double officeLongitude = 77.3911159;
        double earthRadius = 6371;
        double latDistance = Math.toRadians(officeLatitude - latitude);
        double lonDistance = Math.toRadians(officeLongitude - longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(officeLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c * 1000; // Convert distance to meters

        return distance <= 25; // Check if distance is less than or equal to 5 meters
    }

}
