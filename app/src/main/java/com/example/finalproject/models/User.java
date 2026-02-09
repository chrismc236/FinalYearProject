package com.example.finalproject.models;

public class User {
    private String userId;
    private String name;
    private String email;
    private String profileImageUrl;
    private long createdAt;

    // Empty constructor required for Firebase
    public User() {
    }

    // Constructor for creating new users
    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}