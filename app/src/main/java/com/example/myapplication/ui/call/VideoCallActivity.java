package com.example.myapplication.ui.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.media.MediaManager;
import com.example.myapplication.data.model.CallState;
import com.example.myapplication.data.sip.SipCallManager;

import java.util.Locale;

public class VideoCallActivity extends AppCompatActivity {

    private static final String TAG = "VideoCallActivity";
    private static final String EXTRA_TARGET_USERNAME = "target_username";

    private SurfaceView surfaceRemote;
    private SurfaceView surfaceLocal;
    private TextView tvCaller;
    private TextView tvCallDuration;
    private TextView tvCallStatus;
    private LinearLayout btnMute;
    private LinearLayout btnSpeaker;
    private LinearLayout btnHangUp;
    private LinearLayout btnVideo;
    private ImageView btnBack;

    private SipCallManager callManager;
    private MediaManager mediaManager;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int callSeconds = 0;
    private boolean isMuted = false;
    private boolean isVideoEnabled = false;

    public static void start(android.content.Context context, String targetUsername) {
        android.content.Intent intent = new android.content.Intent(context, VideoCallActivity.class);
        intent.putExtra(EXTRA_TARGET_USERNAME, targetUsername);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        callManager = SipCallManager.getInstance(this);
        mediaManager = MediaManager.getInstance();

        initViews();
        setupListeners();
        setupCallCallback();

        String targetUsername = getIntent().getStringExtra(EXTRA_TARGET_USERNAME);
        if (targetUsername != null) {
            tvCaller.setText(targetUsername);
            tvCallStatus.setVisibility(View.VISIBLE);
            tvCallStatus.setText("正在呼叫 " + targetUsername + "...");
            callManager.startCall(targetUsername);
        }
    }

    private void initViews() {
        surfaceRemote = findViewById(R.id.surfaceRemote);
        surfaceLocal = findViewById(R.id.surfaceLocal);
        tvCaller = findViewById(R.id.tvCaller);
        tvCallDuration = findViewById(R.id.tvCallDuration);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnHangUp = findViewById(R.id.btnHangUp);
        btnVideo = findViewById(R.id.btnVideo);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHangUp.setOnClickListener(v -> hangUp());
        btnMute.setOnClickListener(v -> toggleMute());
        btnVideo.setOnClickListener(v -> toggleVideo());
        btnSpeaker.setOnClickListener(v -> {
            Toast.makeText(this, "免提功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCallCallback() {
        callManager.setCallStateCallback(new SipCallManager.CallStateCallback() {
            @Override
            public void onCallStateChanged(CallState state) {
                runOnUiThread(() -> handleCallState(state));
            }

            @Override
            public void onCallIncoming(String caller) {
                runOnUiThread(() -> {
                    tvCaller.setText(caller);
                    tvCallStatus.setVisibility(View.VISIBLE);
                    tvCallStatus.setText(caller + " 来电");
                });
            }

            @Override
            public void onCallConnected() {
                runOnUiThread(() -> {
                    tvCallStatus.setVisibility(View.GONE);
                    startCallTimer();
                });
            }

            @Override
            public void onCallEnded() {
                runOnUiThread(() -> finish());
            }

            @Override
            public void onCallFailed(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(VideoCallActivity.this, "通话失败: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void handleCallState(CallState state) {
        switch (state) {
            case CALLING:
                tvCallStatus.setVisibility(View.VISIBLE);
                tvCallStatus.setText("正在呼叫...");
                break;
            case CONNECTED:
                tvCallStatus.setVisibility(View.GONE);
                startCallTimer();
                break;
            case ENDED:
            case FAILED:
                stopCallTimer();
                break;
        }
    }

    private void startCallTimer() {
        timerHandler = new Handler(Looper.getMainLooper());
        callSeconds = 0;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                callSeconds++;
                int mins = callSeconds / 60;
                int secs = callSeconds % 60;
                tvCallDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopCallTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void hangUp() {
        callManager.hangUp();
        finish();
    }

    private void toggleMute() {
        isMuted = !isMuted;
        mediaManager.setAudioEnabled(!isMuted);
        Toast.makeText(this, isMuted ? "已静音" : "已取消静音", Toast.LENGTH_SHORT).show();
    }

    private void toggleVideo() {
        isVideoEnabled = !isVideoEnabled;
        mediaManager.setVideoEnabled(isVideoEnabled);
        surfaceLocal.setVisibility(isVideoEnabled ? View.VISIBLE : View.GONE);
        Toast.makeText(this, isVideoEnabled ? "视频已开启" : "视频已关闭", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCallTimer();
        if (callManager.isInCall()) {
            callManager.hangUp();
        }
    }
}
