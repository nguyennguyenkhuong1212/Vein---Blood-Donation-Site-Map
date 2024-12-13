package com.example.vein_blooddonationsite.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.adapters.DonationSiteAdapter;
import com.example.vein_blooddonationsite.adapters.DonationSiteEventAdapter; // Create this adapter
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ViewManageEventActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView siteNameTextView;
    RecyclerView eventsRecyclerView;
    ImageButton backButton;
    TextView manageEventEmptyInform;
    FloatingActionButton addEventBtn;
    DonationSite site;
    DonationSiteEventAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_manage_event);
        adapter = new DonationSiteEventAdapter(new ArrayList<>());

        siteNameTextView = findViewById(R.id.site_name_textview);
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        eventsRecyclerView.setAdapter(adapter);
        backButton = findViewById(R.id.back_button);
        manageEventEmptyInform = findViewById(R.id.manage_event_empty_inform);
        addEventBtn = findViewById(R.id.add_event_float_button);
        site = (DonationSite) getIntent().getSerializableExtra("site");

        backButton.setOnClickListener(v -> {
            finish();
        });

        addEventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewManageEventActivity.this, AddEventActivity.class);
            intent.putExtra("site", site);
            startActivity(intent);
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
                    Log.d("VMEA", "Not found!");
                    manageEventEmptyInform.setVisibility(View.VISIBLE);
                    eventsRecyclerView.setVisibility(View.GONE);
                } else {
                    // Event found
                    Log.d("VMEA", events.toString());
                    eventsRecyclerView.setVisibility(View.VISIBLE);
                    manageEventEmptyInform.setVisibility(View.GONE);
                    adapter.events = events;
                    adapter.notifyDataSetChanged();
                }
            });
    }
}