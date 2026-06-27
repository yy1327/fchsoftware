package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
    }

    private void initViews(View view) {
        rvTabFilters = view.findViewById(R.id.rvTabFilters);
        rvCameras = view.findViewById(R.id.rvCameras);
        etSearch = view.findViewById(R.id.etSearch);
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
}
