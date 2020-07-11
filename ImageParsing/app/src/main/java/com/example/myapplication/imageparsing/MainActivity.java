package com.example.myapplication.imageparsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button selectButton, uploadButton, aboutButton, contactUsButton;
    private ImageView imageView;
    private int img_req = 1;
    private Bitmap bitmap;
    String picturePath;
    private Uri path;
    private String id;
    private final int delay = 3000;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    private void setup() {
        selectButton = findViewById(R.id.selectImage);
        uploadButton = findViewById(R.id.submitButton);
        selectButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        imageView = findViewById(R.id.myImage);
        aboutButton = findViewById(R.id.buttonAbout);
        aboutButton.setOnClickListener(this);
        contactUsButton = findViewById(R.id.buttonContactUs);
        contactUsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.selectImage :
                selectImage();
                break;
            case R.id.submitButton :
                id = Long.toHexString(new Date().getTime());
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.i("Information : ", "Hurrah!! I have permision");
                    if(new UploadImage().uploadImage(path, this, id, this, picturePath)) {
                        new PollForImage().checkAvailabilityandDownload(delay, this, id);
                    }
                    else{
                        Log.e("Issue in Upload : ",new UploadImage().getIssue().getLocalizedMessage());
                    }
                    imageView.animate().alpha(0f).setDuration(500);
                }
                else{
                    Log.i("Information : ", "Requesting permission");
                    requestStoragePermission(this, this);
                }
                break;
            case R.id.buttonAbout :
                startActivity(new Intent(MainActivity.this, About.class));
                break;
            case R.id.buttonContactUs :
                startActivity(new Intent(MainActivity.this, ContactUs.class));
                break;
        }
    }

    private void requestStoragePermission(Context context, final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(context).setTitle("Permission needed").setMessage("Need to access File Storage to upload files")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Log.i("Information : ", "Atlast permission received"+ grantResults[0]+ " "+ grantResults[1]);
                if(new UploadImage().uploadImage(path, this, id, this, picturePath)) {
                    new PollForImage().checkAvailabilityandDownload(delay, this, id);
                }
                else{
                    Log.e("Issue in Upload : ",new UploadImage().getIssue().getLocalizedMessage());
                }
                imageView.animate().alpha(0f).setDuration(500);
            }
        }
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Image"),img_req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == img_req && resultCode == RESULT_OK && data != null){
            path = data.getData();
            String value = data.getData().toString();
            Log.d("Picture Path : ", value);
            String[] projection = {MediaStore.Images.Media.DATA};
            try {
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(path, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                Log.d("Picture Path : ", picturePath);
                cursor.close();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                imageView.setImageBitmap((bitmap));
                imageView.setVisibility(View.VISIBLE);
                imageView.animate().alpha(1f).setDuration(500);
                Log.i("Image selection : ","Image selected successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}