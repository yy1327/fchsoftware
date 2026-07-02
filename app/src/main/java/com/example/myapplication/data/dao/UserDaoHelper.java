package com.example.myapplication.data.dao;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.model.User;

import java.util.List;

public class UserDaoHelper {

    private static UserDaoHelper instance;
    private final UserDao userDao;

    private UserDaoHelper() {
        userDao = MyApplication.getAppDatabase().userDao();
    }

    public static UserDaoHelper getInstance() {
        if (instance == null) {
            instance = new UserDaoHelper();
        }
        return instance;
    }

    // ========== 增 ==========

    public long insertUser(User user) {
        return userDao.insert(user);
    }

    public void insertAll(List<User> users) {
        userDao.insertAll(users);
    }

    // ========== 删 ==========

    public void deleteUserById(Long id) {
        userDao.deleteById(id);
    }

    public void deleteUserByUserId(String userId) {
        userDao.deleteByUserId(userId);
    }

    public void deleteAllUsers() {
        userDao.deleteAll();
    }

    // ========== 改 ==========

    public void updateUser(User user) {
        userDao.update(user);
    }

    public void updateOrInsert(User user) {
        User existing = getUserByUserId(user.getUserId());
        if (existing != null) {
            user.setId(existing.getId());
            userDao.update(user);
        } else {
            userDao.insert(user);
        }
    }

    // ========== 查 ==========

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public User getUserById(Long id) {
        return userDao.getById(id);
    }

    public User getUserByUserId(String userId) {
        return userDao.getByUserId(userId);
    }

    public User getUserByPhone(String phone) {
        return userDao.getByPhone(phone);
    }

    public long getUserCount() {
        return userDao.getCount();
    }

    // ========== 分页查询 ==========

    public List<User> getUsersByPage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (pageNum - 1) * pageSize;
        return userDao.getByPage(offset, pageSize);
    }

    public List<User> getUsersByPageDesc(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (pageNum - 1) * pageSize;
        return userDao.getByPageDesc(offset, pageSize);
    }

    // ========== 模糊查询 ==========

    public List<User> searchUsersByName(String keyword) {
        return userDao.searchByName(keyword);
    }

    public List<User> searchUsersByPhone(String keyword) {
        return userDao.searchByPhone(keyword);
    }

    public List<User> searchUsers(String keyword) {
        return userDao.search(keyword);
    }

    // ========== 分页 + 模糊组合查询 ==========

    public List<User> searchUsersByPage(String keyword, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (pageNum - 1) * pageSize;
        return userDao.searchByPage(keyword, offset, pageSize);
    }
}
