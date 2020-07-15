package com.example.myapplication.imageparsing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

public class LoadingActivity extends AppCompatActivity{

    private Activity myActivity;
    private AlertDialog alertDialog;
    private Context ctx;

    public LoadingActivity(){}
    public LoadingActivity(Activity myActivity, Context ctx){
        this.myActivity = myActivity;
        this.ctx = ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
    }

    void startLoading(){
        Log.i("Imageparsing.info ", "Initializing the laod");
        AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
        LayoutInflater layoutInflater = myActivity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.activity_loading, null));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    void dismissDialog(String value){
        Log.i("Imageparsing.info ", "Stopping the loading dialog : " + value);
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ctx, value, Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
                finish();
            }
        });
    }
}