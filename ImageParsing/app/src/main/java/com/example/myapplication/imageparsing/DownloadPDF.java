package com.example.myapplication.imageparsing;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DownloadPDF {

    private File futureStudioIconFile;
    private Activity activity;
    private Context context;


    void downloadPDF(String identity, LoadingActivity loadingActivity, Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        Log.i("Imageparsing.info ", "downloading file with id "+identity);
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
                            Log.i("Imageparsing.info : ", "writting to disk : "+writtenToDisk);
                            loadingActivity.dismissDialog("Success! Please check Scanocle folder");
                            return null;
                        }
                    }.execute();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.i("Imageparsing.info : ", "Failed to get PDF file due to : "+t.getLocalizedMessage());
                loadingActivity.dismissDialog("File download failed");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean writeResponseBodyToDisk(ResponseBody body) {
        Log.i("Imageparsing.info ", "Trying to write the pdf to device");
        //file name modification (Take current system time)
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdfDate.format(new Date());
        String filepath = null;
        try {
            //filepath = context.getFilesDir() + "/Scanocle";
            //filepath = Environment.getExternalStoragePublicDirectory(Environment.getRootDirectory().getAbsolutePath()).getAbsolutePath();
            /*filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.i("Imageparsing.info", "Writing at: "+ filepath);
            File dir = new File(filepath);
            if (!dir.exists()) {
                dir.mkdirs();
            }*/
            filepath = context.getExternalFilesDir("Scanocle").getAbsolutePath();
            futureStudioIconFile = new File(filepath, "Scanned_Result_"+strDate+".pdf");
            Log.i("Imageparsing.info", "The full path : "+ filepath+"/Scanned_Result_"+strDate+".pdf");
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
                    Log.i("Imageparsing.info: ", fileSizeDownloaded + " of " + fileSize);
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
        showPDF(futureStudioIconFile);
        return true;
    }

    private void showPDF(File file){
        //open the pdf file
        Log.i("Imageparsing.info", "home directory: "+context.getFilesDir());
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent pdfViewIntent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                pdfViewIntent.setDataAndType(fileUri,"application/pdf");
                pdfViewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pdfViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent intent = Intent.createChooser(pdfViewIntent, "Open File");
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }
            }
        });
    }
}
