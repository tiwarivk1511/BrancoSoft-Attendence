package com.android.brancoattendence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {

    private List<Date> dates;
    private List<DayOfWeek> dayOfWeeks;
    private DateClickListener listener;
    private Context context;
    private Date selectedDate;

    public DateAdapter(Context context, List<Date> dates, List<DayOfWeek> dayOfWeeks) {
        this.context = context;
        this.dates = dates;
        this.dayOfWeeks = dayOfWeeks;

        // Initially set selected date to current date
        this.selectedDate = getTodayDate();
    }

    private Date getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        //get the current date
        return calendar.getTime();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = dates.get(position);
        DayOfWeek dayOfWeek = dayOfWeeks.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        String dayOfMonth = sdf.format(date);
        String dayOfWeekName = dayOfWeek.toString().substring(0, 3);
        holder.dateTextView.setText(dayOfMonth);
        holder.dayOfWeekTextView.setText(dayOfWeekName);

        Calendar today = Calendar.getInstance();
        today.setTime(getTodayDate());

        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(date);

        // Set the background color of today's element of RecyclerView
        if (currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {

            holder.itemView.setBackgroundResource(R.drawable.selected_date);
            holder.dateTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white)); // Set white text color
            holder.dayOfWeekTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }

        // Highlight selected date
        else if (date.equals(selectedDate)) {
            holder.itemView.setBackgroundResource(R.drawable.selected_date); // Set selected date background
            holder.dateTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white)); // Set white text color
            holder.dayOfWeekTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        } else {
            holder.itemView.setBackgroundResource(R.drawable.unselected_date); // Remove background
            holder.dateTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black)); // Set default text color
            holder.dayOfWeekTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                selectedDate = date; // Update selected date
                notifyDataSetChanged(); // Notify adapter to refresh views
                listener.onDateClick(date);
            }
        });
    }


    @SuppressLint("ResourceType")
    public void highlightTodayAndScroll(RecyclerView recyclerView) {
        int todayPosition = getTodayPosition();
        selectedDate = getTodayDate();
        if (todayPosition != -1) {
            recyclerView.scrollToPosition(todayPosition);
            recyclerView.smoothScrollToPosition(todayPosition);

            ViewHolder todayViewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(todayPosition);
            if (todayViewHolder != null) {
                todayViewHolder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_date)); // Highlight color
            }
        }
    }

    private int getTodayPosition() {
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


    @Override
    public int getItemCount() {
        return dates.size();
    }

    public interface DateClickListener {
        void onDateClick(Date date);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView dayOfWeekTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTxt);
            dayOfWeekTextView = itemView.findViewById(R.id.dayTxt);
        }
    }
}
