package com.example.myapplication.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapplication.data.dao.CamerasDao;
import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.model.Cameras;
import com.example.myapplication.data.model.User;

@Database(entities = {User.class, Cameras.class}, version = 1, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract CamerasDao camerasDao();
}
