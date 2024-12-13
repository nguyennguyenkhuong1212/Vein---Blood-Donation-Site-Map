package com.example.vein_blooddonationsite.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.activities.FilterActivity;
import com.example.vein_blooddonationsite.activities.LogInActivity;
import com.example.vein_blooddonationsite.activities.RegisterActivity;
import com.example.vein_blooddonationsite.adapters.ViewDonationSiteAdapter;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.User;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomePage extends Fragment {

    TextView emptyInform;
    RecyclerView viewDonationSitesRecyclerView;
    ViewDonationSiteAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int FILTER_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private Location userLocation;
    List<DonationSiteEvent> events = new ArrayList<>();
    private ProgressBar loadingSpinner;
    private View overlay;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int distance = data.getIntExtra("distance", 0);
                    ArrayList<String> bloodTypes = data.getStringArrayListExtra("bloodTypes");
                    String eventDate = data.getStringExtra("eventDate");
                    assert eventDate != null;
                    showLoading();
                    fetchFilteredDonationSites(distance, bloodTypes, eventDate);
                    hideLoading();
                }
            });

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation(null);
                } else {
                    // Handle permission denial (optional)
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        emptyInform = view.findViewById(R.id.view_all_donation_sites_empty_inform);
        loadingSpinner = view.findViewById(R.id.loading_spinner);
        overlay = view.findViewById(R.id.loading_spinner_overlay);

        ImageButton filterButton = view.findViewById(R.id.filter_button);

        filterButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), FilterActivity.class);
            filterLauncher.launch(intent);
        });

        adapter = new ViewDonationSiteAdapter(new ArrayList<>(), new ArrayList<>());
        viewDonationSitesRecyclerView = view.findViewById(R.id.view_all_donation_sites_recycler_view);
        viewDonationSitesRecyclerView.setAdapter(adapter);

        fetchAllDonationSites();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllDonationSites() {
        List<User> users = new ArrayList<>();
        db.collection("users")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        users.add(user);
                    }
                    db.collection("donationSites")
                            .addSnapshotListener((response, error) -> {
                                if (error != null) {
                                    Log.w("ManageSitePage", "Listen failed.", error);
                                    return;
                                }

                                List<DonationSite> sites = new ArrayList<>();
                                assert response != null;
                                for (QueryDocumentSnapshot document : response) {
                                    try {
                                        Map<String, Object> data = document.getData();

                                        int adminId = ((Long) Objects.requireNonNull(data.get("adminId"))).intValue();
                                        int siteId = ((Long) Objects.requireNonNull(data.get("siteId"))).intValue();
                                        String name = (String) data.get("name");
                                        String address = (String) data.get("address");
                                        double latitude = ((Double) data.get("latitude")).doubleValue();
                                        double longitude = ((Double) data.get("longitude")).doubleValue();
                                        String contactNumber = (String) data.get("contactNumber");
                                        String operatingHours = (String) data.get("operatingHours");
                                        List<String> neededBloodTypes = (List<String>) data.get("neededBloodTypes");
                                        List<Integer> followerIds = (List<Integer>) data.get("followerIds");

                                        DonationSite site = new DonationSite(siteId, name, address, latitude, longitude,
                                                contactNumber, operatingHours, neededBloodTypes, adminId, followerIds);
                                        sites.add(site);
                                    } catch (ClassCastException | NullPointerException e) {
                                        Log.e("ManageSitePage", "Error parsing document data: " + e.getMessage());
                                    }
                                }

                                if (sites.isEmpty()) {
                                    // No donation sites found
                                    Log.d("HP", "Not found!");
                                    emptyInform.setVisibility(View.VISIBLE);
                                } else {
                                    // Donation sites found
                                    Log.d("HP", sites.toString());
                                    viewDonationSitesRecyclerView.setVisibility(View.VISIBLE);
                                    adapter.donationSites = sites;
                                    adapter.users = users;
                                    adapter.notifyDataSetChanged();
                                }
                            });
                } else {
                    Log.w("HomePage", "Error getting users.", task.getException());
                }
            });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchEvents(OnCompleteListener<Void> listener) {
        db.collection("events")
                .addSnapshotListener((response, error) -> {
                    if (error != null) {
                        Log.d("HP", "Listen failed.");
                        listener.onComplete(Tasks.forException(error));
                        return;
                    }

                    assert response != null;
                    for (QueryDocumentSnapshot document : response) {
                        DonationSiteEvent event = document.toObject(DonationSiteEvent.class);

                        LocalDate today = LocalDate.now();
                        LocalDate eventLocalDate = event.getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        if (!eventLocalDate.isBefore(today)) {
                            events.add(event);
                        }
                    }

                    listener.onComplete(Tasks.forResult(null));
                });
    }

    private void showLoading() {
        requireActivity().runOnUiThread(() -> {
            overlay.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);
        });
    }

    private void hideLoading() {
        requireActivity().runOnUiThread(() -> {
            overlay.setVisibility(View.GONE);
            loadingSpinner.setVisibility(View.GONE);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private void fetchFilteredDonationSites(int distance, List<String> bloodTypes, String eventDateString) {
        showLoading();

        new Thread(() -> {
            List<User> users = new ArrayList<>();
            try {
                // Fetch users
                List<User> fetchedUsers = Tasks.await(db.collection("users").get()).toObjects(User.class);
                users.addAll(fetchedUsers);

                // Fetch events
                fetchEvents(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("HomePage", "Error fetching events.", task.getException());
                        throw new RuntimeException("Failed to fetch events", task.getException());
                    }
                });

                // Get location
                getLocation(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("HomePage", "Error getting location", task.getException());
                        throw new RuntimeException("Failed to get location", task.getException());
                    }
                });

                // Fetch donation sites
                List<DonationSite> filteredSites = new ArrayList<>();
                List<DonationSite> fetchedSites =
                        Tasks.await(
                                db.collection("donationSites").get())
                                .toObjects(DonationSite.class);

                // Convert eventDateString to Date object
                Date eventDate = getDate(eventDateString);

                for (DonationSite site : fetchedSites) {
                    // Distance filter (if you want to use it)
                    if (userLocation != null) {
                        double distanceToSite = calculateDistance(
                                userLocation.getLatitude(), userLocation.getLongitude(),
                                site.getLatitude(), site.getLongitude()
                        );
                        if (distanceToSite > distance) {
                            continue;
                        }
                    }

                    // Blood type filter
                    if (!bloodTypes.isEmpty() && !new HashSet<>(site.getNeededBloodTypes()).containsAll(bloodTypes)) {
                        continue;
                    }

                    // Event date filter
                    if (eventDate != null) {
                        for (DonationSiteEvent event : site.getEvents(events)) {
                            if (sdf.format(event.getEventDate()).equals(sdf.format(eventDate))) {
                                filteredSites.add(site);
                                break;
                            }
                        }
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    if (filteredSites.isEmpty()) {
                        Log.d("HomePage", "Not found!");
                        emptyInform.setVisibility(View.VISIBLE);
                        viewDonationSitesRecyclerView.setVisibility(View.GONE);
                    } else {
                        Log.d("HomePage", filteredSites.toString());
                        viewDonationSitesRecyclerView.setVisibility(View.VISIBLE);
                        adapter.donationSites = filteredSites;
                        adapter.users = users;
                        adapter.notifyDataSetChanged();
                    }
                    hideLoading();
                });
            } catch (Exception e) {
                Log.e("HomePage", "Error fetching data", e);
                requireActivity().runOnUiThread(this::hideLoading);
            }
        }).start();
    }

    private static @Nullable Date getDate(String eventDateString) {
        Date eventDate = null;
        if (!eventDateString.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                eventDate = dateFormat.parse(eventDateString);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return eventDate;
    }

    private void getLocation(OnCompleteListener<Location> listener) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            getCurrentLocation(listener);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(OnCompleteListener<Location> listener) {
        LocationRequest locationRequest =
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        LocationServices.getFusedLocationProviderClient(requireActivity())
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(requireActivity()).removeLocationUpdates(this);
                        if (!locationResult.getLocations().isEmpty()) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            userLocation = locationResult.getLocations().get(latestLocationIndex);
                            if (listener != null) {
                                listener.onComplete(Tasks.forResult(userLocation));
                            }
                        } else {
                            if (listener != null) {
                                listener.onComplete(Tasks.forResult(null));
                            }
                        }
                    }
                }, Looper.getMainLooper());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // This uses the Haversine formula
        double earthRadius = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // Distance in km
    }

    public interface FilterListener {
        void onFilterStart();
        void onFilterEnd();
    }
}