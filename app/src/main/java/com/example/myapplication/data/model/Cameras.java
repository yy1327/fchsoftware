package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

public class Cameras {
    @SerializedName("CameraID")
    private String cameraId;

    @SerializedName("CameraName")
    private String cameraName;

    @SerializedName("cameraPhoto2")
    private String cameraPhoto2;

    @SerializedName("CameraCode")
    private String cameraCode;

    @SerializedName("VideoRelayInfo")
    private VideoRelayInfo videoRelayInfo;

    public String getCameraId() {
        return cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public String getCameraPhoto2() {
        return cameraPhoto2;
    }

    public String getCameraCode() {
        return cameraCode;
    }

    public String getRtspUrl() {
        if (videoRelayInfo != null && videoRelayInfo.relayedStreamURL != null) {
            return videoRelayInfo.relayedStreamURL;
        }
        return null;
    }

    public static class VideoRelayInfo {
        @SerializedName("RelayedStreamURL")
        public String relayedStreamURL;
    }
}
