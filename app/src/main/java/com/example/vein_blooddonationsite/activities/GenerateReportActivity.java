package com.example.vein_blooddonationsite.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.adapters.EventReportAdapter;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GenerateReportActivity extends AppCompatActivity {

    private EventReportAdapter eventAdapter;
    private final List<DonationSiteEvent> events = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ActivityResultLauncher<Intent> saveFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // The URI will be handled by EventReportAdapter
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        RecyclerView eventRecyclerView = findViewById(R.id.generate_report_event_recycler_view);
        ImageButton backButton = findViewById(R.id.generate_report_back_button);

        backButton.setOnClickListener(v -> {
            finish();
        });

        fetchAllEvents();
        fetchAllUsers();

        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventReportAdapter(events, users);
        eventRecyclerView.setAdapter(eventAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllEvents() {
        db.collection("events")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        return;
                    }

                    events.clear();

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSiteEvent event = document.toObject(DonationSiteEvent.class);
                        events.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllUsers() {
        db.collection("users")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        return;
                    }

                    users.clear();

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                    eventAdapter.notifyDataSetChanged();
                });
    }
}