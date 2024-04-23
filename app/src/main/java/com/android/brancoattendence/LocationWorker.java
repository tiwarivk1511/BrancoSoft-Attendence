package com.android.brancoattendence;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.brancoattendence.Attendence.CheckInResponse;
import com.android.brancoattendence.Attendence.CheckOutResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LocationWorker extends Worker {

    private static final String ATTENDANCE_CHANNEL_ID = "attendance_channel";

    private String attendanceID;
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
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (isPresent) {
            markCheckIn();
        } else {
            markCheckOut();
        }
    }

    private void markCheckIn() {
        String baseUrl = HostURL.getBaseUrl();
        String token = retrieveTokenFromSharedPreferences();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<CheckInResponse> call = apiService.checkIn("Bearer " + token, CheckInResponse.getCheckInTime());

        call.enqueue(new Callback<CheckInResponse>() {
            @Override
            public void onResponse(Call<CheckInResponse> call, Response<CheckInResponse> response) {
                if (response.isSuccessful()) {
                    CheckInResponse.setCheckInTime(response.body().getCheckInTime());
                     attendanceID = String.valueOf(response.body().getAttendanceId());
                    showAttendanceNotification("Attendance marked!");
                } else {
                    showAttendanceNotification("Failed to mark attendance!");
                }
            }

            @Override
            public void onFailure(Call<CheckInResponse> call, Throwable t) {
                showAttendanceNotification("Failed to mark attendance!");
            }
        });
    }

    private void markCheckOut() {
        // Implement marking checkout if needed
        String attendanceId = String.valueOf(CheckInResponse.getAttendanceId());
        String baseUrl = HostURL.getBaseUrl();
        String token = retrieveTokenFromSharedPreferences();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<CheckOutResponse> call = apiService.checkOut("Bearer " + token,attendanceId,CheckOutResponse.getCheckOutTime());
        call.enqueue(new Callback<CheckOutResponse>() {

            @Override
            public void onResponse(Call<CheckOutResponse> call, Response<CheckOutResponse> response) {

                if (response.isSuccessful()) {
                    CheckOutResponse.setCheckOutTime(response.body().getCheckOutTime());
                    showAttendanceNotification("Checkout marked!");
                }
                else {
                    showAttendanceNotification("Failed to mark checkout!");
                }
            }

            @Override
            public void onFailure(Call<CheckOutResponse> call, Throwable t) {
                showAttendanceNotification("Failed to mark checkout!");
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ATTENDANCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.brancosoft_logo)
                .setContentTitle("Attendance")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    // Method to check if the user is within the specified coordinates range
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

        // Check if the distance is within the specified range (e.g., 500 meters)
        return distance <= 0.5; // Specify the range in kilometers
    }


    // Method to create notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Attendance Channel";
            String description = "Channel for showing attendance notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ATTENDANCE_CHANNEL_ID, name, importance);
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
