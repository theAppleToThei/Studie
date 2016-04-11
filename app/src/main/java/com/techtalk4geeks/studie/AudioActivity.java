package com.techtalk4geeks.studie;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

public class AudioActivity extends Activity {

    Button store, play;
    EditText input;
    String speakTextTxt;
    TextToSpeech mTts;
    Bundle bundle;
    File file;
    String tempDestFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        bundle = savedInstanceState;
        file = new File(getFilesDir(), "Studie_Audio.txt");

        store = (Button) findViewById(R.id.button1);
        play = (Button) findViewById(R.id.button2);
        input = (EditText) findViewById(R.id.editText1);
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

    class MySpeech implements TextToSpeech.OnInitListener {

        String tts;

        public MySpeech(String tts) {
            this.tts = tts;
            mTts = new TextToSpeech(AudioActivity.this, this);
        }

        @Override
        public void onInit(int status) {
            Log.v("log", "initi");
            int i = mTts.synthesizeToFile(speakTextTxt, bundle, file, "PK Studie");
            if (i == TextToSpeech.SUCCESS) {

                Toast toast = Toast.makeText(AudioActivity.this, "Saved " + i,
                        Toast.LENGTH_SHORT);
                toast.show();
            }
            System.out.println("Result : " + i);
        }
    }
}
