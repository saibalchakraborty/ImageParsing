package com.example.myapplication.imageparsing;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class ContactUs extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactuswindow);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getWindow().setLayout((int)(.6*width), (int)(height*.3));
    }
}
