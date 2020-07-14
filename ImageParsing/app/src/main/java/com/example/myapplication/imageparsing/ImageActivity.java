package com.example.myapplication.imageparsing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{

    private Button submitButton;
    private String id, picturePath;
    private ImageView imageView;
    private final int delay = 4000;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private LoadingActivity loadingActivity;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Intent intent = getIntent();
        if(intent.hasExtra("byteArray")){
            processImageSelectionfromCamera(intent);
        }
        else if(intent.hasExtra("file_path")) {
            processImageSelectionfromGallery(intent);
        }
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);
    }

    private void processImageSelectionfromCamera(Intent intent) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra("byteArray"),0,getIntent().getByteArrayExtra("byteArray").length);
        imageView = findViewById(R.id.myImage);
        imageView.setImageBitmap((bitmap));
        imageView.setVisibility(View.VISIBLE);
        imageView.animate().alpha(1f).setDuration(500);
        Log.i("Imageparsing.info : ","Image from Camera captured and displayed successfully");
        try {
            file = new File(this.getCacheDir(), "abc.png");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(intent.getByteArrayExtra("byteArray"));
            fos.flush();
            fos.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void processImageSelectionfromGallery(Intent intent){
        picturePath = intent.getStringExtra("file_path");
        imageView = findViewById(R.id.myImage);
        Bitmap bitmap= BitmapFactory.decodeFile(picturePath);
        imageView.setImageBitmap((bitmap));
        imageView.setVisibility(View.VISIBLE);
        imageView.animate().alpha(1f).setDuration(500);
        Log.i("Imageparsing.info : ","Image from gallery selected and displayed successfully");
        file = new File(picturePath);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.submitButton :
                Log.i("Imageparsing.info ", "Sending your file to server and waiting for pdf!");
                loadingActivity = new LoadingActivity(ImageActivity.this);
                id = Long.toHexString(new Date().getTime());
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                    Log.i("Imageparsing.info : ", "Hurrah!! I have permision for file reading and writing");
                    loadingActivity.startLoading();
                    if(new UploadImage().uploadImage(this, id, this, file, loadingActivity)) {
                        new PollForImage().checkAvailabilityandDownload(delay, this, id, loadingActivity);
                    }
                    else{
                        Log.e("Imageparsing.info : ",new UploadImage().getIssue().getLocalizedMessage());
                    }
                    imageView.animate().alpha(0f).setDuration(500);
                }
                else{
                    Log.i("Imageparsing.info : ", "Requesting permission");
                    requestStoragePermission(this, this);
                }
                picturePath = null;
                break;
        }
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void requestStoragePermission(Context context, final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.INTERNET)){
            new AlertDialog.Builder(context).setTitle("Permission needed").setMessage("Need for uploading files to process")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Log.i("Imageparsing.info : ", "File system permission granted"+ grantResults[0]+ " "+ grantResults[1]);
                loadingActivity.startLoading();
                if(new UploadImage().uploadImage(this, id, this, file, loadingActivity)) {

                    new PollForImage().checkAvailabilityandDownload(delay, this, id, loadingActivity);
                }
                else{
                    Log.e("Imageparsing.info: ",new UploadImage().getIssue().getLocalizedMessage());
                }
                imageView.animate().alpha(0f).setDuration(500);
            }
        }
    }
}