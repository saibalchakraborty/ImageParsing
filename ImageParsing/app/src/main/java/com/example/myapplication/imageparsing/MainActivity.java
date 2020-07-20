package com.example.myapplication.imageparsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button selectButton, aboutButton, contactUsButton, openCamera;
    private int gallery_code = 1, cam_code = 2;
    private int permissionGalery = 100, permissionCamera = 200;
    private String picturePath, currentPhotoPath;
    private Uri path;

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
                String[] galleryPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                if(!PermissionUtil.haspermission(this, galleryPermissions)){
                    ActivityCompat.requestPermissions(this, galleryPermissions, permissionGalery);
                }
                else {
                    selectImage();
                }
                break;
            case R.id.buttonAbout :
                startActivity(new Intent(MainActivity.this, About.class));
                break;
            case R.id.buttonContactUs :
                startActivity(new Intent(MainActivity.this, ContactUs.class));
                break;
            case R.id.openCamera :
                String[] cameraPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                if(!PermissionUtil.haspermission(this, cameraPermissions)){
                    ActivityCompat.requestPermissions(this, cameraPermissions, permissionCamera);
                }
                else {
                    openCamera();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == permissionGalery){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
        }
        else if(requestCode == permissionCamera){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }
        }
    }

    private void openCamera() {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i("Imageparsing.info ", "Going to use img");
        if (camIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                camIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(camIntent, cam_code);
            }
        }
        else{
            Log.i("Imageparsing.info ", "oeee the intent is null");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void selectImage(){
        Log.i("Imageparsing.info", "Image selection process started");
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select gallery Image"),gallery_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Imageparsing.info", "req code : "+ requestCode + " resultCode : "+ resultCode + " data : "+ data);
        if(requestCode == gallery_code && resultCode == RESULT_OK && data != null){
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
                Log.i("Imageparsing.info : ", "path for image when taking from gallery ["+picturePath+ "]");
                cursor.close();
                Intent intent = new Intent(this,ImageActivity.class);
                intent.putExtra("path", picturePath);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(requestCode == cam_code && resultCode == RESULT_OK){
            try {
                Log.i("Imageparsing.info", "taking image from camera directly");
                galleryAddPic();
                Intent intent = new Intent(this,ImageActivity.class);
                intent.putExtra("path", currentPhotoPath);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //implementing for confirming exit
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }
}