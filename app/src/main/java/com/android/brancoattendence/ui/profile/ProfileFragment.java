package com.android.brancoattendence.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.databinding.FragmentProfileBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private static final String BASE_URL = HostURL.getBaseUrl();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Fetch user data
        fetchUserData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchUserData() {
        // Retrieve token from SharedPreferences
        String token = retrieveTokenFromSharedPreferences();

        // Check if token is null
        if (token == null) {
            // Handle null token
            Toast.makeText(getContext(), "Token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        ApiService apiService = retrofit.create(ApiService.class);

        // Make API call to fetch user data with Authorization header
        Call<UserDataResponse> call = apiService.getUserData("Bearer " + token);

        call.enqueue(new Callback<UserDataResponse>() {
            @Override
            public void onResponse(Call<UserDataResponse> call, Response<UserDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update UI with user data
                    updateUI(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDataResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUI(UserDataResponse userData) {
        String MiddName = userData.getMiddleName();
        if (MiddName == null) {
            MiddName = "";
        }
        else {
            MiddName = " " + MiddName;
        }

        String FullName = userData.getFirstName() +MiddName+ " " + userData.getLastName();
        // Update UI with user data
        binding.employeeId.setText(userData.getEmployeeId());
        binding.userName.setText(FullName);
        binding.userEmail.setText(userData.getEmail());
        binding.contactNo.setText(userData.getContactNo());
        binding.department.setText(userData.getDepartment());
        binding.designation.setText(userData.getDesignation());
        binding.dateOfJoining.setText(userData.getDateOfJoining());
    }

    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String token = preferences.getString("token", null);
        System.out.println("Token: " + token);
        return token;
    }
}