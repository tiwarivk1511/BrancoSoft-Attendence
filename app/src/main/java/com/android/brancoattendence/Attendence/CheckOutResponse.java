package com.android.brancoattendence.Attendence;

import java.util.Date;

public class CheckOutResponse {

    public String message;
    public String Token;
    public String CheckOutTime;
    public int attendanceId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getCheckOutTime() {
        return CheckOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        CheckOutTime = checkOutTime;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Date getCheckoutTime() {
        Date date = null;
        try {
            date = new Date(Long.parseLong(CheckOutTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
}
