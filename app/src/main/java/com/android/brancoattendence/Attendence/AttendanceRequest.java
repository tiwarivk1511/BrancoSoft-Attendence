package com.android.brancoattendence.Attendence;

public class AttendanceRequest {
    public String token;
    public static String checkInTime;
    public AttendanceRequest(String token, String checkInTime) {
        this.token = token;
        this.checkInTime = checkInTime;
    }

}
