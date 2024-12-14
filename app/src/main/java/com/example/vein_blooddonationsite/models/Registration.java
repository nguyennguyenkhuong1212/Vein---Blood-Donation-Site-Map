package com.example.vein_blooddonationsite.models;

import java.time.LocalTime;
import java.util.Date;

public class Registration {

    private String registrationId;
    private int userId;
    private int siteId;
    private int eventId;
    private Date registrationDate;
    private Date donationDate;
    private LocalTime donationTime;
    private String status;
    private String role;

    public Registration() {}

    public Registration(String registrationId, int userId, int siteId, int eventId, Date registrationDate, Date donationDate, LocalTime donationTime, String status, String role) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.siteId = siteId;
        this.eventId = eventId;
        this.registrationDate = registrationDate;
        this.donationDate = donationDate;
        this.donationTime = donationTime;
        this.status = status;
        this.role = role;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(Date donationDate) {
        this.donationDate = donationDate;
    }

    public LocalTime getDonationTime() {
        return donationTime;
    }

    public void setDonationTime(LocalTime donationTime) {
        this.donationTime = donationTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "\nRegistration{" +
                "\nregistrationId='" + registrationId + '\'' +
                "\nuserId=" + userId +
                "\nsiteId=" + siteId +
                "\neventId=" + eventId +
                "\nregistrationDate=" + registrationDate +
                "\ndonationDate=" + donationDate +
                "\ndonationTime=" + donationTime +
                "\nstatus='" + status +
                "\nrole='" + role +
                "\n}";
    }
}


