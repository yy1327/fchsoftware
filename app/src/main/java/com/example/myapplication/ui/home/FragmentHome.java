package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.BaseResponse;
import com.example.myapplication.data.model.Camera;
import com.example.myapplication.data.model.CameraListResponse;
import com.example.myapplication.data.model.TabFilter;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.ui.camera.CameraActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentHome extends Fragment {
    private RecyclerView rvTabFilters;
    private RecyclerView rvCameras;
    private EditText etSearch;
    private ImageView ivFilter;
    private TabFilterAdapter tabFilterAdapter;
    private CameraThumbnailAdapter cameraThumbnailAdapter;
    private List<TabFilter> tabFilters;
    private List<Camera> allCameras;
    private List<Camera> filteredCameras;
    private String token;
    private String userId;
    private int pageNo = 0;
    private int pageSize = 30;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString("token");
            userId = getArguments().getString("userId");
        }
        initViews(view);
        loadData();
        setupTabFilters();
        setupCameraGrid();
        setupSearch();
        setupFilterButton();
        loadCameraList();
    }

    private void initViews(View view) {
        rvTabFilters = view.findViewById(R.id.rvTabFilters);
        rvCameras = view.findViewById(R.id.rvCameras);
        etSearch = view.findViewById(R.id.etSearch);
        ivFilter = view.findViewById(R.id.ivFilter);
    }

    private void loadData() {
        tabFilters = getTabFilters();
        allCameras = new ArrayList<>();
        filteredCameras = new ArrayList<>(allCameras);
    }

    private void loadCameraList() {
        if (token == null || token.isEmpty()) {
            Log.d("Home", "Token为空，使用模拟数据");
            allCameras = getMockCameras();
            filteredCameras = new ArrayList<>(allCameras);
            cameraThumbnailAdapter.updateCameras(filteredCameras);
            return;
        }

        Log.d("Home", "请求摄像头列表: token=" + token + ", userId=" + userId);
        RetrofitClient.getInstance()
                .getApiService()
                .getCameraList(token, userId, pageNo, pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResponse<CameraListResponse>>() {
                    @Override
                    public void onNext(BaseResponse<CameraListResponse> response) {
                        if (response.result != null && response.result.message != null && response.result.message.cameras != null) {
                            Log.d("Home", "获取到 " + response.result.message.cameras.size() + " 个摄像头");
                            Log.d("Home", "第一个摄像头: " + response.result.message.cameras.get(0).getCameraName());
                            allCameras.clear();
                            for (int i = 0; i < response.result.message.cameras.size(); i++) {
                                com.example.myapplication.data.model.Cameras item = response.result.message.cameras.get(i);
                                String photoUrl = (item.getCameraPhoto2() != null && !item.getCameraPhoto2().isEmpty()) ? item.getCameraPhoto2() : null;
                                String rtspUrl = item.getRtspUrl();
                                Log.d("Home", "摄像头: " + item.getCameraName() + ", RTSP: " + rtspUrl);
                                allCameras.add(new Camera(
                                    item.getCameraId(),
                                    item.getCameraName(),
                                    "全部",
                                    R.drawable.ic_camera_thumbnail,
                                    true,
                                    photoUrl,
                                    rtspUrl
                                ));
                            }
                            filteredCameras = new ArrayList<>(allCameras);
                            cameraThumbnailAdapter.updateCameras(filteredCameras);
                        } else {
                            Log.d("Home", "无摄像头数据，使用模拟数据");
                            allCameras = getMockCameras();
                            Log.d("Home", "模拟数据数量: " + allCameras.size());
                            if (!allCameras.isEmpty()) {
                                Log.d("Home", "第一个模拟摄像头: " + allCameras.get(0).getName());
                            }
                            filteredCameras = new ArrayList<>(allCameras);
                            cameraThumbnailAdapter.updateCameras(filteredCameras);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Home", "请求失败: " + e.getMessage() + "，使用模拟数据");
                        allCameras = getMockCameras();
                        filteredCameras = new ArrayList<>(allCameras);
                        cameraThumbnailAdapter.updateCameras(filteredCameras);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private List<Camera> getMockCameras() {
        List<Camera> list = new ArrayList<>();
        list.add(new Camera("1", "行政楼1层 C019", "行政办公楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("2", "行政楼2层 C020", "行政办公楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("3", "行政楼2层 C021", "行政办公楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("4", "行政楼2层 C026", "行政办公楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("5", "行政楼3层 C034", "行政办公楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("6", "行政楼3层 C064", "行政办公楼", R.drawable.ic_camera_thumbnail, false));
        list.add(new Camera("7", "教学楼A101", "公共教学楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("8", "教学楼B201", "公共教学楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("9", "教学楼C301", "公共教学楼", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("10", "广场东入口", "1号主广场", R.drawable.ic_camera_thumbnail, true));
        list.add(new Camera("11", "广场西入口", "1号主广场", R.drawable.ic_camera_thumbnail, false));
        list.add(new Camera("12", "广场中心", "1号主广场", R.drawable.ic_camera_thumbnail, true));
        return list;
    }

    private List<TabFilter> getTabFilters() {
        List<TabFilter> list = new ArrayList<>();
        list.add(new TabFilter("全部", true));
        list.add(new TabFilter("行政办公楼", false));
        list.add(new TabFilter("公共教学楼", false));
        list.add(new TabFilter("1号主广场", false));
        return list;
    }

    private void setupTabFilters() {
        tabFilterAdapter = new TabFilterAdapter(tabFilters);
        rvTabFilters.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTabFilters.setAdapter(tabFilterAdapter);

        tabFilterAdapter.setOnTabSelectedListener(position -> {
            for (int i = 0; i < tabFilters.size(); i++) {
                tabFilters.get(i).setSelected(i == position);
            }
            tabFilterAdapter.notifyDataSetChanged();
            filterCameras();
        });
    }

    private void setupCameraGrid() {
        cameraThumbnailAdapter = new CameraThumbnailAdapter(filteredCameras);
        rvCameras.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvCameras.setAdapter(cameraThumbnailAdapter);

        cameraThumbnailAdapter.setOnCameraClickListener(camera -> {
            Intent intent = new Intent(requireContext(), CameraActivity.class);
            intent.putExtra("camera_id", camera.getId());
            intent.putExtra("camera_name", camera.getName());
            intent.putExtra("rtsp_url", camera.getRtspUrl());
            startActivity(intent);
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCameras();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCameras() {
        String searchQuery = etSearch.getText().toString().trim().toLowerCase();
        String selectedTab = getSelectedTab();

        filteredCameras.clear();
        for (Camera camera : allCameras) {
            boolean matchesTab = selectedTab.equals("全部") || camera.getLocation().equals(selectedTab);
            boolean matchesSearch = searchQuery.isEmpty() || camera.getName().toLowerCase().contains(searchQuery);

            if (matchesTab && matchesSearch) {
                filteredCameras.add(camera);
            }
        }

        cameraThumbnailAdapter.updateCameras(filteredCameras);
    }

    private String getSelectedTab() {
        for (TabFilter tab : tabFilters) {
            if (tab.isSelected()) {
                return tab.getLabel();
            }
        }
        return "全部";
    }

    private void setupFilterButton() {
        ivFilter.setOnClickListener(v -> showFilterPopup(v));
    }

    private void showFilterPopup(View anchor) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.layout_filter_menu, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(16f);
        popupWindow.showAsDropDown(anchor, 0, 8);

        popupView.findViewById(R.id.tvFilterAll).setOnClickListener(v -> {
            selectTab("全部");
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.tvFilterAdmin).setOnClickListener(v -> {
            selectTab("行政办公楼");
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.tvFilterTeaching).setOnClickListener(v -> {
            selectTab("公共教学楼");
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.tvFilterSquare).setOnClickListener(v -> {
            selectTab("1号主广场");
            popupWindow.dismiss();
        });
    }

    private void selectTab(String label) {
        for (int i = 0; i < tabFilters.size(); i++) {
            tabFilters.get(i).setSelected(tabFilters.get(i).getLabel().equals(label));
        }
        tabFilterAdapter.notifyDataSetChanged();
        filterCameras();
    }
}
