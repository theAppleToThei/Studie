package com.techtalk4geeks.studie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by alex on 2/6/16.
 */
public class SetActivity extends Activity {

    QuizletSet quizletSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        quizletSet = MainActivity.getCurrentQuizletSet();
        setTitle(quizletSet.getTitle());
        TextView title = (TextView) findViewById(R.id.quizletTitleText);
        TextView creator = (TextView) findViewById(R.id.quizletUserText);
//        TextView termCount = (TextView) findViewById(R.id.quizletTermCount);
        title.setText(quizletSet.getTitle());
        creator.setText(quizletSet.getCreatorName());
//        termCount.setText(quizletSet.getTermCount());
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
        if (id == R.id.viewinbrowser) {
            Uri uri = Uri.parse(quizletSet.getURL());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(webIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
