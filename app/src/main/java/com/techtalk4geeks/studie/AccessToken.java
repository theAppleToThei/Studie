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

    public AccessToken(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        try {
            this.accessToken = jsonOb.getString("access_token");
            this.tokenType = jsonOb.getString("token_type");
            this.scope = jsonOb.getString("scope");
            new ObtainUser().execute(jsonOb.getString("user_id"), this.accessToken);
        } catch (Exception np) {
                Log.e("Studie", jsonOb.getString("error"));
        }
    }
}

class ObtainUser extends AsyncTask<String, String, String> {

    private Exception exception;

    protected String doInBackground(String... params) {
        try {
            String apiLink = "https://api.quizlet.com/2.0/users/" + params[0] + "?access_token=" + params[1] + "&whitespace=1";
            URL url = new URL(apiLink);
            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;

            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpConnection.getInputStream();
                String userJSON = getStringFromInputStream(in);
                return userJSON;
            } else {
                return null;
            }
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    protected void onPostExecute() {
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}