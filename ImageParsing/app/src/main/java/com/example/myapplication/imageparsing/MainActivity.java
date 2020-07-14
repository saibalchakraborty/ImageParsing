package com.example.myapplication.imageparsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button selectButton, aboutButton, contactUsButton, openCamera;
    private int gallery_img = 1, camera_img = 2, camera_request= 100, cam_code = 99;
    private String picturePath;
    private Uri path;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }


    private void setup() {
        selectButton = findViewById(R.id.selectImage);
        selectButton.setOnClickListener(this);
        aboutButton = findViewById(R.id.buttonAbout);
        aboutButton.setOnClickListener(this);
        contactUsButton = findViewById(R.id.buttonContactUs);
        contactUsButton.setOnClickListener(this);
        openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.selectImage :
                selectImage();
                break;
            case R.id.buttonAbout :
                startActivity(new Intent(MainActivity.this, About.class));
                break;
            case R.id.buttonContactUs :
                startActivity(new Intent(MainActivity.this, ContactUs.class));
                break;
            case R.id.openCamera :
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.i("Camera permission ", "Acess = true");
                    openCamera();
                }
                else{
                    requestCameraPermission(this, this);
                }
        }
    }


    private void requestCameraPermission(Context context, final Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(context).setTitle("Permission needed").setMessage("Need for clicking images to proceed")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, camera_request);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        }
        else{
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, camera_request);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == camera_request) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i("Imageparsing.info : ", "Camera and file system permission granted" + grantResults[0] + " " + grantResults[1]);
                openCamera();
            }
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "img1");
        values.put(MediaStore.Images.Media.DESCRIPTION, "New img from cam");
        path = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i("Imageparsing.info ", "Going to use img");
        startActivityForResult(camIntent, cam_code);
    }

    private void selectImage(){
        Log.i("Imageparsing.info", "Image selection process started");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select gallery Image"),gallery_img);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Imageparsing.info", "req code : "+ requestCode + " resultCode : "+ resultCode + " data : "+ data);
        if(requestCode == gallery_img && resultCode == RESULT_OK && data != null){
            path = data.getData();
            String value = data.getData().toString();
            String[] projection = {MediaStore.Images.Media.DATA};
            try {
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(path, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                Log.i("Imageparsing.info", "taking image from gallery");
                Log.i("Imageparsing.info : ", "path for image when taking from gallery [ "+picturePath+ " ]");
                cursor.close();
                Intent intent = new Intent(this,ImageActivity.class);
                intent.putExtra("file_path", picturePath);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(requestCode == cam_code && resultCode == RESULT_OK && data != null){
            try {
                Log.i("Imageparsing.info", "taking image from camera directly");
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bs);
                Intent intent = new Intent(this,ImageActivity.class);
                intent.putExtra("byteArray", bs.toByteArray());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}