package com.example.miniproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class ShowDetailedLogs extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detailed_logs);
        int summaryLogId = getIntent().getIntExtra("summaryLogIndex",-1);
        assert (summaryLogId != -1);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<DetailedLog> detailedLogList = DataBaseHelper.getInstance(ShowDetailedLogs.this).getDetailedLog(summaryLogId);
//        Log.e("Size of received detailed logs",String.valueOf(detailedLogList.size()));
        DetailedLogsAdapter detailedLogsAdapter = new DetailedLogsAdapter(detailedLogList);
        recyclerView.setAdapter(detailedLogsAdapter);
    }
}
