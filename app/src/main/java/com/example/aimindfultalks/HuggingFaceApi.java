package com.example.aimindfultalks;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface HuggingFaceApi {
    @Headers("Content-Type: application/json")
    @POST("meta-llama/Meta-Llama-3-8B-Instruct/v1/chat/completions")
    Call<ResponseBody> getChatbotResponse(@Body RequestBody requestBody, @Header("Authorization") String authHeader);
}
