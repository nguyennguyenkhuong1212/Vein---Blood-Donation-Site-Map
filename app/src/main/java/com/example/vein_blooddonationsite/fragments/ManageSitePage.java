package com.example.vein_blooddonationsite.fragments;

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

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.activities.AddSiteActivity;
import com.example.vein_blooddonationsite.adapters.ViewManageDonationSiteAdapter;
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
    private ViewManageDonationSiteAdapter adapter;
    TextView manageSiteTitle;
    TextView manageSiteInformSiteAdmin;
    TextView manageSiteInform;
    RecyclerView donationSitesRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_site, container, false);
        // Inflate the layout for this fragment
        adapter = new ViewManageDonationSiteAdapter(new ArrayList<>());
        FloatingActionButton addSiteBtn = view.findViewById(R.id.add_site_button);
        assert getArguments() != null;
        User currentUser = (User) getArguments().getSerializable("user");
        assert currentUser != null;

        manageSiteTitle = view.findViewById(R.id.manage_site_title);
        manageSiteInformSiteAdmin = view.findViewById(R.id.manage_site_inform_site_admin);
        manageSiteInform = view.findViewById(R.id.manage_site_inform);
        donationSitesRecyclerView = view.findViewById(R.id.view_manage_donation_sites_recycler_view);
        donationSitesRecyclerView.setAdapter(adapter);

        if (currentUser.isSiteAdmin()) {
            Log.d("MSP", currentUser.toString());
            addSiteBtn.setVisibility(View.VISIBLE);
            manageSiteTitle.setVisibility(View.VISIBLE);
        }

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
            .addSnapshotListener((response, error) -> {
            if (error != null) {
                Log.w("ManageSitePage", "Listen failed.", error);
                return;
            }

            List<DonationSite> sites = new ArrayList<>();
            assert response != null;
            for (QueryDocumentSnapshot document : response) {
                try {
                    // Get the data as a Map
                    Map<String, Object> data = document.getData();

                    int adminId = ((Long) Objects.requireNonNull(data.get("adminId"))).intValue();
                    int siteId = ((Long) Objects.requireNonNull(data.get("siteId"))).intValue();
                    String name = (String) data.get("name");
                    String address = (String) data.get("address");
                    double latitude = ((Double) data.get("latitude")).doubleValue();
                    double longitude = ((Double) data.get("longitude")).doubleValue();
                    String contactNumber = (String) data.get("contactNumber");
                    String operatingHours = (String) data.get("operatingHours");
                    List<Integer> followerIds = (List<Integer>) data.get("followerIds");

                    DonationSite site = new DonationSite(siteId, name, address, latitude, longitude,
                            contactNumber, operatingHours, adminId, followerIds);
                    if (currentUser.getUserId() == adminId) {
                        sites.add(site);
                        Log.d("MSP", currentUser.toString());
                        Log.d("MSP", currentUser.getUserId() + " " + adminId);
                    }
                } catch (ClassCastException | NullPointerException e) {
                    Log.e("ManageSitePage", "Error parsing document data: " + e.getMessage());
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
                donationSitesRecyclerView.setVisibility(View.GONE);
            } else {
                // Donation sites found
                Log.d("MSP", sites.toString());
                donationSitesRecyclerView.setVisibility(View.VISIBLE);
                manageSiteInformSiteAdmin.setVisibility(View.GONE);
                manageSiteInform.setVisibility(View.GONE);
                adapter.donationSites = sites;
                adapter.notifyDataSetChanged();
            }
        });
    }

}