package com.example.myapplication.data.media;

public class VideoStream {

    public enum VideoCodec {
        H264("H.264", 30, 1920, 1080),
        H265("H.265", 30, 1920, 1080),
        VP8("VP8", 30, 1280, 720);

        private final String name;
        private final int defaultFps;
        private final int defaultWidth;
        private final int defaultHeight;

        VideoCodec(String name, int fps, int width, int height) {
            this.name = name;
            this.defaultFps = fps;
            this.defaultWidth = width;
            this.defaultHeight = height;
        }

        public String getName() { return name; }
        public int getDefaultFps() { return defaultFps; }
        public int getDefaultWidth() { return defaultWidth; }
        public int getDefaultHeight() { return defaultHeight; }
    }

    private VideoCodec codec = VideoCodec.H264;
    private int fps = 30;
    private int width = 1280;
    private int height = 720;
    private boolean enabled = false;

    public VideoStream() {
    }

    public VideoStream(VideoCodec codec) {
        this.codec = codec;
        this.fps = codec.getDefaultFps();
        this.width = codec.getDefaultWidth();
        this.height = codec.getDefaultHeight();
    }

    public VideoCodec getCodec() { return codec; }
    public void setCodec(VideoCodec codec) {
        this.codec = codec;
        this.fps = codec.getDefaultFps();
        this.width = codec.getDefaultWidth();
        this.height = codec.getDefaultHeight();
    }

    public int getFps() { return fps; }
    public void setFps(int fps) { this.fps = fps; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
