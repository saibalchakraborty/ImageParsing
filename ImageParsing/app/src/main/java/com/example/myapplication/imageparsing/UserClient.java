package com.example.myapplication.imageparsing;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface UserClient {

    @Multipart
    @POST("uploadfile/{id}")
    Call<PollAfter> uploadPhoto(@Part MultipartBody.Part imageFile, @Part("description") RequestBody description, @Path("id") String id);

    @Streaming
    @GET("downloadFile/{id}")
    Call<ResponseBody> downloadFileWithc(@Path("id") String id);

    @GET("checkStatus/{id}")
    Call<Result> getStatus(@Path("id") String id);
}