package com.example.vein_blooddonationsite.adapters;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSiteEvent event = events.get(position);

        holder.eventNameTextView.setText(event.getEventName());

        // Format the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String eventDateString = dateFormat.format(event.getEventDate());
        holder.dateTextView.setText(eventDateString);

        // Format the time (no need for SDK version check)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Assuming startTime and endTime are Maps in your DonationSiteEvent class
        Map<String, Integer> startTimeMap = event.getStartTime();
        LocalTime startTime = LocalTime.of(startTimeMap.get("hour"), startTimeMap.get("minute"));
        String startTimeString = startTime.format(timeFormatter);

        Map<String, Integer> endTimeMap = event.getEndTime();
        LocalTime endTime = LocalTime.of(endTimeMap.get("hour"), endTimeMap.get("minute"));
        String endTimeString = endTime.format(timeFormatter);

        holder.startTimeTextView.setText("Start Time: " + startTimeString);
        holder.endTimeTextView.setText("End Time: " + endTimeString);

        if (event.isRecurring()){
            holder.recurringTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView;
        public TextView dateTextView;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public TextView recurringTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name_textview);
            dateTextView = itemView.findViewById(R.id.event_date_textview);
            startTimeTextView = itemView.findViewById(R.id.event_start_time_textview);
            endTimeTextView = itemView.findViewById(R.id.event_end_time_textview);
            recurringTextView = itemView.findViewById(R.id.event_recurring_textview);
        }
    }
}