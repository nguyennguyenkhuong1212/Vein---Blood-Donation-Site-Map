package com.example.vein_blooddonationsite.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import com.example.vein_blooddonationsite.R;

import java.util.Calendar;
import java.util.Objects;

public class HomePage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageButton filterButton = view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);

                // Inflate the menu using a try-catch block
                try {
                    popupMenu.inflate(R.menu.filter_menu);
                } catch (Exception e) {
                    Log.e("HomePage", "Error inflating menu: " + e.getMessage());
                    // Handle the error appropriately (e.g., show a toast or log the error)
                    return; // Stop further execution if menu inflation fails
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle filter item clicks here
                        int itemId = item.getItemId();
                        if (itemId == R.id.filter_distance) {
                            Log.d("Filter menu", "distance");
                        } else if (itemId == R.id.filter_blood_type) {
                            Log.d("Filter menu", "blood_type");
                        } else if (item.getItemId() == R.id.select_date) {
                            // Show the date picker dialog
                            final Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);

                            DatePickerDialog datePickerDialog = new DatePickerDialog(
                                    requireContext(),
                                    (view1, year1, month1, dayOfMonth) -> {
                                        // Handle the selected date
                                        Log.d("Selected Date", year1 + "-" + (month1 + 1) + "-" + dayOfMonth);
                                        // ... your filtering logic for event date ...
                                    },
                                    year, month, day);
                            datePickerDialog.show();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        return view;
    }
}