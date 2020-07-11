package com.example.myapplication.imageparsing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadImage extends ActivityCompat {
    boolean result = true;
    Throwable throwable;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    boolean uploadImage(Uri uri, final Context context, String id, Activity activity, String filePath)  {
        File file = new File(filePath);
        Log.i("URI Info : ", uri.getPath() + " file is : "+ file);

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "Image file");
        Retrofit retrofit = NetworkClient.getRetrofit();
        UserClient client = retrofit.create(UserClient.class);
        Call<PollAfter> call = client.uploadPhoto(part, description, id);
        call.enqueue(new Callback<PollAfter>() {
            @Override
            public void onResponse(Call<PollAfter> call, Response<PollAfter> response) {
                if(response.isSuccessful()){
                    Log.v("Upload info :", response.body().getDescription());
                }
                else{
                    Toast.makeText(context, "Uploading failed!", Toast.LENGTH_LONG).show();
                    triggerFailure(new Throwable(response.errorBody().toString()));
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(context, "Uploading failed!", Toast.LENGTH_LONG).show();
                triggerFailure(t);
            }
        });
        return result;
    }

    Throwable getIssue(){
        return throwable;
    }

    private void triggerFailure(Throwable t) {
        throwable = t;
        result = false;
    }
}