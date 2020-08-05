package com.example.myapplication.imageparsing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button selectButton, openCamera, sendButton;
    private Toolbar myToolbar;
    private ConstraintLayout constraintLayout;
    private int gallery_code = 1, cam_code = 2;
    private final int permissionGalery = 100, permissionCamera = 200, delay = 4000;;
    private String picturePath, currentPhotoPath, id;
    private Uri path;
    private ImageView imageView;
    private File file;
    private LoadingActivity loadingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    private void setup() {
        selectButton = findViewById(R.id.selectImage);
        selectButton.setOnClickListener(this);
        openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(this);
        sendButton = findViewById(R.id.submitButton);
        sendButton.setOnClickListener(this);
        imageView = findViewById(R.id.homeimageView);
        constraintLayout = findViewById(R.id.constraintLayout);
        myToolbar = findViewById(R.id.myappbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.buttonAbout:
                startActivity(new Intent(MainActivity.this, About.class));
                return true;
            case R.id.buttonContactUs:
                startActivity(new Intent(MainActivity.this, ContactUs.class));
                return true;
            default:
                return true;
        }
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
            case R.id.openCamera :
                String[] cameraPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                if(!PermissionUtil.haspermission(this, cameraPermissions)){
                    ActivityCompat.requestPermissions(this, cameraPermissions, permissionCamera);
                }
                else {
                    openCamera();
                }
                break;
            case R.id.submitButton :
                if(file != null){
                    Log.i("Imageparsing.info ", "Sending your file to server and waiting for pdf!");
                    loadingActivity = new LoadingActivity(this, this);
                    id = Long.toHexString(new Date().getTime());
                    loadingActivity.startLoading();
                    if (new UploadImage().uploadImage(this, id, this, file, loadingActivity)) {
                        new PollForImage().checkAvailabilityandDownload(delay, this, id, loadingActivity, imageView, this);
                    } else {
                        Log.e("Imageparsing.info : ", new UploadImage().getIssue().getLocalizedMessage());
                    }
                    //imageView.animate().alpha(0f).setDuration(500);
                    file = null;
                    imageView.setImageDrawable(getDrawable(R.drawable.scanbground));
                    imageView.animate().alpha(.3f).setDuration(50);
                    break;
                }
                else{
                    Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
                    break;
                }
        }
    }

    private void resizeImageView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final int imageViewHeight = metrics.heightPixels - (myToolbar.getHeight() + constraintLayout.getHeight());
        imageView.getLayoutParams().height = imageViewHeight;
        Log.i("Imageparsing.info","The final height of imageview "+ imageViewHeight+ " pixels");
        imageView.requestLayout();
        imageView.animate().alpha(0f).setDuration(500);
    }

    /*
    private int pxToDip(int pixels){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics());
    }*/

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
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.myapplication.imageparsing.provider", photoFile);
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
        Log.i("Imageparsing.info","captured image is in :"+currentPhotoPath);
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
                processImage(picturePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(requestCode == cam_code && resultCode == RESULT_OK){
            try {
                Log.i("Imageparsing.info", "taking image from camera directly");
                galleryAddPic();
                processImage(currentPhotoPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processImage(String path){
        resizeImageView();
        Log.i("Imageparsing.info ", "The path is : "+ path);
        Bitmap bitmap= BitmapFactory.decodeFile(path);
        imageView.setImageBitmap((bitmap));
        imageView.setVisibility(View.VISIBLE);
        imageView.animate().alpha(1f).setDuration(500);
        Log.i("Imageparsing.info : ","Image from gallery selected and displayed successfully");
        file = new File(picturePath);
        //part adding extra to support image compression
        try {
            Log.i("Imageparsing.info ", "File size before compressing "+file.length()/1024 + "Kb");
            Bitmap bmap = BitmapFactory.decodeFile(file.getName());
            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outStream);
            outStream.flush();
            outStream.close();
            Log.i("Imageparsing.info ", "File size after compressing "+file.length()/1024 + "Kb");
        }
        catch(IOException e){
            e.printStackTrace();
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