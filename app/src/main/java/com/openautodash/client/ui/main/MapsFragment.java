package com.openautodash.client.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.openautodash.client.R;

public class MapsFragment extends Fragment {
    private static final String TAG = "MapsFragment";

    String locationUri = "geo:43.5626795,-80.66848?q=43.5626795,-80.66848";


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //Style the map
            googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.map_theme_night));

            //decode location
            String latLongStr = locationUri.split(":")[1].split("\\?")[0]; // extract "43.5626795,-80.66848"
            String[] latLongArr = latLongStr.split(","); // split into latitude and longitude values
            double latitude = Double.parseDouble(latLongArr[0]); // convert latitude to double
            double longitude = Double.parseDouble(latLongArr[1]); // convert longitude to double

            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            CameraPosition cameraPosition = new CameraPosition(
                    latLng,
                    15,
                    0,
                    0);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getView().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorBold));

        String args = MapsFragmentArgs.fromBundle(getArguments()).getLocationUri();
        if(args != null){
            locationUri = args;
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}