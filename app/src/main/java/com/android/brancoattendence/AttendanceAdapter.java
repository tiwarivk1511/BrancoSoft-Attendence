package com.android.brancoattendence;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

  static List<Attendance> attendanceList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_layout_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance currentAttendance = attendanceList.get(position);
        holder.date.setText(currentAttendance.getDate());
        holder.inTime.setText(currentAttendance.getInTime());
        holder.outTime.setText(currentAttendance.getOutTime());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAttendance(Attendance attendance) {
        attendanceList.add(attendance);
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView inTime;
        TextView outTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateTxt);
            inTime = itemView.findViewById(R.id.checkInTime);
            outTime = itemView.findViewById(R.id.checkOutTime);
        }
    }
}
