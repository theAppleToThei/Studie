package com.example.audioengine;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    class AudioCoordinator {

        TextToSpeech mTts;
        Bundle bundle;
        File file;
        String tempDestFile;
        Voice voice;
        AudioManager audioManager;

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
        }

        public void parse(QuizletSet quizletSet) {

        }
    }

    Button audioTestButton;
    AudioCoordinator audioEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioTestButton = (Button) findViewById(R.id.audioTestButton);
        audioEngine = new AudioCoordinator(savedInstanceState);

        audioTestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                log("audioTestButton triggered");
            }
        });
    }

    void log(String message) {
        Log.d("Audio Engine", message);
    }
}