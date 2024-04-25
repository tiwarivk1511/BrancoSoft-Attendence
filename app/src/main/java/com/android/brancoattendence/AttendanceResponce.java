package com.android.brancoattendence;

public class AttendanceResponce {
    private int status;
    private boolean success;
    private String message;
    private String id;
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

    public static AttendanceData getData() {
        return data;
    }

    public void setData(AttendanceData data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
