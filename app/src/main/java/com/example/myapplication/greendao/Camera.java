package com.example.myapplication.greendao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Camera {
    @Id
    private Long id;

    @NotNull
    private String deviceId;

    private String name;

    private String ip;

    private int port;

    private String location;

    private boolean isOnline;

    private long lastUpdate;

    @Generated(hash = 1579011908)
    public Camera(Long id, @NotNull String deviceId, String name, String ip,
            int port, String location, boolean isOnline, long lastUpdate) {
        this.id = id;
        this.deviceId = deviceId;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.location = location;
        this.isOnline = isOnline;
        this.lastUpdate = lastUpdate;
    }

    @Generated(hash = 960541953)
    public Camera() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}