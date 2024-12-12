package com.example.vein_blooddonationsite.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.User;

import java.util.List;

public class ViewDonationSiteAdapter extends RecyclerView.Adapter<ViewDonationSiteAdapter.ViewHolder> {

    public List<DonationSite> donationSites;
    public List<User> users;

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
        holder.neededBloodTypesTextView.setText(
                "Needed Blood Types: " + String.join(", ", site.getNeededBloodTypes()));

//        holder.viewEventButton.setOnClickListener(v -> {
//            Intent intent = new Intent(holder.itemView.getContext(), ViewSiteEventsActivity.class);
//            intent.putExtra("site", site);
//            holder.itemView.getContext().startActivity(intent);
//        });
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
        public TextView neededBloodTypesTextView;
        public LinearLayout viewEventButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.view_site_name);
            addressTextView = itemView.findViewById(R.id.view_site_address);
            adminTextView = itemView.findViewById(R.id.view_site_admin);
            contactNumberTextView = itemView.findViewById(R.id.view_site_contact);
            operatingHoursTextView = itemView.findViewById(R.id.view_site_operating_hours);
            neededBloodTypesTextView = itemView.findViewById(R.id.view_site_needed_blood_types);
            viewEventButton = itemView.findViewById(R.id.view_site_view_event_button);
        }
    }
}