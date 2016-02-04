package com.techtalk4geeks.studie;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by alex on 2/3/16.
 */
public class QuizletSchool {

    String name;
    String id;
    String city;
    String state;
    String country;
    String latitude;
    String longitude;

    public QuizletSchool(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        name = jsonOb.getString("name");
        id = jsonOb.getString("id");
        city = jsonOb.getString("city");
        state = jsonOb.getString("state");
        country = jsonOb.getString("country");
        latitude = jsonOb.getString("latitude");
        longitude = jsonOb.getString("longitude");

        Log.i("Studie", "School Created\n--- " + name + " ---\n" + city + ", " + state);
    }

    public QuizletSchool(String name, String id, String city, String state, String country, String latitude, String longitude) {
        this.name = name;
        this.id = id;
        this.city = city;
        this.state = state;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
