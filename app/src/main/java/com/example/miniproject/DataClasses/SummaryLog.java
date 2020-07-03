package com.example.miniproject.DataClasses;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SummaryLog {

    private String startTime;
    private String endTime;
    private double distance;
    private int overSpeedCount;
    private int drowsinessCount;

    public SummaryLog() {
    }

    public SummaryLog(String startTime, String endTime, double distance, int overSpeedCount, int drowsinessCount) {
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

    public String getDuration() {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        long duration = 0;
        try {
            Date startDate = formatter.parse(startTime);
            Date endDate = formatter.parse(endTime);
            duration = endDate.getTime() - startDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}
