package com.techtalk4geeks.studie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    public static final String S = "Studie";

    Button linkQuizletAccount;
    Button findSetButton;
    EditText quizletLinkEditText;
    AlertDialog invalidURLDialog;
    AlertDialog noURLDialog;
    AlertDialog itWorkedDialog;
    String quizletTitle = "null";

    String quizletLink;
    String apiLink;
    int substringStart;
    int substringEnd;
    int charCounter = 0;

    String setJSON;

    public static final String ACCESS_TOKEN = "2rcp25WPAuaR4KxU9kEbhzbrPkw5N46nwSc4tgPA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            getActionBar().setDisplayUseLogoEnabled(true);
        } catch (NullPointerException np) {
            Log.e(S, "Method setDisplayUseLogoEnabled produced NullPointerException!");
        }
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.drawable.studieactionbar);

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
                    //TODO: Start Quizlet Set intent
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
                if (quizletLink.contains("quizlet.com/")) {
                    Log.d(S, "quizletLink contains required domain");
                    for (int i = 10; i < quizletLink.length(); i++) {
                        if (quizletLink.charAt(i) == '/') {
                            charCounter += 1;
                            if (charCounter == 1) {
                                substringStart = i + 1;
                            } else if (charCounter == 2) {
                                substringEnd = i;
                                break;
                            }
                        }
                    }
                    apiLink = "https://api.quizlet.com/2.0/sets/"
                            + quizletLink.substring(substringStart, substringEnd)
                            + "?client_id=brgUUPyxDF&whitespace=1";
                    Log.i(S, "Set quizletLink = " + quizletLink);
                    Log.i(S, "Set apiLink = " + apiLink);
                    try {
                        new quizletSetOperations().execute(apiLink);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(S, "Invalid URL");
                    invalidURLDialog.show();
                }
            }
        });
    }

//    @Override
    // public void onBackPressed()
    // {
    // Intent start = new Intent(this, StartActivity.class);
    // start.addCategory(Intent.CATEGORY_HOME);
    // startActivity(start);
    // overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
    // }

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

    public void updateSet(QuizletSet quizletSet) {
        new updateSetOperations().execute(quizletSet.getAPILink());
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
                QuizletUser quizletUser = new QuizletUser(getUser(quizletSet.getCreatorName()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itWorkedDialog.setMessage(quizletSet.getDebugSummary());
                        itWorkedDialog.show();
//                        setContentView(R.layout.set_layout);
//                        overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
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
}
