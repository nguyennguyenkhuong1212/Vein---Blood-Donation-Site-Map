package com.example.vein_blooddonationsite.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddEventActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        EditText eventDateEditText = findViewById(R.id.event_date_edittext);
        EditText startTimeEditText = findViewById(R.id.start_time_edittext);
        EditText endTimeEditText = findViewById(R.id.end_time_edittext);
        CheckBox recurringCheckbox = findViewById(R.id.recurring_checkbox);
        Button addEventButton = findViewById(R.id.add_event_button);

        DonationSite site = (DonationSite) getIntent().getSerializableExtra("site");

        addEventButton.setOnClickListener(v -> {
            if (site != null) {
                String dateString = eventDateEditText.getText().toString().trim();
                String startTimeString = startTimeEditText.getText().toString().trim();
                String endTimeString = endTimeEditText.getText().toString().trim();
                boolean isRecurring = recurringCheckbox.isChecked();

                // Validation (add more validation as needed)
                if (dateString.isEmpty() || startTimeString.isEmpty() || endTimeString.isEmpty()) {
                    Toast.makeText(AddEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date eventDate = dateFormat.parse(dateString);

                    DateTimeFormatter timeFormatter = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    }
                    LocalTime startTime;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startTime = LocalTime.parse(startTimeString, timeFormatter);
                    } else {
                        startTime = null;
                    }
                    LocalTime endTime;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        endTime = LocalTime.parse(endTimeString, timeFormatter);
                    } else {
                        endTime = null;
                    }

                    getNewEventId(task -> {
                        if (task.isSuccessful()) {
                            int newEventId = task.getResult();

                            DonationSiteEvent newEvent = new DonationSiteEvent(
                                    newEventId,
                                    site.getSiteId(),
                                    eventName,
                                    eventDate,
                                    startTime,
                                    endTime,
                                    isRecurring
                            );
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
}