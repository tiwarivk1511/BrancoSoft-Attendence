package com.android.brancoattendence;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.brancoattendence.databinding.ActivityLoginBinding;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends ComponentActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 101 ;
    private ActivityLoginBinding binding;
    private static final String BASE_URL = "http://192.168.1.11:8000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //change the color of status bar without and SDK version check
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //check if user is already logged in
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString("token", null);
        if (token != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        //lock the orientation of the screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            // Reset error messages
            binding.errorEmailTxt.setVisibility(View.GONE);
            binding.errorPasswordTxt.setVisibility(View.GONE);

            // Validate email and password
            if (TextUtils.isEmpty(email)) {
                binding.errorEmailTxt.setVisibility(View.VISIBLE);
                binding.errorEmailTxt.setText("Email can't be empty, Please enter the E-mail");
                binding.errorEmailTxt.setTextColor(Color.RED);
            } else if (!isValidEmail(email)) {
                binding.errorEmailTxt.setVisibility(View.VISIBLE);
                binding.errorEmailTxt.setText("Please enter a valid email address");
                binding.errorEmailTxt.setTextColor(Color.RED);
            } else if (TextUtils.isEmpty(password)) {
                binding.errorPasswordTxt.setVisibility(View.VISIBLE);
                binding.errorPasswordTxt.setText("Password can't be empty,\nPlease enter the Password");
                binding.errorPasswordTxt.setTextColor(Color.RED);
            } else {
                try {
                    login(email, password);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        binding.forgetPasswordTxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            finish();
        });

        requestPermissions();
    }

    private boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void login(String email, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<LoginResponse> call = apiService.login(email, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getToken();
                    // Handle successful login, e.g., store token in shared preferences
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token);
                    editor.apply();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Handle error response
                    int statusCode = response.code();
                    Log.e("LoginActivity", "Login failed with status code: " + statusCode);
                    try {
                        Log.e("LoginActivity", "Error response body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // You can handle different status codes here
                    switch (statusCode) {
                        case 400:
                            Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                            break;
                        case 401:
                            Toast.makeText(getApplicationContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(getApplicationContext(), "Internal server error", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Handle network errors
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "Network error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
