package com.example.myapplication.ui.call;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.model.CallState;
import com.example.myapplication.data.sip.SipCallManager;

public class IncomingCallActivity extends AppCompatActivity {

    private static final String TAG = "IncomingCallActivity";
    private static final String EXTRA_CALLER = "caller_number";

    private TextView tvCallerNumber;
    private TextView tvCallState;
    private LinearLayout btnReject;
    private LinearLayout btnAccept;

    private SipCallManager callManager;
    private Ringtone ringtone;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    public static void start(android.content.Context context, String callerNumber) {
        android.content.Intent intent = new android.content.Intent(context, IncomingCallActivity.class);
        intent.putExtra(EXTRA_CALLER, callerNumber);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        callManager = SipCallManager.getInstance(this);

        initViews();
        setupListeners();
        setupCallCallback();
        startRingtone();
        startTimeout();
    }

    private void initViews() {
        tvCallerNumber = findViewById(R.id.tvCallerNumber);
        tvCallState = findViewById(R.id.tvCallState);
        btnReject = findViewById(R.id.btnReject);
        btnAccept = findViewById(R.id.btnAccept);

        String caller = getIntent().getStringExtra(EXTRA_CALLER);
        if (caller != null) {
            tvCallerNumber.setText(caller);
        }
    }

    private void setupListeners() {
        btnReject.setOnClickListener(v -> rejectCall());
        btnAccept.setOnClickListener(v -> acceptCall());
    }

    private void setupCallCallback() {
        callManager.setCallStateCallback(new SipCallManager.CallStateCallback() {
            @Override
            public void onCallStateChanged(CallState state) {
                runOnUiThread(() -> {
                    if (state == CallState.CONNECTED) {
                        stopRingtone();
                        VideoCallActivity.start(IncomingCallActivity.this, tvCallerNumber.getText().toString());
                        finish();
                    } else if (state == CallState.ENDED || state == CallState.FAILED) {
                        stopRingtone();
                        finish();
                    }
                });
            }

            @Override
            public void onCallIncoming(String caller) {
            }

            @Override
            public void onCallConnected() {
            }

            @Override
            public void onCallEnded() {
                runOnUiThread(() -> {
                    stopRingtone();
                    finish();
                });
            }

            @Override
            public void onCallFailed(String error) {
                runOnUiThread(() -> {
                    stopRingtone();
                    Toast.makeText(IncomingCallActivity.this, "通话失败: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void acceptCall() {
        stopRingtone();
        tvCallState.setText("接听中...");
        callManager.answerCall();
    }

    private void rejectCall() {
        stopRingtone();
        callManager.hangUp();
        finish();
    }

    private void startRingtone() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to play ringtone", e);
        }
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void startTimeout() {
        timeoutHandler = new Handler(Looper.getMainLooper());
        timeoutRunnable = () -> {
            Toast.makeText(this, "来电超时", Toast.LENGTH_SHORT).show();
            rejectCall();
        };
        timeoutHandler.postDelayed(timeoutRunnable, 30000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }
}
