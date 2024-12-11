package com.example.vein_blooddonationsite.models;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.util.Date;

public class DonationSiteEvent {

    private int eventId;
    private int siteId;
    private String eventName;
    private Date eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isRecurring;

    public DonationSiteEvent() {
    }

    public DonationSiteEvent(int eventId, int siteId, String eventName, Date eventDate, LocalTime startTime, LocalTime endTime, boolean isRecurring) {
        this.eventId = eventId;
        this.siteId = siteId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    @NonNull
    @Override
    public String toString() {
        return "DonationSiteEvent{" +
                "eventId=" + eventId +
                "\nsiteId=" + siteId +
                "\neventName='" + eventName + '\'' +
                "\neventDate=" + eventDate +
                "\nstartTime=" + startTime +
                "\nendTime=" + endTime +
                "\nisRecurring=" + isRecurring +
                '}';
    }
}
