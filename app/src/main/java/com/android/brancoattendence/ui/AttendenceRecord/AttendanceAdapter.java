package com.android.brancoattendence.ui.AttendenceRecord;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.brancoattendence.AttendanceData;
import com.android.brancoattendence.R;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
    private List<AttendanceData> attendanceList;

    // Constructor to initialize the list of attendance data
    public AttendanceAdapter(List<AttendanceData> attendanceList) {
        this.attendanceList = attendanceList;
    }

    // Define a method to update the list of attendance data
    public void setData(List<AttendanceData> newData) {
        attendanceList = newData;
        notifyDataSetChanged(); // Notify RecyclerView that the data has changed
    }

    // ViewHolder class to hold the views of each item
    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView checkInTextView;
        TextView checkOutTextView;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date);
            checkInTextView = itemView.findViewById(R.id.checkInTime);
            checkOutTextView = itemView.findViewById(R.id.checkOutTime);
        }
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_layout_tile, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        // Bind the data to the views
        AttendanceData attendanceData = attendanceList.get(position);
        holder.dateTextView.setText(String.valueOf(attendanceData.getDate()));
        holder.checkInTextView.setText(attendanceData.getCheckIn());
        holder.checkOutTextView.setText(attendanceData.getCheckOut());
    }

    @Override
    public int getItemCount() {
        // Return the size of the attendance list
        return attendanceList.size();
    }
}