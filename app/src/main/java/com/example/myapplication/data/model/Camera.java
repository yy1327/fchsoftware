package com.example.myapplication.data.model;

public class Camera {
    private String id;
    private String name;
    private String location;
    private int thumbnailResId;
    private boolean isOnline;
    private String photoUrl;
    private String rtspUrl;

    public Camera(String id, String name, String location, int thumbnailResId, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.thumbnailResId = thumbnailResId;
        this.isOnline = isOnline;
        this.photoUrl = null;
        this.rtspUrl = null;
    }

    public Camera(String id, String name, String location, int thumbnailResId, boolean isOnline, String photoUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.thumbnailResId = thumbnailResId;
        this.isOnline = isOnline;
        this.photoUrl = photoUrl;
        this.rtspUrl = null;
    }

    public Camera(String id, String name, String location, int thumbnailResId, boolean isOnline, String photoUrl, String rtspUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.thumbnailResId = thumbnailResId;
        this.isOnline = isOnline;
        this.photoUrl = photoUrl;
        this.rtspUrl = rtspUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getThumbnailResId() { return thumbnailResId; }
    public boolean isOnline() { return isOnline; }
    public String getPhotoUrl() { return photoUrl; }
    public String getRtspUrl() { return rtspUrl; }
}
