package com.example.vein_blooddonationsite.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.activities.ChangePasswordActivity;
import com.example.vein_blooddonationsite.activities.EditProfileActivity;
import com.example.vein_blooddonationsite.activities.GenerateReportActivity;
import com.example.vein_blooddonationsite.activities.LogInActivity;
import com.example.vein_blooddonationsite.activities.ViewAchievementsActivity;
import com.example.vein_blooddonationsite.models.User;

public class ProfilePage extends Fragment {

    private User currentUser;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Retrieve the current user passed in arguments
        assert getArguments() != null;
        currentUser = (User) getArguments().getSerializable("user");

        // Initialize UI components
        TextView userName = view.findViewById(R.id.profile_user_name);
        TextView avatarTextView = view.findViewById(R.id.avatar_textview);
        TextView userEmail = view.findViewById(R.id.profile_user_email);
        TextView bloodType = view.findViewById(R.id.profile_blood_type);
        Button profileEditProfileButton = view.findViewById(R.id.profile_edit_profile_button);
        Button profileChangePasswordButton = view.findViewById(R.id.profile_change_password_button);
        Button profileAchievementButton = view.findViewById(R.id.profile_achievement_button);
        Button profileReportButton = view.findViewById(R.id.profile_generate_report_button);
        Button logoutButton = view.findViewById(R.id.logout_button);

        userName.setText(currentUser.getName());
        avatarTextView.setText(getInitials(currentUser.getName()));
        userEmail.setText(currentUser.getEmail());
        bloodType.setText("Your Blood Type: " + currentUser.getBloodType());

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    getActivity();
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        User updatedUser = (User) result.getData().getSerializableExtra("updatedUser");
                        if (updatedUser != null) {
                            currentUser = updatedUser;
                            updateUI(view);
                        }
                    }
                }
        );

        profileEditProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            intent.putExtra("user", currentUser);
            editProfileLauncher.launch(intent);
        });

        profileChangePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        profileAchievementButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewAchievementsActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        profileReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GenerateReportActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        if (!currentUser.isSuperUser()) {
            profileReportButton.setVisibility(View.GONE);
            view.findViewById(R.id.profile_line5).setVisibility(View.GONE);
        }

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(View view) {
        TextView userName = view.findViewById(R.id.profile_user_name);
        TextView avatarTextView = view.findViewById(R.id.avatar_textview);
        TextView userEmail = view.findViewById(R.id.profile_user_email);
        TextView bloodType = view.findViewById(R.id.profile_blood_type);

        userName.setText(currentUser.getName());
        avatarTextView.setText(getInitials(currentUser.getName()));
        userEmail.setText(currentUser.getEmail());
        bloodType.setText("Your Blood Type: " + currentUser.getBloodType());
    }

    private String getInitials(String name) {
        String[] nameParts = name.split(" ");
        if (nameParts.length >= 2) {
            return (nameParts[0].charAt(0) + "" + nameParts[nameParts.length - 1].charAt(0)).toUpperCase();
        } else if (nameParts.length == 1) {
            return String.valueOf(nameParts[0].charAt(0)).toUpperCase();
        } else {
            return "";
        }
    }
}
