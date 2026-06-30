package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

public class Cameras {
    @SerializedName("cameraId")
    private String cameraId;

    @SerializedName("cameraName")
    private String cameraName;

    @SerializedName("cameraPhoto2")
    private String cameraPhoto2;

    @SerializedName("cameraStatus")
    private int cameraStatus;

    public String getCameraId() {
        return cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public String getCameraPhoto2() {
        return cameraPhoto2;
    }

    public int getCameraStatus() {
        return cameraStatus;
    }
}
