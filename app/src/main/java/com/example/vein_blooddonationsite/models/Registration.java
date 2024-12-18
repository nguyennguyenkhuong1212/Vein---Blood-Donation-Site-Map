package com.example.vein_blooddonationsite.models;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Registration {

    private int registrationId;
    private int userId;
    private int siteId;
    private int eventId;
    private Date registrationDate;
    private Date donationDate;
    private Map<String, Integer> donationTime;
    private String status; // "COMPLETED", "PENDING", "APPROVED", "DECLINED"
    // "COMPLETED": For user who already donate blood
    // "PENDING": For user who waits for approval from the site manager
    // "APPROVED": For user who already got an approval
    // "DECLINED": For user who already got declined

    private String role; // "DONOR", "MANAGER", "VOLUNTEER"
    // "DONOR": For user who goes to an event as a donor
    // "MANAGER": For site admin only, when they create an event, they will be registered as MANAGER,
    // and they need to be in person in each of their event
    // "VOLUNTEER": For other site admin who wants to work as a volunteer at the site not their own


    public Registration() {}

    public Registration(int registrationId, int userId, int siteId, int eventId, Date registrationDate, Date donationDate, Map<String, Integer> donationTime, String status, String role) {
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

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
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

    public Map<String, Integer> getDonationTime() {
        return donationTime;
    }

    public void setDonationTime(Map<String, Integer> donationTime) {
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

    public User getUser(List<User> users) {
        for (User user : users) {
            if (user.getUserId() == this.userId) {
                return user;
            }
        }
        return null;
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


