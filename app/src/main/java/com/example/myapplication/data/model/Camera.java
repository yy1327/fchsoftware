package com.example.myapplication.data.model;

public class Camera {
    private String id;
    private String name;
    private String location;
    private int thumbnailResId;
    private boolean isOnline;

    public Camera(String id, String name, String location, int thumbnailResId, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.thumbnailResId = thumbnailResId;
        this.isOnline = isOnline;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getThumbnailResId() { return thumbnailResId; }
    public boolean isOnline() { return isOnline; }
}
