package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "cameras")
public class Cameras {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "camera_id")
    @SerializedName("CameraID")
    private String cameraId;

    @ColumnInfo(name = "camera_name")
    @SerializedName("CameraName")
    private String cameraName;

    @ColumnInfo(name = "camera_photo2")
    @SerializedName("cameraPhoto2")
    private String cameraPhoto2;

    @ColumnInfo(name = "camera_code")
    @SerializedName("CameraCode")
    private String cameraCode;

    private String rtspUrl;

    @Ignore
    @SerializedName("VideoRelayInfo")
    private VideoRelayInfo videoRelayInfo;

    public Cameras() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraPhoto2() {
        return cameraPhoto2;
    }

    public void setCameraPhoto2(String cameraPhoto2) {
        this.cameraPhoto2 = cameraPhoto2;
    }

    public String getCameraCode() {
        return cameraCode;
    }

    public void setCameraCode(String cameraCode) {
        this.cameraCode = cameraCode;
    }

    public String getRtspUrl() {
        if (videoRelayInfo != null && videoRelayInfo.relayedStreamURL != null) {
            return videoRelayInfo.relayedStreamURL;
        }
        return rtspUrl;
    }

    public void setRtspUrl(String rtspUrl) {
        this.rtspUrl = rtspUrl;
    }

    /**
     * 从 videoRelayInfo 提取 rtspUrl 写入持久化字段，
     * 保存数据库前调用，防止 videoRelayInfo 被 @Ignore 后数据丢失
     */
    public void prepareForSave() {
        if (videoRelayInfo != null && videoRelayInfo.relayedStreamURL != null) {
            this.rtspUrl = videoRelayInfo.relayedStreamURL;
        }
    }

    public static class VideoRelayInfo {
        @SerializedName("RelayedStreamURL")
        public String relayedStreamURL;
    }
}
