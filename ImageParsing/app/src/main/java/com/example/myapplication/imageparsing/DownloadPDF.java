package com.example.myapplication.imageparsing;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DownloadPDF {
    void downloadPDF(String identity) {
        Log.i("Info ; ", "downloading file with id "+identity);
        Retrofit retrofit = NetworkClient.getRetrofit();
        UserClient client = retrofit.create(UserClient.class);
        Call call = client.downloadFileWithc(identity);
        call.enqueue(new Callback() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(Call call, final Response response) {
                if(response.isSuccessful()){
                    new AsyncTask<Void, Void, Void>(){

                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk((ResponseBody) response.body());
                            Log.i("Status : ", "writting to disk : "+writtenToDisk);
                            return null;
                        }
                    }.execute();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.i("Failed to get PDF : ", t.getLocalizedMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean writeResponseBodyToDisk(ResponseBody body) {
        Log.i("Info", "Trying to write the pdf to device");
        try {
            File futureStudioIconFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "result.pdf");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096000];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d("File Download: ", fileSizeDownloaded + " of " + fileSize);
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
