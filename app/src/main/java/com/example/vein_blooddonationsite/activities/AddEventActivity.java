package com.example.vein_blooddonationsite.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddEventActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        EditText eventNameEditText = findViewById(R.id.event_name_edittext);
        EditText eventDateEditText = findViewById(R.id.event_date_edittext);
        EditText startTimeEditText = findViewById(R.id.start_time_edittext);
        EditText endTimeEditText = findViewById(R.id.end_time_edittext);
        CheckBox recurringCheckbox = findViewById(R.id.recurring_checkbox);
        Button addEventButton = findViewById(R.id.add_event_button);
        Button cancelButton = findViewById(R.id.add_event_cancel_button);
        CheckBox checkbox_o = findViewById(R.id.checkbox_o);
        CheckBox checkbox_a = findViewById(R.id.checkbox_a);
        CheckBox checkbox_b = findViewById(R.id.checkbox_b);
        CheckBox checkbox_ab = findViewById(R.id.checkbox_ab);

        DonationSite site = (DonationSite) getIntent().getSerializableExtra("site");

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        addEventButton.setOnClickListener(v -> {
            if (site != null) {
                String eventName = eventNameEditText.getText().toString().trim();
                String dateString = eventDateEditText.getText().toString().trim();
                String startTimeString = startTimeEditText.getText().toString().trim();
                String endTimeString = endTimeEditText.getText().toString().trim();
                boolean isRecurring = recurringCheckbox.isChecked();

                List<String> neededBloodTypes = new ArrayList<>();
                if (checkbox_o.isChecked()) {
                    neededBloodTypes.add("O");
                }
                if (checkbox_a.isChecked()) {
                    neededBloodTypes.add("A");
                }
                if (checkbox_b.isChecked()) {
                    neededBloodTypes.add("B");
                }
                if (checkbox_ab.isChecked()) {
                    neededBloodTypes.add("AB");
                }

                // Validation
                if (eventName.isEmpty() || dateString.isEmpty() || startTimeString.isEmpty() || endTimeString.isEmpty()) {
                    Toast.makeText(AddEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate date format (dd/MM/yyyy)
                if (!dateString.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/[0-9]{4}$")) {
                    Toast.makeText(AddEventActivity.this, "Invalid date format. Please use dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate time format (HH:mm)
                if (!startTimeString.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") ||
                        !endTimeString.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    Toast.makeText(AddEventActivity.this, "Invalid time format. Please use HH:mm", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date eventDate = dateFormat.parse(dateString);

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime startTime = LocalTime.parse(startTimeString, timeFormatter);
                    LocalTime endTime = LocalTime.parse(endTimeString, timeFormatter);

                    if (!startTime.isBefore(endTime)) {
                        Toast.makeText(AddEventActivity.this, "Start time must be before end time", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (site.getOperatingHours().split(" - ").length != 2) {
                        throw new IllegalArgumentException("Invalid operating hours format. Expected format: HH:mm - HH:mm");
                    }

                    // Parse the start and end times
                    LocalTime operatingStartTime = LocalTime.parse(site.getOperatingHours().split(" - ")[0], timeFormatter);
                    LocalTime operatingEndTime = LocalTime.parse(site.getOperatingHours().split(" - ")[1], timeFormatter);

                    if (startTime.isBefore(operatingStartTime) || endTime.isAfter(operatingEndTime)){
                        Toast.makeText(AddEventActivity.this, "The scheduling of blood donation events must align with the site's operating hours.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getNewEventId(task -> {
                        if (task.isSuccessful()) {
                            int newEventId = task.getResult();

                            // Create maps for startTime and endTime
                            Map<String, Integer> startTimeMap = new HashMap<>();
                            startTimeMap.put("hour", startTime.getHour());
                            startTimeMap.put("minute", startTime.getMinute());
                            startTimeMap.put("second", startTime.getSecond());
                            startTimeMap.put("nano", startTime.getNano());

                            Map<String, Integer> endTimeMap = new HashMap<>();
                            endTimeMap.put("hour", endTime.getHour());
                            endTimeMap.put("minute", endTime.getMinute());
                            endTimeMap.put("second", endTime.getSecond());
                            endTimeMap.put("nano", endTime.getNano());

                            // Check for overlapping events across all sites managed by the user
                            db.collection("donationSites")
                                    .whereEqualTo("adminId", site.getAdminId())
                                    .get()
                                    .addOnCompleteListener(siteTask -> {
                                        if (siteTask.isSuccessful()) {
                                            List<Integer> adminSiteIds = new ArrayList<>();
                                            for (QueryDocumentSnapshot siteDoc : siteTask.getResult()) {
                                                adminSiteIds.add(siteDoc.getLong("siteId").intValue());
                                            }

                                            db.collection("events")
                                                    .whereIn("siteId", adminSiteIds)
                                                    .get()
                                                    .addOnCompleteListener(overlapTask -> {
                                                        if (overlapTask.isSuccessful()) {
                                                            boolean overlaps = false;
                                                            for (QueryDocumentSnapshot document : overlapTask.getResult()) {
                                                                DonationSiteEvent existingEvent = document.toObject(DonationSiteEvent.class);
                                                                if (eventOverlaps(existingEvent, eventDate, startTime, endTime)) {
                                                                    overlaps = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (overlaps) {
                                                                Toast.makeText(AddEventActivity.this, "Event overlaps with an existing event", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // No overlaps, proceed to add the event
                                                                DonationSiteEvent newEvent = new DonationSiteEvent(
                                                                        newEventId,
                                                                        site.getSiteId(),
                                                                        eventName,
                                                                        eventDate,
                                                                        startTimeMap,
                                                                        endTimeMap,
                                                                        isRecurring,
                                                                        neededBloodTypes
                                                                );

                                                                db.collection("events")
                                                                        .document(String.valueOf(newEventId))
                                                                        .set(newEvent)
                                                                        .addOnSuccessListener(documentReference -> {
                                                                            Toast.makeText(AddEventActivity.this, "Event added successfully", Toast.LENGTH_SHORT).show();
                                                                            finish();
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            Toast.makeText(AddEventActivity.this, "Error adding event", Toast.LENGTH_SHORT).show();
                                                                            Log.e("AddEventActivity", "Error adding event", e);
                                                                        });

                                                                createAdminRegistration(site, newEvent);
                                                            }
                                                        } else {
                                                            Log.w("AddEventActivity", "Error checking for overlapping events", overlapTask.getException());
                                                        }
                                                    });
                                        } else {
                                            Log.w("AddEventActivity", "Error getting admin sites", siteTask.getException());
                                        }
                                    });
                        } else {
                            // Handle error getting new event ID
                            Log.e("AddEventActivity", "Error getting new event ID", task.getException());
                            Toast.makeText(AddEventActivity.this, "Error adding event", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (ParseException e) {
                    Toast.makeText(AddEventActivity.this, "Invalid date or time format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getNewEventId(OnCompleteListener<Integer> listener) {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxEventId = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                int eventId = Math.toIntExact(document.getLong("eventId"));
                                if (eventId > maxEventId) {
                                    maxEventId = eventId;
                                }
                            } catch (NumberFormatException e) {
                                Log.e("Firestore", "Error parsing eventId", e);
                            }
                        }
                        listener.onComplete(Tasks.forResult(maxEventId + 1));
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        listener.onComplete(Tasks.forException(Objects.requireNonNull(task.getException())));
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean eventOverlaps(DonationSiteEvent existingEvent, Date newEventDate, LocalTime newStartTime, LocalTime newEndTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String existingEventDateString = sdf.format(existingEvent.getEventDate());
        String newEventDateString = sdf.format(newEventDate);

        Log.d("HAHAHA", existingEvent.toString());
        Log.d("HAHAHA", newEventDate.toString());
        Log.d("HAHAHA", newStartTime.toString());
        Log.d("HAHAHA", newEndTime.toString());

        if (existingEventDateString.equals(newEventDateString)) {
            LocalTime existingStartTime = LocalTime.of(existingEvent.getStartTime().get("hour"), existingEvent.getStartTime().get("minute"));
            LocalTime existingEndTime = LocalTime.of(existingEvent.getEndTime().get("hour"), existingEvent.getEndTime().get("minute"));

            return newStartTime.isBefore(existingEndTime) || newEndTime.isAfter(existingStartTime);
        }
        return false;
    }


    private void createAdminRegistration(DonationSite site, DonationSiteEvent event) {
        getNewRegistrationId(task -> {
            if (task.isSuccessful()) {
                int newRegistrationId = task.getResult();

                Map<String, Integer> donationTimeMap = new HashMap<>();
                donationTimeMap.put("hour", event.getStartTime().get("hour"));
                donationTimeMap.put("minute", event.getStartTime().get("minute"));

                Registration registration = new Registration(
                        newRegistrationId,
                        site.getAdminId(),
                        site.getSiteId(),
                        event.getEventId(),
                        new Date(),
                        event.getEventDate(),
                        donationTimeMap,
                        "APPROVED",
                        "MANAGER"
                );

                db.collection("registrations")
                        .document(String.valueOf(newRegistrationId))
                        .set(registration)
                        .addOnSuccessListener(aVoid -> Log.d("AddEventActivity", "Admin registration added"))
                        .addOnFailureListener(e -> Log.e("AddEventActivity", "Error adding admin registration", e));
            }
        });
    }

    private void getNewRegistrationId(OnCompleteListener<Integer> listener) {
        db.collection("registrations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxRegistrationId = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                int registrationId = Math.toIntExact(document.getLong("registrationId"));
                                if (registrationId > maxRegistrationId) {
                                    maxRegistrationId = registrationId;
                                }
                            } catch (NumberFormatException e) {
                                Log.e("Firestore", "Error parsing registrationId", e);
                            }
                        }
                        listener.onComplete(Tasks.forResult(maxRegistrationId + 1));
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        listener.onComplete(Tasks.forException(Objects.requireNonNull(task.getException())));
                    }
                });
    }
}
