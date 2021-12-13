package com.example.badgrtrackr_final.api;

import com.example.badgrtrackr_final.data_types.User;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UserAPI {
    private User user;

    public UserAPI(InputStream is) {
        loadUserData(is);
    }

    public void loadUserData(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            reader.readLine();
            String[] userData = reader.readLine().split(";");
            user = new User(userData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }
}
