package com.example.miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DrivingLogs extends AppCompatActivity implements SummaryLogsAdapter.OnSummaryLogClickListener{

    private BottomNavigationView bottomNavigationView;
    private Button journeyStateButton;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_logs);

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
                JourneyStatus.getInstance(DrivingLogs.this).updateJourneyLog();
                JourneyStatus.getInstance(DrivingLogs.this).toggleJourneyState();
                //TODO: Include the below method in toggle
                JourneyStatus.getInstance(DrivingLogs.this).setJourneyStateButton(journeyStateButton, DrivingLogs.this);
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

    }

    @Override
    public void onSummaryLogClick(int position) {
        Intent intent = new Intent(DrivingLogs.this, ShowDetailedLogs.class);
        intent.putExtra("summaryLogIndex", position+1);
        startActivity(intent);
    }
}
