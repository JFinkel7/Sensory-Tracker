/*
 * Project: << Sensory Remote Tracker >>
 * Software Developer: Denis J Finkel
 * Start Date: 10/16/2020
 * Description: Track Device Based On Motion Event
 * Tools: ?
 * App Uses: Multi-Threading, Background Service , JobScheduler
 * INFO: ?
 * NOTE: Note The Data is Sent To My Host Computer
 */

package com.jfinkelstudios.mobile.sensorytracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class App extends Application {
    // >
    public static final String CHANNEL_ID = "Sensory_Channel";
    public static final String KEY_LOCATION_STATE = "LocationState";
    public static final String KEY_TIME_STAMP = "TimeStamp";
    public static SharedPreferences prefs;

    // >
    @Override
    public void onCreate() {
        super.onCreate();
        // - Set Shared Preference
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // - Set The Notification Channel
        createNotificationChannel();

        // Get The Current Time
        //String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        //Toast.makeText(this, "Time = " + currentTime, Toast.LENGTH_SHORT).show();

    }


    // ***  Configure Notification Channel ***
    private void createNotificationChannel() {
        // Checks To See If The Phone API Level is >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sensory Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            // Create A Notification Manager
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(serviceChannel);

        }
    }


}// CLASS ENDS