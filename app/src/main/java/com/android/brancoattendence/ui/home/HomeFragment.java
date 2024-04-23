package com.android.brancoattendence.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.Attendance;
import com.android.brancoattendence.AttendanceAdapter;
import com.android.brancoattendence.DateAdapter;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.LocationWorker;
import com.android.brancoattendence.R;
import com.android.brancoattendence.WeeklyAttendanceAdapter;
import com.android.brancoattendence.databinding.FragmentHomeBinding;
import com.android.brancoattendence.ui.profile.UserDataResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements DateAdapter.DateClickListener {

    private FragmentHomeBinding binding;
    private static final String LOCATION_WORK_TAG = "location_work";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Load dates for current month
        RecyclerView recyclerViewDates = binding.ViewDates;
        recyclerViewDates.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Load dates for current month
        List<Date> dates = getDatesForCurrentMonth();
        assert dates != null;
        List<DayOfWeek> dayOfWeeks = getDaysOfWeekForDates(dates);
        DateAdapter adapter = new DateAdapter(requireContext(), dates, dayOfWeeks, this);
        recyclerViewDates.setAdapter(adapter);
        adapter.highlightTodayAndScroll(recyclerViewDates);


        // Load weekly attendance records
        RecyclerView recyclerViewAttendance = binding.WeeklyAttendanceRecords;
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load weekly attendance records
        WeeklyAttendanceAdapter adapter1 = new WeeklyAttendanceAdapter(requireContext(), new ArrayList<>());
        recyclerViewAttendance.setAdapter(adapter1);

        // Fetch user profile data for current user's name
        fetchUserData();

        // Schedule the periodic work to fetch location and update address
        PeriodicWorkRequest locationWorkRequest =
                new PeriodicWorkRequest.Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                        .addTag(LOCATION_WORK_TAG)
                        .build();
        WorkManager.getInstance(requireContext())
                .enqueueUniquePeriodicWork(LOCATION_WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, locationWorkRequest);


        // Load address from SharedPreferences and update UI
        updateAddressUI();

        //navigate to view all attendance
        binding.viewAllAttendeceTxt.setOnClickListener(
                v -> {
                    //move to attendance fragment
                    Navigation.findNavController(requireView()).navigate(R.id.nav_attendance);
                }
        );

        return root;
    }

    // Fetch user profile data
    private void fetchUserData() {
        // Retrieve token from SharedPreferences
        String token = retrieveTokenFromSharedPreferences();

        System.out.println("Token: " + token);
        // Check if token is null
        if (token == null) {
            // Handle null token
            Toast.makeText(getContext(), "Token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
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
                    Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserDataResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return preferences.getString("token", null);
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
        binding.greeting.setText("Welcome \uD83D\uDC4B!" +
                "\n"+ FullName);
    }
    private List<DayOfWeek> getDaysOfWeekForDates(List<Date> dates) {
        List<DayOfWeek> dayOfWeeks = new ArrayList<>();
        for (Date date : dates) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                dayOfWeeks.add(date.toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek());
            }
        }
        return dayOfWeeks;
    }


    @SuppressLint("ResourceAsColor")
    private void highlightTodayAndScroll(RecyclerView recyclerView, List<Date> dates) {
        int todayPosition = getTodayPosition(dates);
        if (todayPosition != -1) {
            recyclerView.scrollToPosition(todayPosition);
            recyclerView.smoothScrollToPosition(todayPosition);

            DateAdapter.ViewHolder todayViewHolder = (DateAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(todayPosition);
            if (todayViewHolder != null) {
                todayViewHolder.itemView.setBackgroundColor(R.color.bg_color); // Highlight color
            }
        }
    }

    private int getTodayPosition(List<Date> dates) {
        Calendar calToday = Calendar.getInstance();
        for (int i = 0; i < dates.size(); i++) {
            Calendar calDate = Calendar.getInstance();
            calDate.setTime(dates.get(i));
            if (calDate.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                    calDate.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) &&
                    calDate.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH)) {
                return i;
            }
        }
        return -1;
    }

    private void updateAddressUI() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String address = sharedPreferences.getString("address", "");
        if (!address.isEmpty()) {
            TextView textViewAddress = binding.currentAddress;
            textViewAddress.setText(address);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(LOCATION_WORK_TAG);
        binding = null;
    }

    private List<Date> getDatesForCurrentMonth() {
        List<Date> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            dates.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return !dates.isEmpty() ? dates : null;
    }


    @Override
    public void onDateClick(Date date) {

    }
}