package com.example.badgrtrackr_final;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNav; // create bottom nav
    HomePage home = new HomePage(); // create new home page fragment
    MapPage map = new MapPage(); // create new map fragment
    AccountPage account = new AccountPage(); // create new account fragment
    int PERMISSIONS_REQUEST_ACCESS_ACCESS_FINE_LOCATION = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav); // set bottom nav object to the bottom nav xml object
        bottomNav.setSelectedItemId(R.id.home_option); // set the selected item to the home page
        bottomNav.setOnItemSelectedListener(this::handleSelectNavigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, home).commit();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); // sets the header to custom instead of default
        getSupportActionBar().setCustomView(R.layout.action_bar); // pass in the custom app header
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_ACCESS_FINE_LOCATION);
    }

    // handles the nav page change selection
    public boolean handleSelectNavigation(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.home_option:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, home).commit();
                return true;
            case R.id.map_option:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, map).commit();
                return true;
            case R.id.account_option:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, account).commit();
                return true;
        }
        return false;
    }
}