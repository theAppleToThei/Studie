package com.techtalk4geeks.studie;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.VolumeProvider;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class AudioActivity extends Activity {

    Button store, play;
    EditText input;
    String speakTextTxt;
    TextToSpeech mTts;
    Bundle bundle;
    File file;
    String tempDestFile;
    Voice voice;

    public static final String S = "Studie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_audio);
        bundle = savedInstanceState;
        file = new File(getFilesDir(), "Studie_Audio.txt");

        store = (Button) findViewById(R.id.button1);
        play = (Button) findViewById(R.id.button2);
        input = (EditText) findViewById(R.id.editText1);

        mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Log.i(S, "onInit() called");
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

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(S, "Play Button Clicked");
                String toSpeak = "Hello world";
                Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
                        Log.i(S, "onStart()");
                    }

                    @Override
                    public void onDone(String s) {
                        Log.i(S, "onDone()");
                        mTts.shutdown();
                    }

                    @Override
                    public void onError(String s) {
                        Log.i(S, "onError()");
                    }
                });
                mTts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "");
//                new MySpeech(toSpeak);
            }
        });

        store.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                speakTextTxt = "Hello world";
                HashMap<String, String> myHashRender = new HashMap<String, String>();
                myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, speakTextTxt);

                String exStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                Log.d("MainActivity", "exStoragePath : " + exStoragePath);
                File appTmpPath = new File(exStoragePath + "/sounds/");
                boolean isDirectoryCreated = appTmpPath.mkdirs();
                Log.d("MainActivity", "directory " + appTmpPath + " is created : " + isDirectoryCreated);
                String tempFilename = "tmpaudio.wav";
                tempDestFile = appTmpPath.getAbsolutePath() + File.separator + tempFilename;
                Log.d("MainActivity", "tempDestFile : " + tempDestFile);
                new MySpeech(speakTextTxt);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio, menu);
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

    public void onPause() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onPause();
    }

    public void displayDevelopmentToast(String text) {
        Toast toast = Toast.makeText(AudioActivity.this, "DEV: " + text,
                Toast.LENGTH_SHORT);
        toast.show();
        Log.i(S, "Dev Toast: " + text);
    }

    class MySpeech implements TextToSpeech.OnInitListener {

        String tts;

        public MySpeech(String tts) {
            this.tts = tts;
//            mTts = new TextToSpeech(AudioActivity.this, this);
        }

        @Override
        public void onInit(int status) {
            Log.v("log", "initi");
            int i = mTts.synthesizeToFile(speakTextTxt, bundle, file, "PK Studie");
            if (i == TextToSpeech.SUCCESS) {

                Toast toast = Toast.makeText(AudioActivity.this, "Saved " + i,
                        Toast.LENGTH_SHORT);
                toast.show();
                Log.d(S, "speakTextTxt = " + speakTextTxt);
                displayDevelopmentToast("speakTextTxt = " + speakTextTxt);
//                mTts.setVoice(voice);
//                mTts.setLanguage(Locale.US);
//                mTts.setSpeechRate(1);
//                mTts.setAudioAttributes(new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                        .build());
                String utteranceId = this.hashCode() + "";
                mTts.speak(speakTextTxt, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            }
            System.out.println("Result : " + i);
        }
    }
}
