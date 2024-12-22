package com.example.vein_blooddonationsite.models;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class User implements Serializable {

    private int userId;
    private String name;
    private String email;
    private String username;
    private String password;
    private String bloodType;
    private boolean isSiteAdmin;
    private boolean isSuperUser;

    public User() {}

    public User(int userId, String name, String email, String username, String password, String bloodType, boolean isSiteAdmin, boolean isSuperUser) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.bloodType = bloodType;
        this.isSiteAdmin = isSiteAdmin;
        this.isSuperUser = isSuperUser;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public boolean isSiteAdmin() {
        return isSiteAdmin;
    }

    public void setSiteAdmin(boolean siteAdmin) {
        isSiteAdmin = siteAdmin;
    }

    public boolean isSuperUser() {
        return isSuperUser;
    }

    public void setSuperUser(boolean superUser) {
        isSuperUser = superUser;
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    public Date getLastDonationDate(List<Registration> registrations){
        if (registrations == null || registrations.isEmpty()) {
            return null; // No donations yet
        }

        // Filter for "DONOR" registrations with matching userId
        List<Registration> donorRegistrations = registrations.stream()
                .filter(r -> r.getRole().equals("DONOR")
                        && r.getUserId() == userId)
                .toList();

        if (donorRegistrations.isEmpty()) {
            return null; // No completed donor registrations for this user
        }

        // 1. Create a new ArrayList from the filtered list
        List<Registration> sortedDonorRegistrations = new ArrayList<>(donorRegistrations);

        // 2. Sort the new list
        sortedDonorRegistrations.sort(Comparator.comparing(Registration::getDonationDate).reversed());

        // Return the donationDate of the most recent registration
        return sortedDonorRegistrations.get(0).getDonationDate();
    }

    public int getNumMedals(List<Registration> registrations){
        int numMedals = 0;
        Log.d("AHAHAHAHAHA", String.valueOf(registrations.size()));
        for (Registration registration : registrations) {
            Log.d("Status",  registration.getStatus());
            if (registration.getStatus().equals("COMPLETED")) {
                numMedals++;
            }
        }
        return numMedals;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                "\nname='" + name + '\'' +
                "\nemail='" + email + '\'' +
                "\npassword='" + password + '\'' +
                "\nbloodType='" + bloodType + '\'' +
                "\nisSiteAdmin=" + isSiteAdmin +
                "\nisSuperUser=" + isSuperUser +
                '}';
    }
}
