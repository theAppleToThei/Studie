package com.techtalk4geeks.studie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by alex on 2/6/16.
 */
public class SetActivity extends Activity {

    QuizletSet quizletSet;
    TableLayout termsView;
    Button playButton;
    TextToSpeech mTts = null;
    ProgressDialog progress;
    AlertDialog onDoneDialog;
    private static SetActivity parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        quizletSet = MainActivity.getCurrentQuizletSet();
//        playButton.setEnabled(true);
        if (MainActivity.isDemo) {
            onDoneDialog = new AlertDialog.Builder(SetActivity.this).create();
            onDoneDialog.setTitle("Demo Over");
            onDoneDialog.setMessage("Thank you so much for demoing Studie. I hope you've enjoyed what I've created in the past few months and there is much more to come.\n\nThanks again!\nAlex Baratti");
            onDoneDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "RESTART DEMO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(SetActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
        }
        setTitle(quizletSet.getTitle());
        playButton = (Button) findViewById(R.id.playSet);
        playButton.setBackgroundColor(getResources().getColor(R.color.green));
        TextView title = (TextView) findViewById(R.id.quizletTitleText);
        TextView creator = (TextView) findViewById(R.id.quizletUserText);
        TextView termCount = (TextView) findViewById(R.id.quizletTermCount);
        title.setText(quizletSet.getTitle());
        creator.setText(quizletSet.getCreatorName());
        termCount.setText(String.valueOf(quizletSet.getTermCount()) + " Terms");
        termsView = (TableLayout) findViewById(R.id.termsTable);
        for (int i = 0; i < quizletSet.getTermCount(); i++) {
            TableRow row = new TableRow(this);
            TextView term = new TextView(this);
            term.setText(quizletSet.getTerms().get(i).toString());
//            term.setGravity(3);
            TextView definition = new TextView(this);
            definition.setText(quizletSet.getDefinitions().get(i).toString());
//            definition.setGravity(5);
            Space space = new Space(SetActivity.this);
            space.setMinimumWidth(50);
            termsView.addView(row);
            row.addView(term);
            row.addView(space);
            row.addView(definition);
        }
        creator.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.user_layout);
                overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
                TextView username = (TextView) findViewById(R.id.userTitleText);
                TextView setsCreated = (TextView) findViewById(R.id.setsText);
                username.setText(quizletSet.getCreatorName());
                setsCreated.setText(String.valueOf(quizletSet.getCreator().getTotalTermsEntered()) + " Terms Entered");
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("Studie", "Play Button was clicked.");
                playButton.setEnabled(false);
                playButton.setFocusable(false);
                playButton.setBackgroundColor(getResources().getColor(R.color.grey));
                playSet();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.play) {
            setContentView(R.layout.play_layout);
            overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
            playSetup();
            return true;
        }
        if (id == R.id.viewinbrowser) {
            Uri uri = Uri.parse(quizletSet.getURL());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(webIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(this, MainActivity.class);
        main.addCategory(Intent.CATEGORY_HOME);
        startActivity(main);
        overridePendingTransition(R.anim.anim_in_up, R.anim.anim_out_down);
    }

    public void playSetup() {
        Button playButton = (Button) findViewById(R.id.play);
        final StringBuffer speakQuizlet = new StringBuffer();
        Spinner voices = (Spinner) findViewById(R.id.voice_spinner);
        final SeekBar speechRate = (SeekBar) findViewById(R.id.rateSeekBar);
        speechRate.setMax(2);
        speechRate.setProgress(1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.voices_array, android.R.layout.simple_spinner_item);
//        Object[] voiceList = mTts.getVoices().toArray();
//        for (int i = 0; i < mTts.getVoices().size(); i++) {
//            Log.i("Studie", voiceList[i].toString());
//            adapter.add(voiceList[i].toString());
//        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voices.setAdapter(adapter);
        speechRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTts.setSpeechRate(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SetActivity.this, "Speech Rate: " + seekBar.getProgress(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Log.i("Studie", "onInit() called");
                    mTts.setLanguage(Locale.US);
                    mTts.setVoice(mTts.getDefaultVoice());
                    mTts.setSpeechRate(1);
                    mTts.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build());
                }
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (int i = 0; i < quizletSet.getTermCount(); i++) {
                    speakQuizlet.append(quizletSet.getTerms().get(i));
                    speakQuizlet.append(". ");
                    speakQuizlet.append(quizletSet.getDefinitions().get(i));
                    speakQuizlet.append(". ");
                }
                Log.i("Studie", speakQuizlet.toString());

                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
                        Log.i("Studie", "onStart()");
                    }

                    @Override
                    public void onDone(String s) {
                        Log.i("Studie", "onDone()");
                        mTts.shutdown();
                    }

                    @Override
                    public void onError(String s) {
                        Log.i("Studie", "onError()");
                    }
                });
                mTts.speak(speakQuizlet, TextToSpeech.QUEUE_FLUSH, null, "");
            }
        });
    }

    public void playSet() {
        showProgressDialog();
        final StringBuffer speakQuizlet = new StringBuffer();
        mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Log.i("Studie", "onInit() called");
                    mTts.setLanguage(Locale.US);
                    mTts.setVoice(mTts.getDefaultVoice());
                    mTts.setSpeechRate(1);
                    mTts.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build());
//                    for (int i = 0; i < quizletSet.getTermCount(); i++) {
//                        mTts.speak((CharSequence) quizletSet.getTerms().get(i), TextToSpeech.QUEUE_FLUSH, null, "");
//                        mTts.speak("../", TextToSpeech.QUEUE_FLUSH, null, "");
//                    }
                    mTts.speak(speakQuizlet, TextToSpeech.QUEUE_FLUSH, null, "");
                } else if (status == TextToSpeech.ERROR) {
                    showErrorDialog();
                }
            }
        });
        for (int i = 0; i < quizletSet.getTermCount(); i++) {
            speakQuizlet.append(quizletSet.getTerms().get(i));
            speakQuizlet.append(". ");
            speakQuizlet.append(quizletSet.getDefinitions().get(i));
            speakQuizlet.append(". ");
        }
        Log.i("Studie", speakQuizlet.toString());

        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                progress.dismiss();
                Log.i("Studie", "onStart()");
            }

            @Override
            public void onDone(String s) {
                Log.i("Studie", "onDone()");
                mTts.shutdown();
                Log.i("Studie", "isDemo = " + MainActivity.isDemo);
                if (MainActivity.isDemo == true) {
                    Log.i("Studie", "Displaying demo over");
                    SetActivity mActivity = SetActivity.this;
                    mActivity = SetActivity.this;

                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            showDoneDialog();
                        }
                    });
                }
            }

            @Override
            public void onError(String s) {
                showErrorDialog();
                Log.i("Studie", "onError()");
            }
        });
    }

    public void showDoneDialog() {
        onDoneDialog.show();
    }

    public void showErrorDialog() {
        progress.dismiss();
        Log.e("Studie", "Showing Error Dialog!");
        AlertDialog onErrorDialog = new AlertDialog.Builder(SetActivity.this).create();
        onErrorDialog.setTitle("Error");
        onErrorDialog.setMessage("An unexpected error occured.");
        onErrorDialog.setIcon(R.drawable.error);
        onErrorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(SetActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
        onErrorDialog.show();
    }

    public void showProgressDialog() {
        progress = new ProgressDialog(SetActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Preparing to Play...");
        progress.show();
    }
}
