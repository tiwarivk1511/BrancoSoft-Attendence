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
import java.util.List;

public class AllAttendanceAdapter extends RecyclerView.Adapter<AllAttendanceAdapter.ViewHolder> {

    private List<AttendanceData> attendanceList = new ArrayList<>();
    private Context contex;

    public AllAttendanceAdapter(Context context, ArrayList<AttendanceData> attendanceList) {
        this.contex = context; // Assign context parameter to the context variable
        this.attendanceList = attendanceList;

    }


//    public void fetchAllAttendance() {
//        String token = getUserToken();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(HostURL.getBaseUrl())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ApiService apiService = retrofit.create(ApiService.class);
//
//        apiService.getAttendances("Bearer " + token).enqueue(new Callback<List<AttendanceData>>() {
//            @Override
//            public void onResponse(Call<List<AttendanceData>> call, Response<List<AttendanceData>> response) {
//                if (response.isSuccessful()) {
//                    attendanceList.clear(); // Clear existing data
//                    attendanceList.addAll(response.body()); // Add new data
//                } else {
//                    // Handle unsuccessful response
//                    Toast.makeText(contex.getApplicationContext(), "Something went wrong, Please try again later.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<AttendanceData>> call, Throwable t) {
//                // Handle failure
//                Toast.makeText(contex.getApplicationContext(), "Error: " +t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private String getUserToken() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(contex.getApplicationContext());
//        return sharedPreferences.getString(TOKEN_KEY, "");
//    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_layout_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceData attendanceData = attendanceList.get(position);
        if (attendanceData != null) {

            //formate DD/MM/YYYY fromate of date
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            String fromattedDate = inputFormat.format(attendanceData.getDate());

            SimpleDateFormat inTime = new SimpleDateFormat("HH:mm:ss a");
            String fromattedInTime = inTime.format(attendanceData.getCheckInTime());

            SimpleDateFormat outTime = new SimpleDateFormat("HH:mm:ss a");
            String fromattedOutTime = outTime.format(attendanceData.getCheckOutTime());

            holder.date.setText(fromattedDate);
            holder.checkinTime.setText(fromattedInTime);
            holder.checkOutTime.setText(fromattedOutTime);
        }

    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
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
