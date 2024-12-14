package com.example.vein_blooddonationsite.activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.adapters.ViewDonationSiteEventAdapter;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewEventActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView siteNameTextView;
    RecyclerView eventsRecyclerView;
    ImageButton backButton;
    TextView manageEventEmptyInform;
    DonationSite site;
    User currentUser;
    ViewDonationSiteEventAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        adapter = new ViewDonationSiteEventAdapter(new ArrayList<>());

        siteNameTextView = findViewById(R.id.view_event_site_name_textview);
        eventsRecyclerView = findViewById(R.id.view_event_events_recycler_view);
        eventsRecyclerView.setAdapter(adapter);
        backButton = findViewById(R.id.view_event_back_button);
        manageEventEmptyInform = findViewById(R.id.view_event_manage_event_empty_inform);
        site = (DonationSite) getIntent().getSerializableExtra("site");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        backButton.setOnClickListener(v -> {
            finish();
        });

        if (site != null) {
            siteNameTextView.setText(site.getName());
            fetchEvents(site);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private void fetchEvents(DonationSite site) {
        Log.d("VMEA", String.valueOf(site.getSiteId()));
        db.collection("events").whereEqualTo("siteId", site.getSiteId())
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.w("ViewSiteEvents", "Listen failed.", error);
                        return;
                    }

                    List<DonationSiteEvent> events = new ArrayList<>();
                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSiteEvent event = document.toObject(DonationSiteEvent.class);
                        LocalDate today = LocalDate.now();
                        LocalDate eventLocalDate = event.getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        if (!eventLocalDate.isBefore(today)) {
                            events.add(event); // Add the event only if it's not before today
                        }
                    }

                    if (events.isEmpty()) {
                        // No event found
                        Log.d("VEA", "Not found!");
                        manageEventEmptyInform.setVisibility(View.VISIBLE);
                        eventsRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Event found
                        Log.d("VEA", events.toString());
                        eventsRecyclerView.setVisibility(View.VISIBLE);
                        manageEventEmptyInform.setVisibility(View.GONE);
                        adapter.events = events;
                        adapter.currentUser = currentUser;
                        adapter.currentSite = site;
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}