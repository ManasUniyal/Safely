package com.example.miniproject.DataClasses;

public class DetailedLog {
    private long summaryId;
    private double latitude;
    private double longitude;
    private int speed;
    private int speedLimit;
    private String dateTime;

    public DetailedLog() {
    }

    public DetailedLog(long summaryId, double latitude, double longitude, int speed, int speedLimit, String dateTime) {
        this.summaryId = summaryId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.speedLimit = speedLimit;
        this.dateTime = dateTime;
    }

    public long getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(long summaryId) {
        this.summaryId = summaryId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
