package com.example.vein_blooddonationsite;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the splash screen layout

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed after the specified delay
                setContentView(R.layout.activity_log_in); // Switch to the login page
            }
        }, 3000); // Delay for 3 seconds (3000 milliseconds)
    }
}