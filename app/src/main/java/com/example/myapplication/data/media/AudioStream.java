package com.example.myapplication.data.media;

public class AudioStream {

    public enum AudioCodec {
        G711_ULAW("G.711 u-law", 8000, 64000),
        G711_ALAW("G.711 A-law", 8000, 64000),
        G729("G.729", 8000, 8000);

        private final String name;
        private final int sampleRate;
        private final int bitrate;

        AudioCodec(String name, int sampleRate, int bitrate) {
            this.name = name;
            this.sampleRate = sampleRate;
            this.bitrate = bitrate;
        }

        public String getName() { return name; }
        public int getSampleRate() { return sampleRate; }
        public int getBitrate() { return bitrate; }
    }

    private AudioCodec codec = AudioCodec.G711_ULAW;
    private int sampleRate = 8000;
    private boolean enabled = true;
    private boolean muted = false;

    public AudioStream() {
    }

    public AudioStream(AudioCodec codec) {
        this.codec = codec;
        this.sampleRate = codec.getSampleRate();
    }

    public AudioCodec getCodec() { return codec; }
    public void setCodec(AudioCodec codec) {
        this.codec = codec;
        this.sampleRate = codec.getSampleRate();
    }

    public int getSampleRate() { return sampleRate; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isMuted() { return muted; }
    public void setMuted(boolean muted) { this.muted = muted; }
}
