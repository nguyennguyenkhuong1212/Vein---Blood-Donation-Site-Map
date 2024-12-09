package com.example.vein_blooddonationsite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.vein_blooddonationsite.utils.PasswordUtils;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RegisterActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        EditText register_name = findViewById(R.id.register_name);
        EditText register_username = findViewById(R.id.register_username);
        EditText register_email = findViewById(R.id.register_email);
        EditText register_password = findViewById(R.id.register_password);
        EditText register_confirm_password = findViewById(R.id.register_confirm_password);
        EditText register_blood_type = findViewById(R.id.register_blood_type);
        Button register_button = findViewById(R.id.signUpButton);
        TextView log_in_link = findViewById(R.id.signInLink);

        log_in_link.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        register_button.setOnClickListener(v -> {
            String name = register_name.getText().toString().trim();
            String username = register_username.getText().toString().trim();
            String email = register_email.getText().toString().trim();
            String password = register_password.getText().toString().trim();
            String confirmPassword = register_confirm_password.getText().toString().trim();
            String bloodType = register_blood_type.getText().toString().trim().toUpperCase();

            if (validateRegisterFields(name, username, email, password, confirmPassword, bloodType)) {
                String hashedPassword = PasswordUtils.hashPassword(password);

                getNewUserId(task -> { // Get the new user ID asynchronously
                    if (task.isSuccessful()) {
                        int newUserId = task.getResult();

                        // Create the user object with the new ID
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", newUserId);
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("username", username);
                        userData.put("password", password);
                        userData.put("bloodType", bloodType);
                        userData.put("isSiteAdmin", false);
                        userData.put("isSuperUser", false);

                        db.collection("users").document(username).set(userData)
                                .addOnSuccessListener(e -> Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show());

                    } else {
                        // Handle error getting new user ID
                        Log.e("Firestore", "Error getting new user ID", task.getException());
                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean validateRegisterFields(String name, String username, String email, String password, String confirmPassword, String bloodType) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Confirm Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        Set<String> validBloodTypes = new HashSet<>(Arrays.asList("A", "B", "AB", "O"));

        if (!validBloodTypes.contains(bloodType)) {
            Toast.makeText(this, "Invalid blood type", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Email validation (basic check)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check password matching
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password size >= 6
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Regular expression to check for at least one uppercase letter, one lowercase letter, and one digit
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
        if (!password.matches(pattern)) {
            Toast.makeText(this, "Password must contain at least one uppercase letter, one lowercase letter, and one digit", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void getNewUserId(OnCompleteListener<Integer> listener) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxUserId = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                int userId = Math.toIntExact(document.getLong("userId"));
                                if (userId > maxUserId) {
                                    maxUserId = userId;
                                }
                            } catch (NumberFormatException e) {
                                Log.e("Firestore", "Error parsing userId", e);
                            }
                        }
                        listener.onComplete(Tasks.forResult(maxUserId + 1));
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        listener.onComplete(Tasks.forException(Objects.requireNonNull(task.getException())));
                    }
                });
    }
}