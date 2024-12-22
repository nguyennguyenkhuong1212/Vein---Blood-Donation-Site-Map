package com.example.vein_blooddonationsite.activities;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.Registration;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewRegistrationActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    DonationSiteEvent event;
    List<Registration> registrations = new ArrayList<>();
    List<User> users = new ArrayList<>();
    TextView event_name_textview;
    ImageButton backButton;
    TextView empty_inform;
    RecyclerView registrationsRecyclerView;
    TableLayout registrationTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_registration);

        event_name_textview = findViewById(R.id.view_registration_event_name_text_view);
        backButton = findViewById(R.id.view_registration_back_button);
        empty_inform = findViewById(R.id.registration_empty_inform);
        registrationTable = findViewById(R.id.registrations_table);

        event = (DonationSiteEvent) getIntent().getSerializableExtra("event");

        backButton.setOnClickListener(v -> {
            finish();
        });

        if (event != null){
            event_name_textview.setText(event.getEventName());
            fetchAllUsers();
            fetchRegistrations(event);
        }
    }

    public void fetchAllUsers() {
        db.collection("users")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.w("VRA", "Listen failed (GET users).", error);
                        return;
                    }

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                });
    }

    public void fetchRegistrations(DonationSiteEvent event) {
        db.collection("registrations")
                .whereEqualTo("eventId", event.getEventId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Registration registration = document.toObject(Registration.class);
                            if (Objects.equals(registration.getRole(), "MANAGER")) continue;
                            registrations.add(registration);
                        }

                        if (registrations.isEmpty()) {
                            // No registrations found
                            Log.d("VRA", "Not found!");
                            empty_inform.setVisibility(View.VISIBLE);
                            registrationTable.setVisibility(View.GONE);
                        } else {
                            // Registrations found
                            Log.d("VRA", registrations.toString());
                            registrationTable.setVisibility(View.VISIBLE);
                            empty_inform.setVisibility(View.GONE);
                            populateTable(registrationTable);
                        }
                    } else {
                        Log.w("ViewRegistrations", "Error getting registrations.", task.getException());
                    }
                });
    }

    private void refreshTable(TableLayout tableLayout) {
        tableLayout.removeAllViews();
        populateTable(tableLayout);
    }


    @SuppressLint("SetTextI18n")
    private void populateTable(TableLayout tableLayout) {
        // Define column layout parameters
        TableRow.LayoutParams nameParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        nameParams.setMargins(15, 10, 15, 10);

        TableRow.LayoutParams bloodTypeParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        bloodTypeParams.setMargins(15, 10, 15, 10);

        TableRow.LayoutParams roleParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.35f);
        roleParams.setMargins(15, 10, 15, 10);

        TableRow.LayoutParams statusParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.4f);
        statusParams.setMargins(15, 10, 15, 10);

        TableRow.LayoutParams actionParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.35f);
        actionParams.setMargins(15, 10, 15, 10);

        Typeface headerFont = ResourcesCompat.getFont(this, R.font.instrumentsansbold);
        Typeface dataFont = ResourcesCompat.getFont(this, R.font.instrumentsans);

        TableRow headerRow = new TableRow(this);
        String[] headers = {"Name", "Blood Type", "Role", "Status", "Actions"};

        for (String header : headers) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(headerFont, Typeface.BOLD);

            switch (header) {
                case "Name":
                    textView.setLayoutParams(nameParams);
                    break;
                case "Blood Type":
                    textView.setLayoutParams(bloodTypeParams);
                    break;
                case "Role":
                    textView.setLayoutParams(roleParams);
                    break;
                case "Status":
                    textView.setLayoutParams(statusParams);
                    break;
                case "Actions":
                    textView.setLayoutParams(actionParams);
                    break;
            }

            headerRow.addView(textView);
        }

        headerRow.setBackgroundColor(ContextCompat.getColor(ViewRegistrationActivity.this, R.color.primary));
        headerRow.setGravity(Gravity.CENTER);
        tableLayout.addView(headerRow);

        // Create data rows
        for (Registration registration : registrations) {
            TableRow row = new TableRow(this);
            User tempUser = registration.getUser(users);

            TextView nameTextView = new TextView(this);
            nameTextView.setText(tempUser.getName());
            nameTextView.setLayoutParams(nameParams);
            nameTextView.setTypeface(dataFont);
            row.addView(nameTextView);

            TextView bloodTypeTextView = new TextView(this);
            bloodTypeTextView.setText(tempUser.getBloodType());
            bloodTypeTextView.setLayoutParams(bloodTypeParams);
            bloodTypeTextView.setTypeface(dataFont);
            row.addView(bloodTypeTextView);

            TextView roleTextView = new TextView(this);
            roleTextView.setText(registration.getRole().substring(0, 1).toUpperCase()
                    + registration.getRole().substring(1).toLowerCase());
            roleTextView.setLayoutParams(roleParams);
            roleTextView.setTypeface(dataFont);
            row.addView(roleTextView);

            TextView statusTextView = new TextView(this);
            statusTextView.setText(registration.getStatus().substring(0, 1).toUpperCase()
                    + registration.getStatus().substring(1).toLowerCase());
            statusTextView.setLayoutParams(statusParams);
            statusTextView.setTypeface(dataFont);
            row.addView(statusTextView);

            LinearLayout buttonsLayout = new LinearLayout(this);
            buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Create buttons for approve/decline
            if (Objects.equals(registration.getStatus(), "PENDING")) {

                ImageButton approveButton = new ImageButton(this);
                approveButton.setImageResource(R.drawable.baseline_check_24);
                approveButton.setBackground(ContextCompat.getDrawable(this, R.drawable.view_registration_button_background));
                approveButton.setColorFilter(Color.WHITE);
                approveButton.setPadding(15, 15, 15, 15);

                ImageButton declineButton = new ImageButton(this);
                declineButton.setImageResource(R.drawable.baseline_decline_24);
                declineButton.setBackground(ContextCompat.getDrawable(this, R.drawable.view_registration_button_background_secondary));
                declineButton.setColorFilter(Color.WHITE);
                declineButton.setPadding(15, 15, 15, 15);

                approveButton.setOnClickListener(v -> {
                    db.collection("registrations")
                            .document(String.valueOf(registration.getRegistrationId()))
                            .update("status", "APPROVED")
                            .addOnSuccessListener(aVoid -> {
                                registration.setStatus("APPROVED");

                                runOnUiThread(() -> {
                                    Toast.makeText(ViewRegistrationActivity.this, "Registration approved", Toast.LENGTH_SHORT).show();
                                    refreshTable(tableLayout);
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewRegistrationActivity.this, "Error approving registration", Toast.LENGTH_SHORT).show();
                                Log.e("ViewRegistrations", "Error approving registration", e);
                            });
                });

                declineButton.setOnClickListener(v -> {
                    db.collection("registrations")
                            .document(String.valueOf(registration.getRegistrationId()))
                            .update("status", "DECLINED")
                            .addOnSuccessListener(aVoid -> {
                                registration.setStatus("DECLINED");

                                runOnUiThread(() -> {
                                    Toast.makeText(ViewRegistrationActivity.this, "Registration declined", Toast.LENGTH_SHORT).show();
                                    refreshTable(tableLayout);
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewRegistrationActivity.this, "Error declining registration", Toast.LENGTH_SHORT).show();
                                Log.e("ViewRegistrations", "Error declining registration", e);
                            });
                });

                buttonsLayout.addView(approveButton);
                buttonsLayout.addView(declineButton);
            } else if (Objects.equals(registration.getStatus(), "APPROVED")) {
                Button completeButton = new Button(this);
                completeButton.setBackground(ContextCompat.getDrawable(this, R.drawable.view_manage_site_button));
                completeButton.setText("Complete");
                completeButton.setTextSize(COMPLEX_UNIT_SP, 12);
                completeButton.setTextColor(Color.WHITE);
                completeButton.setPadding(5, -5, 5, -5);
                completeButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                completeButton.setOnClickListener(v -> {
                    db.collection("registrations")
                            .document(String.valueOf(registration.getRegistrationId()))
                            .update("status", "COMPLETED")
                            .addOnSuccessListener(aVoid -> {
                                registration.setStatus("COMPLETED");

                                runOnUiThread(() -> {
                                    Toast.makeText(ViewRegistrationActivity.this, "Registration completed", Toast.LENGTH_SHORT).show();
                                    refreshTable(tableLayout);
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewRegistrationActivity.this, "Error ending registration", Toast.LENGTH_SHORT).show();
                                Log.e("ViewRegistrations", "Error ending registration", e);
                            });
                });

                buttonsLayout.addView(completeButton);
            }

            buttonsLayout.setLayoutParams(actionParams);
            buttonsLayout.setGravity(Gravity.CENTER);
            row.addView(buttonsLayout);
            row.setGravity(Gravity.CENTER_VERTICAL);

            tableLayout.addView(row);
        }
    }

}