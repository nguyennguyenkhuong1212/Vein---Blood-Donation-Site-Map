package com.example.vein_blooddonationsite;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.models.User;
import com.example.vein_blooddonationsite.utils.PasswordUtils;  // Make sure to import your PasswordUtils class
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
        Button signInButton = findViewById(R.id.signInButton);

        signInButton.setOnClickListener(v -> {
            String username = String.valueOf(log_in_username.getText());
            String password = String.valueOf(log_in_password.getText());

            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean isPasswordCorrect = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Retrieve the document data
                                Map<String, Object> response = document.getData();

                                // Retrieve the stored hashed password
                                String storedHashedPassword = String.valueOf(response.get("password"));

                                // Hash the input password and compare
                                String hashedInputPassword = PasswordUtils.hashPassword(password);

                                if (hashedInputPassword.equals(storedHashedPassword)) {
                                    isPasswordCorrect = true; // Password matches
                                }

                                // Create user object
                                User currentUser = new User(
                                        Integer.parseInt(String.valueOf(response.get("userId"))),
                                        String.valueOf(response.get("name")),
                                        String.valueOf(response.get("email")),
                                        String.valueOf(response.get("username")),
                                        String.valueOf(response.get("password")),
                                        String.valueOf(response.get("bloodType")),
                                        Boolean.parseBoolean(String.valueOf(response.get("isSuperUser")))
                                );

                                Log.d("Khuong", currentUser.toString());
                            }

                            if (isPasswordCorrect) {
                                Log.d("LogIn", "Login successful");
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                // ... (Proceed with login)
                            } else {
                                Log.d("LogIn", "Incorrect password");
                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Firestore", "No such document");
                        }
                    });
        });
    }
}
