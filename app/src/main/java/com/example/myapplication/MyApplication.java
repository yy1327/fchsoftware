package com.example.myapplication;

import android.app.Application;

import androidx.room.Room;

import com.example.myapplication.data.AppDatabase;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static AppDatabase appDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initRoom();
    }

    private void initRoom() {
        appDatabase = Room.databaseBuilder(this, AppDatabase.class, "app-db")
                .allowMainThreadQueries()
                .build();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
