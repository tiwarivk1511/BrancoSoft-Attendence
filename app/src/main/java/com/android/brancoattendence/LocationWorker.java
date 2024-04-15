package com.android.brancoattendence;

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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LocationWorker extends Worker {

    private static final String Attendance_CHANNEL_ID = "attendance_channel";

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    @NonNull
    public Result doWork() {
        // Check if it's Saturday or Sunday, if yes, return without doing anything
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return Result.success();
        }

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
    }

    // Method to mark attendance
    private void markAttendance(boolean isPresent) {
        String message = isPresent ? "Attendance marked!" : "User marked out!";
        showAttendanceNotification(message);
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

    // Schedule the worker to run every day at 8:10 AM
    public static void scheduleLocationWorker(Context context) {
        // Create a Calendar instance
        Calendar cal = Calendar.getInstance();

        // Check if today is Saturday or Sunday, if yes, cancel any existing work and return
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            WorkManager.getInstance(context).cancelAllWork();
            return;
        }

        // Set the time to turn on the worker (8:10 AM)
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 10);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        // Set the time to turn off the worker (8:00 PM)
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long stopTime = cal.getTimeInMillis();

        // Check if current time is between stop and start time
        long currentTime = System.currentTimeMillis();
        boolean shouldRunNow = currentTime > startTime && currentTime < stopTime;

        // If current time is between stop and start time, schedule the worker to run every day
        if (shouldRunNow) {
            PeriodicWorkRequest.Builder locationWorkerRequestBuilder =
                    new PeriodicWorkRequest.Builder(LocationWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);

            // Schedule the worker
            WorkManager.getInstance(context).enqueue(locationWorkerRequestBuilder.build());
        }

    }
}
