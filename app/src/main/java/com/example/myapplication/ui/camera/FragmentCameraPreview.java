package com.example.myapplication.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class FragmentCameraPreview extends Fragment {
    private TextView tvCameraName;
    private TextView tvZoomLevel;
    private int zoomLevel = 1;
    private LinearLayout tvTabRealtime;
    private LinearLayout tvTabRecording;
    private LinearLayout tvTabMessage;
    private LinearLayout tvTabScene;
    private LinearLayout[] tabs;

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
        setupSubTabs();

        View topBar = view.findViewById(R.id.topBar);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBar, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
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
                tvZoomLevel.setText(getString(R.string.zoom_level_format, zoomLevel));
            }
        });

        ivZoomOut.setOnClickListener(v -> {
            if (zoomLevel > 1) {
                zoomLevel--;
                tvZoomLevel.setText(getString(R.string.zoom_level_format, zoomLevel));
            }
        });

        setupPtzControls(view);
    }

    private void loadCameraData() {
        String cameraName = getArguments() != null ? getArguments().getString("camera_name", "摄像头") : "摄像头";
        tvCameraName.setText(cameraName);
    }

    private void setupPtzControls(View view) {
        View.OnClickListener ptzClickListener = v -> {
            int id = v.getId();
            if (id == R.id.ivPtzUp) {
                Toast.makeText(requireContext(), R.string.ptz_up, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ivPtzDown) {
                Toast.makeText(requireContext(), R.string.ptz_down, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ivPtzLeft) {
                Toast.makeText(requireContext(), R.string.ptz_left, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.ivPtzRight) {
                Toast.makeText(requireContext(), R.string.ptz_right, Toast.LENGTH_SHORT).show();
            }
        };

        view.findViewById(R.id.ivPtzUp).setOnClickListener(ptzClickListener);
        view.findViewById(R.id.ivPtzDown).setOnClickListener(ptzClickListener);
        view.findViewById(R.id.ivPtzLeft).setOnClickListener(ptzClickListener);
        view.findViewById(R.id.ivPtzRight).setOnClickListener(ptzClickListener);
    }

    private void setupSubTabs() {
        tvTabRealtime = getView().findViewById(R.id.tvTabRealtime);
        tvTabRecording = getView().findViewById(R.id.tvTabRecording);
        tvTabMessage = getView().findViewById(R.id.tvTabMessage);
        tvTabScene = getView().findViewById(R.id.tvTabScene);

        tabs = new LinearLayout[]{tvTabRealtime, tvTabRecording, tvTabMessage, tvTabScene};

        for (LinearLayout tab : tabs) {
            tab.setOnClickListener(v -> {
                for (LinearLayout t : tabs) {
                    TextView textView = getTabTextView(t);
                    if (textView != null) {
                        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint));
                        textView.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                    View indicator = getTabIndicator(t);
                    if (indicator != null) {
                        indicator.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
                    }
                }
                TextView textView = getTabTextView(tab);
                if (textView != null) {
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
                    textView.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                View indicator = getTabIndicator(tab);
                if (indicator != null) {
                    indicator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
                }
            });
        }
    }

    private TextView getTabTextView(LinearLayout tab) {
        if (tab.getChildCount() > 0 && tab.getChildAt(0) instanceof TextView) {
            return (TextView) tab.getChildAt(0);
        }
        return null;
    }

    private View getTabIndicator(LinearLayout tab) {
        if (tab.getChildCount() > 1) {
            return tab.getChildAt(1);
        }
        return null;
    }

    private void setupBottomToolbar() {
        getView().findViewById(R.id.btnScreenshot).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.camera_screenshot, Toast.LENGTH_SHORT).show());

        getView().findViewById(R.id.btnRecord).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.camera_record, Toast.LENGTH_SHORT).show());

        getView().findViewById(R.id.btnIntercom).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.camera_intercom, Toast.LENGTH_SHORT).show());

        getView().findViewById(R.id.btnResolution).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.camera_resolution, Toast.LENGTH_SHORT).show());

        getView().findViewById(R.id.btnPreset).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.camera_preset, Toast.LENGTH_SHORT).show());
    }
}
