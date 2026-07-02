package com.example.myapplication.data.sip;

import android.content.Context;
import android.net.sip.SipAudioCall;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.util.Log;

public class SipService {

    private static final String TAG = "SipService";

    private static SipService instance;
    private final Context context;
    private SipManager sipManager;
    private SipProfile sipProfile;
    private SipAudioCall currentCall;
    private SipRegistrationListener registrationListener;
    private SipAudioCall.Listener callListener;

    private boolean isRegistered = false;
    private String registrationError = null;

    public interface SipCallback {
        void onRegistered();
        void onRegistrationFailed(String error);
        void onUnregistered();
        void onCallIncoming(String caller);
        void onCallConnected();
        void onCallEnded();
        void onCallFailed(String error);
    }

    private SipCallback callback;

    private SipService(Context context) {
        this.context = context.getApplicationContext();
        initSipManager();
        initCallListener();
    }

    public static synchronized SipService getInstance(Context context) {
        if (instance == null) {
            instance = new SipService(context);
        }
        return instance;
    }

    private void initSipManager() {
        try {
            sipManager = SipManager.newInstance(context);
            if (sipManager == null) {
                Log.w(TAG, "SipManager is null - device may not support SIP");
            } else {
                Log.d(TAG, "SipManager initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SipManager", e);
            sipManager = null;
        }
    }

    private void initCallListener() {
        callListener = new SipAudioCall.Listener() {
            @Override
            public void onCallEstablished(SipAudioCall call) {
                Log.d(TAG, "Call established");
                currentCall = call;
                if (callback != null) {
                    callback.onCallConnected();
                }
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                Log.d(TAG, "Call ended");
                currentCall = null;
                if (callback != null) {
                    callback.onCallEnded();
                }
            }

            @Override
            public void onCallBusy(SipAudioCall call) {
                Log.d(TAG, "Call busy");
                if (callback != null) {
                    callback.onCallFailed("对方忙");
                }
            }

            @Override
            public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                Log.e(TAG, "Call error: " + errorCode + " - " + errorMessage);
                if (callback != null) {
                    callback.onCallFailed(errorMessage);
                }
            }
        };
    }

    public void setCallback(SipCallback callback) {
        this.callback = callback;
    }

    public boolean register() {
        if (sipManager == null) {
            Log.e(TAG, "SipManager is null");
            return false;
        }

        try {
            SipConfig config = SipConfig.getInstance();

            SipProfile.Builder builder = new SipProfile.Builder(config.getUsername(), config.getDomain());
            builder.setPassword(config.getPassword());
            builder.setOutboundProxy(config.getServerAddress());
            builder.setPort(config.getServerPort());
            sipProfile = builder.build();

            registrationListener = new SipRegistrationListener() {
                @Override
                public void onRegistering(String localProfileUri) {
                    Log.d(TAG, "Registering: " + localProfileUri);
                }

                @Override
                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    Log.d(TAG, "Registration done: " + localProfileUri + ", expires: " + expiryTime);
                    isRegistered = true;
                    registrationError = null;
                    if (callback != null) {
                        callback.onRegistered();
                    }
                }

                @Override
                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                    Log.e(TAG, "Registration failed: " + errorCode + " - " + errorMessage);
                    isRegistered = false;
                    registrationError = errorMessage;
                    if (callback != null) {
                        callback.onRegistrationFailed(errorMessage);
                    }
                }
            };

            sipManager.open(sipProfile, null, registrationListener);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to register", e);
            if (callback != null) {
                callback.onRegistrationFailed(e.getMessage());
            }
            return false;
        }
    }

    public void unregister() {
        if (sipManager != null && sipProfile != null) {
            try {
                sipManager.close(sipProfile.getUriString());
                isRegistered = false;
                Log.d(TAG, "Unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister", e);
            }
        }
    }

    public boolean makeCall(String targetUsername) {
        if (!isRegistered) {
            Log.e(TAG, "Not registered");
            if (callback != null) {
                callback.onCallFailed("未注册");
            }
            return false;
        }

        try {
            SipConfig config = SipConfig.getInstance();
            String targetUri = "sip:" + targetUsername + "@" + config.getDomain();

            if (sipManager != null) {
                currentCall = sipManager.makeAudioCall(
                    sipProfile.getUriString(),
                    targetUri,
                    callListener,
                    config.getCallTimeout()
                );
                Log.d(TAG, "Calling: " + targetUri);
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Failed to make call", e);
            if (callback != null) {
                callback.onCallFailed(e.getMessage());
            }
            return false;
        }
    }

    public void answerCall() {
        if (currentCall != null) {
            try {
                currentCall.answerCall(30);
                Log.d(TAG, "Call answered");
            } catch (Exception e) {
                Log.e(TAG, "Failed to answer call", e);
            }
        }
    }

    public void hangUp() {
        if (currentCall != null) {
            try {
                currentCall.endCall();
                currentCall.close();
                currentCall = null;
                Log.d(TAG, "Call hung up");
            } catch (Exception e) {
                Log.e(TAG, "Failed to hang up", e);
            }
        }
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public String getRegistrationError() {
        return registrationError;
    }

    public SipAudioCall getCurrentCall() {
        return currentCall;
    }

    public void destroy() {
        unregister();
        if (currentCall != null) {
            currentCall.close();
            currentCall = null;
        }
        instance = null;
    }
}
