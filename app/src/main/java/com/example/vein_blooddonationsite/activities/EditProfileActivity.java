package com.example.vein_blooddonationsite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText nameEditText = findViewById(R.id.edit_profile_name_edittext);
        EditText usernameEditText = findViewById(R.id.edit_profile_username_edittext);
        EditText emailEditText = findViewById(R.id.edit_profile_email_edittext);
        EditText bloodTypeEditText = findViewById(R.id.edit_profile_blood_type_edittext);
        Button cancelButton = findViewById(R.id.edit_profile_cancel_button);
        Button saveButton = findViewById(R.id.edit_profile_save_button);

        // Get the current user object (you might need to pass it from the Fragment)
        User currentUser = (User) getIntent().getSerializableExtra("user");

        // Populate EditTexts with current user data
        if (currentUser != null) {
            nameEditText.setText(currentUser.getName());
            usernameEditText.setText(currentUser.getUsername());
            emailEditText.setText(currentUser.getEmail());
            bloodTypeEditText.setText(currentUser.getBloodType());
        }

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String bloodType = bloodTypeEditText.getText().toString().trim();

            if (validateFields(name, username, email, bloodType)){

                if (currentUser != null) {
                    // Update the User object
                    currentUser.setName(name);
                    currentUser.setUsername(username);
                    currentUser.setEmail(email);
                    currentUser.setBloodType(bloodType);

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", currentUser.getUserId());
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("username", username);
                    userData.put("password", currentUser.getPassword());
                    userData.put("bloodType", bloodType);
                    userData.put("isSiteAdmin", currentUser.isSiteAdmin());
                    userData.put("isSuperUser", currentUser.isSuperUser());

                    // Update the user data in Firestore
                    db.collection("users")
                            .document(String.valueOf(currentUser.getUserId()))
                            .set(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("updatedUser", currentUser);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                                Log.e("EditProfile", "Error updating profile", e);
                            });
                }

            }
        });
    }

    private boolean validateFields(String name, String username, String email, String bloodType) {
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

        return true;
    }
}