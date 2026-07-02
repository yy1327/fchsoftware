package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.model.User;

import java.util.List;

@Dao
public interface UserDao {

    // ========== 增 ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    // ========== 删 ==========

    @Query("DELETE FROM user WHERE id = :id")
    void deleteById(Long id);

    @Query("DELETE FROM user WHERE user_id = :userId")
    void deleteByUserId(String userId);

    @Query("DELETE FROM user")
    void deleteAll();

    // ========== 改 ==========

    @Update
    void update(User user);

    // ========== 查 ==========

    @Query("SELECT * FROM user")
    List<User> getAllUsers();

    @Query("SELECT * FROM user WHERE id = :id")
    User getById(Long id);

    @Query("SELECT * FROM user WHERE user_id = :userId")
    User getByUserId(String userId);

    @Query("SELECT * FROM user WHERE phone = :phone")
    User getByPhone(String phone);

    @Query("SELECT COUNT(*) FROM user")
    long getCount();

    // ========== 分页查询 ==========

    @Query("SELECT * FROM user ORDER BY user_name ASC LIMIT :limit OFFSET :offset")
    List<User> getByPage(int offset, int limit);

    @Query("SELECT * FROM user ORDER BY create_time DESC LIMIT :limit OFFSET :offset")
    List<User> getByPageDesc(int offset, int limit);

    // ========== 模糊查询 ==========

    @Query("SELECT * FROM user WHERE user_name LIKE '%' || :keyword || '%'")
    List<User> searchByName(String keyword);

    @Query("SELECT * FROM user WHERE phone LIKE '%' || :keyword || '%'")
    List<User> searchByPhone(String keyword);

    @Query("SELECT * FROM user WHERE user_name LIKE '%' || :keyword || '%' OR phone LIKE '%' || :keyword || '%'")
    List<User> search(String keyword);

    // ========== 分页 + 模糊组合查询 ==========

    @Query("SELECT * FROM user WHERE user_name LIKE '%' || :keyword || '%' OR phone LIKE '%' || :keyword || '%' ORDER BY user_name ASC LIMIT :limit OFFSET :offset")
    List<User> searchByPage(String keyword, int offset, int limit);
}
