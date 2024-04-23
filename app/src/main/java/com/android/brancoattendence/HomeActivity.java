package com.android.brancoattendence;

import static com.android.brancoattendence.Attendence.CheckInResponse.getCheckInTime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.brancoattendence.Attendence.CheckInResponse;
import com.android.brancoattendence.databinding.ActivityHomeBinding;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_attendance, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        myBroadcastReceiver.onReceive(this, new Intent(Intent.ACTION_BOOT_COMPLETED));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_profile || destination.getId() == R.id.nav_attendance) {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_arrow_back_ios_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> onBackPressed());
            } else {
                binding.appBarHome.toolbar.setNavigationIcon(R.drawable.round_menu_24);
                binding.appBarHome.toolbar.setNavigationOnClickListener(v -> {
                    drawer.openDrawer(GravityCompat.START);
                });
            }
        });

        binding.navView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            performLogOut();
            return true;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.statusBarColor));

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }



    private void performLogOut() {
        String baseUrl = HostURL.getBaseUrl();
        String token = retrieveTokenFromSharedPreferences();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<Void> call = apiService.logout("Bearer " + token);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    clearTokenFromSharedPreferences();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(HomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Logout failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("token", null);
    }

    private void clearTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
