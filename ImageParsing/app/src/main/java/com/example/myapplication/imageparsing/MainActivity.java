package com.example.myapplication.imageparsing;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button selectButton, uploadButton;
    private ImageView imageView;
    private int img_req = 1;
    private Bitmap bitmap;
    private Uri path;
    private String id;
    private final int delay = 3000;

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
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.selectImage :
                selectImage();
                break;
            case R.id.submitButton :
                id = Long.toHexString(new Date().getTime());
                if(new UploadImage().uploadImage(path, this, id)) {
                    new PollForImage().checkAvailabilityandDownload(delay, this, id);
                    Toast.makeText(this, "Success! Please check download folder", Toast.LENGTH_LONG).show();
                }
                else{
                    Log.e("Issue in Upload : ",new UploadImage().getIssue().getLocalizedMessage());
                }
                imageView.animate().alpha(0f).setDuration(500);
                break;
        }
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, img_req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == img_req && resultCode == RESULT_OK && data != null){
            path = data.getData();
            try {
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