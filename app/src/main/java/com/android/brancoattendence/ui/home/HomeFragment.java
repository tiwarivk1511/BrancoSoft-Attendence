package com.android.brancoattendence.ui.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import com.android.brancoattendence.ApiService;
import com.android.brancoattendence.AttendanceData;
import com.android.brancoattendence.CheckInOutTimeManager;
import com.android.brancoattendence.DateAdapter;
import com.android.brancoattendence.HostURL;
import com.android.brancoattendence.R;
import com.android.brancoattendence.databinding.FragmentHomeBinding;
import com.android.brancoattendence.ui.AttendenceRecord.AttendanceAdapter;
import com.android.brancoattendence.ui.profile.UserDataResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements DateAdapter.DateClickListener {

    private FragmentHomeBinding binding;
    private static final String LOCATION_WORK_TAG = "location_work";
    private static final String LAST_CHECK_IN_TIME_KEY = "last_check_in_time";
    private static final String LAST_CHECK_OUT_TIME_KEY = "last_check_out_time";
    private AttendanceAdapter adapterAttendance;
    private List<AttendanceData> attendanceList;

    private String Year;
    private String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        // Load dates for current month
        RecyclerView recyclerViewDates = binding.ViewDates;
        recyclerViewDates.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Load dates for current month
        List<Date> dates = getDatesForCurrentMonth();
        List<DayOfWeek> dayOfWeeks = getDaysOfWeekForDates(dates);
        DateAdapter adapter = new DateAdapter(requireContext(), dates, dayOfWeeks);
        recyclerViewDates.setAdapter(adapter);
        adapter.highlightTodayAndScroll(recyclerViewDates);

        // Initialize the adapterAttendance here
        adapterAttendance = new AttendanceAdapter(attendanceList);
//
//        CheckInOutTimeManager checkInOutTimeManager = new CheckInOutTimeManager();
//
//        binding.checkInTime.setText(checkInOutTimeManager.getCheckInTime());
//
//        // Retrieve and display check-out time
//        binding.checkOutTime.setText(checkInOutTimeManager.getCheckOutTime());


        // Load weekly attendance records
        RecyclerView recyclerViewAttendance = binding.WeeklyAttendanceRecords;
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Fetch user profile data for current user's name
        fetchUserData();
        fetchDataFromAPI();
        fetchTodayAttendance ();

        // Navigate to view all attendance
        binding.viewAllAttendeceTxt.setOnClickListener(
                v -> Navigation.findNavController(requireView()).navigate(R.id.nav_attendance)
        );

        highlightTodayAndScroll(binding.ViewDates, dates);

        return root;
    }

//    private String getCheckIn() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
//        String time = preferences.getString(LAST_CHECK_IN_TIME_KEY, "");
//
//        return time.isEmpty() ? "--:--" : time;
//    }
//
//    private String getCheckOut() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
//        return preferences.getString(LAST_CHECK_OUT_TIME_KEY, "");
//    }

    //create a method to fetch the data of today's attendance from private List<AttendanceData> attendanceList by searching the date in the list
    private void fetchTodayAttendance() {
        if (attendanceList == null) {
            return;
        } else {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String firstCheckIn = null;
            String lastCheckOut = null;

            for (AttendanceData attendance : attendanceList) {
                if (attendance.getDate().equals(today)) {
                    // Check if firstCheckIn is null, if so, set it to the current checkIn
                    if (firstCheckIn == null) {
                        firstCheckIn = attendance.getCheckIn();
                    }

                    // Always update lastCheckOut with the current checkOut
                    lastCheckOut = attendance.getCheckOut();
                }
            }

            // Update UI with firstCheckIn and lastCheckOut
            if (firstCheckIn != null) {
                binding.checkInTime.setText(firstCheckIn);
            }
            if (lastCheckOut != null) {
                binding.checkOutTime.setText(lastCheckOut);
            }
        }
    }


    // Fetch user profile data
    private void fetchUserData() {
        String token = retrieveTokenFromSharedPreferences();
        if (token == null) {
            Toast.makeText(getContext(), "Token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<UserDataResponse> call = apiService.getUserData("Bearer " + token);

        call.enqueue(new Callback<UserDataResponse>() {
            @Override
            public void onResponse(Call<UserDataResponse> call, Response<UserDataResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
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

    // Fetch data from API
    private void fetchDataFromAPI() {
        String token = retrieveTokenFromSharedPreferences();
        if (token == null) {
            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HostURL.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<List<AttendanceData>> call = apiService.getAttendances("Bearer " + token);

        call.enqueue(new Callback<List<AttendanceData>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    attendanceList = response.body();
                    adapterAttendance = new AttendanceAdapter(attendanceList);
                    binding.WeeklyAttendanceRecords.setAdapter(adapterAttendance);
                    adapterAttendance.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
                if (isAdded() && requireContext() != null) {
                    Toast.makeText(requireContext(), "Failed to fetch attendance data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private String retrieveTokenFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return preferences.getString("token", null);
    }

    private void updateUI(UserDataResponse userData) {
        String middleName = userData.getMiddleName() != null ? " " + userData.getMiddleName() : "";
        String fullName = userData.getFirstName() + middleName + " " + userData.getLastName();
        binding.greeting.setText("Welcome \uD83D\uDC4B! " + fullName);
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
            recyclerView.post(() -> recyclerView.smoothScrollToPosition(todayPosition));

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        DateAdapter.ViewHolder todayViewHolder = (DateAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(todayPosition);
                        if (todayViewHolder != null) {
                            todayViewHolder.itemView.setBackgroundColor(requireContext().getColor(R.color.bg_color)); // Highlight color
                        }
                        recyclerView.removeOnScrollListener(this); // Remove the listener after highlighting the date
                    }
                }
            });
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
    public void onDestroyView() {
        super.onDestroyView();
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(LOCATION_WORK_TAG);
        binding = null;
    }

    @Override
    public void onDateClick(Date date) {
        // Handle date click event here
    }
}
