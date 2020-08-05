package com.example.myapplication.imageparsing;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PollForImage extends AppCompatActivity {
    private Handler handler = new Handler();
    private Retrofit retrofit;
    private UserClient client;
    private Context ctx;
    private LoadingActivity loadingActivity;
    int timeDuration;
    private String identity;
    private ImageView imageView;
    private Activity activity;

    private Runnable repeatInstanceThread= new Runnable() {
        @Override
        public void run() {
            Call<Result> call = client.getStatus(identity);
            call.enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    if(response.isSuccessful()) {
                        if(response.body().getStatus() !=0 ){
                            Log.i("Imageparsing.info ", "File ready to downoad. Stopping polling activity");
                            exitCondition(response.body().getStatus());
                        }
                        else {
                            Log.i("Imageparsing.info ", "Continue polling...");
                            handler.postDelayed(repeatInstanceThread, timeDuration);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    loadingActivity.dismissDialog("Server didnt send document back");
                    Log.e("Imageparsing.info :", t.getMessage());
                }
            });
        }
    };

    private void exitCondition(int status) {
        handler.removeCallbacks(repeatInstanceThread);
        if(status == 1){
            new DownloadPDF().downloadPDF(identity, loadingActivity, activity, ctx);
            Log.i("Imageparsing.info ", "Success! Please check downloads folder.");
        }
        else{
            Log.i("Imageparsing.info ", "Failed! Please upload a different/better file.");
            loadingActivity.dismissDialog("Failed! Please upload a different/better file.");
        }
    }

    private void setup(){
        retrofit = NetworkClient.getRetrofit();
        client = retrofit.create(UserClient.class);
    }

    void checkAvailabilityandDownload(int duration, Context context, String id, LoadingActivity loadingActivity, ImageView imageView, Activity activity){
        Log.i("Imageparsing.info ", "Polling started");
        this.imageView = imageView;
        this.loadingActivity = loadingActivity;
        this.activity = activity;
        ctx = context;
        timeDuration = duration;
        identity = id;
        setup();
        handler.postDelayed(repeatInstanceThread, timeDuration);
    }
}
