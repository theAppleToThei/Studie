package com.techtalk4geeks.studie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by alex on 2/6/16.
 */
public class SetActivity extends Activity {

    QuizletSet quizletSet;
    TableLayout termsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        quizletSet = MainActivity.getCurrentQuizletSet();
        setTitle(quizletSet.getTitle());
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
            termsView.addView(row);
            row.addView(term);
            row.addView(definition);
        }
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
