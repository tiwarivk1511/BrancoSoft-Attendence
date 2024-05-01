package com.android.brancoattendence;

import com.google.gson.annotations.SerializedName;

public class AttendanceData {

    @SerializedName("attd_id")
    private int attendanceId;

    @SerializedName("emp_id")
    private int employeeId;

    @SerializedName("date")
    private String date;

    @SerializedName("check_in")
    private  String checkIn;

    @SerializedName("check_out")
    private static String checkOut;

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public  String getCheckIn() {
        return checkIn;
    }

    public  void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public  String getCheckOut() {
        return checkOut;
    }

    public  void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }
}
