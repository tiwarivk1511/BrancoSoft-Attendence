package com.android.brancoattendence;

public class AttendanceResponse {
    private int status;
    private boolean success;
    private String Date;
    private  String InTime;

    private String OutTime;
    private String message;
    private static String id;
    private static AttendanceData data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getInTime(String currentTime) {
        return InTime;
    }

    public void setInTime(String inTime) {
        InTime = inTime;
    }

    public String getOutTime() {
        return OutTime;
    }

    public void setOutTime(String outTime) {
        OutTime = outTime;
    }

    public static AttendanceData getData() {
        return data;
    }

    public void setData(AttendanceData data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }
    public static String getId() {
        return id;
    }
}
