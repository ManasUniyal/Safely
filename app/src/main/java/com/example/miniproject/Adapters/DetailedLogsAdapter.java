package com.example.miniproject.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniproject.DataClasses.DetailedLog;
import com.example.miniproject.R;

import java.util.List;

public class DetailedLogsAdapter extends RecyclerView.Adapter<DetailedLogsAdapter.DetailedLogsViewHolder> {

    private List<DetailedLog> detailedLogList;

    public DetailedLogsAdapter(List<DetailedLog> detailedLogs) {
        this.detailedLogList = detailedLogs;
    }

    @NonNull
    @Override
    public DetailedLogsAdapter.DetailedLogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_log_row_layout, parent, false);
        DetailedLogsViewHolder detailedLogsViewHolder = new DetailedLogsViewHolder(view);
        return detailedLogsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DetailedLogsAdapter.DetailedLogsViewHolder holder, int position) {
        DetailedLog detailedLog = detailedLogList.get(position);
        holder.textViewLatitude.setText(String.valueOf(detailedLog.getLatitude()));
        holder.textViewLongitude.setText(String.valueOf(detailedLog.getLongitude()));
        holder.textViewSpeed.setText("Speed: " + detailedLog.getSpeed());
        holder.textViewSpeedLimit.setText("Speed limit: " + detailedLog.getSpeedLimit());
        holder.textViewDateTime.setText(detailedLog.getDateTime());
    }

    @Override
    public int getItemCount() {
        return detailedLogList.size();
    }

    public class DetailedLogsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLatitude;
        TextView textViewLongitude;
        TextView textViewSpeed;
        TextView textViewSpeedLimit;
        TextView textViewDateTime;

        public DetailedLogsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLatitude = itemView.findViewById(R.id.latitude);
            textViewLongitude = itemView.findViewById(R.id.longitude);
            textViewSpeed = itemView.findViewById(R.id.speed);
            textViewSpeedLimit = itemView.findViewById(R.id.speedLimit);
            textViewDateTime = itemView.findViewById(R.id.dateTime);
        }
    }
}
