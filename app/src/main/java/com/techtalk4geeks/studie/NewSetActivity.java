package com.techtalk4geeks.studie;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

public class NewSetActivity extends Activity {

    public NewSetActivity(Context context, AttributeSet attributeSet) {
        super();
    }

    public NewSetActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_set);
    }
}
