package com.example.vein_blooddonationsite.models;

import androidx.annotation.NonNull;

public class User {

    private int userId;
    private String name;
    private String email;
    private String password;
    private String bloodType;
    private boolean isSuperUser;

    public User() {}

    public User(int userId, String name, String email, String password, String bloodType, boolean isSuperUser) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.bloodType = bloodType;
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

    public boolean isSuperUser() {
        return isSuperUser;
    }

    public void setSuperUser(boolean superUser) {
        isSuperUser = superUser;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                "\nname='" + name + '\'' +
                "\nemail='" + email + '\'' +
                "\npassword='" + password + '\'' +
                "\nbloodType='" + bloodType + '\'' +
                "\nisSuperUser=" + isSuperUser +
                '}';
    }
}
