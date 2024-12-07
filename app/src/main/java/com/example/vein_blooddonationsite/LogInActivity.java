package com.example.vein_blooddonationsite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;
import java.util.Objects;

public class LogInActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        Log.d("Hello", "Hello");
        EditText log_in_username = findViewById(R.id.log_in_username);
        EditText log_in_password = findViewById(R.id.log_in_password);
        Button signInButton = findViewById(R.id.signInButton);

        signInButton.setOnClickListener(v -> {
            String username = String.valueOf(log_in_username.getText());
            String password = String.valueOf(log_in_password.getText());
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve the document data
                                Map<String, Object> response = document.getData();

                                User currentUser = new User(
                                        Integer.parseInt(String.valueOf(response.get("userId"))),
                                        String.valueOf(response.get("name")),
                                        String.valueOf(response.get("email")),
                                        String.valueOf(response.get("password")),
                                        String.valueOf(response.get("bloodType")),
                                        Boolean.parseBoolean(String.valueOf(response.get("isSuperUser")))
                                );

                                Log.d("Khuong", currentUser.toString());
                            }
                            }
                        else {
                            Log.d("Firestore", "No such document");
                        }
                    });
        });
    }
}