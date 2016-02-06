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

        try {
            name = jsonOb.getString("username");
        } catch (NullPointerException np) {
            if (jsonOb.getString("error_description") != null) {
                Log.e("Studie", jsonOb.getString("error_description"));
            }
        }
        if (jsonOb.getString("account_type").equalsIgnoreCase("plus")) {
            isPlus = true;
        }
        id = jsonOb.getString("id");
        profileImage = jsonOb.getString("profile_image");
        try {
            totalTermsEntered = Integer.parseInt(jsonOb.getJSONObject("statistics").getString("public_terms_entered"));
        } catch (NumberFormatException nf) {
            Log.e("Studie", "NUMBER FORMAT EXCEPTION ON TOTAL TERMS ENTERED!!!");
            totalTermsEntered = -1;
        }
        Log.i("Studie", "User Created\n--- " + name + " ---\nisPlus = " + isPlus + "\nid = " + id);
    }

}
