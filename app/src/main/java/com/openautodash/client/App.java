package com.openautodash.client;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class App extends Application {
    public static final String ForeGroundService = "foreground_service";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        NotificationManager manager = getSystemService(NotificationManager.class);

        NotificationChannel foregroundService = new NotificationChannel(
                ForeGroundService,
                "Foreground Service",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        foregroundService.setDescription("Required notification to keep things running when device asleep");

        //Create notification channels.
        manager.createNotificationChannel(foregroundService);
    }
}
