package com.techtalk4geeks.studie;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * Created by alex on 3/19/16.
 */
public class AccessToken {
    String accessToken;
    String tokenType;
    String scope;
    QuizletUser user;
    MainActivity m;

    final String S = "Studie";

    public AccessToken(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        Log.d(S, "ACCESS TOKEN OBJECT IS BEING CREATED!");

        try {
            this.accessToken = jsonOb.getString("access_token");
            Log.i(S, "Access Token = " + this.accessToken);
            this.tokenType = jsonOb.getString("token_type");
            this.scope = jsonOb.getString("scope");
//            new ObtainUser().execute(jsonOb.getString("user_id"), this.accessToken);
            String username = jsonOb.getString("user_id");
            Log.i(S, "Username = " + username);
            user = new QuizletUser(username, accessToken);
//            MainActivity.setACCESSTOKEN(accessToken);
        } catch (Exception np) {
                Log.e("Studie", jsonOb.getString("error"));
        }
    }

    public QuizletUser getUser() {
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public void setMainActivity(MainActivity m) {
        this.m = m;
    }
}