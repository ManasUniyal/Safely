package com.example.miniproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SummaryLogsAdapter extends RecyclerView.Adapter<SummaryLogsAdapter.SummaryLogsViewHolder> {

    private List<SummaryLog> summaryLogList;
    private OnSummaryLogClickListener onSummaryLogClickListener;

    public SummaryLogsAdapter(List<SummaryLog> summaryLogList, OnSummaryLogClickListener onSummaryLogClickListener) {
        this.summaryLogList = summaryLogList;
        this.onSummaryLogClickListener = onSummaryLogClickListener;
    }

    @NonNull
    @Override
    public SummaryLogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_log_row_layout, parent, false);
        SummaryLogsViewHolder summaryLogsViewHolder = new SummaryLogsViewHolder(view, onSummaryLogClickListener);
        return summaryLogsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryLogsViewHolder holder, int position) {
        SummaryLog summaryLog = summaryLogList.get(position);
        holder.textViewOverSpeedCount.setText("Over Speed: " + summaryLog.getOverSpeedCount());
        if ((summaryLog.getOverSpeedCount() > 0)) {
            holder.textViewOverSpeedCount.setBackgroundColor(Color.parseColor("#db4437"));
        } else {
            holder.textViewOverSpeedCount.setBackgroundColor(Color.parseColor("#2196F3"));
        }
        holder.textViewDrowsinessCount.setText("Drowsiness: " + summaryLog.getDrowsinessCount());
        if ((summaryLog.getDrowsinessCount() > 0)) {
            holder.textViewDrowsinessCount.setBackgroundColor(Color.parseColor("#db4437"));
        } else {
            holder.textViewDrowsinessCount.setBackgroundColor(Color.parseColor("#2196F3"));
        }

        holder.textViewStartTime.setText("Start Time\n" + summaryLog.getStartTime());
        holder.textViewEndTime.setText("End Time\n" + summaryLog.getEndTime());
        holder.textViewDistance.setText("Distance \n" + String.format("%.2f",summaryLog.getDistance()) + " Km");
        holder.textViewDuration.setText("Duration \n" +  summaryLog.getDuration() + " Hrs");
    }

    @Override
    public int getItemCount() {
        return summaryLogList.size();
    }

    public class SummaryLogsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textViewOverSpeedCount;
        TextView textViewDrowsinessCount;
        TextView textViewStartTime;
        TextView textViewEndTime;
        TextView textViewDistance;
        TextView textViewDuration;
        OnSummaryLogClickListener onSummaryLogClickListener;

        public SummaryLogsViewHolder(@NonNull View itemView, OnSummaryLogClickListener onSummaryLogClickListener) {
            super(itemView);
            textViewOverSpeedCount = itemView.findViewById(R.id.overSpeedCount);
            textViewDrowsinessCount = itemView.findViewById(R.id.drowsinessCount);
            textViewStartTime = itemView.findViewById(R.id.startTime);
            textViewEndTime = itemView.findViewById(R.id.endTime);
            textViewDistance = itemView.findViewById(R.id.distance);
            textViewDuration = itemView.findViewById(R.id.duration);
            this.onSummaryLogClickListener = onSummaryLogClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSummaryLogClickListener.onSummaryLogClick(getAdapterPosition());
        }
    }

    public interface OnSummaryLogClickListener {
        void onSummaryLogClick(int position);
    }
}
