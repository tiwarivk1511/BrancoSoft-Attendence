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

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.RecyclerAdepters.AllAttendanceAdapter;
import com.android.brancoattendence.RecyclerAdepters.AttendanceData;
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



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);


        // Set layout manager for the RecyclerView
        try{
            binding.AttendenceRecords.setLayoutManager(new LinearLayoutManager(getContext()));
            attendanceList.add(fetchAllAttendance());

            AllAttendanceAdapter adapter = new AllAttendanceAdapter(requireContext(), (ArrayList<AttendanceData>) attendanceList);
            System.out.println("attendanceList: " + attendanceList);
            binding.AttendenceRecords.setAdapter(adapter);
        }catch (Exception e){
            e.printStackTrace();
        }


        // Fetch attendance data on activity creation
//        fetchAllAttendance();


        // Set click listener for filter button
        binding.filterTxt.setOnClickListener(v -> showDatePickerDialog());

        return binding.getRoot();
    }

    public AttendanceData fetchAllAttendance() {
        String token = getUserToken();
        AttendanceData attendanceData = null;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getAttendances("Bearer " + token).enqueue(new Callback<List<AttendanceData>>() {
            @Override
            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
                if (response.isSuccessful()) {
                    attendanceList.addAll(response.body()); // Add new data
                    for (int i = 0; i < attendanceList.size(); i++) {

                        attendanceData.add(attendanceList.get(i));
                    }

                } else {
                    // Handle unsuccessful response
                    Toast.makeText(requireContext(), "Something went wrong, Please try again later.", Toast.LENGTH_SHORT).show();
                }
                
            }

            @Override
            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
                // Handle failure
                Toast.makeText(requireContext(), "Error: " +t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return attendanceData;
    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return sharedPreferences.getString(TOKEN_KEY, "");
    }

    private void showDatePickerDialog() {
        // Get the current year and month
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // Create a DatePickerDialog and set the current year and month as default
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, dayOfMonth) -> {
            // Perform filtering based on the selected year and month
        }, year, month, Calendar.DAY_OF_MONTH);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Release the binding when fragment view is destroyed
        binding = null;
    }
}
