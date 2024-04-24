package com.android.brancoattendence.ui.AttendenceRecord;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.RecyclerAdepters.AllAttendanceAdapter;
import com.android.brancoattendence.RecyclerAdepters.AttendanceData;
import com.android.brancoattendence.databinding.FragmentAttendenceBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private String date;


    private AllAttendanceAdapter adapter;

    RecyclerView recyclerView;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);
        recyclerView = binding.AttendenceRecords;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AllAttendanceAdapter(requireContext());
//        recyclerView.setAdapter(adapter);

        // Fetch attendance data on fragment creation
        fetchDataFromAPI();

        // Set click listener for filter button
        binding.filterTxt.setOnClickListener(v -> showDatePickerDialog());

        return binding.getRoot();
    }

    private void fetchDataFromAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        String token =getUserToken(); // Replace with your actual token
        Call<List<AttendanceData>> call = apiService.getAttendances("Bearer " + token);

        call.enqueue(new Callback<List<AttendanceData>>() {
            @Override
            public void onResponse(@NonNull Call<List<AttendanceData>> call, @NonNull Response<List<AttendanceData>> response) {
                if (response.isSuccessful()) {
                    List<AttendanceData> attendanceList = response.body();

                    System.out.println("API Response:"+ response.body());
                    if (attendanceList != null) {
                        adapter.setData(attendanceList);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AttendanceData>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getUserToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return sharedPreferences.getString(TOKEN_KEY, "");
    }

    private void showDatePickerDialog() {
        // Get the current year, month, and day
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set the current date as default
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (startDatePicker, startYear, startMonth, startDayOfMonth) -> {
            Calendar selectedStartDate = Calendar.getInstance();
            selectedStartDate.set(startYear, startMonth, startDayOfMonth);

            // Format selected date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = sdf.format(selectedStartDate.getTime());

            // Filter attendance records based on the selected date
            filterAttendanceByDate(startDate);
        }, year, month, dayOfMonth);

        // Set maximum date for the date picker (current date)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }



    private void filterAttendanceByDate(String date) {
        // Filter attendance records based on the selected date
        List<AttendanceData> filteredList = new ArrayList<>();
        for (AttendanceData data : attendanceList) {
            if (data.getDate().equals(date)) {
                filteredList.add(data);
            }
        }
        // Update the RecyclerView with filtered data
        adapter.setData(filteredList);
    }

    private void filterAttendanceByDateRange(String startDate, String endDate) {
        // Filter attendance records based on the selected date range
        List<AttendanceData> filteredList = new ArrayList<>();
        for (AttendanceData data : attendanceList) {
            if (isDateInRange(data.getDate(), startDate, endDate)) {
                filteredList.add(data);
            }
        }
        // Update the RecyclerView with filtered data
        adapter.setData(filteredList);
    }

    private boolean isDateInRange(String date, String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currentDate = sdf.parse(date);
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            return currentDate.after(start) && currentDate.before(end);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the binding when fragment view is destroyed
        binding = null;
    }
}
