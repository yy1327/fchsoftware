package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> {
    @SerializedName("result")
    public T result;
}
