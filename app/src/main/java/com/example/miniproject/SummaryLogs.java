package com.example.miniproject;

public class SummaryLogs {

    private String startTime;
    private String endTime;
    private double distance;
    private int overSpeedCount;
    private int drowsinessCount;
    private long duration;

    //TODO: Think about handling this for duration of journey while using recycler view
//    public int getDuration() {
//        return duration;
//    }

    public SummaryLogs() {
    }

    public SummaryLogs(String startTime, String endTime, double distance, int overSpeedCount, int drowsinessCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.overSpeedCount = overSpeedCount;
        this.drowsinessCount = drowsinessCount;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getOverSpeedCount() {
        return overSpeedCount;
    }

    public void setOverSpeedCount(int overSpeedCount) {
        this.overSpeedCount = overSpeedCount;
    }

    public int getDrowsinessCount() {
        return drowsinessCount;
    }

    public void setDrowsinessCount(int drowsinessCount) {
        this.drowsinessCount = drowsinessCount;
    }

}
