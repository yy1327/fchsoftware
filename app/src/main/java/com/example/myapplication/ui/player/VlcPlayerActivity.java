package com.example.myapplication.ui.player;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.sip.SipService;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

public class VlcPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VlcPlayerActivity";

    private SurfaceView surfaceView;
    private ImageButton btnBack;
    private TextView tvTitle;
    private ImageButton btnPtzUp;
    private ImageButton btnPtzDown;
    private ImageButton btnPtzLeft;
    private ImageButton btnPtzRight;
    private ImageButton btnScreenshot;
    private ImageButton btnRecord;
    private ImageButton btnTalk;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private String deviceId;
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);

        deviceId = getIntent().getStringExtra("device_id");
        deviceName = getIntent().getStringExtra("device_name");

        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "设备ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initVlc();
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surface_view);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        btnPtzUp = findViewById(R.id.btn_ptz_up);
        btnPtzDown = findViewById(R.id.btn_ptz_down);
        btnPtzLeft = findViewById(R.id.btn_ptz_left);
        btnPtzRight = findViewById(R.id.btn_ptz_right);
        btnScreenshot = findViewById(R.id.btn_screenshot);
        btnRecord = findViewById(R.id.btn_record);
        btnTalk = findViewById(R.id.btn_talk);

        if (deviceName != null) {
            tvTitle.setText(deviceName);
        }

        btnBack.setOnClickListener(v -> finish());

        btnPtzUp.setOnClickListener(v -> sendPtzCommand("up"));
        btnPtzDown.setOnClickListener(v -> sendPtzCommand("down"));
        btnPtzLeft.setOnClickListener(v -> sendPtzCommand("left"));
        btnPtzRight.setOnClickListener(v -> sendPtzCommand("right"));

        btnScreenshot.setOnClickListener(v -> takeScreenshot());
        btnRecord.setOnClickListener(v -> toggleRecording());
        btnTalk.setOnClickListener(v -> toggleTalk());
    }

    private void initVlc() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-drop-late-frames");
        options.add("--no-skip-frames");
        options.add("--no-video-title-show");
        options.add("--no-stats");
        options.add("--no-autoplay");

        libVLC = new LibVLC(this, options);
        mediaPlayer = new MediaPlayer(libVLC);

        mediaPlayer.setRenderWindow(surfaceView.getHolder());
        mediaPlayer.setOnEventListener(event -> {
            switch (event.type) {
                case MediaPlayer.Event.Playing:
                    Log.d(TAG, "Playing");
                    break;
                case MediaPlayer.Event.Paused:
                    Log.d(TAG, "Paused");
                    break;
                case MediaPlayer.Event.Stopped:
                    Log.d(TAG, "Stopped");
                    break;
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "End reached");
                    break;
                case MediaPlayer.Event.Error:
                    Log.e(TAG, "Error: " + event.getErrorMessage());
                    runOnUiThread(() -> Toast.makeText(VlcPlayerActivity.this, "播放错误", Toast.LENGTH_SHORT).show());
                    break;
            }
        });
    }

    private void sendPtzCommand(String direction) {
        String xml = "<?xml version=\"1.0\"?>" +
                "<Control>" +
                "<CmdType>DeviceControl</CmdType>" +
                "<SN>" + (int) (System.currentTimeMillis() % 10000) + "</SN>" +
                "<DeviceID>" + deviceId + "</DeviceID>" +
                "<PTZCmd>" + direction + "</PTZCmd>" +
                "</Control>";

        SipService.getInstance(this).sendTextMessage(xml);
        Log.d(TAG, "Sent PTZ command: " + direction);
    }

    private void takeScreenshot() {
        Toast.makeText(this, "截图功能", Toast.LENGTH_SHORT).show();
    }

    private void toggleRecording() {
        Toast.makeText(this, "录屏功能", Toast.LENGTH_SHORT).show();
    }

    private void toggleTalk() {
        Toast.makeText(this, "对讲功能", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (libVLC != null) {
            libVLC.release();
        }
    }
}