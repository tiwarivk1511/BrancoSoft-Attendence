package com.android.brancoattendence;

import static com.android.brancoattendence.Attendence.CheckInResponse.getCheckInTime;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.brancoattendence.Attendence.CheckInResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LocationWorker extends Worker {

    private static final String Attendance_CHANNEL_ID = "attendance_channel";

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    @NonNull
    public Result doWork() {
        // Check if the user is logged in
        if (isUserLoggedIn()) {
            // Permission check
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                return Result.failure();
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
                    .addOnFailureListener(Throwable::printStackTrace);

            return Result.success();
        } else {
            // User is not logged in, do nothing
            return Result.failure();
        }
    }

    private boolean isUserLoggedIn() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Check if the token is available in SharedPreferences or any other way you handle login status
        String token = preferences.getString("token", null);
        return token != null;
    }

    // Method to mark attendance
    private void markAttendance(boolean isPresent) {
        String message = isPresent ? "Attendance marked!" : "User marked out!";
        showAttendanceNotification(message);

    }

    void MarkCheckIn() {
        String baseUrl = HostURL.getBaseUrl();
        String token = retrieveTokenFromSharedPreferences();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<CheckInResponse> call = apiService.checkIn("Bearer " + token, getCheckInTime());

        call.enqueue(new Callback<CheckInResponse>() {

            @Override
            public void onResponse(Call<CheckInResponse> call, Response<CheckInResponse> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "CheckIn Successful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckInResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "CheckIn Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getString("token", null);
    }

    // Method to show attendance notification
    private void showAttendanceNotification(String message) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Attendance_CHANNEL_ID)
                .setSmallIcon(R.drawable.brancosoft_logo)
                .setContentTitle("Attendance")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Method to check if the user is within the specified coordinates range
    private boolean isWithinCoordinatesRange(double latitude, double longitude) {
        // Specify the coordinates of the office location
        double officeLatitude = 28.6188512; // Example latitude of office
        double officeLongitude = 77.3911159; // Example longitude of office
        double maxDistance = 500; // Maximum distance in meters

        // Calculate the distance between the current location and the office location
        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, officeLatitude, officeLongitude, results);
        getAddress(latitude, longitude);

        System.out.println("Distance to office: " + results[0] + " meters");
        // Check if the distance is within the specified range
        return results[0] <= maxDistance;
    }

    // Method to get address from coordinates
    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                saveAddressToSharedPreferences(addresses.get(0).getAddressLine(0));
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to save address to SharedPreferences
    private void saveAddressToSharedPreferences(String address) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("address", address);
        editor.apply();
    }

    // Method to create notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Attendance Channel";
            String description = "Channel for showing attendance notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Attendance_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Method to schedule the worker to run every 15 minutes
    public static void scheduleLocationWorker(Context context) {
        PeriodicWorkRequest.Builder locationWorkerRequestBuilder =
                new PeriodicWorkRequest.Builder(LocationWorker.class, 15, TimeUnit.MINUTES);

        // Schedule the worker
        WorkManager.getInstance(context).enqueue(locationWorkerRequestBuilder.build());
    }

    // Method to cancel the scheduled worker
    public static void cancelLocationWorker(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
    }
}