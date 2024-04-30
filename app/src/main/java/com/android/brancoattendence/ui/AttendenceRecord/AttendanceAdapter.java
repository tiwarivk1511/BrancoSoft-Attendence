package com.android.brancoattendence.ui.AttendenceRecord;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.brancoattendence.AttendanceData;
import com.android.brancoattendence.R;

import java.sql.Time;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
    private List<AttendanceData> attendanceList;

    // Constructor to initialize the list of attendance data
    public AttendanceAdapter(List<AttendanceData> attendanceList) {
        this.attendanceList = attendanceList;
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
        // Get the AttendanceData object at the specified position
        AttendanceData attendanceData = attendanceList.get(position);

        Log.d("asddfsd", ""+ attendanceData.getCheckIn());

        // Set the date to the date TextView
        holder.dateTextView.setText(attendanceData.getDate());

        // Set the check-in time to the check-in TextView
        if (attendanceData.getCheckIn() != null ) {
            holder.checkInTextView.setText("In: " + attendanceData.getCheckIn());
        } else {
            holder.checkInTextView.setText("In: --:--");
        }

        // Set the check-out time to the check-out TextView
        if (attendanceData.getCheckOut() != null) {
            holder.checkOutTextView.setText("Out: " + attendanceData.getCheckOut());
        } else {
            holder.checkOutTextView.setText("Out: --:--");
        }
    }


    @Override
    public int getItemCount() {
        // Return the size of the attendance list
        return attendanceList.size();
    }
}
