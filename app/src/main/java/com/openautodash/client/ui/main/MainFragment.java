package com.openautodash.client.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.openautodash.client.R;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private Button buttonClimate;
    private Button buttonControls;
    private Button buttonRoutines;
    private Button buttonLocation;
    private Button buttonOBDData;

    private TextView testText;

    private BottomSheetBehavior bottomSheetBehavior;


    private MainViewModel mViewModel;


    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        buttonClimate = view.findViewById(R.id.b_main_climate);
        buttonControls = view.findViewById(R.id.b_main_controls);
        buttonRoutines = view.findViewById(R.id.b_main_routines);
        buttonLocation = view.findViewById(R.id.b_main_location);
        buttonOBDData = view.findViewById(R.id.b_main_obd);

        testText = view.findViewById(R.id.tv_main_test);

//        LinearLayout bottomSheet = view.findViewById(R.id.bottom_sheet_container);
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
//        // Set a drag callback to allow the user to drag the bottom sheet
//        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                // Handle the slide offset
//            }
//
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                // Handle the state change
//            }
//        });



        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, new IntentFilter("MyServiceData"));

        setClickListeners(view);
        return view;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("MyServiceData".equals(intent.getAction())) {
                String data = intent.getStringExtra("data");
                // Handle the received data
                Log.d("MainActivity", "Received data: " + data);
                testText.setText(data);
            }
        }
    };


    private void setClickListeners(View view){
        buttonClimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        buttonControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        buttonRoutines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_mapsFragment);
            }
        });

        buttonOBDData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);

    }
}