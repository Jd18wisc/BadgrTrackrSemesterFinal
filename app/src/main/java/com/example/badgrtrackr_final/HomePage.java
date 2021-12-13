package com.example.badgrtrackr_final;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.badgrtrackr_final.api.LocationListAPI;
import com.example.badgrtrackr_final.data_types.LocationData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePage extends Fragment {
    ExpandableListView expListView; // expandable list view
    LocationListAdapter expListAdapter; // expandable list adapter (put data into expandable list), using custom LocationListAdapter class
    SearchView searchView; // the search bar object
    LocationListAPI locAPI; // location list API to access location data
    FusedLocationProviderClient client;
    LatLng currLocation;
    Spinner dropdown;
    ArrayAdapter<String> adapter;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InputStream is = getResources().openRawResource(R.raw.location_data); // create a new input stream for the location_data csv
        InputStream allLocHisIs = getResources().openRawResource(R.raw.location_history); // create a new input stream for the location_history csv

        context = view.getContext();
        locAPI = new LocationListAPI(is, allLocHisIs); // create a new location API with the required data

        expListView = view.findViewById(R.id.expandable_list); // access the expandable view xml object
        expListAdapter = new LocationListAdapter(view.getContext(), locAPI, locAPI.getLocationList()); // set the adapter to a new instance of the location adapter
        expListView.setAdapter(expListAdapter); // set the expandable list with the adapter

        int permission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client = LocationServices.getFusedLocationProviderClient(getContext());
            client.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                            locAPI.setCurrLoc(currLocation);
                            expListAdapter = new LocationListAdapter(view.getContext(), locAPI, locAPI.getLocationList());
                            expListView.setAdapter(expListAdapter);
                        }
                    });
        }

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int lastExpandedPosition = -1;
            @Override
            public void onGroupExpand(int i) {
                if (lastExpandedPosition != -1 && i != lastExpandedPosition) {
                    expListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = i;
            }
        });
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                String selected = expListAdapter.getChild(i, i1).toString();
                return true;
            }
        });

        String[] items = new String[]{
                "None", "Dist L->H", "Dist H->L", "Traffic L->H", "Traffic H->L"
        };

        // get the search view object in xml
        searchView = view.findViewById(R.id.search_bar);

        // override the search features
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<LocationData> temp = expListAdapter.filterData(query); // search locations for submitted string
                expListAdapter = new LocationListAdapter(view.getContext(), locAPI, temp); // new adapter instance with only locations found by search
                expListView.setAdapter(expListAdapter); // set the view to the new adapterni
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<LocationData> temp = expListAdapter.filterData(newText); // search locations for submitted string
                expListAdapter = new LocationListAdapter(view.getContext(), locAPI, temp); // new adapter instance with only locations found by search
                expListView.setAdapter(expListAdapter); // set the view to the new adapter
                return false;
            }
        });


        dropdown = view.findViewById(R.id.home_dropdown);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String comp = parent.getItemAtPosition(position).toString();
                List<LocationData> temp = new ArrayList<>();
                if (comp.equals("None")) {
                    temp = expListAdapter.filterData(""); // search locations for submitted string
                } else if (comp.equals("Traffic L->H")) {
                    temp = expListAdapter.trafficAsc(); // search locations for submitted string
                } else if (comp.equals("Traffic H->L")) {
                    temp = expListAdapter.trafficDesc(); // search locations for submitted string
                } else if (comp.equals("Dist H->L")) {
                    temp = expListAdapter.distanceDesc(); // search locations for submitted string
                } else if (comp.equals("Dist L->H")) {
                    temp = expListAdapter.distanceAsc(); // search locations for submitted string
                }
                expListAdapter = new LocationListAdapter(context, locAPI, temp); // new adapter instance with only locations found by search
                expListView.setAdapter(expListAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
}
