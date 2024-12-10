package com.example.vein_blooddonationsite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vein_blooddonationsite.adapters.DonationSiteAdapter;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManageSitePage extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DonationSiteAdapter adapter;
    TextView manageSiteTitle;
    TextView manageSiteInformSiteAdmin;
    TextView manageSiteInform;
    RecyclerView donationSitesRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_site, container, false);
        // Inflate the layout for this fragment
        adapter = new DonationSiteAdapter(new ArrayList<>());
        FloatingActionButton addSiteBtn = view.findViewById(R.id.add_site_button);
        assert getArguments() != null;
        User currentUser = (User) getArguments().getSerializable("user");
        assert currentUser != null;
        if (currentUser.isSiteAdmin()) {
            Log.d("MSP", currentUser.toString());
            addSiteBtn.setVisibility(View.VISIBLE);
        }

        manageSiteTitle = view.findViewById(R.id.manage_site_title);
        manageSiteInformSiteAdmin = view.findViewById(R.id.manage_site_inform_site_admin);
        manageSiteInform = view.findViewById(R.id.manage_site_inform);
        donationSitesRecyclerView = view.findViewById(R.id.donation_sites_recycler_view);
        donationSitesRecyclerView.setAdapter(adapter);

        addSiteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddSiteActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
        });

        fetchDonationSites(currentUser);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchDonationSites(User currentUser) {
        db.collection("donationSites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DonationSite> sites = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Get the data as a Map
                                Map<String, Object> data = document.getData();

                                // Extract the fields and handle potential type mismatches
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

                                // Create DonationSite object
                                DonationSite site = new DonationSite(siteId, name, address, latitude, longitude,
                                        contactNumber, operatingHours, neededBloodTypes, adminId, followerIds);
                                if (currentUser.getUserId() == adminId) {
                                    sites.add(site);
                                    Log.d("MSP", currentUser.toString());
                                    Log.d("MSP", currentUser.getUserId() + " " + adminId);
                                }
                            } catch (ClassCastException | NullPointerException e) {
                                Log.e("ManageSitePage", "Error parsing document data: " + e.getMessage());
                                // Handle the error appropriately (e.g., skip the document or show an error message)
                            }
                        }
                        if (sites.isEmpty()) {
                            // No donation sites found
                            Log.d("MSP", "Not found!");
                            if (currentUser.isSiteAdmin()) {
                                manageSiteInformSiteAdmin.setVisibility(View.VISIBLE);
                            } else {
                                manageSiteInform.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Donation sites found
                            Log.d("MSP", sites.toString());
                            manageSiteTitle.setVisibility(View.VISIBLE);
                            donationSitesRecyclerView.setVisibility(View.VISIBLE);
                            adapter.donationSites = sites;
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w("ManageSitePage", "Error getting documents.", task.getException());
                    }
                });
    }

}