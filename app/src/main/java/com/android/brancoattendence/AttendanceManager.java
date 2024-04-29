package com.android.brancoattendence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceManager {

    private static ApiService mApiService;
    private static Context mContext;

    public AttendanceManager(ApiService apiService, Context context) {
        this.mApiService = apiService;
        this.mContext = context;
    }

    public static void checkIn(String token) {
        // Get current time
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(new Date());

        // Call checkIn API
        Call<AttendanceResponse> call = mApiService.checkIn(token, currentTime);
        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Toast.makeText(mContext, "Check-in successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Failed to check-in", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Toast.makeText(mContext, "Failed to check-in", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //get token
    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String token = preferences.getString("token", null);
        System.out.println("Token: " + token);
        return token;
    }


}
