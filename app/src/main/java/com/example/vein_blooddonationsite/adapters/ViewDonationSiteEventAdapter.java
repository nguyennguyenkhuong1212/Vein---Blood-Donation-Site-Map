package com.example.vein_blooddonationsite.adapters;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSite;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.Registration;
import com.example.vein_blooddonationsite.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ViewDonationSiteEventAdapter extends RecyclerView.Adapter<ViewDonationSiteEventAdapter.ViewHolder> {

    public List<DonationSiteEvent> events;

    public List<Registration> registrations;

    public User currentUser;

    public DonationSite currentSite;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ViewDonationSiteEventAdapter(List<DonationSiteEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_in_manage_event_list, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationSiteEvent event = events.get(position);

        holder.eventNameTextView.setText(event.getEventName());

        // Format the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String eventDateString = dateFormat.format(event.getEventDate());
        holder.dateTextView.setText(eventDateString);

        // Format the time (no need for SDK version check)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Assuming startTime and endTime are Maps in your DonationSiteEvent class
        Map<String, Integer> startTimeMap = event.getStartTime();
        LocalTime startTime = LocalTime.of(startTimeMap.get("hour"), startTimeMap.get("minute"));
        String startTimeString = startTime.format(timeFormatter);

        Map<String, Integer> endTimeMap = event.getEndTime();
        LocalTime endTime = LocalTime.of(endTimeMap.get("hour"), endTimeMap.get("minute"));
        String endTimeString = endTime.format(timeFormatter);

        holder.startTimeTextView.setText("Start Time: " + startTimeString);
        holder.endTimeTextView.setText("End Time: " + endTimeString);

        if (event.isRecurring()){
            holder.recurringTextView.setVisibility(View.VISIBLE);
        }

        holder.neededBloodTypes.setText(
                "Needed Blood Types: " + String.join(", ", event.getNeededBloodTypes()));

        holder.registerRoleDonor.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            if (!event.getNeededBloodTypes().contains(currentUser.getBloodType())) {
                Toast.makeText(v.getContext(), "This event seeks other blood types. Thank you for your understanding.", Toast.LENGTH_SHORT).show();
                return;
            }

            Date lastDonationDate = currentUser.getLastDonationDate(registrations);

            if (!(lastDonationDate == null)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(lastDonationDate);
                calendar.add(Calendar.WEEK_OF_YEAR, 12);
                Date twelveWeeksAfterDonation = calendar.getTime();

                Date currentDate = new Date();

                if (!currentDate.after(twelveWeeksAfterDonation)) {
                    // It has not been 12 weeks yet
                    Toast.makeText(v.getContext(),
                            "You must wait at least 12 weeks after your last donation.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    v.getContext(),
                    R.style.MyDateTimePickerDialogTheme,
                    (view, selectedHour, selectedMinute) -> {
                        LocalTime donationTime = LocalTime.of(selectedHour, selectedMinute);

                        if (donationTime.isBefore(startTime) || donationTime.isAfter(endTime)) {
                            Toast.makeText(v.getContext(), "Please choose a time within the event's timeframe.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Integer> donationTimeMap = new HashMap<>();
                        donationTimeMap.put("hour", donationTime.getHour());
                        donationTimeMap.put("minute", donationTime.getMinute());

                        getNewRegistrationId(task -> {
                            if (task.isSuccessful()) {
                                int newRegistrationId = task.getResult();
                                Registration registration = new Registration(
                                        newRegistrationId,
                                        currentUser.getUserId(),
                                        currentSite.getSiteId(),
                                        event.getEventId(),
                                        new Date(),
                                        event.getEventDate(),
                                        donationTimeMap,
                                        "PENDING",
                                        "DONOR"
                                );

                                db.collection("registrations")
                                        .document(String.valueOf(newRegistrationId))
                                        .set(registration)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Registration", "Registration added with ID: " + newRegistrationId);
                                            Toast.makeText(v.getContext(), "Registered as DONOR", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("Registration", "Error updating registration ID", e);
                                            Toast.makeText(v.getContext(), "Error: Cannot be registered as DONOR", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
                    },
                    hour, minute, true
            );
            timePickerDialog.show();
        });

        holder.registerRoleVolunteer.setOnClickListener(v -> {
            getNewRegistrationId(task -> {
                if (task.isSuccessful()) {
                    int newRegistrationId = task.getResult();

                    Map<String, Integer> donationTimeMap = new HashMap<>();
                    donationTimeMap.put("hour", startTime.getHour());
                    donationTimeMap.put("minute", startTime.getMinute());

                    Registration registration = new Registration(
                            newRegistrationId,
                            currentUser.getUserId(),
                            currentSite.getSiteId(),
                            event.getEventId(),
                            new Date(),
                            event.getEventDate(),
                            donationTimeMap,
                            "PENDING",
                            "VOLUNTEER"
                    );

                    db.collection("registrations")
                            .document(String.valueOf(newRegistrationId))
                            .set(registration)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Registration", "Registration added with ID: " + newRegistrationId);
                                Toast.makeText(v.getContext(), "Registered as VOLUNTEER", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Registration", "Error updating registration ID", e);
                                Toast.makeText(v.getContext(), "Error: Cannot be registered as VOLUNTEER", Toast.LENGTH_SHORT).show();
                            });
                }
            });
        });

        // If current user is not current site's admin, display donor button
        if (currentSite.getAdminId() != currentUser.getUserId()){
            holder.registerRoleButtons.setVisibility(View.VISIBLE);
            holder.registerRoleDonor.setVisibility(View.VISIBLE);

            // If current user is a site admin and is not current site's admin, display volunteer button
            if (currentUser.isSiteAdmin()){
                holder.registerRoleVolunteer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView;
        public TextView dateTextView;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public TextView recurringTextView;
        public LinearLayout registerRoleButtons;
        public LinearLayout registerRoleDonor;
        public LinearLayout registerRoleVolunteer;
        public TextView neededBloodTypes;

        public ViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name_textview);
            dateTextView = itemView.findViewById(R.id.event_date_textview);
            startTimeTextView = itemView.findViewById(R.id.event_start_time_textview);
            endTimeTextView = itemView.findViewById(R.id.event_end_time_textview);
            recurringTextView = itemView.findViewById(R.id.event_recurring_textview);
            registerRoleButtons = itemView.findViewById(R.id.register_role_buttons);
            registerRoleDonor = itemView.findViewById(R.id.register_role_donor);
            registerRoleVolunteer = itemView.findViewById(R.id.register_role_volunteer);
            neededBloodTypes = itemView.findViewById(R.id.event_needed_blood_types_textview);
        }
    }

    private void getNewRegistrationId(OnCompleteListener<Integer> listener) {
        db.collection("registrations")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int maxRegistrationId = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            int registrationId = Math.toIntExact(document.getLong("registrationId"));
                            if (registrationId > maxRegistrationId) {
                                maxRegistrationId = registrationId;
                            }
                        } catch (NumberFormatException e) {
                            Log.e("Firestore", "Error parsing registrationId", e);
                        }
                    }
                    listener.onComplete(Tasks.forResult(maxRegistrationId + 1));
                } else {
                    Log.w("Firestore", "Error getting documents.", task.getException());
                    listener.onComplete(Tasks.forException(Objects.requireNonNull(task.getException())));
                }
            });
    }
}