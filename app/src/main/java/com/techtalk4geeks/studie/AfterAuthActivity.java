package com.techtalk4geeks.studie;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class AfterAuthActivity extends Activity {

    public static final String S = "Studie";
    String CODE;
    MainActivity ma = new MainActivity();
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon(R.drawable.studieactionbar);

        setTitle("Studie");
        Uri uri = getIntent().getData();
        String uriString = uri.toString();
        Log.i(S, "URI = " + uriString);
        if (uriString.substring(35, 39).equalsIgnoreCase("code")) {
            Log.i(S, "Success!");
            CODE = uriString.substring(40);
            Log.i(S, "CODE = " + CODE);
//            Handler handler = new Handler() {
//                public void handleMessage(Message msg) {
//                    performPostCall(hashMap);
//                }
//            };
//            handler.dispatchMessage(new Message());

            try {
                new ObtainAccessToken().execute(CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            ma.setACCESSTOKEN(ACCESS_TOKEN);
        } else if (uriString.substring(35, 39).equalsIgnoreCase("erro")) { // NOT A TYPO
            Log.e(S, "Failed!");
        } else {
            Log.e(S, "Failed!");
            throw new NullPointerException();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String performPostCall(String requestCode) {
        URL url;
        String response = "";
        try {
            url = new URL("https://api.quizlet.com/oauth/token");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            String basicAuth = "Basic YnJnVVVQeXhERjpIUWhCQ0tXUDVKR1M1N25ZZFVIcUF2";
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            Log.i(S, "Starting token process");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            Log.d(S, "requestCode = " + requestCode.toString());

            writer.write(requestCode);

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            Log.d(S, "responseCode = " + responseCode);
            Log.d(S, "response = " + conn.getResponseMessage());

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                InputStream in = conn.getInputStream();
                response = getStringFromInputStream(in);
                Log.d(S, response);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                while ((line = br.readLine()) != null) {
//                    response += line;
//                }
            } else {
                Log.e(S, "responseCode != HttpsURLConnection.HTTP_OK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
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


    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    class ObtainAccessToken extends AsyncTask<String, String, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                final HashMap hashMap = new HashMap<String, String>();
                hashMap.put("grant_type", "authorization_code");
                hashMap.put("code", CODE);
                String requestCode = "grant_type=authorization_code&code=" + CODE;
                String tokenJSON = performPostCall(requestCode);
                JSONObject jsonOb;
                String token;
                jsonOb = new JSONObject(tokenJSON);
                token = jsonOb.getString("access_token");
                Log.i(S, "token = " + token);
                accessToken = new AccessToken(tokenJSON);
                return tokenJSON;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute() {
        }
    }
}