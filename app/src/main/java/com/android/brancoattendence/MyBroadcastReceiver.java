package com.android.brancoattendence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            // Check if the user is logged in
            if (isUserLoggedIn(context)) {
                // Device has booted or time/timezone changed and user is logged in, check if it's a weekday
                if (isWeekday()) {
                    // It's a weekday, check if the current time is within the specified interval
                    if (isWithinTimeInterval()) {
                        // Current time is within the interval, schedule the LocationWorker
                        LocationWorker.scheduleLocationWorker(context);
                    } else {
                        // Current time is outside the interval, cancel the LocationWorker
                        LocationWorker.cancelLocationWorker(context);
                    }
                } else {
                    // It's a weekend, cancel the LocationWorker
                    LocationWorker.cancelLocationWorker(context);
                }
            } else {
                // User is not logged in, disable the LocationWorker
                LocationWorker.cancelLocationWorker(context);
            }
        }
    }

    private boolean isUserLoggedIn(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Check if the token is available in SharedPreferences or any other way you handle login status
        String token = preferences.getString("token", null);
        return token != null;
    }

    private boolean isWeekday() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
    }

    private boolean isWithinTimeInterval() {
        Calendar cal = Calendar.getInstance();
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Check if the current time is between 8:15 AM and 7:40 PM
        return (hourOfDay > 8 || (hourOfDay == 8 && minute >= 15)) && (hourOfDay < 19 || (hourOfDay == 19 && minute <= 40));
    }
}
