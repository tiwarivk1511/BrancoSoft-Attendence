package com.android.brancoattendence.ui.AttendenceRecord;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

    private static final String BASE_URL = "http://192.168.1.11:8000/api/";
    private static String TOKEN = null;

    // Retrofit instance
    private Retrofit retrofit;
    // API service interface
    private ApiService apiService;
    private static final String TOKEN_KEY = "token";
    private FragmentAttendenceBinding binding;
    private List<AttendanceData> attendanceList = new ArrayList<>();
    private AttendanceAdapter adapterAttendance;
    private String Year;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private String SelectedMonth;
    private Context context;
    private Call<List<AttendanceData>> call;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        AutoCompleteTextView autoCompleteTextView = binding.AutoCompleteTextViewMonths;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_items, months);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String monthItem = parent.getItemAtPosition(position).toString();
            Toast.makeText(requireContext(), "Selected: " + monthItem, Toast.LENGTH_SHORT).show();
            SelectedMonth = monthItem;
            filterDataByMonthAndYear();
        });

        TOKEN = getUserToken();

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
            Toast.makeText(requireContext(), "Selected: " + yearItem, Toast.LENGTH_SHORT).show();
            Year = yearItem;
            filterDataByMonthAndYear();
        });


        fetchDataFromAPI();


        RecyclerView recyclerView = binding.AttendenceRecords;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterAttendance = new AttendanceAdapter(attendanceList);
        recyclerView.setAdapter(adapterAttendance);



        return view;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the binding when fragment view is destroyed
        binding = null;
        // Cancel the Retrofit call if it's not null
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    private void fetchDataFromAPI() {
        String token = getUserToken();
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        call = apiService.getAttendances("Bearer " + token);

        call.enqueue(new Callback<List<AttendanceData>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
                System.out.println("asddfsd: " + response.body().toString());
                if (response.isSuccessful() && response.body() != null) {
                    attendanceList = response.body();
                    adapterAttendance = new AttendanceAdapter(attendanceList);
                    binding.AttendenceRecords.setAdapter(adapterAttendance);
                    adapterAttendance.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
                // Check if the fragment is attached to a context
                if (isAdded() && requireContext() != null) {
                    Toast.makeText(requireContext(), "Failed to fetch attendance data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    // Log the error or handle it accordingly
                Log.e(TAG, "Failed to fetch attendance data", t);
                }
            }

        });
    }

    private void filterDataByMonthAndYear() {
        List<AttendanceData> filteredList = new ArrayList<>();
        for (AttendanceData data : attendanceList) {
            if (matchesSelectedMonthAndYear(data)) {
                filteredList.add(data);
            }
        }
        RecyclerView recyclerView = binding.AttendenceRecords;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterAttendance = new AttendanceAdapter(filteredList);
        recyclerView.setAdapter(adapterAttendance); // Update adapter data
    }

    private boolean matchesSelectedMonthAndYear(AttendanceData data) {
        if (SelectedMonth == null || Year == null) {
            return true; // No filtering applied if month or year is not selected
        }

        // Parse the month and year from the data
        String[] parts = data.getDate().split("-");
        if (parts.length != 3) {
            return false; // Invalid date format
        }
        int dataMonth = Integer.parseInt(parts[1]);
        int dataYear = Integer.parseInt(parts[0]);

        // Compare with selected month and year
        return months[dataMonth - 1].equalsIgnoreCase(SelectedMonth) && Year.equals(String.valueOf(dataYear));
    }




    private String getUserToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(TOKEN_KEY, "");
    }
}