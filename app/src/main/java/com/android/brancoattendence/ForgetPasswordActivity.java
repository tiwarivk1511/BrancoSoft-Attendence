package com.android.brancoattendence;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.ForgetPasswordResponse;
import com.android.brancoattendence.LoginActivity;
import com.android.brancoattendence.databinding.ActivityForgetPasswordBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgetPasswordActivity extends AppCompatActivity {
    ActivityForgetPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        //lock the orientation of the screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        binding.SendOtpBtn.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString();

            if (!email.isEmpty()) {
                requestPasswordReset(email);
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void requestPasswordReset(String email) {
        binding.progressCircular.setVisibility(View.VISIBLE);
        String baseUrl = HostURL.getBaseUrl();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ForgetPasswordResponse> call = apiService.forgetPassword(email);

        call.enqueue(new Callback<ForgetPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgetPasswordResponse> call, Response<ForgetPasswordResponse> response) {
                if (response.isSuccessful()) {
                    ForgetPasswordResponse forgetPasswordResponse = response.body();
                    if (forgetPasswordResponse != null && forgetPasswordResponse.isSuccess()) {
                        Toast.makeText(ForgetPasswordActivity.this, "Password reset email sent successfully", Toast.LENGTH_SHORT).show();
                        binding.progressCircular.setVisibility(View.GONE);
                        startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(ForgetPasswordActivity.this, "Failed to reset password. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForgetPasswordResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(ForgetPasswordActivity.this, "Failed to reset password. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
