// ProfilePage.java
package com.example.vein_blooddonationsite.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.activities.LogInActivity;

public class ProfilePage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            // 1. Sign out the user from Firebase Authentication (if applicable)
            // If you're using Firebase Authentication, sign out the user here.

            // 2. Clear any saved user data or preferences
            // You might want to clear any SharedPreferences or local data related to the user.

            // 3. Navigate back to the login activity
            Intent intent = new Intent(getActivity(), LogInActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Finish the current activity
        });

        return view;
    }
}