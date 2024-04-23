package com.android.brancoattendence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeeklyAttendanceAdapter extends RecyclerView.Adapter<WeeklyAttendanceAdapter.ViewHolder> {

    private List<Attendance> attendanceList;
    private Context context;

    public WeeklyAttendanceAdapter(Context context, List<Attendance> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_layout_tile, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);

        String date = Calendar.getInstance().getTime().toString();
        //format the date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, MMM d, yyyy");
        String formattedDate = sdf.format(date);

        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("h:mm a");
        String formattedInTime = sdf1.format(attendance.getInTime());
        String formattedOutTime = sdf1.format(attendance.getOutTime());

        holder.dateTextView.setText(formattedDate);
        holder.checkInTimeTextView.setText(formattedInTime);
        holder.checkOutTimeTextView.setText(formattedOutTime);

    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView checkInTimeTextView;
        TextView checkOutTimeTextView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date);
            checkInTimeTextView = itemView.findViewById(R.id.checkInTime);
            checkOutTimeTextView = itemView.findViewById(R.id.checkOutTime);
        }
    }
}

