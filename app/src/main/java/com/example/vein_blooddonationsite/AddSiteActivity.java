package com.example.vein_blooddonationsite;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        CheckBox checkbox_o = findViewById(R.id.checkbox_o);
        CheckBox checkbox_a = findViewById(R.id.checkbox_a);
        CheckBox checkbox_b = findViewById(R.id.checkbox_b);
        CheckBox checkbox_ab = findViewById(R.id.checkbox_ab);
        Button addSiteConfirmButton = findViewById(R.id.add_site_confirm_button);

        addSiteConfirmButton.setOnClickListener(v -> {
            String siteName = siteNameEditText.getText().toString().trim();
            String siteAddress = siteAddressEditText.getText().toString().trim();
            String contactNumber = contactNumberEditText.getText().toString().trim();
            String operatingHours = operatingHoursEditText.getText().toString().trim();

            // Get latitude and longitude
            final double[] latitude = {0.0}; // Declare latitude as a final array
            final double[] longitude = {0.0}; // Declare longitude as a final array

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
                Toast.makeText(AddSiteActivity.this, "Geocoding error", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get needed blood types from checkboxes
            List<String> neededBloodTypes = new ArrayList<>();
            if (checkbox_o.isChecked()) {
                neededBloodTypes.add("O");
            }
            if (checkbox_a.isChecked()) {
                neededBloodTypes.add("A");
            }
            if (checkbox_b.isChecked()) {
                neededBloodTypes.add("B");
            }
            if (checkbox_ab.isChecked()) {
                neededBloodTypes.add("AB");
            }

            // ... (Perform validation if needed)

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
                            neededBloodTypes,
                            currentUser.getUserId(),
                            new ArrayList<>());

                    // Add the new site to Firestore
                    db.collection("donationSites")
                            .add(newSite)
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