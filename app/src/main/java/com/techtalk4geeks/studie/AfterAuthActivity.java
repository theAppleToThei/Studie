package com.techtalk4geeks.studie;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class AfterAuthActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Studie");
        Uri uri = getIntent().getData();
        Log.i("Studie", "URI = " + uri.toString());
        String scheme = uri.getScheme();
        if(scheme.substring(35, 39).equalsIgnoreCase("code")) {
            Log.i("Studie", "Success!");
        } else if (scheme.substring(35, 39).equalsIgnoreCase("erro")) {
            Log.e("Studie", "Failed!");
        } else {
            Log.e("Studie", "Failed!");
            throw new NullPointerException();
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
