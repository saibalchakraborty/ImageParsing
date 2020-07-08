package com.example.myapplication.imageparsing;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadImage {
    boolean result = true;
    Throwable throwable;

    boolean uploadImage(Uri uri, final Context context, String id) {
        File file = new File(FileUtil.getPath(uri, context));
        Log.i("URI Info : ", uri.getPath());
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