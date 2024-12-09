package com.example.vein_blooddonationsite.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

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
