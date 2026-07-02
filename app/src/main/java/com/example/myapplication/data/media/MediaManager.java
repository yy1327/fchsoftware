package com.example.myapplication.data.media;

import android.util.Log;

import com.example.myapplication.data.sip.SipService;

public class MediaManager {

    private static final String TAG = "MediaManager";

    private static MediaManager instance;
    private AudioStream audioStream;
    private VideoStream videoStream;
    private SipService sipService;

    private MediaManager() {
        audioStream = new AudioStream();
        videoStream = new VideoStream();
    }

    public static synchronized MediaManager getInstance() {
        if (instance == null) {
            instance = new MediaManager();
        }
        return instance;
    }

    public void setSipService(SipService sipService) {
        this.sipService = sipService;
    }

    public AudioStream getAudioStream() {
        return audioStream;
    }

    public VideoStream getVideoStream() {
        return videoStream;
    }

    public boolean isAudioEnabled() {
        return audioStream.isEnabled();
    }

    public void setAudioEnabled(boolean enabled) {
        audioStream.setEnabled(enabled);
        Log.d(TAG, "Audio " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isVideoEnabled() {
        return videoStream.isEnabled();
    }

    public void setVideoEnabled(boolean enabled) {
        videoStream.setEnabled(enabled);
        Log.d(TAG, "Video " + (enabled ? "enabled" : "disabled"));
    }

    public boolean toggleMute() {
        boolean newMuted = !audioStream.isMuted();
        audioStream.setMuted(newMuted);
        Log.d(TAG, "Microphone " + (newMuted ? "muted" : "unmuted"));
        return newMuted;
    }

    public void release() {
        audioStream = null;
        videoStream = null;
        sipService = null;
        instance = null;
    }
}
