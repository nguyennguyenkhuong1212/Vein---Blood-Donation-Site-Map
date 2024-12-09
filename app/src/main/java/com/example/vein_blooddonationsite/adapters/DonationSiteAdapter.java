package com.example.vein_blooddonationsite.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.vein_blooddonationsite.R;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vein_blooddonationsite.models.DonationSite;
import java.util.List;

public class DonationSiteAdapter extends RecyclerView.Adapter<DonationSiteAdapter.ViewHolder> {

    public List<DonationSite> donationSites; // Make sure this is public

    public DonationSiteAdapter(List<DonationSite> donationSites) {
        this.donationSites = donationSites;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_in_site_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSite site = donationSites.get(position);
        holder.nameTextView.setText(site.getName());
        holder.addressTextView.setText(site.getAddress());
        // ... set other TextViews with data from the DonationSite object
    }

    @Override
    public int getItemCount() {
        return donationSites.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView addressTextView;
        // ... other TextViews for the item layout

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.site_name); // Use the correct IDs
            addressTextView = itemView.findViewById(R.id.site_address);
            // ... find other TextViews from the item layout
        }
    }
}
