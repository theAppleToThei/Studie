package com.techtalk4geeks.studie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends Activity {

    public static final String S = "Studie";

    Button linkQuizletAccount;
    Button findSetButton;
    Button signInButton;
    Button recentSet1;
    Button recentSet2;
    EditText quizletLinkEditText;
    AlertDialog invalidURLDialog;
    AlertDialog noURLDialog;
    AlertDialog itWorkedDialog;

    ArrayList<String> feedSetNames;
    String feedJSON;

    Animation animationFadeIn;

    ProgressDialog progress;

    Boolean inCard = false;
    int cardNumber;
    final int FIND_SET = 0;
    final int QUIZLET_FEED = 1;
    final int SAVED = 2;
    final int SHARE = 3;
    final int SETTINGS = 4;

    public static Boolean isSignedIn = false;
    public static String ACCESS_TOKEN;
    public static String username;
    public static final String CLIENT_ID = "brgUUPyxDF";
    public static final String state = "PK_STUDIE";

    @SuppressLint("StaticFieldLeak")
    private static MainActivity me;
    public static boolean isDemo = false;

    int WIDTH;
    int HEIGHT;

    String quizletLink;
    String apiLink;
    String authLink = "https://quizlet.com/authorize?response_type=code&client_id=" + CLIENT_ID + "&scope=read&state=" + state;
    int substringStart;
    int substringEnd;

    public static QuizletSet currentQuizletSet;

    String setJSON;

    ConnectivityManager cm;
    NetworkInfo activeNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = this;
        setContentView(R.layout.activity_main);
        File file = new File(getFilesDir(), "Studie.txt");

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        HEIGHT = displaymetrics.heightPixels;
        WIDTH = displaymetrics.widthPixels;

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.studieactionbar);

        progress = new ProgressDialog(MainActivity.this);

        signInButton = (Button) findViewById(R.id.signInButton);
        recentSet1 = (Button) findViewById(R.id.recentView1);
        recentSet2 = (Button) findViewById(R.id.recentView2);

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);

        displayDevelopmentToast("This is a development build.");

        if (!isConnected()) {
            displayToast("No Network Connection.");
        }

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
        noURLDialog.setMessage("Please paste a Quizlet link in the field or share a set from the Quizlet app to find a set.");
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
                return (actionId == EditorInfo.IME_ACTION_SEARCH)
                        || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        && (event.getAction() == KeyEvent.ACTION_DOWN));
            }
        });

        findSetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(S, "onClick received for findSetButton");
                quizletLink = String.valueOf(quizletLinkEditText.getText().toString());
                if (quizletLink.isEmpty()) {
                    displayToast("No URL Provided.");
                } else if (!isConnected()) {
                    displayToast("No Network Connection.");
                } else {
                    showProgressDialog();
                    new checkLink().execute(quizletLink);
                }
            }
        });

        if (file.exists()) {
            FileInputStream FIN;
            displayDevelopmentToast("File exists");
            try {
                FIN = openFileInput("Studie.txt");
                InputStreamReader ISR = new InputStreamReader(FIN);
                BufferedReader br = new BufferedReader(ISR);
                String jsonString = br.readLine();
                JSONObject JSONObject = new JSONObject(jsonString);
                isSignedIn = JSONObject.getBoolean("isSignedIn");
                Log.i(S, "isSignedIn = " + isSignedIn);
                if (isSignedIn) {
                    displayDevelopmentToast("Signed in.");
                    ACCESS_TOKEN = JSONObject.getString("ACCESS_TOKEN");
                    Log.i(S, "ACCESS_TOKEN = " + ACCESS_TOKEN);
                    username = JSONObject.getString("username");
                    Log.i(S, "username = " + username);
                    TextView userView = (TextView) findViewById(R.id.quizletUser);
                    userView.setText(String.format("%s%s", getString(R.string.signed_in_as), username));
                    signInButton.setText("View Feed");
                } else {
                    Log.i(S, "isSignedIn = " + isSignedIn);
                    setContentView(R.layout.before_auth);
                    overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
//                    Button nothanks = (Button) this.findViewById(R.id.nosignin);
                    TextView disclaimer = (TextView) this.findViewById(R.id.signindescription);
                    disclaimer.startAnimation(animationFadeIn);
//                    nothanks.setOnClickListener(new View.OnClickListener() {
//                        public void onClick(View v) {
//                            setContentView(R.layout.activity_main);
//                            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
//                        }
//                    });
                    Button signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
                    signInIntro.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Uri uri = Uri.parse(authLink);
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(webIntent);
                        }
                    });
                    signInButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            signedOutPromptSetup();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            displayDevelopmentToast("File Doesn't Exist");
            try {
                boolean newFile = file.createNewFile();
                assert newFile;
                saveFile(MainActivity.this);
            } catch (Exception e) {
                Log.e(S, "Exception while creating/saving file.");
                e.printStackTrace();
            }
            if (!isSignedIn) {
                setContentView(R.layout.before_auth);
                overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                TextView disclaimer = (TextView) this.findViewById(R.id.signindescription);
                disclaimer.startAnimation(animationFadeIn);
//                Button nothanks = (Button) this.findViewById(R.id.nosignin);
//                nothanks.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        setContentView(R.layout.activity_main);
//                        overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
//                    }
//                });
                Button signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
                signInIntro.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Uri uri = Uri.parse(authLink);
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(webIntent);
                    }
                });

            } else {
                Log.e(S, "Signed In But File Doesn't Exist!!!");
                try {
                    getActionBar().setDisplayUseLogoEnabled(false);
                } catch (NullPointerException np) {
                    Log.e(S, "Method setDisplayUseLogoEnabled produced NullPointerException!");
                }

            }
        }
        if (isDemo) {
            Log.i(S, "THIS IS A DEMO BUILD OF " + S + "!");
            setContentView(R.layout.demo);
            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
            Button startDemo = (Button) findViewById(R.id.startDemo);
            startDemo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setContentView(R.layout.demo_sets);
                    overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                    TableLayout setsTable = (TableLayout) findViewById(R.id.demosSetTable);
                    ArrayList<String> demoSetNames = new ArrayList<>();
                    demoSetNames.add(0, "Medical Terms");
                    demoSetNames.add(1, "Spanish Terms");
                    for (int i = 0; i < demoSetNames.size(); i++) {
                        TableRow name = new TableRow(MainActivity.this);
                        TextView setName = new TextView(MainActivity.this);
                        name.setPadding(0, 10, 0, 0);
                        name.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.MATCH_PARENT, 1.0f));
                        setName.setTextSize(20);
                        setName.setTypeface(Typeface.DEFAULT_BOLD);
                        setName.setPadding(10, 0, 0, 0);
                        setName.setTextColor(Color.BLACK);
                        setName.setText(demoSetNames.get(i));
                        setName.setMinWidth(400);
                        Button go = new Button(MainActivity.this);
