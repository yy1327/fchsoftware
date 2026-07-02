package com.example.myapplication.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import androidx.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId = "";

    @ColumnInfo(name = "user_name")
    private String userName;

    private String phone;
    private String token;

    @ColumnInfo(name = "create_time")
    private Date createTime;

    public User() {
    }

    @Ignore
    public User(Long id, String userId, String userName, String phone, String token, Date createTime) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.phone = phone;
        this.token = token;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
