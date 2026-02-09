package com.example.finalproject.models;

public class Place {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String imageUrl;
    private long timestamp;
    private int likesCount;  // NEW: Number of likes

    // Empty constructor required for Firebase
    public Place() {
    }

    // Updated constructor
    public Place(String title, String description, String imageUrl, long timestamp, String userId) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.userId = userId;
        this.likesCount = 0;  // Initialize to 0
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

    public int getLikesCount() {
        return likesCount;
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

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }
}