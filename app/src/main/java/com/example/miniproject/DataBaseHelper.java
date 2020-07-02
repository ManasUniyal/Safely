package com.example.miniproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "journeyTracker";
    private static final int DATABASE_VERSION = 1;
    private Context applicationContext;
    private static DataBaseHelper dataBaseHelperInstance = null;

    //TODO: Use better naming
    private static final String TABLE_SUMMARY_LOGS = "summaryLogs";
    private static final String TABLE_DETAILED_LOGS = "detailedLogs";

    //Column common to both tables
    private static final String KEY_ID = "id";

    //Columns for SUMMARY TABLE
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_OVER_SPEED_COUNT = "over_speed_count";
    private static final String KEY_DROWSINESS_COUNT = "drowsiness_count";

    //Columns for DETAILED TABLE
    private static final String KEY_SUMMARY_ID = "summary_id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_SPEED_LIMIT = "speed_limit";
    private static final String KEY_DATE_TIME = "dateTime";

    public static DataBaseHelper getInstance(Context context) {
        if(dataBaseHelperInstance == null) {
            dataBaseHelperInstance = new DataBaseHelper(context.getApplicationContext());
        }
        return dataBaseHelperInstance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        applicationContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUMMARY_LOGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_START_TIME + " DATETIME, "
                + KEY_END_TIME + " DATETIME, "
                + KEY_DISTANCE + " REAL, "
                + KEY_OVER_SPEED_COUNT + " INTEGER, "
                + KEY_DROWSINESS_COUNT + " INTEGER"
                + ")"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DETAILED_LOGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_SUMMARY_ID + " INTEGER, "
                + KEY_LATITUDE + " REAL, "
                + KEY_LONGITUDE + " REAL, "
                + KEY_SPEED + " INTEGER, "
                + KEY_SPEED_LIMIT + " INTEGER, "
                + KEY_DATE_TIME + " DATETIME"
                + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILED_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUMMARY_LOGS);
    }

    public void insertSummaryLog(SummaryLog summaryLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_START_TIME, summaryLog.getStartTime());
        values.put(KEY_END_TIME, summaryLog.getEndTime());
        values.put(KEY_DISTANCE, summaryLog.getDistance());
        values.put(KEY_OVER_SPEED_COUNT, summaryLog.getOverSpeedCount());
        values.put(KEY_DROWSINESS_COUNT, summaryLog.getDrowsinessCount());
        long val = db.insert(TABLE_SUMMARY_LOGS, null, values);
        Log.e("Inserted "+TABLE_SUMMARY_LOGS,String.valueOf(val));
    }

    public void insertDetailedLog(DetailedLog detailedLog) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SUMMARY_ID, detailedLog.getSummaryId());
        values.put(KEY_LATITUDE, detailedLog.getLatitude());
        values.put(KEY_LONGITUDE, detailedLog.getLongitude());
        values.put(KEY_SPEED, detailedLog.getSpeed());
        values.put(KEY_SPEED_LIMIT, detailedLog.getSpeedLimit());
        values.put(KEY_DATE_TIME, detailedLog.getDateTime());
        long val = db.insert(TABLE_DETAILED_LOGS, null, values);

        Log.e("Inserted "+TABLE_DETAILED_LOGS, String.valueOf(val));

        Log.e(KEY_SUMMARY_ID, String.valueOf(detailedLog.getSummaryId()));
        Log.e(KEY_LATITUDE, String.valueOf(detailedLog.getLatitude()));
        Log.e(KEY_LONGITUDE, String.valueOf(detailedLog.getLongitude()));
        Log.e(KEY_SPEED, String.valueOf(detailedLog.getSpeed()));
        Log.e(KEY_SPEED_LIMIT, String.valueOf(detailedLog.getSpeedLimit()));
        Log.e(KEY_DATE_TIME, String.valueOf(detailedLog.getDateTime()));

    }

    public List<SummaryLog> getAllSummaryLogs() {
        List<SummaryLog> summaryLogList = new ArrayList<SummaryLog>();
        String selectQuery = "SELECT  * FROM " + TABLE_SUMMARY_LOGS;
        Log.e(TAG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery(selectQuery, null);
        if (cur.moveToFirst()) {
            do {

                SummaryLog summaryLogObject = new SummaryLog();
//                Log.e("ID", String.valueOf(cur.getInt(cur.getColumnIndex(KEY_ID))));
                summaryLogObject.setStartTime(cur.getString(cur.getColumnIndex(KEY_START_TIME)));
                summaryLogObject.setEndTime(cur.getString(cur.getColumnIndex(KEY_END_TIME)));
                summaryLogObject.setDistance((cur.getDouble(cur.getColumnIndex(KEY_DISTANCE))));
                summaryLogObject.setOverSpeedCount(cur.getInt(cur.getColumnIndex(KEY_OVER_SPEED_COUNT)));
                summaryLogObject.setDrowsinessCount(cur.getInt(cur.getColumnIndex(KEY_DROWSINESS_COUNT)));

                summaryLogList.add(summaryLogObject);

//                Log.e("Start time", summaryLogObject.getStartTime());
//                Log.e("End time", summaryLogObject.getEndTime());
//                Log.e("Distance", String.valueOf(summaryLogObject.getDistance()));
//                Log.e("Over speed count", String.valueOf(summaryLogObject.getOverSpeedCount()));
//                Log.e("Drowsiness count", String.valueOf(summaryLogObject.getDrowsinessCount()));

            } while (cur.moveToNext());
        }
        return summaryLogList;
    }

    public List<DetailedLog> getDetailedLog(int summaryLogId) {
        List<DetailedLog> detailedLogList = new ArrayList<DetailedLog>();
        String selectQuery = "SELECT  * FROM " + TABLE_DETAILED_LOGS + " WHERE " + KEY_SUMMARY_ID + " = " + summaryLogId;
        Log.e(TAG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery(selectQuery, null);
        if (cur.moveToFirst()) {
            do {
                DetailedLog detailedLogObject = new DetailedLog();
                Log.e(KEY_ID, String.valueOf(cur.getInt(cur.getColumnIndex(KEY_ID))));
                Log.e(KEY_SUMMARY_ID, String.valueOf(cur.getLong(cur.getColumnIndex(KEY_SUMMARY_ID))));

                detailedLogObject.setSummaryId(cur.getLong(cur.getColumnIndex(KEY_SUMMARY_ID)));
                detailedLogObject.setLatitude(cur.getDouble(cur.getColumnIndex(KEY_LATITUDE)));
                detailedLogObject.setLongitude(cur.getDouble(cur.getColumnIndex(KEY_LONGITUDE)));
                detailedLogObject.setSpeed(cur.getInt(cur.getColumnIndex(KEY_SPEED)));
                detailedLogObject.setSpeedLimit(cur.getInt(cur.getColumnIndex(KEY_SPEED_LIMIT)));
                detailedLogObject.setDateTime(cur.getString(cur.getColumnIndex(KEY_DATE_TIME)));

                detailedLogList.add(detailedLogObject);

                Log.e(TAG,"Fetching details from Detailed Logs");
                Log.e(KEY_LATITUDE, String.valueOf(detailedLogObject.getLatitude()));
                Log.e(KEY_LONGITUDE, String.valueOf(detailedLogObject.getLongitude()));
                Log.e(KEY_SPEED, String.valueOf(detailedLogObject.getSpeed()));
                Log.e(KEY_SPEED_LIMIT, String.valueOf(detailedLogObject.getSpeedLimit()));
                Log.e(KEY_DATE_TIME, String.valueOf(detailedLogObject.getDateTime()));

            } while (cur.moveToNext());
        }
        Log.e("Size of "+TABLE_SUMMARY_LOGS, String.valueOf(detailedLogList.size()));
        return detailedLogList;
    }

    public long getNumberOfEntriesInSummaryLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        long numberOfEntries = DatabaseUtils.queryNumEntries(db, TABLE_SUMMARY_LOGS);
        return numberOfEntries;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public List<SummaryLog> getChartData(int id)
    {
        List<SummaryLog> logs = new ArrayList<>();
        String start_time = getDate(System.currentTimeMillis(),0);
        String end_time="";

        if(id == 0)
            end_time =getDate(System.currentTimeMillis(),365);
        else if(id==1)
            end_time =getDate(System.currentTimeMillis(),30);
        else if(id==2)
            end_time =getDate(System.currentTimeMillis(),7);
        else if(id==3)
            end_time =getDate(System.currentTimeMillis(),1);
        String query="SELECT * FROM " + TABLE_SUMMARY_LOGS + " where (substr(start_time, 7, 4)||'-'||substr(start_time, 4, 2)||'-'||substr(start_time,1,2))"+" between '"+end_time +"' and '"+start_time+"'";

        Log.e(TAG, query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery(query, null);
        if (cur.moveToFirst()) {
            do {

                SummaryLog summaryLogObject = new SummaryLog();
//                Log.e("ID", String.valueOf(cur.getInt(cur.getColumnIndex(KEY_ID))));
                summaryLogObject.setStartTime(cur.getString(cur.getColumnIndex(KEY_START_TIME)));
                summaryLogObject.setEndTime(cur.getString(cur.getColumnIndex(KEY_END_TIME)));
                summaryLogObject.setDistance((cur.getDouble(cur.getColumnIndex(KEY_DISTANCE))));
                summaryLogObject.setOverSpeedCount(cur.getInt(cur.getColumnIndex(KEY_OVER_SPEED_COUNT)));
                summaryLogObject.setDrowsinessCount(cur.getInt(cur.getColumnIndex(KEY_DROWSINESS_COUNT)));

                logs.add(summaryLogObject);

                Log.e("Start time", summaryLogObject.getStartTime());
                Log.e("End time", summaryLogObject.getEndTime());
                Log.e("Distance", String.valueOf(summaryLogObject.getDistance()));
                Log.e("Over speed count", String.valueOf(summaryLogObject.getOverSpeedCount()));
                Log.e("Drowsiness count", String.valueOf(summaryLogObject.getDrowsinessCount()));

            } while (cur.moveToNext());
        }
        return logs;
    }

    private String getDate(long time,int previous) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        cal.add(Calendar.DATE,-previous);
        String date = DateFormat.format("yyyy-MM-dd", cal).toString();
        Log.e("Date", date);
        return date;
    }

}
