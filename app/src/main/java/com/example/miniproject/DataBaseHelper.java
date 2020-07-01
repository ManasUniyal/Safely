package com.example.miniproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_SPEED_LIMIT = "speed_limit";
    private static final String KEY_DATE_TIME = "time";

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
        Log.e(TAG,String.valueOf(val));
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

                Log.e("Start time", summaryLogObject.getStartTime());
                Log.e("End time", summaryLogObject.getEndTime());
                Log.e("Distance", String.valueOf(summaryLogObject.getDistance()));
                Log.e("Over speed count", String.valueOf(summaryLogObject.getOverSpeedCount()));
                Log.e("Drowsiness count", String.valueOf(summaryLogObject.getDrowsinessCount()));

            } while (cur.moveToNext());
        }
        return summaryLogList;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
