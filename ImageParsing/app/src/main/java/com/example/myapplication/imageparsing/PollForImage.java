package com.example.myapplication.imageparsing;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PollForImage {
    private Handler handler = new Handler();
    private Retrofit retrofit;
    private UserClient client;
    Context ctx;
    int timeDuration;
    String identity;
    private Runnable repeatInstanceThread= new Runnable() {
        @Override
        public void run() {
            Call<Result> call = client.getStatus(identity);
            call.enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    if(response.isSuccessful()) {
                        if(response.body().getStatus() !=0 ){
                            exitCondition(response.body().getStatus());
                        }
                        else {
                            handler.postDelayed(repeatInstanceThread, timeDuration);
                        }
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    Log.e("Failure in polling :", t.getMessage());
                    Toast.makeText(ctx, "Failed to retrieve status", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private void exitCondition(int status) {
        handler.removeCallbacks(repeatInstanceThread);
        if(status == 1){
            new DownloadPDF().downloadPDF(identity);
            Toast.makeText(ctx, "Success! Please check downloads folder.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(ctx, "Failed! Please upload a different/better file", Toast.LENGTH_LONG).show();
        }
    }

    private void setup(){
        retrofit = NetworkClient.getRetrofit();
        client = retrofit.create(UserClient.class);
    }

    void checkAvailabilityandDownload(int duration, Context context, String id){
        timeDuration = duration;
        identity = id;
        setup();
        ctx = context;
        handler.postDelayed(repeatInstanceThread, timeDuration);
    }
}
