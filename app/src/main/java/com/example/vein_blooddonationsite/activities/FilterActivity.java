package com.example.vein_blooddonationsite.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vein_blooddonationsite.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        SeekBar distanceSeekBar = findViewById(R.id.distance_seekbar);
        CheckBox checkboxO = findViewById(R.id.checkbox_o);
        CheckBox checkboxA = findViewById(R.id.checkbox_a);
        CheckBox checkboxB = findViewById(R.id.checkbox_b);
        CheckBox checkboxAB = findViewById(R.id.checkbox_ab);
        LinearLayout openDatePickerButton = findViewById(R.id.open_datepicker_button);
        TextView filterDistance = findViewById(R.id.filter_distance);
        TextView eventDate = findViewById(R.id.filter_event_date);
        Button confirmButton = findViewById(R.id.filter_confirm_button);
        Button cancelButton = findViewById(R.id.filter_cancel_button);

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress  >= 10) {
                    double progressInKm = progress / 10.0;
                    filterDistance.setText(String.format("%.1f km", progressInKm));
                } else {
                    filterDistance.setText(progress * 100 + " m");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        openDatePickerButton.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        eventDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        confirmButton.setOnClickListener(v -> {
            double distance = (double) distanceSeekBar.getProgress() * 100;
            ArrayList<String> bloodTypes = new ArrayList<>();
            if (checkboxO.isChecked()) {
                bloodTypes.add("O");
            }
            if (checkboxA.isChecked()) {
                bloodTypes.add("A");
            }
            if (checkboxB.isChecked()) {
                bloodTypes.add("B");
            }
            if (checkboxAB.isChecked()) {
                bloodTypes.add("AB");
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("distance", distance);
            resultIntent.putExtra("bloodTypes", bloodTypes);
            resultIntent.putExtra("eventDate", eventDate.getText().toString());
            setResult(RESULT_OK, resultIntent);

            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
    }
}