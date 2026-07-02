package com.example.myapplication.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.myapplication.data.sip.SipService;

public class KeepAliveManager {

    private static final String TAG = "KeepAliveManager";
    private static KeepAliveManager instance;
    private final Context context;
    private boolean isServiceRunning = false;

    private KeepAliveManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized KeepAliveManager getInstance(Context context) {
        if (instance == null) {
            instance = new KeepAliveManager(context);
        }
        return instance;
    }

    public void startKeepAlive() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running");
            return;
        }

        Log.d(TAG, "Starting foreground service");
        Intent serviceIntent = new Intent(context, ForegroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        isServiceRunning = true;
    }

    public void stopKeepAlive() {
        if (!isServiceRunning) {
            return;
        }

        Log.d(TAG, "Stopping foreground service");
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
        isServiceRunning = false;
    }

    public void updateNotification() {
        // Will be called when SIP registration status changes
        // The ForegroundService will handle the notification update
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }
}
