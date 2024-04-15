package com.android.brancoattendence;

import com.google.android.libraries.mapsplatform.transportation.consumer.model.Location;

//get the attendance of the day using geolocation
public class Attendance {
    //get the attendance of the day using geolocation
    private String location;
    private String date;
    private String inTime;
    private String outTime;

    public Attendance(String location, String date, String inTime, String outTime) {

        this.location = location;
        this.date = date;
        this.inTime = inTime;
        this.outTime = outTime;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getInTime() {
        return inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setLocation(Location location) {
        this.location = String.valueOf(location);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "location=" + location +
                ", date='" + date + '\'' +
                ", inTime='" + inTime + '\'' +
                ", outTime='" + outTime + '\'' +
                '}';
    }
}
