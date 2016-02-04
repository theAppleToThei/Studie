package com.techtalk4geeks.studie;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alex on 2/3/16.
 */
public class QuizletGroup {

    String name;
    String description;
    String id;
    int setCount;
    ArrayList<QuizletSet> sets = new ArrayList<QuizletSet>();
    ArrayList<QuizletUser> members = new ArrayList<QuizletUser>();
    QuizletSchool school;

    public QuizletGroup(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        name = jsonOb.getString("name");
        description = jsonOb.getString("description");
        id = jsonOb.getString("id");
        try {
            setCount = Integer.parseInt(jsonOb.getString("set_count"));
        } catch (NumberFormatException nf) {
            Log.e("Studie", "NUMBER FORMAT EXCEPTION ON TERM COUNT!!!");
            setCount = -1;
        }

        Log.i("Studie", "Group Created\n--- " + name + " ---\n" + setCount + " sets");

        school = new QuizletSchool(jsonOb.getJSONObject("school").getString("name"), jsonOb.getJSONObject("school").getString("id"), jsonOb.getJSONObject("school").getString("city"), jsonOb.getJSONObject("school").getString("state"), jsonOb.getJSONObject("school").getString("country"), jsonOb.getJSONObject("school").getString("latitude"), jsonOb.getJSONObject("school").getString("longitude"));
    }
}
