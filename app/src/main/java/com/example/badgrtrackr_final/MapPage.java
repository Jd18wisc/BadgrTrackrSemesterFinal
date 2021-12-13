package com.example.badgrtrackr_final;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.badgrtrackr_final.api.LocationListAPI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.badgrtrackr_final.data_types.Location;

public class MapPage extends Fragment implements OnMapReadyCallback {
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    LocationListAPI locAPI; // location list API to access location data
    List<Location> locList;
    FusedLocationProviderClient client;

    public MapPage() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        InputStream lis = getResources().openRawResource(R.raw.location_data);

        mView = inflater.inflate(R.layout.map_page, container, false);
        client = LocationServices.getFusedLocationProviderClient(getContext());
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InputStream is = getResources().openRawResource(R.raw.location_data); // create a new input stream for the location_data csv
        InputStream allLocHisIs = getResources().openRawResource(R.raw.location_history); // create a new input stream for the location_history csv

        locAPI = new LocationListAPI(is, allLocHisIs); // create a new location API with the required data
        locList =  locAPI.getLocationList();

        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        int permission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED){
            googleMap.setMyLocationEnabled(true);
            client.getLastLocation()
                    .addOnCompleteListener(task -> {
                        android.location.Location mLastKnown = task.getResult();
                        if (task.isSuccessful() && mLastKnown != null){
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnown.getLatitude(), mLastKnown.getLongitude()),
                                    14
                            ));
                        }
                    });
        }
        ArrayList<MarkerOptions> locations = new ArrayList<MarkerOptions>();
        for (Location location : locList){
            Log.e("Loc", location.getName() + ": "  + location.getCoordinates().get("longitude") + ", " +  location.getCoordinates().get("latitude"));
            LatLng latlng = new LatLng(location.getCoordinates().get("longitude"),location.getCoordinates().get("latitude"));

            locations.add(new MarkerOptions().position(latlng));
        }
        for (int i=0;i<locations.size();i++){
            mGoogleMap.addMarker(locations.get(i));
        }
    }
}