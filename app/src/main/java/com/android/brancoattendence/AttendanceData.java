package com.android.brancoattendence;

import com.google.gson.annotations.SerializedName;

public class AttendanceData {

    @SerializedName("attd_id")
    private static int attendanceId;

    @SerializedName("emp_id")
    private int employeeId;

    @SerializedName("date")
    private String date;

    @SerializedName("check_in")
    private static String checkIn;

    @SerializedName("check_out")
    private static String checkOut;

    @SerializedName("location")
    private String location;




    // Add getters and setters
    public static int getAttendanceId() {
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

    public static String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
       this.checkIn = checkIn;
    }

    public static String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
