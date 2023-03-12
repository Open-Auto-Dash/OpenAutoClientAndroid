package com.openautodash.client;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.openautodash.client.ui.main.MainFragment;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

        Uri locationUri = getIntent().getData();

        if (locationUri != null) {
            String location = locationUri.toString();
            Log.d(TAG, "onCreate: Location String :" + location);
            //decode location
            String[] locationArray = location.split("[^,]*(?=\\\\?)");

            Log.d(TAG, "onCreate: Striped string is: " + Arrays.toString(locationArray));


            Toast.makeText(this, location, Toast.LENGTH_LONG).show();
        }
    }
}