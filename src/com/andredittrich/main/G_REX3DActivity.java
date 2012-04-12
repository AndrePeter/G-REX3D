package com.andredittrich.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class G_REX3DActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textview = new TextView(this);
        textview.setText("TEST");
        
        setContentView(textview);
    }
}