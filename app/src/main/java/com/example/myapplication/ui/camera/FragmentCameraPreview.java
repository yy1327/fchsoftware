package com.example.myapplication.ui.camera;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
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
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;

public class FragmentCameraPreview extends Fragment {
    private static final String TAG = "CameraPreview";
    private TextView tvCameraName;
    private TextView tvZoomLevel;
    private TextView tvLoading;
    private ImageView ivPreview;
    private TextureView textureView;
    private ExoPlayer exoPlayer;
    private String rtspUrl;
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
        setupVideoPlayer();

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
        tvLoading = view.findViewById(R.id.tvLoading);
        ivPreview = view.findViewById(R.id.ivPreview);
        textureView = view.findViewById(R.id.textureView);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            releasePlayer();
            requireActivity().finish();
        });

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
        Bundle args = getArguments();
        if (args != null) {
            String cameraName = args.getString("camera_name", "摄像头");
            rtspUrl = args.getString("rtsp_url");
            tvCameraName.setText(cameraName);
            Log.d(TAG, "摄像头: " + cameraName + ", RTSP: " + rtspUrl);
        }
    }

    private void setupVideoPlayer() {
        if (rtspUrl == null || rtspUrl.isEmpty()) {
            Log.d(TAG, "RTSP地址为空，显示占位图");
            return;
        }

        tvLoading.setVisibility(View.VISIBLE);
        ivPreview.setVisibility(View.GONE);

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                exoPlayer.setVideoSurface(new Surface(surface));
                startPlayback();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
    }

    private void startPlayback() {
        RtspMediaSource mediaSource = new RtspMediaSource.Factory()
                .createMediaSource(MediaItem.fromUri(Uri.parse(rtspUrl)));

        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    Log.d(TAG, "准备完成，开始播放");
                    tvLoading.setVisibility(View.GONE);
                    ivPreview.setVisibility(View.GONE);
                } else if (state == Player.STATE_ENDED) {
                    Log.d(TAG, "播放结束");
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(TAG, "播放错误: " + error.getMessage());
                tvLoading.setVisibility(View.GONE);
                ivPreview.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "播放失败: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
                Log.d(TAG, "视频尺寸: " + videoSize.width + "x" + videoSize.height);
            }
        });

        exoPlayer.play();
        Log.d(TAG, "开始连接: " + rtspUrl);
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
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
