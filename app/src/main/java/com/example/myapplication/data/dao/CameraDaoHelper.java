package com.example.myapplication.data.dao;

import com.example.myapplication.MyApplication;
import com.example.myapplication.data.model.Cameras;

import java.util.List;

public class CameraDaoHelper {

    private static CameraDaoHelper instance;
    private final CamerasDao camerasDao;

    private CameraDaoHelper() {
        camerasDao = MyApplication.getAppDatabase().camerasDao();
    }

    public static CameraDaoHelper getInstance() {
        if (instance == null) {
            instance = new CameraDaoHelper();
        }
        return instance;
    }

    // ========== 增 ==========

    public long insertCamera(Cameras camera) {
        camera.prepareForSave();
        return camerasDao.insert(camera);
    }

    public void insertAll(List<Cameras> cameras) {
        for (Cameras cam : cameras) {
            cam.prepareForSave();
        }
        camerasDao.insertAll(cameras);
    }

    // ========== 删 ==========

    public void deleteCameraById(Long id) {
        camerasDao.deleteById(id);
    }

    public void deleteCameraByCameraId(String cameraId) {
        camerasDao.deleteByCameraId(cameraId);
    }

    public void deleteAllCameras() {
        camerasDao.deleteAll();
    }

    // ========== 改 ==========

    public void updateCamera(Cameras camera) {
        camera.prepareForSave();
        camerasDao.update(camera);
    }

    public void updateOrInsert(Cameras camera) {
        camera.prepareForSave();
        Cameras existing = getCameraByCameraId(camera.getCameraId());
        if (existing != null) {
            camera.setId(existing.getId());
            camerasDao.update(camera);
        } else {
            camerasDao.insert(camera);
        }
    }

    // ========== 查 ==========

    public List<Cameras> getAllCameras() {
        return camerasDao.getAll();
    }

    public Cameras getCameraById(Long id) {
        return camerasDao.getById(id);
    }

    public Cameras getCameraByCameraId(String cameraId) {
        return camerasDao.getByCameraId(cameraId);
    }

    public List<Cameras> searchCamerasByName(String keyword) {
        return camerasDao.searchByName(keyword);
    }

    public long getCameraCount() {
        return camerasDao.getCount();
    }

    // ========== 批量同步（事务操作，先清空再插入）==========

    public void syncCameras(List<Cameras> cameras) {
        for (Cameras cam : cameras) {
            cam.prepareForSave();
        }
        camerasDao.syncAll(cameras);
    }
}
