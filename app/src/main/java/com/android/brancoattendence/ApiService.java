package com.android.brancoattendence;

import com.android.brancoattendence.Attendence.CheckInResponse;
import com.android.brancoattendence.Attendence.CheckOutResponse;
import com.android.brancoattendence.RecyclerAdepters.AttendanceData;
import com.android.brancoattendence.RecyclerAdepters.AttendanceResponse;
import com.android.brancoattendence.ui.profile.UserDataResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    String BASE_URL = HostURL.getBaseUrl(); // base URL

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    static OkHttpClient getClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @FormUrlEncoded
    @POST("check_in")
    Call<CheckInResponse> checkIn(
            @Header("Authorization") String token,
            @Field("check_in") String check_in
    );

    @FormUrlEncoded
    @PUT("check_out")
    Call<CheckOutResponse> checkOut(
            @Header("Authorization") String token,
            @Field("attd_id") String attendanceId,
            @Field("check_out") String check_out
    );

    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("user")
    Call<UserDataResponse> getUserData(@Header("Authorization") String token);


    @POST("forget-password")
    Call<ForgetPasswordResponse> forgetPassword(@Query("email") String email);


    @POST("logout")
    Call<Void> logout(@Header("Authorization") String token);

    @GET("attendances")
    Call<List<AttendanceData>> getAttendances(@Header("Authorization") String token);


}
