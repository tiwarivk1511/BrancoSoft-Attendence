package com.android.brancoattendence;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.brancoattendence.databinding.ActivityLoginBinding;

public class LoginActivity extends ComponentActivity {

    private ActivityLoginBinding binding;

    private final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private final int REQUEST_LOCATION_PERMISSION = 102;
    private final int REQUEST_BACKGROUND_LOCATION_PERMISSIONS = 103;
    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


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

                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();

            }
        });

        binding.forgetPasswordTxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            finish();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions();
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
    }

    private void requestPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, getPermissionRequestCode(permission));
            }
        }
    }

    private int getPermissionRequestCode(String permission) {
        for (String s : permissions) {
            if (s.equals(permission)) {
                switch (permission) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        return REQUEST_LOCATION_PERMISSION;
                    case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                        return REQUEST_BACKGROUND_LOCATION_PERMISSIONS;
                    case Manifest.permission.POST_NOTIFICATIONS:
                        return REQUEST_NOTIFICATION_PERMISSION;
                }
            }
        }
        return -1; // Default value
    }



    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            requestBackgroundLocationPermission();
        }
    }

    private void requestBackgroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_BACKGROUND_LOCATION_PERMISSIONS);
        } else {
            requestNotificationPermission();
        }
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
        } else {
            // All permissions already granted
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle permission request results if needed
    }
}
