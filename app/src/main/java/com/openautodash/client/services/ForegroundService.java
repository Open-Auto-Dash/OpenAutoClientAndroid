package com.openautodash.client.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.openautodash.client.App.ForeGroundService;


import com.openautodash.client.MainActivity;
import com.openautodash.client.R;
import com.openautodash.client.bluetooth.BLEScanner;

public class ForegroundService extends Service {
    private static final String TAG = "ForeGroundService";

    private static final int NOTIFICATION_ID = 42069;
    private static final long SCAN_INTERVAL_MS = 10000; // 20 seconds
    private static final long SCAN_DURATION_MS = 3000; // 4 seconds

    private BLEScanner bleScanner;
    private Handler handler;
    private boolean isScanning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        bleScanner = new BLEScanner(this);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        // If we get killed, after returning from here, restart
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, ForeGroundService)
                .setContentTitle("Background Service Running")
                .setSmallIcon(R.drawable.ic_ignition_on)
                .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        startScanning();
        return START_STICKY;
    }

    private void startScanning() {
        if (!isScanning) {
            isScanning = true;
            scanCycle();
        }
    }

    private void scanCycle() {
        bleScanner.startScanning();
        handler.postDelayed(() -> {
            bleScanner.stopScanning();
            handler.postDelayed(this::scanCycle, SCAN_INTERVAL_MS - SCAN_DURATION_MS);
        }, SCAN_DURATION_MS);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isScanning = false;
        handler.removeCallbacksAndMessages(null);
        bleScanner.stopScanning();
    }

}
