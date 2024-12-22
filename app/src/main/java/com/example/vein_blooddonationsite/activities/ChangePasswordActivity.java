package com.example.vein_blooddonationsite.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.User;
import com.example.vein_blooddonationsite.utils.PasswordUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChangePasswordActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        EditText currentPasswordEditText = findViewById(R.id.change_password_current_edittext);
        EditText newPasswordEditText = findViewById(R.id.change_password_new_edittext);
        EditText confirmNewPasswordEditText = findViewById(R.id.change_password_confirm_edittext);
        Button cancelButton = findViewById(R.id.change_password_cancel_button);
        Button saveButton = findViewById(R.id.change_password_save_button);

        // Get the current user object (you might need to pass it from the Fragment)
        User currentUser = (User) getIntent().getSerializableExtra("user");

        cancelButton.setOnClickListener(v-> {
            finish();
        });

        saveButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

            // Perform validation
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(ChangePasswordActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser != null && validateFields(newPassword, confirmNewPassword)) {
                // Check if the current password matches the stored password
                String storedHashedPassword = currentUser.getPassword();
                String hashedInputPassword = PasswordUtils.hashPassword(currentPassword);
                if (!hashedInputPassword.equals(storedHashedPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Incorrect current password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hash the new password
                String hashedNewPassword = PasswordUtils.hashPassword(newPassword);

                // Update the password in Firestore
                db.collection("users")
                        .document(String.valueOf(currentUser.getUserId()))
                        .update("password", hashedNewPassword)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful update
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChangePasswordActivity.this, "Error changing password", Toast.LENGTH_SHORT).show();
                            Log.e("ChangePassword", "Error changing password", e);
                        });
            }
        });
    }

    private boolean validateFields(String password, String confirmPassword) {

        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Confirm Password is required", Toast.LENGTH_SHORT).show();
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
}