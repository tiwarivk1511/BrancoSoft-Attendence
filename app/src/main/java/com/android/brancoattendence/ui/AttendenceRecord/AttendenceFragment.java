package com.android.brancoattendence.ui.AttendenceRecord;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.AttendanceData;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.R;
import com.android.brancoattendence.databinding.FragmentAttendenceBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AttendenceFragment extends Fragment {

    private static final String TOKEN_KEY = "token";
    private FragmentAttendenceBinding binding;
    private List<AttendanceData> attendanceList = new ArrayList<>();
    private AttendanceAdapter adapterAttendance;
    private String Year;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private String SelectedMonth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize RecyclerView
        RecyclerView recyclerView = binding.AttendenceRecords;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterAttendance = new AttendanceAdapter(attendanceList);
        recyclerView.setAdapter(adapterAttendance);

        // Fetch all attendance data by default
        fetchDataFromAPI();

        // Create an AutoCompleteTextView to select the month
        AutoCompleteTextView autoCompleteTextView = binding.AutoCompleteTextViewMonths;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_items, months);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String monthItem = parent.getItemAtPosition(position).toString();
            Toast.makeText(getContext(), "Selected: " + monthItem, Toast.LENGTH_SHORT).show();
            SelectedMonth = monthItem;
            filterDataByMonthAndYear(); // Filter data when month is selected
        });

        // Create a AutoCompleteTextView to select year from 2010 to current year
        AutoCompleteTextView autoCompleteTextViewYear = binding.AutoCompleteTextViewYears;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[currentYear - 2010 + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_items, years);
        autoCompleteTextViewYear.setAdapter(yearAdapter);

        autoCompleteTextViewYear.setOnItemClickListener((parent, view12, position, id) -> {
            String yearItem = parent.getItemAtPosition(position).toString();
            Toast.makeText(getContext(), "Selected: " + yearItem, Toast.LENGTH_SHORT).show();
            Year = yearItem;
            filterDataByMonthAndYear(); // Filter data when year is selected
        });

        return view;
    }

    private void fetchDataFromAPI() {
        // Get the user token
        String token = getUserToken();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        ApiService apiService = retrofit.create(ApiService.class);

        // Make network request using Retrofit to get all attendance data
        Call<List<AttendanceData>> call = apiService.getAttendances("Bearer " + token);

        // Execute the request asynchronously
        call.enqueue(new Callback<List<AttendanceData>>() {
            @Override
            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Handle successful response
                    attendanceList.clear();
                    attendanceList.addAll(response.body());
                    adapterAttendance.notifyDataSetChanged();
                } else {
                    // Handle unsuccessful response
                    Toast.makeText(requireContext(), "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
                // Handle network errors
                Toast.makeText(requireContext(), "Failed to fetch attendance data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterDataByMonthAndYear() {
        List<AttendanceData> filteredList = new ArrayList<>();
        for (AttendanceData data : attendanceList) {
            // Check if the data matches the selected month and year
            if (matchesSelectedMonthAndYear(data)) {
                filteredList.add(data);
            }
        }
        // Update the RecyclerView with filtered data
        adapterAttendance.setData(filteredList);
    }

    private boolean matchesSelectedMonthAndYear(AttendanceData data) {
        if (SelectedMonth == null || Year == null) {
            return true; // No filter selected, so include all data
        }

        // Extract month and year from the data's date
        Calendar cal = Calendar.getInstance();

        int dataMonth = cal.get(Calendar.MONTH) + 1; // Month is zero-based
        int dataYear = cal.get(Calendar.YEAR);

        // Check if the data's month and year match the selected month and year
        return months[dataMonth - 1].equalsIgnoreCase(SelectedMonth) && Year.equals(String.valueOf(dataYear));
    }

//    private void fetchDataFromAPI() {
//        // Get the user token
//        String token = getUserToken();
//
//        // Create Retrofit instance
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(HostURL.getBaseUrl())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // Create ApiService instance
//        ApiService apiService = retrofit.create(ApiService.class);
//
//        // Make network request using Retrofit to get all attendance data
//        Call<List<AttendanceData>> call = apiService.getAttendances("Bearer " + token);
//
//        // Execute the request asynchronously
//        call.enqueue(new Callback<List<AttendanceData>>() {
//            @Override
//            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    // Handle successful response
//                    attendanceList.clear();
//                    attendanceList.addAll(response.body());
//                    adapterAttendance.notifyDataSetChanged();
//                } else {
//                    // Handle unsuccessful response
//                    Toast.makeText(requireContext(), "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
//                // Handle network errors
//                Toast.makeText(requireContext(), "Failed to fetch attendance data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return sharedPreferences.getString(TOKEN_KEY, "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the binding when fragment view is destroyed
        binding = null;
    }
}
