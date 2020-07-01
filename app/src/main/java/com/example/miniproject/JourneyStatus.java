package com.example.miniproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.Locale;

public class JourneyStatus {

    private final static int JOURNEY_NOT_STARTED = 0;
    private final static int JOURNEY_STARTED = 1;
    private static JourneyStatus instance = null;
    private static int journeyState;
    private static double journeyDistance;
    private static int journeyDuration;
    private static String journeyStartTime;
    private static String journeyEndTime;
    private static int overSpeedCount;
    private static int drowsinessCount;

    public static synchronized JourneyStatus getInstance() {
        if(instance == null) {
            instance = new JourneyStatus();
        }
        return instance;
    }

    private void resetJourneyStatus() {
        journeyState = 0;
        journeyDistance = 0;
        journeyDuration = 0;
        overSpeedCount = 0;
        drowsinessCount = 0;
        journeyStartTime = new String();
        journeyEndTime = new String();
    }

    private JourneyStatus() {
        resetJourneyStatus();
    }

    public int getJourneyState() {
        return journeyState;
    }

    public void toggleJourneyState() {
        journeyState ^= 1;
    }

    //TODO: Whenever the journey starts or ends, intend it to the maps activity
    public void setJourneyStateButtonView(View view, Context context) {
        final Button btn = (Button) view;
        if(getJourneyState() == JOURNEY_NOT_STARTED) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setText(R.string.start_journey);
                    btn.setBackgroundColor(Color.GREEN);
                }
            });
        } else {
            journeyDistance = 0;
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setText(R.string.end_journey);
                    btn.setBackgroundColor(Color.RED);
                }
            });
        }
    }

    //        DataBaseHelper.getInstance(context).insertOuterTable(new SummaryLogs(getDate(System.currentTimeMillis()), getDate(System.currentTimeMillis()+10000), 10.2, 1, 0));
//        SummaryLogs obj = ((List<SummaryLogs>) DataBaseHelper.getInstance(context).getAllOuterTableEntries()).get(0);
//        Log.e("Start time", obj.getStartTime());
//        Log.e("End time", obj.getEndTime());
//        Log.e("Distance", String.valueOf(obj.getDistance()));
//        Log.e("Over speed count", String.valueOf(obj.getOverSpeedCount()));
//        Log.e("Drowsiness count", String.valueOf(obj.getDrowsinessCount()));

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        Log.e("Date", date);
        return date;
    }

}
