package com.openautodash.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.openautodash.client.services.ForegroundService;
import com.openautodash.client.ui.main.MainFragment;
import com.openautodash.client.ui.main.MainFragmentDirections;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, MainFragment.newInstance())
//                    .commitNow();
//        }

        // Get the NavController from the NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        navController = navHostFragment.getNavController();

        Uri locationUri = getIntent().getData();

        if (locationUri != null) {
            String location = locationUri.toString();
            Log.d(TAG, "onCreate: Location String :" + location);


            navController.navigate(MainFragmentDirections.actionMainFragmentToMapsFragment().setLocationUri(location));


//            navController.navigate(R.id.action_mainFragment_to_mapsFragment);
        }
        startScanningService();
    }
    private void startScanningService() {
        if (!isServiceRunning(ForegroundService.class)) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        Log.d(TAG, "isServiceRunning");
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, "isServiceRunning: yes");
                return true;
            }
        }
        Log.d(TAG, "isServiceRunning: no");
        return false;
    }
}