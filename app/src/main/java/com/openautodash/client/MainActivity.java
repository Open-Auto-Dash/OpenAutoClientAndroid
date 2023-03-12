package com.openautodash.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.openautodash.client.ui.main.MainFragment;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private NavController navController;


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
            //decode location
            String[] locationArray = location.split("[^,]*(?=\\\\?)");

            String latLongStr = location.split(":")[1].split("\\?")[0]; // extract "43.5626795,-80.66848"
            String[] latLongArr = latLongStr.split(","); // split into latitude and longitude values
            double latitude = Double.parseDouble(latLongArr[0]); // convert latitude to double
            double longitude = Double.parseDouble(latLongArr[1]); // convert longitude to double

            Toast.makeText(this, latitude + ":" + longitude, Toast.LENGTH_LONG).show();

            navController.navigate(R.id.action_mainFragment_to_mapsFragment);
        }
    }
}