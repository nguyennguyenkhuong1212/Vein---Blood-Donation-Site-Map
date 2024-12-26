package com.example.vein_blooddonationsite.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.activities.ViewEventActivity;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Iterator;
import java.util.List;

public class ViewDonationSiteAdapter extends RecyclerView.Adapter<ViewDonationSiteAdapter.ViewHolder> {

    public List<DonationSite> donationSites;
    public List<User> users;
    public User currentUser;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ViewDonationSiteAdapter(List<DonationSite> donationSites, List<User> users) { // Modify constructor
        this.donationSites = donationSites;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_in_view_site_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSite site = donationSites.get(position);
        holder.nameTextView.setText(site.getName());
        holder.addressTextView.setText(site.getAddress());
        User adminUser = null;
        for (User user : users) {
            if (user.getUserId() == site.getAdminId()) {
                adminUser = user;
                break;
            }
        }

        if (adminUser != null) {
            holder.adminTextView.setText("Site Manager: " + adminUser.getName());
        } else {
            holder.adminTextView.setText("Site Manager: Unknown");
        }

        holder.contactNumberTextView.setText("Contact Number: " + site.getContactNumber());
        holder.operatingHoursTextView.setText("Operating Hours: " + site.getOperatingHours());

        holder.viewEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ViewEventActivity.class);
            intent.putExtra("currentUser", currentUser);
            intent.putExtra("site", site);
            holder.itemView.getContext().startActivity(intent);
        });

        Log.d("Test", String.valueOf(site.getFollowerIds()));
        Log.d("Test", String.valueOf(currentUser.getUserId()));
        if (currentUser != null && site.getFollowerIds().contains(currentUser.getUserId())) {
            holder.followButton.setVisibility(View.GONE);
            holder.unfollowButton.setVisibility(View.VISIBLE);
        } else {
            holder.followButton.setVisibility(View.VISIBLE);
            holder.unfollowButton.setVisibility(View.GONE);
        }

        if (currentUser != null && currentUser.getUserId() == site.getAdminId()){
            holder.followButton.setVisibility(View.GONE);
            holder.unfollowButton.setVisibility(View.GONE);
        }

        holder.followButton.setOnClickListener(v -> {
            if (currentUser == null) {
                return;
            }

            site.getFollowerIds().add(currentUser.getUserId());
            Log.d("Test", String.valueOf(site.getFollowerIds()));
            db.collection("donationSites").document(String.valueOf(site.getSiteId())).set(site)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(holder.itemView.getContext(), "Followed " + site.getName(), Toast.LENGTH_SHORT).show();
                        holder.followButton.setVisibility(View.GONE);
                        holder.unfollowButton.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Failed to follow " + site.getName(), Toast.LENGTH_SHORT).show()
                    );
        });

        holder.unfollowButton.setOnClickListener(v -> {
            if (currentUser == null) {
                return;
            }

            Log.d("Test", String.valueOf(site.getFollowerIds()));
            Integer userId = currentUser.getUserId();

            if (site.getFollowerIds().contains(userId)) {
                site.getFollowerIds().remove(userId);
                db.collection("donationSites").document(String.valueOf(site.getSiteId())).set(site)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(holder.itemView.getContext(), "Unfollowed " + site.getName(), Toast.LENGTH_SHORT).show();
                            holder.followButton.setVisibility(View.VISIBLE);
                            holder.unfollowButton.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(holder.itemView.getContext(), "Failed to unfollow " + site.getName(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(holder.itemView.getContext(), "User is not following this site", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return donationSites.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView addressTextView;
        public TextView adminTextView;
        public TextView contactNumberTextView;
        public TextView operatingHoursTextView;
        public LinearLayout viewEventButton;
        public ImageButton unfollowButton;
        public ImageButton followButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.view_site_name);
            addressTextView = itemView.findViewById(R.id.view_site_address);
            adminTextView = itemView.findViewById(R.id.view_site_admin);
            contactNumberTextView = itemView.findViewById(R.id.view_site_contact);
            operatingHoursTextView = itemView.findViewById(R.id.view_site_operating_hours);
            viewEventButton = itemView.findViewById(R.id.view_site_view_event_button);
            followButton = itemView.findViewById(R.id.follow_button);
            unfollowButton = itemView.findViewById(R.id.unfollow_button);
        }
    }
}