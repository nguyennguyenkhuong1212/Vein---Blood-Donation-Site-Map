package com.example.vein_blooddonationsite.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapPage extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DonationSite> sites = new ArrayList<>();
    List<User> users = new ArrayList<>();
    List<DonationSiteEvent> events = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LatLng currentLatLng;
    private Polyline routePolyline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        getCurrentLocation();
                    } else {
                        Log.d("MapPage", "Location permission denied");
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                });

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

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            DonationSite clickedSite = getSiteFromMarker(marker);
            if (clickedSite != null) {
                showSiteDetailsDialog(clickedSite);
                return true;
            }
            return false;
        });

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
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
        db.collection("events")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.w("MapPage", "Error getting events.", error);
                        return;
                    }

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSiteEvent event = document.toObject(DonationSiteEvent.class);
                        events.add(event);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void showSiteDetailsDialog(DonationSite site) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_info_window, null);

        // Find views in the dialog layout
        TextView title = dialogView.findViewById(R.id.info_window_title);
        TextView address = dialogView.findViewById(R.id.info_window_address);
        TextView hours = dialogView.findViewById(R.id.info_window_operating_hours);
        TextView bloodTypes = dialogView.findViewById(R.id.info_window_blood_types);
        Button directionsButton = dialogView.findViewById(R.id.draw_route_button);

        title.setText(site.getName());
        address.setText("Address: " + site.getAddress());
        hours.setText("Hours: " + site.getOperatingHours());

        List<String> neededBloodTypes = site.getNeededBloodTypes(events);
        String bloodTypesText = "Needed Blood Types: " + String.join(", ", neededBloodTypes);
        bloodTypes.setText(bloodTypesText);

        builder.setView(dialogView)
                .setPositiveButton("Close", (dialog, id) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();

        directionsButton.setOnClickListener(v -> {
            LatLng destination = new LatLng(site.getLatitude(), site.getLongitude());
//            drawRouteOnMap(currentLatLng, destination, true);
            drawRouteOnMap(currentLatLng, destination, false);
            dialog.dismiss();
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        });

        dialog.show();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        mMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .title("YOU ARE HERE")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
                    } else {
                        Log.d("MapPage", "Location is null");
                    }
                });
    }

    private void drawRouteOnMap(LatLng origin, LatLng destination, boolean useDirectionAPI) {
        if (routePolyline != null) {
            routePolyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions();

        if (useDirectionAPI) {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyAmYG0ewlmb4zaJAkC6pBsFjqi0NBQu-Po")
                    .build();

            try {
                DirectionsResult result = DirectionsApi.newRequest(context)
                        .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                        .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                        .mode(TravelMode.DRIVING)
                        .await();

                if (result.routes != null && result.routes.length > 0) {

                    List<LatLng> decodedPath = decodePoly(result.routes[0].overviewPolyline.getEncodedPath());

                    polylineOptions = new PolylineOptions()
                            .addAll(decodedPath)
                            .width(10)
                            .color(ContextCompat.getColor(requireContext(), R.color.primary));

                }
            } catch (InterruptedException | IOException e) {
                Log.e("MapPage", "Error getting directions: " + e.getMessage());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
        else {

            polylineOptions = new PolylineOptions()
                    .add(origin)
                    .add(destination)
                    .width(5)
                    .color(ContextCompat.getColor(requireContext(), R.color.primary));

        }
        routePolyline = mMap.addPolyline(polylineOptions);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((lat / 1E5), (lng / 1E5));
            poly.add(p);
        }

        return poly;
    }
}