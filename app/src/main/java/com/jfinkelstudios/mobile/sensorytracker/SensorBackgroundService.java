package com.jfinkelstudios.mobile.sensorytracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.jfinkelstudios.mobile.sensorytracker.App.CHANNEL_ID;
import static com.jfinkelstudios.mobile.sensorytracker.App.KEY_TIME_STAMP;
import static com.jfinkelstudios.mobile.sensorytracker.App.prefs;

public final class SensorBackgroundService extends Service {
    // ----------------------------------->
    private static final String TAG = "SensorBackgroundService";
    private static BroadcastReceiver receiver;
    private final IBinder BROADCAST_BINDER = new BroadCastBinder();
    // ----------------------------------->


    /**** ========= [On-Bind] ========= ****/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return (BROADCAST_BINDER);
    }


    /**** ========= [On-Create] ========= ****/
    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new TaskBroadCast();
    }


    /**** ========= [On-Start] ========= ****/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter FILTER = new IntentFilter();
        FILTER.addAction(Intent.ACTION_USER_PRESENT);
        FILTER.addAction(Intent.ACTION_SCREEN_OFF);
        FILTER.addAction(Intent.ACTION_SCREEN_ON);


        // Register The (Motion Sensor) Receiver
        registerReceiver(receiver, FILTER);

        Toast.makeText(SensorBackgroundService.this, "Service Started", Toast.LENGTH_SHORT).show();

        // - Create Notification Intent
        Intent notificationIntent = new Intent(SensorBackgroundService.this, MainActivity.class);

        // - Create PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(SensorBackgroundService.this, 0,
                notificationIntent, 0);

        // - Create Notification
        Notification notification = new NotificationCompat.Builder(SensorBackgroundService.this, CHANNEL_ID)
                .setContentTitle("Sensory Service")
                .setContentText("Working...")
                .setSmallIcon(R.drawable.ic_motion30)
                .setContentIntent(pendingIntent)
                .build();

        // - Start The Foreground Service Notification
        startForeground(1, notification);


        return START_NOT_STICKY;
    }


    /**** ========= [On-Destroy] ========= ****/
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(SensorBackgroundService.this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        unregisterReceiver(receiver);
    }


    /**** <<<==========================[Binder Inner-Class] ==========================>>> ****/
    public final class BroadCastBinder extends Binder {
        // Gets The Service
        public SensorBackgroundService getServices() {
            return (SensorBackgroundService.this);
        }

        public String[] getTimeStampData() {
            Set<String> data = prefs.getStringSet(KEY_TIME_STAMP, new HashSet<>());
            String[] array = new String[data.size()];
            data.toArray(array);
            return (array);
        }


    }


    /**** <<<==========================[Receiver Inner-Class] ==========================>>> ****/
    public static final class TaskBroadCast extends BroadcastReceiver implements SensorEventListener {
        private static final Set<String> SET = new HashSet<>();
        private static SharedPreferences.Editor timeStampEditor;
        private Context context;


        @SuppressLint("NewApi")
        // - On Receive
        @Override
        public void onReceive(Context context, Intent intent) {
            this.context = context;

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // ----------------------------------->
                SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

                // Check To See If Sensory Manger is Not Null
                if (sensorManager != null) {
                    // - Create Sensory Instance
                    Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

                    //  <===========  On Screen (OFF) ===========>
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        // * Register The Motion Proximity Sensor
                        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                        //Log.d(TAG, "onReceive: Screen OFF");
                    }

                    //  <===========  On Screen (ON) ===========>
                    else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                        Toast.makeText(context, "Un-Registered Listener", Toast.LENGTH_SHORT).show();
                        // * Un-Register The Motion Proximity Sensor
                        sensorManager.unregisterListener(this, sensor);

                        // - Get Shared Preference Data Set
                        // Replace The Second Argument With (NULL | new HashSet<String>()
                        //Set<String> data = prefs.getStringSet(KEY_TIME_STAMP, null);

                        // - Sort The Data
                        //TreeSet<String> sortedSet = new TreeSet<>(data);

                        // - Show The Data
                        //Log.d(TAG, "onReceive: " + data);


                        // - Force Garbage Collection
                        System.gc();
                        System.runFinalization();
                        //Log.d(TAG, "onReceive: Screen ON");
                    }

                }// END

            }// END

        }// END - onReceive
        // <===========  On Sensory Changed Event ===========>


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Uri notification;
            Ringtone ringtone;
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            // - Get The Difference
            float diff = (float) Math.sqrt(x * x + y * y + z * z);
            // - Check If Phone is Moving
            if (diff > 2.0) { // 2.0 Is the Current Threshold
                // - Play Ringtone
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                ringtone = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
                ringtone.play();

                // - Get Current Time
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                // - Write The Current Time
                SET.add(currentTime);


                // - Save The Current Time
                timeStampEditor = prefs.edit();
                timeStampEditor.putStringSet(KEY_TIME_STAMP, SET);
                timeStampEditor.apply();
            }

            // Get The Current Time
            //String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            //Toast.makeText(context, "Time = " + currentTime, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }


    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        Log.d(TAG, "Used Memory Before " + usedMemoryBefore);
        //-----------------------------------------------------------------------
        // Check Memory Usage For This Block Of Code
        //someMethod();
        //-----------------------------------------------------------------------
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        Log.d(TAG, "Memory Increased: " + (usedMemoryAfter - usedMemoryBefore));
    }


}// CLASS-ENDS

