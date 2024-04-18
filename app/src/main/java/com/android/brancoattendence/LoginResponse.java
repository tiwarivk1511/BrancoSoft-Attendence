package com.android.brancoattendence;

import com.google.gson.annotations.SerializedName;


public class LoginResponse {
    @SerializedName("token")
    private String token;



    public String getToken() {

        return token;
    }
}


