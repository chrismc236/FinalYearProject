package com.example.finalproject.models;

public class Place {
    private String id, title, description, imageUrl;
    private long timestamp;

    public Place() {} // firebase always requires an empty constructor

    public Place( String title, String description, String imageUrl, long timestamp){
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
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
}
