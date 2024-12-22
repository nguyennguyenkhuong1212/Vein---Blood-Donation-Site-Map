package com.example.vein_blooddonationsite.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapPage extends Fragment implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private GoogleMap mMap;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DonationSite> sites = new ArrayList<>();
    List<User> users = new ArrayList<>();

    List<DonationSiteEvent> events = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        fetchDonationSitesAndPopulateMap();
        getAllEvents();

        mMap.setInfoWindowAdapter(this);

        mMap.setOnMarkerClickListener(marker -> {
            DonationSite clickedSite = getSiteFromMarker(marker);
            if (clickedSite != null) {
                marker.showInfoWindow();
            }
            return true;
        });
    }

    private void fetchDonationSitesAndPopulateMap() {
        db.collection("donationSites")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.w("ManageSitePage", "Listen failed.", error);
                        return;
                    }

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSite site = document.toObject(DonationSite.class);
                        sites.add(site);
                    }

                    addMarkersToMap(sites);
                });
    }

    private void addMarkersToMap(List<DonationSite> sites) {
        if (mMap != null && sites != null) {
            for (DonationSite site : sites) {
                LatLng latLng = new LatLng(site.getLatitude(), site.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(site.getName())
                        .snippet(site.getAddress())
                        .icon(resizeMapIcon()));
            }

            if (!sites.isEmpty()) {
                LatLng firstSiteLatLng = new LatLng(sites.get(0).getLatitude(),
                        sites.get(0).getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstSiteLatLng, 13));
            }
        }
    }

    private BitmapDescriptor resizeMapIcon() {
        if (isAdded() && getContext() != null) {
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 110, 110, false);
            return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
        }
        return null;
    }

    private DonationSite getSiteFromMarker(Marker marker) {
        for (DonationSite site : sites) {
            if (site.getLatitude() == marker.getPosition().latitude &&
                    site.getLongitude() == marker.getPosition().longitude) {
                return site;
            }
        }
        return null;
    }

    private void getAllEvents() {
        db.collection("donationSiteEvents")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.w("MapPage", "Error getting events.", error);
                        return;
                    }

                    events.clear();

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSiteEvent event = document.toObject(DonationSiteEvent.class);
                        events.add(event);
                    }
                });
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);

        TextView title = view.findViewById(R.id.info_window_title);
        TextView address = view.findViewById(R.id.info_window_address);
        TextView operatingHours = view.findViewById(R.id.info_window_operating_hours);
        TextView bloodTypes = view.findViewById(R.id.info_window_blood_types);

        address.setEllipsize(TextUtils.TruncateAt.END);
        address.setMaxLines(1);

        DonationSite clickedSite = getSiteFromMarker(marker);
        if (clickedSite != null) {
            title.setText(clickedSite.getName());
            address.setText("Address: " + clickedSite.getAddress());
            operatingHours.setText("Operating Hours: " + clickedSite.getOperatingHours());

            List<DonationSiteEvent> siteEvents = clickedSite.getEvents(events);
            List<String> neededBloodTypes = clickedSite.getNeededBloodTypes(siteEvents);
            String bloodTypesText = "Needed Blood Types: " + String.join(", ", neededBloodTypes);
            bloodTypes.setText(bloodTypesText);
        }

        return view;
    }
}