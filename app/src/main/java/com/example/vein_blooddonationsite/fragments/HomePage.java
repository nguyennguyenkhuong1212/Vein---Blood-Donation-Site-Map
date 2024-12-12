package com.example.vein_blooddonationsite.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.adapters.ViewDonationSiteAdapter;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomePage extends Fragment {

    TextView emptyInform;
    RecyclerView viewDonationSitesRecyclerView;
    ViewDonationSiteAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        emptyInform = view.findViewById(R.id.view_all_donation_sites_empty_inform);

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

        adapter = new ViewDonationSiteAdapter(new ArrayList<>(), new ArrayList<>());
        viewDonationSitesRecyclerView = view.findViewById(R.id.view_all_donation_sites_recycler_view);
        viewDonationSitesRecyclerView.setAdapter(adapter);

        fetchAllDonationSites();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllDonationSites() {
        List<User> users = new ArrayList<>();
        db.collection("users")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                    db.collection("donationSites")
                            .addSnapshotListener((response, error) -> {
                                if (error != null) {
                                    Log.w("ManageSitePage", "Listen failed.", error);
                                    return;
                                }

                                List<DonationSite> sites = new ArrayList<>();
                                assert response != null;
                                for (QueryDocumentSnapshot document : response) {
                                    try {
                                        Map<String, Object> data = document.getData();

                                        int adminId = ((Long) Objects.requireNonNull(data.get("adminId"))).intValue();
                                        int siteId = ((Long) Objects.requireNonNull(data.get("siteId"))).intValue();
                                        String name = (String) data.get("name");
                                        String address = (String) data.get("address");
                                        double latitude = ((Double) data.get("latitude")).doubleValue();
                                        double longitude = ((Double) data.get("longitude")).doubleValue();
                                        String contactNumber = (String) data.get("contactNumber");
                                        String operatingHours = (String) data.get("operatingHours");
                                        List<String> neededBloodTypes = (List<String>) data.get("neededBloodTypes");
                                        List<Integer> followerIds = (List<Integer>) data.get("followerIds");

                                        DonationSite site = new DonationSite(siteId, name, address, latitude, longitude,
                                                contactNumber, operatingHours, neededBloodTypes, adminId, followerIds);
                                        sites.add(site);
                                    } catch (ClassCastException | NullPointerException e) {
                                        Log.e("ManageSitePage", "Error parsing document data: " + e.getMessage());
                                    }
                                }

                                if (sites.isEmpty()) {
                                    // No donation sites found
                                    Log.d("MSP", "Not found!");
                                    emptyInform.setVisibility(View.VISIBLE);
                                } else {
                                    // Donation sites found
                                    Log.d("MSP", sites.toString());
                                    viewDonationSitesRecyclerView.setVisibility(View.VISIBLE);
                                    adapter.donationSites = sites;
                                    adapter.users = users;
                                    adapter.notifyDataSetChanged();
                                }
                            });
                } else {
                    Log.w("HomePage", "Error getting users.", task.getException());
                }
            });
    }
}