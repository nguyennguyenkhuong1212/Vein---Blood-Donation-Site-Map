package com.example.vein_blooddonationsite;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.vein_blooddonationsite.activities.LogInActivity;
import com.example.vein_blooddonationsite.fragments.ManageSitePage;
import com.example.vein_blooddonationsite.fragments.ProfilePage;
import com.example.vein_blooddonationsite.fragments.HomePage;
import com.example.vein_blooddonationsite.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    User currentUser;

    private final NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                int itemId = item.getItemId();
                Fragment selectedFragment = null;

                if (itemId == R.id.bottom_bar_home) {
                    selectedFragment = new HomePage();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", currentUser);
                    selectedFragment.setArguments(bundle);
                } else if (itemId == R.id.bottom_bar_manage_site) {
                    selectedFragment = new ManageSitePage();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", currentUser);
                    selectedFragment.setArguments(bundle);
                } else if (itemId == R.id.bottom_bar_profile) {
                    selectedFragment = new ProfilePage();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", currentUser);
                    selectedFragment.setArguments(bundle);
                } else { // default
                    selectedFragment = new HomePage();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", currentUser);
                    selectedFragment.setArguments(bundle);
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();

                return true;
            };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser != null) {
            // Check if user already logged in
            setContentView(R.layout.activity_main);
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.home);
            bottomNav.setOnItemSelectedListener(navListener);

            Fragment selectedFragment = new HomePage();
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", currentUser);
            selectedFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }
        else {
            Log.e("MainActivity", "User object not received");
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}