package com.android.brancoattendence.RecyclerAdepters;

public class AttendanceData {

    private String date;
    private String checkInTime;
    private String checkOutTime;

    public AttendanceData(String date, String checkInTime, String checkOutTime) {
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public void add(AttendanceData attendanceData) {
        this.date = attendanceData.date;
        this.checkInTime = attendanceData.checkInTime;
        this.checkOutTime = attendanceData.checkOutTime;
    }
}
