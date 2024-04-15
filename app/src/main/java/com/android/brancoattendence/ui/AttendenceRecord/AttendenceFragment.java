package com.android.brancoattendence.ui.AttendenceRecord;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.brancoattendence.AttendanceAdapter;
import com.android.brancoattendence.databinding.FragmentAttendenceBinding;

import java.util.Calendar;

public class AttendenceFragment extends Fragment {

    private FragmentAttendenceBinding binding;
    private AttendanceAdapter adapter;

    public AttendenceFragment() {
        // Required empty public constructor
    }

    public static AttendenceFragment newInstance() {
        return new AttendenceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = FragmentAttendenceBinding.inflate(inflater, container, false);

        // Set up RecyclerView
       binding.AttendenceRecords.setAdapter(adapter);

        // Set click listener for filter button
        binding.filterTxt.setOnClickListener(v -> showDatePickerDialog());

        return binding.getRoot();
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

