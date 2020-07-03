package com.example.miniproject.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject.Utilities.AlertUserAudio;
import com.example.miniproject.SingletonClasses.DataBaseHelper;
import com.example.miniproject.SingletonClasses.JourneyStatus;
import com.example.miniproject.R;
import com.example.miniproject.DataClasses.SummaryLog;
import com.example.miniproject.Adapters.SummaryLogsAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;
import java.util.List;



public class DrivingLogs extends AppCompatActivity implements SummaryLogsAdapter.OnSummaryLogClickListener {


    private BottomNavigationView bottomNavigationView;
    private Button journeyStateButton;
    private RecyclerView recyclerView;
    private Spinner spin;
    private String[] types = {"Year","Month","Week","Today"};
    LineChart mpLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_logs);

        spin = findViewById(R.id.graph_spinner);
        mpLineChart = findViewById(R.id.graph_chart);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.logs);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.camera:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.maps:
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.logs:
                        startActivity(new Intent(getApplicationContext(), DrivingLogs.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
        //SetUp Spinner
        setUpSpinner();

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               setUpChart(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        SummaryLogsAdapter summaryLogsAdapter = new SummaryLogsAdapter(DataBaseHelper.getInstance(DrivingLogs.this).getAllSummaryLogs(), this);
        recyclerView.setAdapter(summaryLogsAdapter);

        journeyStateButton = findViewById(R.id.journeyStateButton);
        JourneyStatus.getInstance(getApplicationContext()).setJourneyStateButton(journeyStateButton, DrivingLogs.this);
        journeyStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JourneyStatus.getInstance(DrivingLogs.this).updateJourneyLog(journeyStateButton, DrivingLogs.this);
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });
    }


    void setUpSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
    }

    void setUpChart(int id)
    {
        List<SummaryLog> logList = JourneyStatus.getInstance(DrivingLogs.this).LineChart(id);
        Log.d("Chart List Size", String.valueOf(logList.size()));
        ArrayList<Entry> overspeed_chart = new ArrayList<>();
        ArrayList<Entry> drowsiness_chart = new ArrayList<>();
        for( SummaryLog summaryLog: logList)
        {
            overspeed_chart.add(new Entry(Float.parseFloat(summaryLog.getStartTime().substring(0,2)),summaryLog.getOverSpeedCount()));
            drowsiness_chart.add(new Entry(Float.parseFloat(summaryLog.getStartTime().substring(0,2)),summaryLog.getDrowsinessCount()));
        }
        //TODO: Customization of Graph
        LineDataSet lineDataSet1 = new LineDataSet(overspeed_chart,"OverSpeed");

        LineDataSet lineDataSet2 = new LineDataSet(drowsiness_chart,"Drowsiness");

        ArrayList<ILineDataSet> dataset = new ArrayList<>();
        dataset.add(lineDataSet1);
        dataset.add(lineDataSet2);
        LineData lineData  = new LineData(dataset);
        mpLineChart.setData(lineData);
        mpLineChart.invalidate();
    }


    @Override
    public void onSummaryLogClick(int position) {
        Intent intent = new Intent(DrivingLogs.this, ShowDetailedLogs.class);
        intent.putExtra("summaryLogIndex", position+1);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AlertUserAudio.getInstance(DrivingLogs.this).endWarning();
    }
}
