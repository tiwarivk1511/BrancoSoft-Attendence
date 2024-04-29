package com.android.brancoattendence;


import com.android.brancoattendence.ui.profile.UserDataResponse;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @POST("attendances")
    Call<AttendanceResponse> checkIn(@Header("Authorization") String token, @Query("check_in") String checkInTime);

    @POST("attendances/{checkInId}")
    Call<AttendanceResponse> checkOut(
            @Header("Authorization") String token,
            @Path("checkInId") int checkInId,
            @Query("check_out") String checkOutTime
    );


//    @PUT("attendances/{id}")
//    Call<Void> markCheckout(@Path("id") int attendanceId, @Body CheckoutRequest checkoutRequest);

}
