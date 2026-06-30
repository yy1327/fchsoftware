package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CameraListResponse {
    @SerializedName("list")
    public List<Cameras> list;

    @SerializedName("total")
    public int total;
}
