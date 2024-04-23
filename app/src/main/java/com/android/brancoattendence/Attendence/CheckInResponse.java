package com.android.brancoattendence.Attendence;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CheckInResponse {
    @SerializedName("attd_id")
    private int attendanceId;
    private static String checkInTime;

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public static String getCheckInTime() {
        return checkInTime;
    }

    public static void setCheckInTime(String checkInTime) {
        CheckInResponse.checkInTime = checkInTime;
    }

    public Date getCheckoutTime() {
        try {
            return new Date(Long.parseLong(checkInTime));
        } catch (Exception e) {
            return null;
        }
    }
}
