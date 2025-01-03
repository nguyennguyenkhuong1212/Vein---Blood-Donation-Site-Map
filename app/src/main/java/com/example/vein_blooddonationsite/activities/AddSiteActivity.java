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
import com.example.vein_blooddonationsite.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddSiteActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        User currentUser = (User) getIntent().getSerializableExtra("user");
        geocoder = new Geocoder(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);

        EditText siteNameEditText = findViewById(R.id.add_site_name_edittext);
        EditText siteAddressEditText = findViewById(R.id.add_site_address_edittext);
        EditText siteLatEditText = findViewById(R.id.add_site_lat_edittext);
        EditText siteLngEditText = findViewById(R.id.add_site_lng_edittext);
        EditText contactNumberEditText = findViewById(R.id.add_site_contact_edittext);
        EditText operatingHoursEditText = findViewById(R.id.add_site_hours_edittext);
        Button addSiteConfirmButton = findViewById(R.id.add_site_confirm_button);
        Button cancelButton = findViewById(R.id.add_site_cancel_button);

        addSiteConfirmButton.setOnClickListener(v -> {
            String siteName = siteNameEditText.getText().toString().trim();
            String siteAddress = siteAddressEditText.getText().toString().trim();
            String siteLat = siteLatEditText.getText().toString().trim();
            String siteLng = siteLngEditText.getText().toString().trim();
            String contactNumber = contactNumberEditText.getText().toString().trim();
            String operatingHours = operatingHoursEditText.getText().toString().trim();

            // Get latitude and longitude
            final double[] latitude = {0.0}; // Declare latitude as a final array
            final double[] longitude = {0.0}; // Declare longitude as a final array

            if (!siteLat.isEmpty() && !siteLng.isEmpty()){
                latitude[0] = Double.parseDouble(siteLat);
                longitude[0] = Double.parseDouble(siteLng);
            }
            else {
                try {
                    if (!siteLatEditText.getText().toString().isEmpty() && !siteLngEditText.getText().toString().isEmpty()) {
                        latitude[0] = Double.parseDouble(siteLatEditText.getText().toString().trim());
                        longitude[0] = Double.parseDouble(siteLngEditText.getText().toString().trim());
                    } else {
                        List<Address> addresses = geocoder.getFromLocationName(siteAddress, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            latitude[0] = address.getLatitude();
                            longitude[0] = address.getLongitude();
                        } else {
                            Toast.makeText(AddSiteActivity.this, "Unable to find address", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (IOException e) {
                    Log.e("AddSiteActivity", "Geocoding error", e);
                    Toast.makeText(AddSiteActivity.this, "Unable to find coordinate", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!operatingHours.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9] - ([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                Toast.makeText(AddSiteActivity.this, "Invalid operating hours format", Toast.LENGTH_SHORT).show();
                return;
            }

            getNewSiteId(task -> {
                if (task.isSuccessful()) {
                    int newSiteId = task.getResult();

                    // Create a new DonationSite object with the new ID
                    assert currentUser != null;
                    DonationSite newSite = new DonationSite(newSiteId,
                            siteName,
                            siteAddress,
                            latitude[0],
                            longitude[0],
                            contactNumber,
                            operatingHours,
                            currentUser.getUserId(),
                            new ArrayList<>());

                    // Add the new site to Firestore
                    db.collection("donationSites")
                            .document(String.valueOf(newSiteId)).set(newSite)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AddSiteActivity.this, "Site added successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddSiteActivity.this, "Error adding site", Toast.LENGTH_SHORT).show();
                                // Handle the error appropriately
                            });

                } else {
                    // Handle error getting new site ID
                    Log.e("Firestore", "Error getting new site ID", task.getException());
                    Toast.makeText(AddSiteActivity.this, "Error adding site", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void getNewSiteId(OnCompleteListener<Integer> listener) {
        db.collection("donationSites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxSiteId = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                int siteId = Math.toIntExact(document.getLong("siteId")); // Access "siteId" field
                                if (siteId > maxSiteId) {
                                    maxSiteId = siteId;
                                }
                            } catch (NumberFormatException e) {
                                Log.e("Firestore", "Error parsing siteId", e);
                            }
                        }
                        listener.onComplete(Tasks.forResult(maxSiteId + 1));
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        listener.onComplete(Tasks.forException(Objects.requireNonNull(task.getException())));
                    }
                });
    }
}