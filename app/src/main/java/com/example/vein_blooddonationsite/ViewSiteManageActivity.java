package com.example.vein_blooddonationsite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.models.DonationSite;

public class ViewSiteManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_site_manage);

        TextView siteNameTextView = findViewById(R.id.site_name_textview);
        // ... (Get references to other TextViews to display site details)
        Button addEventButton = findViewById(R.id.add_event_button);

        DonationSite site = (DonationSite) getIntent().getSerializableExtra("site");
        if (site != null) {
            siteNameTextView.setText(site.getName());
            // ... (Set text for other TextViews using site data)
        }

        addEventButton.setOnClickListener(v -> {
            // TODO: Implement add event functionality
            // You can start a new activity or show a dialog to add an event for this site
        });
    }
}