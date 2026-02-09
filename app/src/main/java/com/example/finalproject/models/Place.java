package com.example.finalproject.models;

public class Place {
    private String id;
    private String userId;  // Added: ID of user who created this place
    private String title;
    private String description;
    private String imageUrl;
    private long timestamp;

    // Empty constructor required for Firebase
    public Place() {
    }

    // Updated constructor with userId
    public Place(String title, String description, String imageUrl, long timestamp, String userId) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}