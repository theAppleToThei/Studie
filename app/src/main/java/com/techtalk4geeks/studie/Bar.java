package com.techtalk4geeks.studie;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by alex on 8/13/16.
 */
public class Bar extends LinearLayout {
    TextView barText;
    ImageView barIcon;

    public Bar(Context context, AttributeSet attrs) {
        super(context, attrs);
//        try {
//
//        } catch (Exception e) {
//            Log.wtf("Studie", "Exception in bar lol ur scrwed m8\n" + Log.getStackTraceString(e));
//        }
    }

    public void setBar(String text, int icon, int color, int textID, int iconID) {
        barText = (TextView) this.findViewById(textID);
        barIcon = (ImageView) this.findViewById(iconID);

        barText.setText(text);
        barIcon.setImageResource(icon);
        this.setBackgroundColor(getResources().getColor(color));
    }

    public void setBarText(String text) {
        barText.setText(text);
    }

    public void setBarIcon(int icon) {
        barIcon.setImageResource(icon);
    }

    public void setBarColor(int color) {
        this.setBackgroundColor(color);
    }
}
