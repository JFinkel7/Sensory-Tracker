package com.jfinkelstudios.mobile.sensorytracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import static com.jfinkelstudios.mobile.sensorytracker.App.KEY_LOCATION_STATE;
import static com.jfinkelstudios.mobile.sensorytracker.App.prefs;

/**** (1) - Add Listener ****/
public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private static final String TAG = "Server";
    private SwitchCompat locationSwitch;
    private Intent serviceIntent;
    private boolean isBound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // - Find View By ID's
        locationSwitch = findViewById(R.id.serviceSwitch);
        // - Service Intent
        if (!isBound) {
            serviceIntent = new Intent(MainActivity.this, SensorBackgroundService.class);
            // - Start The Foreground Service
            ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
            bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }




    }


    @Override
    protected void onStart() {
        super.onStart();
        // - Get Switch (Button) State
        boolean isLocationServiceOn = prefs.getBoolean(KEY_LOCATION_STATE, false);
        locationSwitch.setChecked(isLocationServiceOn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Create An Instance
        SharedPreferences.Editor switchBtnEditor = prefs.edit();
        // Save The Data
        switchBtnEditor.putBoolean(KEY_LOCATION_STATE, locationSwitch.isChecked());
        // Apply The Data
        switchBtnEditor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(this);
        }
    }

    public void btn_StopService(View view) {
        // * Stop The Foreground Service *
        stopService(serviceIntent);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        SensorBackgroundService.BroadCastBinder binder = (SensorBackgroundService.BroadCastBinder) iBinder;
        SensorBackgroundService sensorBackgroundService = binder.getServices();

        
        final ArrayAdapter<String> ADAPTER = new ArrayAdapter<>(MainActivity.this, R.layout.activity_listview, binder.getTimeStampData());

        // (3) - Set The ListView
        final ListView LIST_VIEW = findViewById(R.id.mobile_list);
        LIST_VIEW.setAdapter(ADAPTER);
        isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        isBound = false;
    }
}// - END