package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CameraListResponse {
    @SerializedName("ret")
    public int ret;

    @SerializedName("message")
    public CameraMessage message;

    public static class CameraMessage {
        @SerializedName("all")
        public String all;

        @SerializedName("count")
        public int count;

        @SerializedName("cameras")
        public List<Cameras> cameras;
    }
}
