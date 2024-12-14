package com.example.vein_blooddonationsite.activities;

import android.location.Address;
import android.location.Geocoder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewManageSiteActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_manage_site);
        geocoder = new Geocoder(this);

        DonationSite site = (DonationSite) getIntent().getSerializableExtra("site");

        EditText siteNameEditText = findViewById(R.id.view_manage_site_name_edittext);
        EditText siteAddressEditText = findViewById(R.id.view_manage_site_address_edittext);
        EditText siteLatEditText = findViewById(R.id.view_manage_site_lat_edittext);
        EditText siteLngEditText = findViewById(R.id.view_manage_site_lng_edittext);
        EditText contactNumberEditText = findViewById(R.id.view_manage_site_contact_edittext);
        EditText operatingHoursEditText = findViewById(R.id.view_manage_site_hours_edittext);
        Button confirmButton = findViewById(R.id.view_manage_site_confirm_button);
        Button cancelButton = findViewById(R.id.view_manage_site_cancel_button);

        if (site != null) {
            siteNameEditText.setText(site.getName());
            siteAddressEditText.setText(site.getAddress());
            siteLatEditText.setText(String.valueOf(site.getLatitude()));
            siteLngEditText.setText(String.valueOf(site.getLongitude()));
            contactNumberEditText.setText(site.getContactNumber());
            operatingHoursEditText.setText(site.getOperatingHours());

            confirmButton.setOnClickListener(v -> {
                // Get the updated values from EditTexts
                String updatedSiteName = siteNameEditText.getText().toString().trim();
                String updatedSiteAddress = siteAddressEditText.getText().toString().trim();
                String updatedLatitudeString = siteLatEditText.getText().toString().trim();
                String updatedLongitudeString = siteLngEditText.getText().toString().trim();
                String updatedContactNumber = contactNumberEditText.getText().toString().trim();
                String updatedOperatingHours = operatingHoursEditText.getText().toString().trim();

                if (!updatedOperatingHours.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9] - ([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    Toast.makeText(ViewManageSiteActivity.this, "Invalid operating hours format", Toast.LENGTH_SHORT).show();
                    return;
                }

                double updatedLatitude = 0;
                double updatedLongitude = 0;

                final double[] latitude = {0.0}; // Declare latitude as a final array
                final double[] longitude = {0.0}; // Declare longitude as a final array

                if (!updatedLatitudeString.isEmpty() && !updatedLongitudeString.isEmpty()){
                    latitude[0] = Double.parseDouble(updatedLatitudeString);
                    longitude[0] = Double.parseDouble(updatedLongitudeString);
                }
                else {
                    try {
                        if (!siteLatEditText.getText().toString().isEmpty() && !siteLngEditText.getText().toString().isEmpty()) {
                            latitude[0] = Double.parseDouble(siteLatEditText.getText().toString().trim());
                            longitude[0] = Double.parseDouble(siteLngEditText.getText().toString().trim());
                        } else {
                            Log.d("VSMP", updatedSiteAddress);
                            List<Address> addresses = geocoder.getFromLocationName(updatedSiteAddress, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                latitude[0] = address.getLatitude();
                                longitude[0] = address.getLongitude();
                            } else {
                                Toast.makeText(ViewManageSiteActivity.this, "Unable to find address", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } catch (IOException e) {
                        Log.e("AddSiteActivity", "Geocoding error", e);
                        Toast.makeText(ViewManageSiteActivity.this, "Unable to find coordinate", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                updatedLatitude = latitude[0];
                updatedLongitude = longitude[0];
                Log.d("VSMP", latitude[0] + " " + String.valueOf(longitude[0]));

                // Update the DonationSite object
                site.setName(updatedSiteName);
                site.setAddress(updatedSiteAddress);
                site.setLatitude(updatedLatitude);
                site.setLongitude(updatedLongitude);
                site.setContactNumber(updatedContactNumber);
                site.setOperatingHours(updatedOperatingHours);

                Log.d("VSMP", site.toString());

                // Update the site in Firestore
                db.collection("donationSites")
                        .document(String.valueOf(site.getSiteId())) // Use siteId as the document ID
                        .set(site)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ViewManageSiteActivity.this, "Site updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful update
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ViewManageSiteActivity.this, "Error updating site", Toast.LENGTH_SHORT).show();
                            // Handle the error appropriately
                        });
            });
            cancelButton.setOnClickListener(v -> finish());
        }
    }
}