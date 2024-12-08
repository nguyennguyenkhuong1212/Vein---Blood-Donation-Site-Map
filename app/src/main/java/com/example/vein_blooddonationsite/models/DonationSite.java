package com.example.vein_blooddonationsite.models;

import androidx.annotation.NonNull;

import java.util.List;

public class DonationSite {

    private int siteId;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String contactNumber;
    private String operatingHours;
    private List<String> neededBloodTypes;
    private int adminId;
    private List<Integer> followerIds;

    public DonationSite() {
    }

    public DonationSite(int siteId, String name, String address, double latitude, double longitude, String contactInfo, String operatingHours, List<String> neededBloodTypes, int adminId, List<Integer> followerIds) {
        this.siteId = siteId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactInfo;
        this.operatingHours = operatingHours;
        this.neededBloodTypes = neededBloodTypes;
        this.adminId = adminId;
        this.followerIds = followerIds;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public List<String> getNeededBloodTypes() {
        return neededBloodTypes;
    }

    public void setNeededBloodTypes(List<String> neededBloodTypes) {
        this.neededBloodTypes = neededBloodTypes;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public List<Integer> getFollowerIds() {
        return followerIds;
    }

    public void setFollowerIds(List<Integer> followerIds) {
        this.followerIds = followerIds;
    }

    @NonNull
    @Override
    public String toString() {
        return "DonationSite{" +
                "siteId='" + siteId + '\'' +
                "\nname='" + name + '\'' +
                "\naddress='" + address + '\'' +
                "\nlatitude=" + latitude +
                "\nlongitude=" + longitude +
                "\ncontactInfo='" + contactNumber + '\'' +
                "\noperatingHours='" + operatingHours + '\'' +
                "\nneededBloodTypes=" + neededBloodTypes +
                "\nadminId='" + adminId + '\'' +
                "\nfollowerIds=" + followerIds +
                '}';
    }
}
