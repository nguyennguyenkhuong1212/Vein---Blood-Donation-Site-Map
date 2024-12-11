package com.example.vein_blooddonationsite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DonationSiteEventAdapter extends RecyclerView.Adapter<DonationSiteEventAdapter.ViewHolder> {

    public List<DonationSiteEvent> events;

    public DonationSiteEventAdapter(List<DonationSiteEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_in_manage_event_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSiteEvent event = events.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String eventDateString = dateFormat.format(event.getEventDate());

        holder.dateTextView.setText(eventDateString);
        DateTimeFormatter timeFormatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        }
        String startTimeString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startTimeString = event.getStartTime().format(timeFormatter);
        }
        String endTimeString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endTimeString = event.getEndTime().format(timeFormatter);
        }

        holder.startTimeTextView.setText(startTimeString);
        holder.endTimeTextView.setText(endTimeString);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView startTimeTextView;
        public TextView endTimeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.event_date_textview);
            startTimeTextView = itemView.findViewById(R.id.event_start_time_textview);
            endTimeTextView = itemView.findViewById(R.id.event_end_time_textview);
        }
    }
}