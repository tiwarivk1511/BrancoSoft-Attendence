package com.android.brancoattendence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LocationBroadcastReceiver extends BroadcastReceiver {

    private static final String LOCATION_WORK_TAG = "location_work";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("TURN_ON_WORK_MANAGER")) {
                turnOnWorkManager(context);
            } else if (action.equals("TURN_OFF_WORK_MANAGER")) {
                turnOffWorkManager(context);
            }
        }
    }

    void turnOnWorkManager(Context context) {
        // Check if the current time is within the specified time frame (8:30 AM to 7:20 PM)
        if (isWithinTimeFrame() && !isWeekend()) {
            // Create a periodic work request to fetch live location
            PeriodicWorkRequest locationWorkRequest =
                    new PeriodicWorkRequest.Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                            .addTag(LOCATION_WORK_TAG)
                            .build();

            // Enqueue the work request
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    LOCATION_WORK_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    locationWorkRequest
            );
        } else {
            // If it's outside the specified time frame or weekend, cancel the periodic work
            turnOffWorkManager(context);
        }
    }

    private boolean isWithinTimeFrame() {
        // Get the current time
        Calendar cal = Calendar.getInstance();
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Check if the current time is within the specified time frame (8:30 AM to 7:20 PM)
        return (hourOfDay > 8 || (hourOfDay == 8 && minute >= 30)) && (hourOfDay < 19 || (hourOfDay == 19 && minute <= 20));
    }

    private boolean isWeekend() {
        // Get the current day of the week
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        // Check if it's Saturday or Sunday
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    private void turnOffWorkManager(Context context) {
        // Cancel the periodic work
        WorkManager.getInstance(context).cancelAllWorkByTag(LOCATION_WORK_TAG);
    }
}
