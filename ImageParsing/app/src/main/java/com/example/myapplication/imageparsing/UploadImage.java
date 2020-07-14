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

    boolean uploadImage(final Context context, String id, Activity activity, File file, LoadingActivity loadingActivity)  {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "Image file");
        Retrofit retrofit = NetworkClient.getRetrofit();
        UserClient client = retrofit.create(UserClient.class);
        Log.i("Imageparsing.info ", "Invoking upload of image : "+ part);
        Call<PollAfter> call = client.uploadPhoto(part, description, id);
        call.enqueue(new Callback<PollAfter>() {
            @Override
            public void onResponse(Call<PollAfter> call, Response<PollAfter> response) {
                if(response.isSuccessful()){
                    Log.v("Imageparsing.info :", response.body().getDescription());
                }
                else{
                    Log.i("Imageparsing.info ", "Response not sucessfull [ "+ response.body().toString()+ " ]");
                    Toast.makeText(context, "Uploading failed!", Toast.LENGTH_LONG).show();
                    loadingActivity.dismissDialog("Stopping due to failed response while submitting image");
                    triggerFailure(new Throwable(response.errorBody().toString()));
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.i("Imageparsing.info ", "Call failed [ "+ t.getLocalizedMessage()+ " ]");
                Toast.makeText(context, "Uploading failed!", Toast.LENGTH_LONG).show();
                loadingActivity.dismissDialog("Stopping due to failure in uploading image");
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