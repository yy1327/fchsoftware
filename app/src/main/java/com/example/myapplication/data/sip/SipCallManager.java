package com.example.myapplication.data.sip;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.data.model.CallState;

public class SipCallManager {

    private static final String TAG = "SipCallManager";

    private static SipCallManager instance;
    private final SipService sipService;
    private CallStateCallback callStateCallback;

    public interface CallStateCallback {
        void onCallStateChanged(CallState state);
        void onCallIncoming(String caller);
        void onCallConnected();
        void onCallEnded();
        void onCallFailed(String error);
    }

    private SipCallManager(Context context) {
        sipService = SipService.getInstance(context);
    }

    public static synchronized SipCallManager getInstance(Context context) {
        if (instance == null) {
            instance = new SipCallManager(context);
        }
        return instance;
    }

    public void setCallStateCallback(CallStateCallback callback) {
        this.callStateCallback = callback;
        sipService.setCallback(new SipService.SipCallback() {
            @Override
            public void onRegistered() {
            }

            @Override
            public void onRegistrationFailed(String error) {
            }

            @Override
            public void onUnregistered() {
            }

            @Override
            public void onCallIncoming(String caller) {
                Log.d(TAG, "Incoming call from: " + caller);
                if (callStateCallback != null) {
                    callStateCallback.onCallIncoming(caller);
                }
            }

            @Override
            public void onCallConnected() {
                Log.d(TAG, "Call connected");
                if (callStateCallback != null) {
                    callStateCallback.onCallConnected();
                    callStateCallback.onCallStateChanged(CallState.CONNECTED);
                }
            }

            @Override
            public void onCallEnded() {
                Log.d(TAG, "Call ended");
                if (callStateCallback != null) {
                    callStateCallback.onCallEnded();
                    callStateCallback.onCallStateChanged(CallState.ENDED);
                }
            }

            @Override
            public void onCallFailed(String error) {
                Log.e(TAG, "Call failed: " + error);
                if (callStateCallback != null) {
                    callStateCallback.onCallFailed(error);
                    callStateCallback.onCallStateChanged(CallState.FAILED);
                }
            }
        });
    }

    public boolean startCall(String targetUsername) {
        Log.d(TAG, "Starting call to: " + targetUsername);
        if (callStateCallback != null) {
            callStateCallback.onCallStateChanged(CallState.CALLING);
        }
        return sipService.makeCall(targetUsername);
    }

    public void answerCall() {
        Log.d(TAG, "Answering call");
        sipService.answerCall();
    }

    public void hangUp() {
        Log.d(TAG, "Hanging up");
        sipService.hangUp();
    }

    public CallState getCallState() {
        return sipService.getCallState();
    }

    public boolean isInCall() {
        CallState state = sipService.getCallState();
        return state == CallState.CALLING || state == CallState.CONNECTED;
    }

    public void destroy() {
        instance = null;
    }
}
