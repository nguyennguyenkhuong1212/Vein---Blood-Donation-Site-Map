package com.example.vein_blooddonationsite.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.Registration;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAchievementsActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Registration> registrations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_achievements);

        LinearLayout medalsContainer = findViewById(R.id.medals_container);

        User currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser != null) {
            displayMedals(medalsContainer, currentUser);
            fetchRegistration(currentUser);
        }
    }

    private void displayMedals(LinearLayout medalsContainer, User currentUser) {
        int numMedals = currentUser.getNumMedals(registrations);
        for (int i = 0; i < numMedals; i++) {
            ImageView medalImageView = new ImageView(this);
            medalImageView.setImageResource(R.drawable.baseline_medal_24);
            medalsContainer.addView(medalImageView);
        }
    }

    private void fetchRegistration(User currentUser) {
        db.collection("registrations")
                .whereEqualTo("userId", currentUser.getUserId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Registration registration = document.toObject(Registration.class);
                            registrations.add(registration);
                        }
                    } else {
                        Log.w("Achievements", "Error getting registration history.", task.getException());
                    }
                });
    }
}