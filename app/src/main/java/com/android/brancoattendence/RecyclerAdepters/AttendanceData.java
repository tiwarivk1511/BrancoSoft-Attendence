package com.android.brancoattendence.RecyclerAdepters;

public class AttendanceData {
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private static int EmpId;

    // Constructor
    public AttendanceData(String date, String checkInTime, String checkOutTime) {
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }



    // Getter and setter methods
    public static int getEmpId() {
        return EmpId;
    }

    public void setEmpId(String empId) {
        EmpId = Integer.parseInt(empId);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Getter and setter methods for check-in time
    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    // Getter and setter methods for check-out time
    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }


}
