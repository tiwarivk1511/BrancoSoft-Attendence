package com.android.brancoattendence;

import com.android.brancoattendence.ui.profile.UserDataResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {


    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("user")
    Call<UserDataResponse> getUserData(@Header("Authorization") String token);

    @FormUrlEncoded
    @POST("forget-password")
    Call<ForgetPasswordResponse> forgetPassword(@Field("email") String email);

    @POST("logout")
    Call<Void> logout(@Header("Authorization") String token);
}
