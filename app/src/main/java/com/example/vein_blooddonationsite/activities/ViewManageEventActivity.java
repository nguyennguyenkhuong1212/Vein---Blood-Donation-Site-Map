package com.example.vein_blooddonationsite.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.adapters.DonationSiteEventAdapter; // Create this adapter
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewManageEventActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView siteNameTextView;
    RecyclerView eventsRecyclerView;
    ImageButton backButton;
    TextView manageEventEmptyInform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_manage_event);

        siteNameTextView = findViewById(R.id.site_name_textview);
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        backButton = findViewById(R.id.back_button);
        manageEventEmptyInform = findViewById(R.id.manage_event_empty_inform);

        backButton.setOnClickListener(v -> {
            finish();
        });

        DonationSite site = (DonationSite) getIntent().getSerializableExtra("site");
        if (site != null) {
            siteNameTextView.setText(site.getName());

            fetchEvents(site, eventsRecyclerView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchEvents(DonationSite site, RecyclerView eventsRecyclerView) {
        db.collection("events").whereEqualTo("siteId", String.valueOf(site.getSiteId()))
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.w("ViewSiteEvents", "Listen failed.", error);
                        return;
                    }

                    List<DonationSiteEvent> events = new ArrayList<>();
                    DonationSiteEventAdapter adapter = new DonationSiteEventAdapter(events);
                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSiteEvent event = document.toObject(DonationSiteEvent.class);
                        events.add(event);
                    }

                    if (events.isEmpty()) {
                        // No event found
                        Log.d("VMEA", "Not found!");
                        manageEventEmptyInform.setVisibility(View.VISIBLE);
                    } else {
                        // Event found
                        Log.d("VMEA", events.toString());
                        eventsRecyclerView.setVisibility(View.VISIBLE);
                        adapter.events = events;
                        adapter.notifyDataSetChanged();
                    }
                    eventsRecyclerView.setAdapter(adapter);
                });
    }
}