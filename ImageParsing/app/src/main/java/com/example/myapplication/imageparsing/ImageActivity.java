package com.example.myapplication.imageparsing;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener{

    private Button submitButton;
    private String id;
    private ImageView imageView;
    private final int delay = 4000;
    private LoadingActivity loadingActivity;
    private File file, uploadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Intent intent = getIntent();
        processImage(intent);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);
    }

    private void processImage(Intent intent){
        String picturePath = intent.getStringExtra("path");
        imageView = findViewById(R.id.myImage);
        Log.i("Imageparsing.info ", "The path is : "+ picturePath);
        Bitmap bitmap= BitmapFactory.decodeFile(picturePath);
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
            Log.i("Imageparsing.info ", "File size before compressing "+file.length()/1024 + "Kb");
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.submitButton :if(file!=null) {
                Log.i("Imageparsing.info ", "Sending your file to server and waiting for pdf!");
                loadingActivity = new LoadingActivity(ImageActivity.this, ImageActivity.this);
                id = Long.toHexString(new Date().getTime());
                loadingActivity.startLoading();
                if (new UploadImage().uploadImage(this, id, this, file, loadingActivity)) {
                    new PollForImage().checkAvailabilityandDownload(delay, this, id, loadingActivity);
                } else {
                    Log.e("Imageparsing.info : ", new UploadImage().getIssue().getLocalizedMessage());
                }
                imageView.animate().alpha(0f).setDuration(500);
                file = null;
                break;
            }
            else{
                Toast.makeText(this, "Please go back and select an image!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}