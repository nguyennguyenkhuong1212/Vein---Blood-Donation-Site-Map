package com.example.vein_blooddonationsite.models;

import androidx.annotation.NonNull;

import java.util.Date;

public class Donation {

    private String donationId;
    private int userId;
    private int siteId;
    private Date date;
    private String bloodType;
    private double amount;

    public Donation() {}

    public Donation(String donationId, int userId, int siteId, Date date, String bloodType, double amount) {
        this.donationId = donationId;
        this.userId = userId;
        this.siteId = siteId;
        this.date = date;
        this.bloodType = bloodType;
        this.amount = amount;
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @NonNull
    @Override
    public String toString() {
        return "Donation{" +
                "donationId='" + donationId + '\'' +
                "\nuserId='" + userId + '\'' +
                "\nsiteId='" + siteId + '\'' +
                "\ndate=" + date +
                "\nbloodType='" + bloodType + '\'' +
                "\namount=" + amount +
                '}';
    }
}
