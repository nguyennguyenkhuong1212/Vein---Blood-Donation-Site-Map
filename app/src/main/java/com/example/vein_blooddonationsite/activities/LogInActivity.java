package com.example.vein_blooddonationsite.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.User;
import com.example.vein_blooddonationsite.utils.PasswordUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class LogInActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        EditText log_in_username = findViewById(R.id.log_in_username);
        EditText log_in_password = findViewById(R.id.log_in_password);
        Button log_in_button = findViewById(R.id.signInButton);

        TextView register_link = findViewById(R.id.signUpLink);

        register_link.setOnHoverListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    register_link.setPaintFlags(register_link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    register_link.setPaintFlags(register_link.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    break;
            }
            return false;
        });

        register_link.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        log_in_button.setOnClickListener(v -> {
            String username = String.valueOf(log_in_username.getText());
            String password = String.valueOf(log_in_password.getText());

            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // No user with that username found
                                Log.d("LogIn", "Username not found");
                                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                            } else {
                                boolean isPasswordCorrect = false;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Retrieve the document data
                                    Map<String, Object> response = document.getData();

                                    // Retrieve the stored hashed password
                                    String storedHashedPassword = String.valueOf(response.get("password"));

                                    // Hash the input password and compare
                                    String hashedInputPassword = PasswordUtils.hashPassword(password);

                                    if (hashedInputPassword.equals(storedHashedPassword)) {
                                        Log.d("LogIn", "Login successful");
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                                        // Store current user
                                        User currentUser = new User(
                                                Integer.parseInt(String.valueOf(response.get("userId"))),
                                                String.valueOf(response.get("name")),
                                                String.valueOf(response.get("email")),
                                                String.valueOf(response.get("username")),
                                                String.valueOf(response.get("password")),
                                                String.valueOf(response.get("bloodType")),
                                                Boolean.parseBoolean(String.valueOf(response.get("isSiteAdmin"))),
                                                Boolean.parseBoolean(String.valueOf(response.get("isSuperUser")))
                                        );

                                        Log.d("Login", currentUser.toString());
                                        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                        intent.putExtra("user", currentUser);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Log.d("LogIn", "Incorrect password");
                                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                    }

                                    break;
                                }
                            }
                        } else {
                            Log.d("Firestore", "No such document");
                        }
                    });
        });
    }
}
