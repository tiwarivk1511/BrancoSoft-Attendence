package com.android.brancoattendence.ui.AttendenceRecord;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.Attendence.CheckInResponse;
import com.android.brancoattendence.Attendence.CheckOutResponse;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.R;
import com.android.brancoattendence.RecyclerAdepters.AllAttendanceAdapter;
import com.android.brancoattendence.RecyclerAdepters.AttendanceData;
import com.android.brancoattendence.databinding.FragmentAttendenceBinding;
import com.android.brancoattendence.ui.profile.UserDataResponse;

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
    private String checkInTime = CheckInResponse.getCheckInTime();
    private String checkOutTime= CheckOutResponse.getCheckOutTime();
    private AllAttendanceAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);

        // Set layout manager for the RecyclerView
        binding.AttendenceRecords.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapter
        adapter = new AllAttendanceAdapter(requireContext(),attendanceList);
        binding.AttendenceRecords.setAdapter(adapter);

        // Fetch attendance data on fragment creation
        fetchAttendanceData();

        // Set click listener for filter button
        binding.filterTxt.setOnClickListener(v -> showDatePickerDialog());

        return binding.getRoot();
    }

    private void fetchAttendanceData() {
        String token = getUserToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<List<AttendanceData>> call = apiService.getAttendances(token);
        call.enqueue(new Callback<List<AttendanceData>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
                if (response.isSuccessful()) {
                    List<AttendanceData> allAttendance = response.body();
                    if (allAttendance != null) {
                        // Filter attendance records for the current user
                        List<AttendanceData> currentUserAttendance = new ArrayList<>();
                        int currentUserId = Integer.parseInt(UserDataResponse.getEmployeeId()); // Assuming you have a method to get the current user's ID
                        for (AttendanceData data : allAttendance) {
                            if (AttendanceData.getEmpId() == currentUserId) {
                                currentUserAttendance.add(data);
                            }
                        }
                        // Update the adapter with attendance records for the current user
                        attendanceList.clear();
                        attendanceList.addAll(currentUserAttendance);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), "No attendance records found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch attendance records", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error. Please try again later.", Toast.LENGTH_SHORT).show();
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
