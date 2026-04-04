package com.example.finalproject.models;

public class Place {

    private String id;
    private String userName;
    private String userProfileImage;
    private String userId;
    private String title;
    private String description;
    private String imageUrl;
    private long   timestamp;
    private int    likesCount;

    private String locationTag;
    private String tips;
    private String category;
    private String mood;

    private double lat;
    private double lng;

    // Empty constructor required for Firebase
    public Place() {}

    public Place(String title, String description, String imageUrl,
                 long timestamp, String userId) {
        this.title       = title;
        this.description = description;
        this.imageUrl    = imageUrl;
        this.timestamp   = timestamp;
        this.userId      = userId;
        this.likesCount  = 0;
        this.lat         = 0;
        this.lng         = 0;
    }

    // ── Getters ──────────────────────────────────

    public String getId()          { return id; }
    public String getUserId()      { return userId; }
    public String getUserName() { return userName; }
    public String getUserProfileImage() { return userProfileImage; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public String getImageUrl()    { return imageUrl; }
    public long   getTimestamp()   { return timestamp; }
    public int    getLikesCount()  { return likesCount; }
    public double getLat()         { return lat; }
    public double getLng()         { return lng; }
    public String getLocationTag() { return locationTag; }
    public String getTips()        { return tips; }
    public String getCategory()    { return category; }
    public String getMood()        { return mood; }

    // ── Setters ──────────────────────────────────

    public void setId(String id)               { this.id = id; }
    public void setUserId(String userId)        { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserProfileImage(String url) { this.userProfileImage = url; }
    public void setTitle(String title)          { this.title = title; }
    public void setDescription(String d)        { this.description = d; }
    public void setImageUrl(String imageUrl)    { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp)    { this.timestamp = timestamp; }
    public void setLikesCount(int likesCount)   { this.likesCount = likesCount; }
    public void setLat(double lat)              { this.lat = lat; }
    public void setLng(double lng)              { this.lng = lng; }
    public void setLocationTag(String l)        { this.locationTag = l; }
    public void setTips(String tips)            { this.tips = tips; }
    public void setCategory(String category)    { this.category = category; }
    public void setMood(String mood)            { this.mood = mood; }
}