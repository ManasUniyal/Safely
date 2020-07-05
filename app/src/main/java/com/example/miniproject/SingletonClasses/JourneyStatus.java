package com.example.miniproject.SingletonClasses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.miniproject.Adapters.SummaryLogsAdapter;
import com.example.miniproject.DataClasses.SummaryLog;
import com.example.miniproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class JourneyStatus {

    private final static int JOURNEY_NOT_STARTED = 0;
    private final static int JOURNEY_STARTED = 1;
    private static JourneyStatus instance = null;
    private static int journeyState;
    private static double journeyDistance;
    private static String journeyStartTime;
    private static String journeyEndTime;
    private static int overSpeedCount;
    private static int drowsinessCount;
    private Context mContext;
    private long lastDetailedLogEntryNumber;
    private boolean journeyOngoing;

    //TODO: Both to be initialized during splash screen
    private List<SummaryLog> summaryLogList;
    private SummaryLogsAdapter summaryLogsAdapter;

    public static synchronized JourneyStatus getInstance(Context context) {
        if(instance == null) {
            instance = new JourneyStatus(context.getApplicationContext());
        }
        return instance;
    }

    private void startNewJourney() {
        journeyDistance = 0;
        overSpeedCount = 0;
        drowsinessCount = 0;
        journeyStartTime = new String();
        journeyEndTime = new String();
        journeyOngoing = false;
    }

    private JourneyStatus(Context context) {
        this.mContext = context;
        //Should be done when the splash screen loads
        //TODO: Adjust the adapter for Summary Logs such that it can be instantiated at splash screen loading time
//        summaryLogList = DataBaseHelper.getInstance(context).getAllSummaryLogs();
//        summaryLogsAdapter = new SummaryLogsAdapter(summaryLogList, );
        startNewJourney();
        //TODO: Shift it to Splash screen
        lastDetailedLogEntryNumber = DataBaseHelper.getInstance(context).getNumberOfEntriesInSummaryLogs();
    }

    public void toggleJourneyState(Button button, BottomNavigationView bottomNavigationView, Context context) {
        journeyState ^= 1;
        setJourneyStateButton(button, bottomNavigationView, context);
    }

    //TODO: Whenever the journey starts or ends, intent it to the maps activity
    public void setJourneyStateButton(final Button btn, final BottomNavigationView bottomNavigationView, Context context) {
        if(journeyState == JOURNEY_NOT_STARTED) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setText(R.string.start_journey);
                    btn.setBackgroundColor(Color.parseColor("#2196F3"));
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setText(R.string.end_journey);
                    btn.setBackgroundColor(Color.parseColor("#8B0000"));
                    bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }
    }

    public String getDateTime(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        Log.e("Date", date);
        return date;
    }

    public void updateDistance(double segmentDistance) {
        journeyDistance += segmentDistance;
    }

    public void incrementOverSpeedCount() {
        overSpeedCount += 1;
    }

    public void incrementDrowsinessCount() {
        drowsinessCount += 1;
    }

    public List<SummaryLog> getSummaryLogList() {
        return summaryLogList;
    }

    private void incrementSummaryLogEntry() {
        lastDetailedLogEntryNumber++;
    }

    public void updateJourneyLog(Button button, BottomNavigationView bottomNavigationView, Context context) {
        if(journeyState == JOURNEY_NOT_STARTED) {
            bottomNavigationView.setVisibility(View.GONE);
            journeyOngoing = true;
            journeyStartTime = getDateTime(System.currentTimeMillis());
        } else if(journeyState == JOURNEY_STARTED) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            journeyOngoing = false;
            journeyEndTime = getDateTime(System.currentTimeMillis());
            incrementSummaryLogEntry();
            final SummaryLog newJourneySummaryLog = new SummaryLog(journeyStartTime, journeyEndTime, journeyDistance, overSpeedCount, drowsinessCount);
//          summaryLogList.add(newJourneySummaryLog);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    DataBaseHelper.getInstance(mContext).insertSummaryLog(newJourneySummaryLog);
                }
            };
            Thread newThread = new Thread(r);
            newThread.start();

            //TODO: Adjust the adapter
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    summaryLogsAdapter.notifyDataSetChanged();
//                }
//            });

            //TODO: Generate a message for ending journey
            startNewJourney();
        }
        toggleJourneyState(button, bottomNavigationView, context);
//        Log.e("Inserting data", "Detailed logs");
//        DataBaseHelper.getInstance(mContext).insertDetailedLog(new DetailedLog(1,56.324564,45.26454562,20,15, getDateTime(System.currentTimeMillis())));
//        DataBaseHelper.getInstance(mContext).getDetailedLog(1);
    }

    public SummaryLogsAdapter getSummaryLogsAdapter() {
        return summaryLogsAdapter;
    }

    
    public List<SummaryLog> LineChart(int id) {
        return DataBaseHelper.getInstance(mContext).getChartData(id);
    }

    public long getLastDetailedLogEntryNumber() {
        return lastDetailedLogEntryNumber;
    }

    public boolean getJourneyOngoing() {
        return journeyOngoing;
    }
}
