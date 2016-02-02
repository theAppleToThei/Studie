package com.techtalk4geeks.studie;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by alex on 2/1/16.
 */
public class QuizletUser {

    String name;
    String id;
    Boolean isPlus = false;
    String profileImage;
    int totalTermsEntered;

    public QuizletUser(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        name = jsonOb.getString("username");
        if (jsonOb.getString("account_type").equalsIgnoreCase("plus")) {
            isPlus = true;
        }
        id = jsonOb.getString("id");
        profileImage = jsonOb.getString("profile_image");
        Log.i("Studie", "User Created\n--- " + name + "---\nisPlus = " + isPlus + "\nid = " + id);
    }

}
