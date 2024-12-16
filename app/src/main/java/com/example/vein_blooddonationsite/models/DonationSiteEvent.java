package com.example.vein_blooddonationsite.models;

import androidx.annotation.NonNull;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DonationSiteEvent {

    private int eventId;
    private int siteId;
    private String eventName;
    private Date eventDate;
    private Map<String, Integer> startTime;
    private Map<String, Integer> endTime;
    private boolean isRecurring;
    private List<String> neededBloodTypes;


    public DonationSiteEvent() {
    }

    public DonationSiteEvent(int eventId, int siteId, String eventName, Date eventDate, Map<String, Integer> startTime, Map<String, Integer> endTime, boolean isRecurring, List<String> neededBloodTypes) {
        this.eventId = eventId;
        this.siteId = siteId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
        this.neededBloodTypes = neededBloodTypes;
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

    public Map<String, Integer> getStartTime() {
        return startTime;
    }

    public void setStartTime(Map<String, Integer> startTime) {
        this.startTime = startTime;
    }

    public Map<String, Integer> getEndTime() {
        return endTime;
    }

    public void setEndTime(Map<String, Integer> endTime) {
        this.endTime = endTime;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public List<String> getNeededBloodTypes() {
        return neededBloodTypes;
    }

    public void setNeededBloodTypes(List<String> neededBloodTypes) {
        this.neededBloodTypes = neededBloodTypes;
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
                "\nneededBloodTypes=" + neededBloodTypes +
                "}\n";
    }
}