//                        go.setBackgroundResource(R.drawable.go);
                        go.setText("Select Set");
                        go.setBackgroundColor(getResources().getColor(R.color.blue));
                        Space space = new Space(MainActivity.this);
                        space.setMinimumWidth(50);
                        setsTable.addView(name);
                        name.addView(setName);
                        name.addView(space);
                        name.addView(go);
                        final int num = i;
                        go.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                ArrayList<String> demoSetURLs = new ArrayList<>();
                                demoSetURLs.add(0, "https://quizlet.com/139054147/medical-terms-flash-cards/");
                                demoSetURLs.add(1, "https://quizlet.com/139055710/spanish-terms-flash-cards/");
                                showProgressDialog();
                                new checkLink().execute(demoSetURLs.get(num));
                            }
                        });
                    }
                }
            });
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(S, "onClick received for signInButton");
                if (isSignedIn) {
                    setContentView(R.layout.feed_layout);
                    overridePendingTransition(R.anim.anim_right_to_left, R.anim.anim_left_to_right);
                    setTitle(username);
                    feedSetNames = new ArrayList<>();
                    showProgressDialog("Retrieving Sets");
                    new getFeed().execute();

                } else {
                    displayDevelopmentToast("This button is currently not functional.");
                }
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Log.i(S, "Recieved ACTION_SEND");
            if ("text/plain".equals(type)) {
                try {
                    if (isSignedIn) {
                        handleSendText(intent);
                    } else {
                        signedOutPromptSetup();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showProgressDialog() {
        progress.setTitle("Loading");
        progress.setMessage("Retrieving Set");
        progress.show();
    }

    public void showProgressDialog(String message) {
        progress.setTitle("Loading");
        progress.setMessage(message);
        progress.show();
    }

    public Boolean isConnected() {
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void displayToast(String text) {
        Toast toast = Toast.makeText(MainActivity.this, text,
                Toast.LENGTH_SHORT);
        toast.show();
        Log.i(S, "Toast: " + text);
    }

    public void displayDevelopmentToast(String text) {
        if (BuildConfig.DEBUG) {
            Toast toast = Toast.makeText(MainActivity.this, "DEV: " + text,
                    Toast.LENGTH_SHORT);
            toast.show();
            Log.i(S, "Dev Toast: " + text);
        }
    }

    public void signedOutPromptSetup() {
        Button signInIntro;
        setContentView(R.layout.before_auth);
        overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
        signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
        signInIntro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse(authLink);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(webIntent);
            }
        });
    }

    void handleSendText(Intent intent) throws MalformedURLException {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        int domainIndex = sharedText.indexOf("https://quizlet.com/");
        if (domainIndex < 0) {
            Log.wtf(S, "domainIndex < 0");
            displayDevelopmentToast("Quizlet URL Not Found in intent");
            throw new MalformedURLException("Quizlet URL Not Found in intent");
        }
        String link = sharedText.substring(domainIndex);
        showProgressDialog();
        new checkLink().execute(link);
    }

    public void checkDomain() {
        int charCounter = 0;
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

    @Override
    public void onBackPressed() {
//     Intent start = new Intent(this, StartActivity.class);
//     start.addCategory(Intent.CATEGORY_HOME);
//     startActivity(start);
//     overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
        if (inCard) {
            setContentView(R.layout.new_ui);
            inCard = false;
        }
    }

    public String reformatShortURL(String url) throws Exception {
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
        } catch (MalformedURLException m) {
            Log.i(S, "Link is not a valid URL.");
            int domainIndex = url.indexOf("quizlet.com/");
            url = url.substring(domainIndex);
            Log.i(S, "URL = " + url);
            con = (HttpURLConnection) new URL(url).openConnection();
        }
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
//            Button nothanks = (Button) this.findViewById(R.id.nosignin);
//            nothanks.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    setContentView(R.layout.activity_main);
//                    overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
//                }
//            });
            Button signInIntro = (Button) this.findViewById(R.id.signInButtonUI);
            signInIntro.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Uri uri = Uri.parse(authLink);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(webIntent);
                }
            });
            signInButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setContentView(R.layout.before_auth);
                    overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                }
            });
            return true;
        }
        if (id == R.id.dev_audio_test) {
            Log.i(S, "Developer Mode: Audio Test");
            Intent intent = new Intent(this, AudioActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
        }
        if (id == R.id.dev_quizlet_set) {
            Intent intent = new Intent(this, NewerSetActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
        }
        if (id == R.id.dev_test_ui) {
            Log.i(S, "Developer Mode: New UI");
            setContentView(R.layout.new_ui);
            inCard = false;
            final LinearLayout findBar = (LinearLayout) findViewById(R.id.barBar1);
            final LinearLayout quizletBar = (LinearLayout) findViewById(R.id.barBar2);
            final LinearLayout savedBar = (LinearLayout) findViewById(R.id.barBar3);
            final LinearLayout shareBar = (LinearLayout) findViewById(R.id.barBar4);
            final LinearLayout settingsBar = (LinearLayout) findViewById(R.id.barBar5);
            findBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(S, "findBar onClick triggered\ninCard is " + inCard);
                    if (!inCard) {
                        LinearLayout parent = (LinearLayout) findBar.getParent();
                        quizletBar.animate().translationY(750);
                        savedBar.animate().translationY(550);
                        shareBar.animate().translationY(350);
                        settingsBar.animate().translationY(150);
                        int x = (WIDTH / 2) - (findBar.getChildAt(0).getWidth() + (findBar.getPaddingLeft() / 2));
                        findBar.getChildAt(0).animate().scaleY(1.0f / 3).translationY(75).translationX(-x);
                        findBar.getChildAt(1).animate().scaleY(2.0f / 3).scaleX(2).translationX(-1 * ((WIDTH / 2) - (findBar.getChildAt(1).getWidth() - (findBar.getPaddingRight() / 2))));
//                        findBar.getChildAt(1).setVisibility(View.INVISIBLE);
                        inCard = true;
                        cardNumber = FIND_SET;
                        findBar.animate().scaleY(3).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
//                                Space space = new Space(MainActivity.this);
//                                space.setBottom(200);
//                                findBar.addView(space);

                            }
                        });
//                        ImageView search = new ImageView(MainActivity.this);
                        ImageView searchVector = (ImageView) findViewById(R.id.baricon1);
                        searchVector.setMinimumHeight(144);
                        searchVector.setMinimumWidth(144);
                        searchVector.setAdjustViewBounds(true);
                        searchVector.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        searchVector.setImageResource(R.drawable.search_vector);
//                        findBar.addView(search);
                        parent.getTop();
                        parent.getBottom();
                    }
                }
            });
            quizletBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(S, "quizletBar onClick triggered");
                    if (!inCard) {
                        inCard = true;
                        cardNumber = QUIZLET_FEED;
                        findBar.animate().translationY(950);
                        savedBar.animate().translationY(550);
                        shareBar.animate().translationY(350);
                        settingsBar.animate().translationY(150);
                    } else {
                        switch (cardNumber) {
                            case FIND_SET:
                                quizletBar.animate().translationY(-350);
                                savedBar.animate().translationY(-550);
                                shareBar.animate().translationY(-350);
                                settingsBar.animate().translationY(-150);
                                findBar.getChildAt(0).setVisibility(View.VISIBLE);
                                findBar.getChildAt(1).setVisibility(View.VISIBLE);
                                inCard = false;
                                findBar.animate().scaleY(1 / 3).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                    }
                                });
                        }
                    }
                }
            });
            savedBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(S, "savedBar onClick triggered");
                    if (!inCard) {
                        inCard = true;
                        cardNumber = SAVED;
                        quizletBar.animate().translationY(750);
                        findBar.animate().translationY(950);
                        shareBar.animate().translationY(350);
                        settingsBar.animate().translationY(150);
                    }
                }
            });
            shareBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(S, "shareBar onClick triggered");
                    if (!inCard) {
                        inCard = true;
                        cardNumber = SHARE;
                    }
                }
            });
            settingsBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(S, "settingsBar onClick triggered");
                    if (!inCard) {
                        inCard = true;
                        cardNumber = SETTINGS;
                    }
                }
            });
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void updateSet(QuizletSet quizletSet) {
        new updateSetOperations().execute(quizletSet.getAPILink());
    }

    @SuppressWarnings("unused")
    public Boolean checkIfSignedIn() {
        if (!isSignedIn) {
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
            progress.dismiss();
            Log.d(S, "Reached onPostExecute, Result = " + setJSON);
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    public void showErrorDialog() {
        progress.dismiss();
        Log.e("Studie", "Showing Error Dialog!");
        AlertDialog onErrorDialog = new AlertDialog.Builder(MainActivity.this).create();
        onErrorDialog.setTitle("Error");
        onErrorDialog.setMessage("An unexpected error occured.");
        onErrorDialog.setIcon(R.drawable.error);
        onErrorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
        onErrorDialog.show();
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
                e.printStackTrace();
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

    private class getFeed extends
            AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            Log.i(S, "Made it to onPreExecute()");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i(S, "Made it to doInBackground()");
            ArrayList<Integer> feedIDs = new ArrayList<>();

            try {
                URL url = new URL("https://api.quizlet.com/2.0/feed/home");
                URLConnection connection;
                connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                int responseCode = httpConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = httpConnection.getInputStream();
                    feedJSON = getStringFromInputStream(in);
                    Log.i(S, "response = " + feedJSON);
                } else {
                    Log.e(S, "Connection was not ok");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorDialog();
                        }
                    });
                    return null;
                }
                try {
                    JSONObject jsonOb = new JSONObject(feedJSON);
                    JSONArray items = jsonOb.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        JSONObject itemData = item.getJSONObject("item_data");
                        int id = itemData.getInt("id");
                        String title = itemData.getString("title");
                        Log.i(S, "Found Set: " + title);
                        feedIDs.add(id);
                        feedSetNames.add(title);
                    }
//                    for (int i = 0; i < feedIDs.size(); i++) {
//                        String JSON = getSet("https://api.quizlet.com/2.0/sets/" + feedIDs.get(i) + "?client_id=brgUUPyxDF&whitespace=1");
//                        QuizletSet quizletSet = new QuizletSet(JSON);
//                        feedSets.add(quizletSet);
//                        Log.i(S, "Added " + quizletSet.getTitle());
//                    }
                } catch (Exception e) {
                    Log.wtf(S, "Something happened while retrieving a feed.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(S, "Error After Program Start: " + e);
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String result) {
            TableLayout feedSetTable = (TableLayout) findViewById(R.id.setsTable);
            for (int i = 0; i < feedSetNames.size(); i++) {
                TableRow row = new TableRow(MainActivity.this);
                TextView name = new TextView(MainActivity.this);
                name.setText(feedSetNames.get(i));
                name.setTextSize(16);
                Space space = new Space(MainActivity.this);
                space.setMinimumWidth(50);
                feedSetTable.addView(row);
                row.addView(name);
                row.addView(space);
            }
            progress.hide();
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }
}
