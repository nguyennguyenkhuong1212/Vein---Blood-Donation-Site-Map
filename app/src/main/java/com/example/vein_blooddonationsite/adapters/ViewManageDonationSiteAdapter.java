package com.example.vein_blooddonationsite.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.vein_blooddonationsite.R;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.activities.ViewManageEventActivity;
import com.example.vein_blooddonationsite.activities.ViewManageSiteActivity;
import com.example.vein_blooddonationsite.models.DonationSite;
import java.util.List;

public class ViewManageDonationSiteAdapter extends RecyclerView.Adapter<ViewManageDonationSiteAdapter.ViewHolder> {

    public List<DonationSite> donationSites;

    public ViewManageDonationSiteAdapter(List<DonationSite> donationSites) {
        this.donationSites = donationSites;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_in_manage_site_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSite site = donationSites.get(position);
        holder.nameTextView.setText(site.getName());
        holder.addressTextView.setText(site.getAddress());
        holder.contactNumberTextView.setText("Contact Number: " + site.getContactNumber());
        holder.operatingHoursTextView.setText("Operating Hours: " + site.getOperatingHours());

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ViewManageSiteActivity.class);
            intent.putExtra("site", site);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.viewEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ViewManageEventActivity.class);
            intent.putExtra("site", site);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return donationSites.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView addressTextView;
        public TextView contactNumberTextView;
        public TextView operatingHoursTextView;
        public TextView neededBloodTypesTextView;
        private LinearLayout editButton;
        private LinearLayout viewEventButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.site_name); // Use the correct IDs
            addressTextView = itemView.findViewById(R.id.site_address);
            contactNumberTextView = itemView.findViewById(R.id.site_contact);
            operatingHoursTextView = itemView.findViewById(R.id.site_operating_hours);
            editButton = itemView.findViewById(R.id.edit_site_button);
            viewEventButton = itemView.findViewById(R.id.view_event_button);
            // ... find other TextViews from the item layout
        }
    }
}
