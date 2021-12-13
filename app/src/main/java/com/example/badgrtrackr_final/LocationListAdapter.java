package com.example.badgrtrackr_final;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.badgrtrackr_final.api.LocationListAPI;
import com.example.badgrtrackr_final.data_types.LocationData;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LocationListAdapter extends BaseExpandableListAdapter {
    private Context context; // context passed from fragment
    private List<LocationData> locations; // list of locations to be displayed, changed by the filter/searches
    private List<LocationData> locationsOriginal; // original list to reset search
    private LocationListAPI locAPI; // instance of the current location API if needed
    private Map<String, Double> distSorter;

    public LocationListAdapter(Context context, LocationListAPI locAPI, List<LocationData> locations) {
        this.context = context;
        this.locAPI = locAPI;
        this.locations = locations;
        this.locationsOriginal =  locAPI.getLocationList();
    }

    public List<LocationData> getLocations() {
        return locations;
    }

    @Override
    public int getGroupCount() {
        return locations.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1; // 4 groups: address, number of people visiting, 2 charts
    }

    @Override
    public Object getGroup(int groupPosition) {
        return locations.get(groupPosition);
    }

    // returns the child data, either address or traffic count for now
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        switch(childPosition) {
            case 0:
                return locations.get(groupPosition).getAddress();
            case 1:
                return locations.get(groupPosition).getTrafficCount();
            default:
                return 0;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // sets the groups in the expandable list
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        LocationData group = (LocationData) getGroup(groupPosition); // get the Location object being created

        // if the view is empty, inflate the view into the screen
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.exp_group, null);
        }

        TextView item = view.findViewById(R.id.groupTextName); // find the text box in the exp_group xml
        item.setTypeface(null, Typeface.BOLD);
        item.setText(group.getName()); // set the text box to the location name

        View trafficIndicator = view.findViewById(R.id.traffic_indicator); // get the traffic indicator for the current location
        switch(group.getTrafficIndicator()) { // return the correct indicator shape based on the location's indicator
            case 0:
                trafficIndicator.setBackground(ContextCompat.getDrawable(context, R.drawable.traffic_indicator_low));
                break;
            case 1:
                trafficIndicator.setBackground(ContextCompat.getDrawable(context, R.drawable.traffic_indicator_medium));
                break;
            case 2:
                trafficIndicator.setBackground(ContextCompat.getDrawable(context, R.drawable.traffic_indicator_high));
                break;
            default:
                break;
        }

        //Calculating and adding the distance data
        double distance;
        float[] res = new float[10];
        if (locAPI.getCurrLoc() != null){
            Location.distanceBetween(locAPI.getCurrLoc().latitude, locAPI.getCurrLoc().longitude, group.getCoordinates().get("longitude"), group.getCoordinates().get("latitude"), res);
            distance = res[0] * 0.000621371;
        } else {
            distance = -1;
        }
        TextView distanceView = view.findViewById(R.id.locDistance);
        locations.get(groupPosition).setDistance(Math.round(100*distance)/100.0);
        distanceView.setText(String.valueOf(Math.round(100*distance)/100.0) + " mi.");

        if (groupPosition % 2 == 1) {
            Drawable draw = context.getDrawable(R.drawable.cus_exp_item_other);
            LinearLayout lin = view.findViewById(R.id.linlay);
            lin.setBackground(draw);
        } else {
            Drawable draw = context.getDrawable(R.drawable.cus_exp_item);
            LinearLayout lin = view.findViewById(R.id.linlay);
            lin.setBackground(draw);
        }
        return view;
    }

    // creates the child view for the dropdown area of each group
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        LocationData group = (LocationData) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.exp_child, null);
        }
        TextView item = view.findViewById(R.id.childTextView);
        String[] addy = group.getAddress().split(",");
        String traffic = "";
        int indicator = group.getTrafficIndicator();
        if (indicator == 0) {
            traffic = "Low";
        } else if (indicator == 1) {
            traffic = "Med";
        } else {
            traffic = "High";
        }
        item.setText(addy[0] + ", " + addy[1] + ", " + addy[2] + "\nTraffic: " + traffic);
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    // filters the locations list for the search strings
    public List<LocationData> filterData(String query) {
        query = query.toLowerCase(); // converts query to lowercase

        if (query.isEmpty() || query.equals("")) { // if the query has nothing in it or is empty
            locations = locationsOriginal; // reset the locations list
             return locations; // return all locations
        } else { // if the search text is not empty
            List<LocationData> newList = new ArrayList<>(); // temporary list
            for (LocationData location : locationsOriginal) { // iterate through all names in the location list
                if (location.getName().toLowerCase().contains(query)) { // add items containing the query
                    newList.add(location); // return the list
                }
            }
            return newList;
        }
    }

    // something wrong
    public List<LocationData> distanceAsc() {
        List<LocationData> temp = distanceDesc();

        Collections.sort(temp, compareById);
        return temp;
    }

    Comparator<LocationData> compareById = new Comparator<LocationData>() {
        @Override
        public int compare(LocationData o1, LocationData o2) {
            return o1.getDistance().compareTo(o2.getDistance());
        }
    };

    public List<LocationData> distanceDesc() {
        List<LocationData> temp = new ArrayList<>();
        String[][] vals = new String[locations.size()][];

        for (int i = 0; i < locations.size(); i++) {
            String[] t = {locations.get(i).getName(), locations.get(i).getDistance().toString()};
            vals[i] = t;
        }

        Arrays.sort(vals, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return Double.valueOf(o2[1]).compareTo(Double.valueOf(o1[1]));
            }
        });

        for (int i = 0; i < vals.length; i++) {
            for (LocationData loc : locations) {
                if (locAPI.getLocation(loc.getName()).getName().equals(vals[i][0])) {
                    temp.add(loc);
                }
            }
        }
        return temp;
    }

    public List<LocationData> trafficDesc() {
        List<LocationData> newList = new ArrayList<>();

        for (LocationData loc : locations) {
            if (loc.getTrafficIndicator() == 2) {
                newList.add(loc);
            }
        }

        for (LocationData loc : locations) {
            if (loc.getTrafficIndicator() == 1) {
                newList.add(loc);
            }
        }

        for (LocationData loc : locations) {
            if (loc.getTrafficIndicator() == 0) {
                newList.add(loc);
            }
        }

        locations = locationsOriginal;
        return newList;
    }

    public List<LocationData> trafficAsc() {
        List<LocationData> newList = new ArrayList<>();

        for (LocationData loc : locations) {
            if (loc.getTrafficIndicator() == 0) {
                newList.add(loc);
            }
        }

        for (LocationData loc : locations) {
            if (loc.getTrafficIndicator() == 1) {
                newList.add(loc);
            }
        }

        for (LocationData loc : locations) {
            if (loc.getTrafficIndicator() == 2) {
                newList.add(loc);
            }
        }

        locations = locationsOriginal;
        return newList;
    }
}
