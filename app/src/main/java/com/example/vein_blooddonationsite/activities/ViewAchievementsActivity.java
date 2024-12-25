package com.example.vein_blooddonationsite.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.Registration;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewAchievementsActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Registration> registrations = new ArrayList<>();
    TableLayout registrationTable;
    TextView medalEmptyInform;
    TextView registrationEmptyInform;
    LinearLayout medalsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_achievements);

        findViewById(R.id.view_achievement_back_button).setOnClickListener(v -> {
            finish();
        });

        medalsContainer = findViewById(R.id.medals_container);
        registrationTable = findViewById(R.id.view_achievement_registrations_table);
        medalEmptyInform = findViewById(R.id.view_achievement_medal_empty_inform);
        registrationEmptyInform = findViewById(R.id.view_achievement_registration_empty_inform);

        User currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser != null) {
            fetchRegistration(currentUser);
        }
    }

    private void displayMedals(LinearLayout medalsContainer, User currentUser) {
        int numMedals = currentUser.getNumMedals(registrations);
        Log.d("HAHAHA", String.valueOf(numMedals));
        for (int i = 0; i < numMedals; i++) {
            ImageView medalImageView = new ImageView(this);
            medalImageView.setImageResource(R.drawable.baseline_medal_24);
            medalImageView.setColorFilter(ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_IN);
            medalsContainer.addView(medalImageView);
        }
    }

    @SuppressLint("SetTextI18n")
    private void populateTable(TableLayout tableLayout) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        TableRow.LayoutParams dateTimeParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        dateTimeParams.setMargins(15, 10, 15, 10);

        TableRow.LayoutParams roleParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        roleParams.setMargins(15, 10, 15, 10);

        TableRow.LayoutParams statusParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        statusParams.setMargins(15, 10, 15, 10);

        Typeface headerFont = ResourcesCompat.getFont(this, R.font.instrumentsansbold);
        Typeface dataFont = ResourcesCompat.getFont(this, R.font.instrumentsans);

        TableRow headerRow = new TableRow(this);
        String[] headers = {"Date & Time", "Role", "Status"};

        for (String header : headers) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(headerFont, Typeface.BOLD);

            switch (header) {
                case "Date & Time":
                    textView.setLayoutParams(dateTimeParams);
                    break;
                case "Role":
                    textView.setLayoutParams(roleParams);
                    break;
                case "Status":
                    textView.setLayoutParams(statusParams);
                    break;
            }

            headerRow.addView(textView);
        }

        headerRow.setBackgroundColor(ContextCompat.getColor(ViewAchievementsActivity.this, R.color.primary));
        headerRow.setGravity(Gravity.CENTER);
        tableLayout.addView(headerRow);

        for (Registration registration : registrations) {
            TableRow row = new TableRow(this);

            TextView dateTimeTextView = new TextView(this);
            dateTimeTextView.setText(dateFormat.format(registration.getDonationDate()) + "\n" + registration.getDonationTime().get("hour") + ":" + registration.getDonationTime().get("minute"));
            dateTimeTextView.setLayoutParams(dateTimeParams);
            dateTimeTextView.setTypeface(dataFont);
            row.addView(dateTimeTextView);

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

            row.setGravity(Gravity.CENTER_VERTICAL);

            tableLayout.addView(row);
        }
    }

    private void fetchRegistration(User currentUser) {
        db.collection("registrations")
                .whereEqualTo("userId", currentUser.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        registrations.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Registration registration = document.toObject(Registration.class);
                            registrations.add(registration);
                        }

                        if (registrations.isEmpty()) {
                            // No registrations found
                            Log.d("VAA", "Not found!");
                            registrationEmptyInform.setVisibility(View.VISIBLE);
                            registrationTable.setVisibility(View.GONE);
                        } else {
                            // Registrations found
                            Log.d("VAA", registrations.toString());
                            registrationTable.setVisibility(View.VISIBLE);
                            registrationEmptyInform.setVisibility(View.GONE);
                            displayMedals(medalsContainer, currentUser);
                            populateTable(registrationTable);
                        }
                    } else {
                        Log.w("Achievements", "Error getting registration history.", task.getException());
                    }
                });
    }
}