package com.example.miniproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SummaryLogsAdapter extends RecyclerView.Adapter<SummaryLogsAdapter.SummaryLogsViewHolder> {

    private List<SummaryLog> summaryLogList;

    public SummaryLogsAdapter(List<SummaryLog> summaryLogList) {
        this.summaryLogList = summaryLogList;
    }

    @NonNull
    @Override
    public SummaryLogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_log_row_layout, parent, false);
        SummaryLogsViewHolder summaryLogsViewHolder = new SummaryLogsViewHolder(view);
        return summaryLogsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryLogsViewHolder holder, int position) {
        SummaryLog summaryLog = summaryLogList.get(position);
        holder.textViewOverSpeedCount.setText("OVER SPEED COUNT: " + summaryLog.getOverSpeedCount());
        holder.textViewDrowsinessCount.setText("DROWSINESS COUNT: " + summaryLog.getDrowsinessCount());
        holder.textViewStartTime.setText("START TIME: " + summaryLog.getStartTime());
        holder.textViewEndTime.setText("END TIME: " + summaryLog.getEndTime());
        holder.textViewDistance.setText("DISTANCE: " + summaryLog.getDistance());
        holder.textViewDuration.setText("DURATION: " +  summaryLog.getDuration());
    }

    @Override
    public int getItemCount() {
        return summaryLogList.size();
    }

    public class SummaryLogsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewOverSpeedCount;
        TextView textViewDrowsinessCount;
        TextView textViewStartTime;
        TextView textViewEndTime;
        TextView textViewDistance;
        TextView textViewDuration;

        public SummaryLogsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOverSpeedCount = itemView.findViewById(R.id.overSpeedCount);
            textViewDrowsinessCount = itemView.findViewById(R.id.drowsinessCount);
            textViewStartTime = itemView.findViewById(R.id.startTime);
            textViewEndTime = itemView.findViewById(R.id.endTime);
            textViewDistance = itemView.findViewById(R.id.distance);
            textViewDuration = itemView.findViewById(R.id.duration);
        }
    }
}
