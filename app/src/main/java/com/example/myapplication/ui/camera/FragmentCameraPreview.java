package com.example.myapplication.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.mock.MockData;
import com.example.myapplication.data.model.BottomToolbarItem;

import java.util.List;

public class FragmentCameraPreview extends Fragment {
    private TextView tvCameraName;
    private TextView tvZoomLevel;
    private int zoomLevel = 1;
    private List<BottomToolbarItem> toolbarItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadCameraData();
        setupBottomToolbar();
    }

    private void initViews(View view) {
        tvCameraName = view.findViewById(R.id.tvCameraName);
        tvZoomLevel = view.findViewById(R.id.tvZoomLevel);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> requireActivity().finish());

        ImageView ivZoomIn = view.findViewById(R.id.ivZoomIn);
        ImageView ivZoomOut = view.findViewById(R.id.ivZoomOut);

        ivZoomIn.setOnClickListener(v -> {
            if (zoomLevel < 20) {
                zoomLevel++;
                tvZoomLevel.setText(zoomLevel + "x");
            }
        });

        ivZoomOut.setOnClickListener(v -> {
            if (zoomLevel > 1) {
                zoomLevel--;
                tvZoomLevel.setText(zoomLevel + "x");
            }
        });

        setupPtzControls(view);
        setupSubTabs(view);
    }

    private void loadCameraData() {
        String cameraName = getArguments() != null ? getArguments().getString("camera_name", "摄像头") : "摄像头";
        tvCameraName.setText(cameraName);
    }

    private void setupPtzControls(View view) {
        View.OnClickListener ptzClickListener = v -> {
            int id = v.getId();
            if (id == R.id.ivPtzUp) {
                Toast.makeText(requireContext(), "向上", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ivPtzDown) {
                Toast.makeText(requireContext(), "向下", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ivPtzLeft) {
                Toast.makeText(requireContext(), "向左", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ivPtzRight) {
                Toast.makeText(requireContext(), "向右", Toast.LENGTH_SHORT).show();
            }
        };

        view.findViewById(R.id.ivPtzUp).setOnClickListener(ptzClickListener);
        view.findViewById(R.id.ivPtzDown).setOnClickListener(ptzClickListener);
        view.findViewById(R.id.ivPtzLeft).setOnClickListener(ptzClickListener);
        view.findViewById(R.id.ivPtzRight).setOnClickListener(ptzClickListener);
    }

    private void setupSubTabs(View view) {
        TextView tvTabRealtime = view.findViewById(R.id.tvTabRealtime);
        TextView tvTabRecording = view.findViewById(R.id.tvTabRecording);
        TextView tvTabMessage = view.findViewById(R.id.tvTabMessage);
        TextView tvTabScene = view.findViewById(R.id.tvTabScene);

        TextView[] tabs = {tvTabRealtime, tvTabRecording, tvTabMessage, tvTabScene};

        for (TextView tab : tabs) {
            tab.setOnClickListener(v -> {
                for (TextView t : tabs) {
                    t.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint));
                    t.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
            });
        }
    }

    private void setupBottomToolbar() {
        RecyclerView rvBottomToolbar = getView().findViewById(R.id.rvBottomToolbar);
        toolbarItems = MockData.getToolbarItems();
        BottomToolbarAdapter adapter = new BottomToolbarAdapter(toolbarItems);
        rvBottomToolbar.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBottomToolbar.setAdapter(adapter);

        adapter.setOnToolbarItemClickListener((item, position) -> {
            if (item.getLabel().equals("录制")) {
                item.setActive(!item.isActive());
                adapter.notifyItemChanged(position);
                if (item.isActive()) {
                    Toast.makeText(requireContext(), "开始录制", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "停止录制", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), item.getLabel(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
