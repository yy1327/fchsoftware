package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.myapplication.data.model.Cameras;

import java.util.List;

@Dao
public interface CamerasDao {

    // ========== 增 ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Cameras camera);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Cameras> cameras);

    // ========== 删 ==========

    @Query("DELETE FROM cameras WHERE id = :id")
    void deleteById(Long id);

    @Query("DELETE FROM cameras WHERE camera_id = :cameraId")
    void deleteByCameraId(String cameraId);

    @Query("DELETE FROM cameras")
    void deleteAll();

    // ========== 改 ==========

    @Update
    void update(Cameras camera);

    // ========== 查 ==========

    @Query("SELECT * FROM cameras")
    List<Cameras> getAll();

    @Query("SELECT * FROM cameras WHERE id = :id")
    Cameras getById(Long id);

    @Query("SELECT * FROM cameras WHERE camera_id = :cameraId")
    Cameras getByCameraId(String cameraId);

    @Query("SELECT * FROM cameras WHERE camera_name LIKE '%' || :keyword || '%'")
    List<Cameras> searchByName(String keyword);

    @Query("SELECT COUNT(*) FROM cameras")
    long getCount();

    // ========== 事务操作 ==========

    @Transaction
    default void syncAll(List<Cameras> cameras) {
        deleteAll();
        insertAll(cameras);
    }
}
