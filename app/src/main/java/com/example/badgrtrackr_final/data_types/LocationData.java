package com.example.badgrtrackr_final.data_types;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocationData {
    // hours of the day for setting the locHistoryWeek map
    private String[] hours = {"8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7pm", "8pm", "9pm", "10pm", "11pm", "12am"};

    private String name; // name of the location
    private String address; // location address
    private Map<String, Double> coordinates; // contains: latitude=x, longitude=y, keys are latitude, longitude
    private int trafficIndicator; // 0 = low, 1 = med, 2 = high
    private int trafficCount; // the total number of visits this week (will potentially be used for calculating traffic for features to be added)
    private Map<String, Map<String, Integer>> locHistoryWeek; // history divided by day and week
    private Double dist;

    // Constructor
    public LocationData(String[] csvRow, Map<String, String> weekData) {
        this.name = csvRow[0];
        this.address = csvRow[1] + ", " + csvRow[2] + ", " + csvRow[3] + " " + csvRow[4];
        this.coordinates = new HashMap<String, Double>();
        this.coordinates.put("longitude", Double.valueOf(csvRow[5]));
        this.coordinates.put("latitude", Double.valueOf(csvRow[6]));
        this.trafficIndicator = Integer.valueOf(csvRow[7]);
        this.dist = -1.0;
        formatLocationData(weekData); // takes the weekData Map and formats it into a Map<String, Map<String, Integer>>
    }

    // convert the Map<String, String> into a Map<String, Map<String, Integer>>
    public void formatLocationData(Map<String, String> row) {
        locHistoryWeek = new HashMap<>();
        int total = 0;
        Map<String, Integer> values;
        for (Map.Entry<String, String> loc : row.entrySet()) {
            String[] tempValues = loc.getValue().split("&");
            values = new HashMap<>();
            for (int i = 0; i < tempValues.length; i++) {
                int val = Integer.valueOf(tempValues[i]);
                values.put(hours[i], val);
                total += val;
            }
            locHistoryWeek.put(loc.getKey(), values);
        }
        trafficCount = total;
    }

    public void setDistance(Double dist) {
        this.dist = dist;
    }

    public Double getDistance() {
        return dist;
    }

    // get name
    public String getName() {
        return name;
    }

    // get address
    public String getAddress() {
        return address;
    }

    // get total visits this week
    public int getTrafficCount() {
        return trafficCount;
    }

    // get the traffic indicator
    public int getTrafficIndicator() {
        return trafficIndicator;
    }

    // returns the lat/long coordinate map
    public Map<String, Double> getCoordinates() {
        return coordinates;
    }

    // returns the Map for location history (to be used for charts if possible)
    public Map<String, Map<String, Integer>> getLocHistoryWeek() {
        return locHistoryWeek;
    }

    // gets the current day as a 3 letter identifier (ie "sun")
    public String getDayOfWeekName() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String today = new SimpleDateFormat("EE", Locale.ENGLISH).format(date.getTime());
        Log.d("time", today);
        return today;
    }

    // returns the hour of the day as a string with am/pm (ie "8am")
    public String getHourOfDay() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String ampm = "";
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 12) {
            hour = hour - 12;
            ampm = "pm";
        } else {
            ampm = "am";
        }
        return "" + hour + ampm;
    }
}
