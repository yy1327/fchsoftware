package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("return")
    public int returnCode;

    @SerializedName("UserID")
    public String userId;

    @SerializedName("auth-token")
    public String authToken;

    @SerializedName("authtoken")
    public String authtoken;
}
