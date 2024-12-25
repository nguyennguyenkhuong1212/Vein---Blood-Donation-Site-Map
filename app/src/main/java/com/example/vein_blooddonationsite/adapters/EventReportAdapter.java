package com.example.vein_blooddonationsite.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vein_blooddonationsite.R;
import com.example.vein_blooddonationsite.models.DonationSiteEvent;
import com.example.vein_blooddonationsite.models.Registration;
import com.example.vein_blooddonationsite.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventReportAdapter extends RecyclerView.Adapter<EventReportAdapter.EventViewHolder> {

    private final List<DonationSiteEvent> events;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<User> users;

    public EventReportAdapter(List<DonationSiteEvent> events, List<User> users) {
        this.events = events;
        this.users = users;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_generate_report, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        DonationSiteEvent event = events.get(position);

        holder.eventNameTextView.setText(event.getEventName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String eventDateString = dateFormat.format(event.getEventDate());
        holder.eventDateTextView.setText(eventDateString);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        Map<String, Integer> startTimeMap = event.getStartTime();
        LocalTime startTime = LocalTime.of(startTimeMap.get("hour"), startTimeMap.get("minute"));
        String startTimeString = startTime.format(timeFormatter);

        Map<String, Integer> endTimeMap = event.getEndTime();
        LocalTime endTime = LocalTime.of(endTimeMap.get("hour"), endTimeMap.get("minute"));
        String endTimeString = endTime.format(timeFormatter);

        holder.eventStartTimeTextView.setText("Start Time: " + startTimeString);
        holder.eventEndTimeTextView.setText("End Time: " + endTimeString);

        List<String> neededBloodTypes = event.getNeededBloodTypes();
        String bloodTypesText = "Needed Blood Types: " + String.join(", ", neededBloodTypes);
        holder.eventBloodTypesTextView.setText(bloodTypesText);

        holder.generateReportButton.setOnClickListener(v -> {
            int eventId = event.getEventId();

            db.collection("registrations")
                    .whereEqualTo("eventId", eventId)
                    .whereEqualTo("status", "completed")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Registration> completedRegistrations = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Registration registration = document.toObject(Registration.class);
                                completedRegistrations.add(registration);
                            }

                            int totalBloodAmount = 0;
                            Map<String, Integer> bloodTypeCounts = new HashMap<>();

                            for (Registration registration : completedRegistrations) {
                                int userId = registration.getUserId();

                                User user = users.stream()
                                        .filter(u -> u.getUserId() == userId)
                                        .findFirst()
                                        .orElse(null);

                                if (user != null) {
                                    String bloodType = user.getBloodType();
                                    totalBloodAmount += 300;
                                    bloodTypeCounts.put(bloodType, bloodTypeCounts.getOrDefault(bloodType, 0) + 1);
                                } else {
                                    Log.w("GenerateReport", "User not found in local list for userId: " + userId);
                                }
                            }

                            holder.generatePdfReport(holder.itemView.getContext(), event, totalBloodAmount, bloodTypeCounts);
                        } else {
                            Log.e("GenerateReport", "Error getting registrations: ", task.getException());
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDateTextView;
        TextView eventStartTimeTextView;
        TextView eventEndTimeTextView;
        TextView eventBloodTypesTextView;
        LinearLayout generateReportButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.generate_report_event_name_textview);
            eventDateTextView = itemView.findViewById(R.id.generate_report_event_date_textview);
            eventStartTimeTextView = itemView.findViewById(R.id.generate_report_event_start_time_textview);
            eventEndTimeTextView = itemView.findViewById(R.id.generate_report_event_end_time_textview);
            eventBloodTypesTextView = itemView.findViewById(R.id.generate_report_event_needed_blood_types_textview);
            generateReportButton = itemView.findViewById(R.id.generate_report_button);
        }

        public void generatePdfReport(Context context, DonationSiteEvent event, int totalBloodAmount, Map<String, Integer> bloodTypeCounts) {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            int x = 50, y = 50;
            paint.setTextSize(18);
            canvas.drawText("Event Report", x, y, paint);
            y += 30;
            canvas.drawText("Event Name: " + event.getEventName(), x, y, paint);
            y += 20;

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String eventDateString = dateFormat.format(event.getEventDate());
            canvas.drawText("Date: " + eventDateString, x, y, paint);

            y += 30;
            canvas.drawText("Total Blood Collected: " + totalBloodAmount + " ml", x, y, paint);

            y += 30;
            canvas.drawText("Blood Type Breakdown:", x, y, paint);
            for (Map.Entry<String, Integer> entry : bloodTypeCounts.entrySet()) {
                y += 20;
                canvas.drawText(entry.getKey() + ": " + entry.getValue() + " units", x, y, paint);
            }

            document.finishPage(page);

            savePdfToMediaStore(context, document);
        }

        private void savePdfToMediaStore(Context context, PdfDocument document) {
            ContentResolver resolver = context.getContentResolver();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "event_report.pdf");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri pdfUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
            if (pdfUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                    if (outputStream != null) {
                        document.writeTo(outputStream);
                        Toast.makeText(context, "PDF saved to Downloads folder", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e("PDFSave", "Error saving PDF: ", e);
                    Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show();
                } finally {
                    document.close();
                }
            } else {
                Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}