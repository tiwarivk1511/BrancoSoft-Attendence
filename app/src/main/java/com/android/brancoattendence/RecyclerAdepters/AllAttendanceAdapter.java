package com.android.brancoattendence.RecyclerAdepters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.brancoattendence.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllAttendanceAdapter extends RecyclerView.Adapter<AllAttendanceAdapter.ViewHolder> {

    private List<AttendanceData> attendanceList;
    private Context context;

    public AllAttendanceAdapter(Context context) {
        this.context = context;
        this.attendanceList = new ArrayList<>(); // Initialize the list
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_layout_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceData attendanceData = attendanceList.get(position);
        if (attendanceData != null && attendanceData.getDate() != null) { // Check for non-null date object
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

            String formattedDate = "";
            try {
                formattedDate = dateFormat.format(attendanceData.getDate());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                formattedDate = "Invalid Date"; // Set a default value in case of an error
            }

            // Format check-in and check-out times
            String formattedCheckInTime = timeFormat.format(attendanceData.getCheckInTime());
            String formattedCheckOutTime = timeFormat.format(attendanceData.getCheckOutTime());

            // Set formatted values to TextViews
            holder.date.setText(formattedDate);
            holder.checkinTime.setText(formattedCheckInTime);
            holder.checkOutTime.setText(formattedCheckOutTime);
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void setData(List<AttendanceData> filteredList) {
        this.attendanceList.clear(); // Clear existing data
        if (filteredList != null) {
            this.attendanceList.addAll(filteredList); // Add new data
        }        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date;
        private TextView checkinTime;
        private TextView checkOutTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            checkinTime = itemView.findViewById(R.id.checkInTime);
            checkOutTime = itemView.findViewById(R.id.checkOutTime);
        }
    }
}
