package com.android.brancoattendence;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationWorker extends Worker {

    private static final String TAG = "LocationWorker";
    private static final String LAST_CHECK_IN_TIME_KEY = "last_check_in_time";
    private static final String LAST_CHECK_OUT_TIME_KEY = "last_check_out_time";
    private static final String CHANNEL_ID = String.valueOf (1256);

    private double latitude;
    private double longitude;
    private String address = "";
    private int checkingId;
    private int notificationId = 1025;

    public LocationWorker (@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super (context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork () {
        try {
            fetchCurrentLocation ();
            return Result.success ();
        } catch (Exception e) {
            Log.e (TAG, "Error fetching location: " + e.getMessage ());
            return Result.failure ();
        }
    }

    private void fetchCurrentLocation () {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient (getApplicationContext ());

        if (ActivityCompat.checkSelfPermission (getApplicationContext (), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (getApplicationContext (), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions (
                    (Activity) getApplicationContext (),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101
            );
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation ()
                .addOnSuccessListener (location -> {
                    if (location != null) {
                        latitude = location.getLatitude ();
                        longitude = location.getLongitude ();

                        new GetAddressTask ().execute (latitude, longitude);
                    }
                })
                .addOnFailureListener (e -> {
                    Toast.makeText (getApplicationContext (), "Failed to get location", Toast.LENGTH_SHORT).show ();
                    e.printStackTrace ();
                });
    }

    private class GetAddressTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground (Double... params) {
            double latitude = params[0];
            double longitude = params[1];
            return getAddressFromCoordinates (latitude, longitude);
        }

        @Override
        protected void onPostExecute (String result) {
            address = result;
            // Perform other operations such as marking attendance, generating notifications, etc.
            processLocation (latitude, longitude);
        }
    }

    private String getAddressFromCoordinates (double latitude, double longitude) {
        Geocoder geocoder = new Geocoder (getApplicationContext (), Locale.getDefault ());
        String currentAddress = address;
        try {
            List<Address> addresses = geocoder.getFromLocation (latitude, longitude, 1);
            if (!addresses.isEmpty ()) {
                address = addresses.get (0).getAddressLine (0);
                Log.d (TAG, "Address: " + address);
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        return currentAddress;
    }

    private void processLocation(double latitude, double longitude) {
        // Implement location processing logic
        // Example: Check if user is within a specific range

        // Check if check-in is required
        if (isCheckInRequired()) {
            performCheckIn();
        }

        // Check if check-out is required
        if (isCheckOutRequired()) {
            // Check if user is more than 500 meters away from the office
            if (isUserOutOfOffice(latitude, longitude)) {
                performCheckOut();
            }
        }
    }

    private boolean isUserOutOfOffice(double latitude, double longitude) {

        // Latitude and longitude of the office
        double officeLatitude = 28.61885408667818 /*  office latitude */;
        double officeLongitude =  77.39100307286857/* office longitude */;

        // Calculate the distance between user's location and office location
        float[] distance = new float[1];
        Location.distanceBetween(latitude, longitude, officeLatitude, officeLongitude, distance);

        Log.d("asddfsd", "Distance: " + distance[0]);

        // If the distance is greater than 500 meters, consider the user out of office
        return distance[0] > 25; //distance in meters to indicate the user is out of office
    }

    private boolean isCheckInRequired () {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences (getApplicationContext ());
        String lastCheckInTime = preferences.getString (LAST_CHECK_IN_TIME_KEY, "");
        String today = getTodayDateString ();

        // If last check-in time is not recorded or is before today, then check-in is required
        return lastCheckInTime.isEmpty () || !lastCheckInTime.equals (today);
    }

    private boolean isCheckOutRequired () {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences (getApplicationContext ());
        String lastCheckOutTime = preferences.getString (LAST_CHECK_OUT_TIME_KEY, "");
        String today = getTodayDateString ();

        // If last check-out time is not recorded or is before today, then check-out is required
        return lastCheckOutTime.isEmpty () || !lastCheckOutTime.equals (today);
    }

    private void performCheckIn () {
        String currentTime = getCurrentTime ();
        saveCheckInTime (currentTime);
        String baseUrl = HostURL.getBaseUrl (); // Update base URL
        String token = retrieveTokenFromSharedPreferences ();


        Retrofit retrofit = new Retrofit.Builder ()
                .baseUrl (baseUrl)
                .addConverterFactory (GsonConverterFactory.create ())
                .build ();
        ApiService apiService = retrofit.create (ApiService.class);

        Call<AttendanceData> call = apiService.checkIn ("Bearer " + token, currentTime);
        call.enqueue (new Callback<AttendanceData> () {
            @Override
            public void onResponse (@NonNull Call<AttendanceData> call, @NonNull Response<AttendanceData> response) {
                if (response.isSuccessful () && response.body () != null) {
                    checkingId = response.body ().getAttendanceId ();
                    Log.d (TAG, "Check-in API response: " + response.raw ().body ().toString ());
                    Toast.makeText (getApplicationContext (), "Check-in successful", Toast.LENGTH_SHORT).show ();
                    // Generate notification for check-in
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        generateNotification ("Check-in", "Check-in successful");
                    }
                } else {
                    Toast.makeText (getApplicationContext (), "Failed to check-in", Toast.LENGTH_SHORT).show ();
                }
            }

            @Override
            public void onFailure (@NonNull Call<AttendanceData> call, Throwable t) {
                Toast.makeText (getApplicationContext (), "Failed to check-in", Toast.LENGTH_SHORT).show ();
            }
        });
    }

    private String retrieveTokenFromSharedPreferences () {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences (getApplicationContext ());
        return preferences.getString ("token", null);
    }

    private void performCheckOut () {
        String currentTime = getCurrentTime ();
        saveCheckOutTime();

        // Check if a recent check-in has been performed
        if (isCheckInPerformed ()) {
            String baseUrl = HostURL.getBaseUrl (); // Update base URL
            String token = retrieveTokenFromSharedPreferences ();
            currentTime = new SimpleDateFormat ("HH:mm", Locale.getDefault ()).format (new Date ());

            Retrofit retrofit = new Retrofit.Builder ()
                    .baseUrl (baseUrl)
                    .addConverterFactory (GsonConverterFactory.create ())
                    .build ();
            ApiService apiService = retrofit.create (ApiService.class);

            Call<AttendanceData> call = apiService.checkOut ("Bearer " + token, checkingId, currentTime);

            String finalCurrentTime = currentTime;
            call.enqueue (new Callback<AttendanceData> () {
                @Override
                public void onResponse (@NonNull Call<AttendanceData> call, @NonNull Response<AttendanceData> response) {
                    if (response.isSuccessful () && response.body () != null) {
                        Log.d (TAG, "Check-out API response: " + response.raw().body ().toString ());
                        Toast.makeText (getApplicationContext (), "Check-out successful", Toast.LENGTH_SHORT).show ();
                        saveCheckOutTime ();
                        // Generate notification for check-out
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            generateNotification ("Check-out", "Check-out successful");
                        }
                    } else {
                        Toast.makeText (getApplicationContext (), "Failed to check-out", Toast.LENGTH_SHORT).show ();
                    }
                }

                @Override
                public void onFailure (@NonNull Call<AttendanceData> call, Throwable t) {
                    Toast.makeText (getApplicationContext (), "Failed to check-out", Toast.LENGTH_SHORT).show ();
                }
            });
        } else {
            Toast.makeText (getApplicationContext (), "Please perform check-in first", Toast.LENGTH_SHORT).show ();
            performCheckOut();
        }
        // Make an API call or perform local storage operations to mark check-out
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void generateNotification (String title, String message) {
        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder (getApplicationContext (), CHANNEL_ID)
                .setSmallIcon (R.drawable.brancosoft_logo)
                .setContentTitle (title)
                .setContentText (message)
                .setPriority (NotificationCompat.PRIORITY_HIGH);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from (getApplicationContext ());
        if (ActivityCompat.checkSelfPermission (getApplicationContext (), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions(
                    (Activity) getApplicationContext (),
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    notificationId
            );
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        notificationManager.notify(notificationId, builder.build ());
    }

    private boolean isCheckInPerformed () {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lastCheckInTime = preferences.getString(LAST_CHECK_IN_TIME_KEY, "");
        String today = getCurrentTime ();
        return !lastCheckInTime.isEmpty() && lastCheckInTime.equals(today);
    }

    private void saveCheckInTime(String time) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_CHECK_IN_TIME_KEY,getCurrentTime ()); // Save the provided time
        editor.apply();
    }

    private void saveCheckOutTime() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_CHECK_OUT_TIME_KEY, getCurrentTime ()); // Save the provided time
        editor.apply();
    }


    private String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }


//    private void performCheckIn(String time) {
//        String baseUrl = HostURL.getBaseUrl(); // Update base URL
//        String token = retrieveTokenFromSharedPreferences();
//        String currentTime = time;
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        ApiService apiService = retrofit.create(ApiService.class);
//
//        Call<AttendanceData> call = apiService.checkIn("Bearer " + token, currentTime);
//        call.enqueue(new Callback<AttendanceData>() {
//            @Override
//            public void onResponse(@NonNull Call<AttendanceData> call, @NonNull Response<AttendanceData> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    checkingId = response.body().getAttendanceId();
//                    System.out.println("Check-in API response: " + response.raw().body().toString());
//                    Toast.makeText(getApplicationContext(), "Check-in successful", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Failed to check-in", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AttendanceData> call, Throwable t) {
//                Toast.makeText(getApplicationContext(), "Failed to check-in", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//    private void performCheckOut(int checkingId) {
//        // Check if a recent check-in has been performed
//        checkingId = this.checkingId;
//        if (isCheckInPerformed()) {
//            String baseUrl = HostURL.getBaseUrl(); // Update base URL
//            String token = retrieveTokenFromSharedPreferences();
//            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
//
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl(baseUrl)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//            ApiService apiService = retrofit.create(ApiService.class);
//
//            Call<AttendanceData> call = apiService.checkOut("Bearer " + token, checkingId, currentTime);
//
//            call.enqueue(new Callback<AttendanceData>() {
//                @Override
//                public void onResponse(@NonNull Call<AttendanceData> call, @NonNull Response<AttendanceData> response) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        generateNotification("Check-out", "Check-out successful");
//                        Toast.makeText(getApplicationContext(), "Check-out successful", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "Failed to check-out", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<AttendanceData> call, Throwable t) {
//                    Toast.makeText(getApplicationContext(), "Failed to check-out", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            Toast.makeText(getApplicationContext(), "Please perform check-in first", Toast.LENGTH_SHORT).show();
//        }
//    }
}
