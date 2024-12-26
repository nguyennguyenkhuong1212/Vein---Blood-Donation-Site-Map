package com.example.vein_blooddonationsite.adapters;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
import java.util.Date;
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
                    .whereEqualTo("role", "DONOR")
                    .whereEqualTo("status", "COMPLETED")
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
            paint.setTypeface(ResourcesCompat.getFont(context, R.font.instrumentsansbold));
            paint.setColor(ContextCompat.getColor(context, R.color.primary));

            int canvasWidth = pageInfo.getPageWidth();

            String header = "Vein - Blood Donation Site Map";
            paint.setTextSize(16);
            float headerWidth = paint.measureText(header);
            canvas.drawText(header, (canvasWidth - headerWidth) / 2, 30, paint);

            int y = 180; // Start Y position
            paint.setTextSize(20);
            paint.setColor(Color.BLACK);

            // Centered "Event Report" title
            String title = "Event Report";
            float titleWidth = paint.measureText(title);
            canvas.drawText(title, (canvasWidth - titleWidth) / 2, y, paint);

            y += 30;

            // Event Name
            String eventName = "Event Name: " + event.getEventName();
            float eventNameWidth = paint.measureText(eventName);
            canvas.drawText(eventName, (canvasWidth - eventNameWidth) / 2, y, paint);

            y += 30;

            // Event Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String eventDateString = dateFormat.format(event.getEventDate());
            String eventDate = "Date: " + eventDateString;
            float eventDateWidth = paint.measureText(eventDate);
            canvas.drawText(eventDate, (canvasWidth - eventDateWidth) / 2, y, paint);

            y += 30;

            // Total Blood Collected
            String totalBlood = "Total Blood Collected: " + totalBloodAmount + " ml";
            float totalBloodWidth = paint.measureText(totalBlood);
            canvas.drawText(totalBlood, (canvasWidth - totalBloodWidth) / 2, y, paint);

            y += 60;
            canvas.drawText("Blood Types Breakdown: ", (canvasWidth - paint.measureText("Blood Types Breakdown: ")) / 2, y, paint);

            y += 140;

            // Blood Type Breakdown
            int pieChartY = y;
            int pieChartRadius = 100;
            int pieChartX = canvasWidth / 2;
            RectF pieChartRect = new RectF(pieChartX - pieChartRadius, pieChartY - pieChartRadius,
                    pieChartX + pieChartRadius, pieChartY + pieChartRadius);

            float totalUnits = 0;
            for (int count : bloodTypeCounts.values()) {
                totalUnits += count;
            }

            float startAngle = 0;
            for (Map.Entry<String, Integer> entry : bloodTypeCounts.entrySet()) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(getColorForBloodType(context, entry.getKey())); // Pass context
                float sweepAngle = (entry.getValue() / totalUnits) * 360;
                canvas.drawArc(pieChartRect, startAngle, sweepAngle, true, paint);
                startAngle += sweepAngle;
            }

            // Legend for the pie chart
            y = pieChartY + pieChartRadius + 30;
            paint.setTextSize(16);

            int legendX = pieChartX + 10;

            for (Map.Entry<String, Integer> entry : bloodTypeCounts.entrySet()) {
                paint.setColor(getColorForBloodType(context, entry.getKey()));

                // Adjust rectangle and text positions based on legendX
                canvas.drawRect(legendX - 100, y - 10, legendX - 80, y + 10, paint);
                canvas.drawText(entry.getKey() + ": " + entry.getValue() + (entry.getValue() > 1 ? " units" : " unit") + " (" + entry.getValue() * 300 + "ml)", legendX - 70, y + 5, paint);

                y += 20;
            }

            document.finishPage(page);

            savePdfToMediaStore(context, document);
        }

        private void savePdfToMediaStore(Context context, PdfDocument document) {
            ContentResolver resolver = context.getContentResolver();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "event_report_" + timeStamp + ".pdf";

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");

            // Set to a valid directory
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri pdfUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
            if (pdfUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                    if (outputStream != null) {
                        document.writeTo(outputStream);
                        Toast.makeText(context, "Report created successfully", Toast.LENGTH_SHORT).show();
                        openPdfFile(context, pdfUri);
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

        private void openPdfFile(Context context, Uri pdfUri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(intent, "Open PDF");
            try {
                context.startActivity(chooser);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No application found to open PDF", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private static int getColorForBloodType(Context context, String bloodType) {
        switch (bloodType) {
            case "A":
                return ContextCompat.getColor(context, R.color.primary);
            case "B":
                return ContextCompat.getColor(context, R.color.secondary);
            case "AB":
                return ContextCompat.getColor(context, R.color.red_tone_1);
            case "O":
                return ContextCompat.getColor(context, R.color.red_tone_2);
            default:
                return Color.MAGENTA;
        }
    }
}