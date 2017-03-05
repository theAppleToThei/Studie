package com.example.audioengine;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    class AudioCoordinator {

        TextToSpeech mTts;
        Bundle bundle;
        File file;
        String tempDestFile;
        Voice voice;
        AudioManager audioManager;

        QuizletSet testSet;
        String testSetJSON;

        String toSpeak = "Hello World";

        public AudioCoordinator(Bundle savedInstanceState) {
            bundle = savedInstanceState;
            file = new File(getFilesDir(), "Studie_Audio.txt");

            mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        mTts.setLanguage(Locale.US);
                        voice = mTts.getDefaultVoice();
                        mTts.setVoice(voice);
                        mTts.setSpeechRate(1);
                        mTts.setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build());
                    }
                }
            });

            audioTestButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    log("audioTestButton triggered");

                    Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                    mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Log.i("audioEngine", "onStart()");
                        }

                        @Override
                        public void onDone(String s) {
                            Log.i("audioEngine", "onDone()");
                            mTts.shutdown();
                        }

                        @Override
                        public void onError(String s) {
                            Log.i("audioEngine", "onError()");
                        }
                    });
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(audioManager.STREAM_MUSIC));
                    Bundle extras = new Bundle();
                    extras.putSerializable("params", params);
                    mTts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, extras, "");
                    audioEngine = new AudioCoordinator(bundle); // Rebounds ttsEngine
                }
            });

            nextTermButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (toSpeak.equalsIgnoreCase("Hello World")) {
                        toSpeak = "World Hello";
                    } else {
                        toSpeak = "Hello World";
                    }
                }
            });

            quizletTestButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    log("quizletTestButton triggered");

                    if (isConnected()) {
                        new getTestSet().execute();


                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("No Connection")
                                .setMessage("There is no internet connection.")
                                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            });
        }

        public void parse(QuizletSet quizletSet) {

        }

        public String getTestSet() throws Exception {
            URL url = new URL("https://api.quizlet.com/2.0/sets/174880891?client_id=brgUUPyxDF&whitespace=1");

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;

            int responseCode = httpConnection.getResponseCode();

            Log.i("audioEngine", "Expecting: " + HttpURLConnection.HTTP_OK);
            Log.i("audioEngine", "Actual: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpConnection.getInputStream();

                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();

                String line;
                try {
                    br = new BufferedReader(new InputStreamReader(in));
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
            } else {
                return null;
            }
        }

        private class getTestSet extends
                AsyncTask<String, String, String> {

            ProgressDialog progress = new ProgressDialog(MainActivity.this);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("audioEngine", "onPreExecute reached");
                progress.setTitle("Loading");
                progress.setMessage("Retrieving Test Set");
                progress.show();
            }

            @Override
            protected String doInBackground(String... params) {
                Log.d("audioEngine", "doInBackground reached");
                try {
                    testSet = new QuizletSet(getTestSet());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("audioEngine", "onPostExecute reached");

                progress.hide();

                for (String term : testSet.fullList) {
                    String toSpeak = term;
                    mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Log.i("audioEngine", "onStart()");
                        }

                        @Override
                        public void onDone(String s) {
                            Log.i("audioEngine", "onDone()");
                            mTts.shutdown();
                        }

                        @Override
                        public void onError(String s) {
                            Log.i("audioEngine", "onError()");
                        }
                    });
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(audioManager.STREAM_MUSIC));
                    Bundle extras = new Bundle();
                    extras.putSerializable("params", params);
                    mTts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, extras, "");
                    audioEngine = new AudioCoordinator(bundle); // Rebounds ttsEngine
                }
            }

            @Override
            protected void onProgressUpdate(String... values) {
            }
        }
    }

    Button audioTestButton;
    Button nextTermButton;
    Button quizletTestButton;
    AudioCoordinator audioEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioTestButton = (Button) findViewById(R.id.audioTestButton);
        nextTermButton = (Button) findViewById(R.id.nextTerm);
        quizletTestButton = (Button) findViewById(R.id.quizletTestButton);
        audioEngine = new AudioCoordinator(savedInstanceState);

        if (isConnected()) {
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            Log.w("audioEngine", "Android device is not connected to the internet.");
        }
    }

    public Boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    void log(String message) {
        Log.d("Audio Engine", message);
    }
}