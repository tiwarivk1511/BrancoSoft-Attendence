package com.android.brancoattendence.RecyclerAdepters;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

public class AttendanceResponse {

    @SerializedName("data")
    private List<AttendanceData> data;

    public List<AttendanceData> getData() {
        return data;
    }

    public void setData(List<AttendanceData> data) {
        this.data = data;
    }

    public Collection<? extends AttendanceData> getAttendanceDataList() {
        return data;
    }
}

