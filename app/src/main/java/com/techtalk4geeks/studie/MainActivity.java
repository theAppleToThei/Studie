package com.techtalk4geeks.studie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {

    public static final String S = "Studie";

    Button linkQuizletAccount;
    Button findSetButton;
    EditText quizletLinkEditText;
    AlertDialog invalidURLDialog;
    AlertDialog noURLDialog;
    AlertDialog itWorkedDialog;
    AlertDialog signInDialog;
    String quizletTitle = "null";

    Animation animationFadeIn;

    public static Boolean isSignedIn = false;
    public static String ACCESS_TOKEN;
    public static String username;
    public static final String CLIENT_ID = "brgUUPyxDF";
    public static final String state = "PK_STUDIE";

    private static MainActivity me;

    String quizletLink;
    String apiLink;
    String authLink = "https://quizlet.com/authorize?response_type=code&client_id=" + CLIENT_ID + "&scope=read&state=" + state;
    int substringStart;
    int substringEnd;
    int charCounter = 0;

    public static QuizletSet currentQuizletSet;

    String setJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        setContentView(R.layout.activity_main);
        File file = new File(getFilesDir(), "Studie.txt");

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon(R.drawable.studieactionbar);

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);

        invalidURLDialog = new AlertDialog.Builder(MainActivity.this).create();
        invalidURLDialog.setTitle("Invalid URL");
        invalidURLDialog.setMessage("The link you provided was not a full Quizlet Link. Note that short URLs and URLs that redirect are not supported at this time.");
        invalidURLDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        noURLDialog = new AlertDialog.Builder(MainActivity.this).create();
        noURLDialog.setTitle("No URL Provided");
        noURLDialog.setMessage("Please paste a Quizlet link in the field to find a set.");
        noURLDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        itWorkedDialog = new AlertDialog.Builder(MainActivity.this).create();
        itWorkedDialog.setTitle("It worked!");
        itWorkedDialog.setMessage("No summary here... Why though?");
        itWorkedDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        linkQuizletAccount = (Button) this.findViewById(R.id.signInButton);
        findSetButton = (Button) this.findViewById(R.id.findSetButton);
        quizletLinkEditText = (EditText) this.findViewById(R.id.quizletLinkEditText);

        quizletLinkEditText.setMaxLines(1);
        quizletLinkEditText.setImeActionLabel("Find", 66);
        quizletLinkEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_SEARCH) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {

                    return true;
                } else {
                    return false;
                }
            }
        });

        findSetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(S, "onClick received for findSetButton");
                quizletLink = String.valueOf(quizletLinkEditText.getText().toString());
                new checkLink().execute(quizletLink);
            }
        });

        if (file.exists()) {
            FileInputStream FIN;
            try {
                FIN = openFileInput("Studie.txt");
                InputStreamReader ISR = new InputStreamReader(FIN);
                BufferedReader br = new BufferedReader(ISR);
                String jsonString = br.readLine();
                JSONObject JSONObject = new JSONObject(jsonString);
                isSignedIn = JSONObject.getBoolean("isSignedIn");
                Log.i(S, "isSignedIn = " + isSignedIn);
                if (isSignedIn == true) {
                    ACCESS_TOKEN = JSONObject.getString("ACCESS_TOKEN");
                    Log.i(S, "ACCESS_TOKEN = " + ACCESS_TOKEN);
                    username = JSONObject.getString("username");
                    Log.i(S, "username = " + username);
                } else {
                    Log.i(S, "isSignedIn = " + isSignedIn);
                    setContentView(R.layout.before_auth);
                    overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                    Button nothanks = (Button) this.findViewById(R.id.nosignin);
                    TextView disclaimer = (TextView) this.findViewById(R.id.signindescription);
                    disclaimer.startAnimation(animationFadeIn);
                    nothanks.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            setContentView(R.layout.activity_main);
                            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                        }
                    });
                    Button signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
                    signInIntro.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Uri uri = Uri.parse(authLink);
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(webIntent);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
                saveFile(MainActivity.this);
            } catch (Exception e) {
                Log.e(S, "Exception while creating/saving file.");
                e.printStackTrace();
            }
            if (isSignedIn == false) {
                setContentView(R.layout.before_auth);
                overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                TextView disclaimer = (TextView) this.findViewById(R.id.signindescription);
                disclaimer.startAnimation(animationFadeIn);
                Button nothanks = (Button) this.findViewById(R.id.nosignin);
                nothanks.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setContentView(R.layout.activity_main);
                        overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                    }
                });
                Button signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
                signInIntro.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Uri uri = Uri.parse(authLink);
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(webIntent);
                    }
                });
            }
            try {
                getActionBar().setDisplayUseLogoEnabled(false);
            } catch (NullPointerException np) {
                Log.e(S, "Method setDisplayUseLogoEnabled produced NullPointerException!");
            }

        }
    }

    public void checkDomain() {
        Log.i(S, "quizletLink = " + quizletLink);
        int domainIndex = quizletLink.indexOf(".com/");
        if (domainIndex <= 0) {
            Log.e(S, "domainIndex <= 0");
            throw new NumberFormatException();
        }
        int slashIndex = domainIndex + 4;
        for (int i = slashIndex; i < quizletLink.length(); i++) {
            charCounter += 1;
            if (quizletLink.charAt(i) == '/') {
                if (charCounter == 1) {
                    substringStart = i + 1;
                } else {
                    substringEnd = i;
                    break;
                }
            }
        }
        String apiID = quizletLink.substring(substringStart, substringEnd);
        Log.i(S, "apiID = " + apiID);
        apiLink = "https://api.quizlet.com/2.0/sets/"
                + apiID
                + "?client_id=brgUUPyxDF&whitespace=1";
        Log.i(S, "Set quizletLink = " + quizletLink);
        Log.i(S, "Set apiLink = " + apiLink);
        try {
            new quizletSetOperations().execute(apiLink);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Override
    // public void onBackPressed()
    // {
    // Intent start = new Intent(this, StartActivity.class);
    // start.addCategory(Intent.CATEGORY_HOME);
    // startActivity(start);
    // overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
    // }

    public String reformatShortURL(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            Log.i(S, "Quizlet Link is being reformatted!");
            Log.i(S, "New Quizlet Link = " + "https://quizlet.com" + redirectUrl);
            return "https://quizlet.com" + redirectUrl;
        }
        return url;
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
        if (id == R.id.dev_sign_out) {
            Log.i(S, "Developer Mode: Simulating sign out");
            isSignedIn = false;
            setContentView(R.layout.before_auth);
            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
            Button nothanks = (Button) this.findViewById(R.id.nosignin);
            nothanks.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setContentView(R.layout.activity_main);
                    overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                }
            });
            Button signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
            signInIntro.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Uri uri = Uri.parse(authLink);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(webIntent);
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getSet(String APILink) throws Exception {
        URL url = new URL(APILink);
        URLConnection connection;
        connection = url.openConnection();

        HttpURLConnection httpConnection = (HttpURLConnection) connection;

        int responseCode = httpConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream in = httpConnection.getInputStream();
            return getStringFromInputStream(in);
        } else {
            return null;
        }
    }

    public static void setACCESSTOKEN(AccessToken accesstoken) throws Exception {
        Log.d(S, "Setting Access Token");
        ACCESS_TOKEN = accesstoken.getAccessToken();
        isSignedIn = true;
        username = accesstoken.getUsername();
        saveFile(MainActivity.me);
    }

    public String getUser(String username) throws Exception {
        String apiLink = "https://api.quizlet.com/2.0/users/" + username + "?access_token=" + ACCESS_TOKEN + "&whitespace=1";
        Log.i(S, "User apiLink = " + apiLink);
        URL url = new URL(apiLink);
        URLConnection connection;
        connection = url.openConnection();

        HttpURLConnection httpConnection = (HttpURLConnection) connection;

        int responseCode = httpConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream in = httpConnection.getInputStream();
            return getStringFromInputStream(in);
        } else {
            return null;
        }
    }

    public static String getUser(String username, String accessToken) throws Exception {
        String apiLink = "https://api.quizlet.com/2.0/users/" + username + "?access_token=" + accessToken + "&whitespace=1";
        Log.i(S, "User apiLink = " + apiLink);
        URL url = new URL(apiLink);
        URLConnection connection;
        connection = url.openConnection();

        HttpURLConnection httpConnection = (HttpURLConnection) connection;

        int responseCode = httpConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream in = httpConnection.getInputStream();
            return getStringFromInputStream(in);
        } else {
            return null;
        }
    }

    public void updateSet(QuizletSet quizletSet) {
        new updateSetOperations().execute(quizletSet.getAPILink());
    }

    public Boolean checkIfSignedIn() {
        if (isSignedIn) {
            setContentView(R.layout.before_auth_required);
            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
            return false;
        } else {
            return true;
        }
    }

    public String getUser(QuizletSet quizletSet) throws Exception {
        String apiLink = "https://api.quizlet.com/2.0/users/" + quizletSet.getCreatorName();
        Log.i(S, "Requesting user " + quizletSet.getCreatorName() + "...");
        Log.i(S, "User apiLink = " + apiLink);
        URL url = new URL(apiLink);
        URLConnection connection;
        connection = url.openConnection();

        HttpURLConnection httpConnection = (HttpURLConnection) connection;

        int responseCode = httpConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream in = httpConnection.getInputStream();
            return getStringFromInputStream(in);
        } else {
            return null;
        }
    }

    public static void saveFile(Context context) {
        Log.d(S, "SAVING FILE!");
        try {
            JSONObject userJSON = toJSON();
            Log.d(S, "toJSON() completed");
            String userString = userJSON.toString();
            File file = new File(context.getFilesDir() + "/Studie.txt");
            Log.d(S, "file = " + file);
            FileOutputStream FOS = new FileOutputStream(file);
            Log.d(S, "FileOutputStream created");
            OutputStreamWriter OSW = new OutputStreamWriter(FOS);
            Log.d(S, "OutputStreamWriter created");
            OSW.write(userString);
            OSW.flush();
            OSW.close();
        } catch (JSONException je) {
            Log.e(S, "JSONException!", je);
        } catch (IOException ioe) {
            Log.e(S, "IOException!", ioe);
        } catch (RuntimeException rte) {
            Log.e(S, "RuntimeException!", rte);
        }
        Log.d(S, "OSW closed, file exists = " + (new File("Studie.txt").exists()));
    }

    public static JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("isSignedIn", isSignedIn);
        Log.d(S, "Put isSignedIn to JSON as " + isSignedIn);
        result.put("ACCESS_TOKEN", ACCESS_TOKEN);
        Log.d(S, "Put ACCESS_TOKEN to JSON as " + ACCESS_TOKEN);
        result.put("username", username);
        Log.d(S, "Put username to JSON as " + username);
        return result;
    }

    public static QuizletSet getCurrentQuizletSet() {
        return currentQuizletSet;
    }

    public static void setCurrentQuizletSet(QuizletSet quizletSet) {
        currentQuizletSet = quizletSet;
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

    private class quizletSetOperations extends
            AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Log.i(S, "Made it to onPreExecute()");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(S, "Made it to doInBackground()");
            try {
                Log.i(S, "Requesting set...");
                setJSON = getSet(params[0]);
                final QuizletSet quizletSet = new QuizletSet(setJSON);
                quizletSet.setAPILink(params[0]);
//                QuizletUser quizletUser = new QuizletUser(getUser(quizletSet.getCreatorName()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itWorkedDialog.setMessage(quizletSet.getDebugSummary());
//                        itWorkedDialog.show();
                        setCurrentQuizletSet(quizletSet);
                        Intent intent = new Intent(MainActivity.this, SetActivity.class);
                        startActivity(intent);
                        Log.i(S, "Started SetActivity");
                        overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                    }
                });

            } catch (Exception e) {
                Log.e(S, "Error After Program Start: " + e);
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(S, "Reached onPostExecute, Result = " + setJSON);
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    private class updateSetOperations extends
            AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Log.i(S, "Made it to onPreExecute()");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(S, "Made it to doInBackground()");
            try {
                Log.i(S, "Requesting set...");
                setJSON = getSet(params[0]);
                QuizletSet quizletSet = new QuizletSet(setJSON);
                QuizletUser quizletUser = new QuizletUser(getUser(quizletSet.getCreatorName()));
                // TODO: Update the current set
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });

            } catch (Exception e) {
                Log.e(S, "Error After Program Start: " + e);
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(S, "Reached onPostExecute, Result = " + setJSON);
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    private class checkLink extends
            AsyncTask<String, String, String> {
        String link;

        @Override
        protected void onPreExecute() {
            Log.i(S, "Made it to onPreExecute()");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(S, "Made it to doInBackground()");
            try {
                Log.i(S, "Reformatting Link");
                link = params[0];
                link = reformatShortURL(link);
            } catch (Exception e) {
                Log.e(S, "Error After Program Start: " + e);
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            quizletLink = link;
            checkDomain();
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }
}
