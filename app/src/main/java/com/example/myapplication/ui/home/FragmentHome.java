package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.mock.MockData;
import com.example.myapplication.data.model.Camera;
import com.example.myapplication.data.model.TabFilter;
import com.example.myapplication.ui.camera.CameraActivity;

import java.util.ArrayList;
import java.util.List;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadData();
        setupTabFilters();
        setupCameraGrid();
        setupSearch();
        setupFilterButton();
    }

    private void initViews(View view) {
        rvTabFilters = view.findViewById(R.id.rvTabFilters);
        rvCameras = view.findViewById(R.id.rvCameras);
        etSearch = view.findViewById(R.id.etSearch);
        ivFilter = view.findViewById(R.id.ivFilter);
    }

    private void loadData() {
        tabFilters = MockData.getTabFilters();
        allCameras = MockData.getCameras();
        filteredCameras = new ArrayList<>(allCameras);
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
