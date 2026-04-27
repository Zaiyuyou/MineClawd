package com.mineclawd.foundation.client.ui.log;

public class LogEntry {

    public enum Level {
        INFO(0xFF888888),
        SUCCESS(0xFF4ADE80),
        WARN(0xFFFFB74D),
        ERROR(0xFFFF6B6B),
        TOOL(0xFF6385F4);

        public final int color;
        Level(int color) { this.color = color; }
    }

    public final Level level;
    public final String text;
    public final long timestampMs;
    public final boolean clickable;
    public final String clickUrl;

    private float mDisplayAlpha = 1.0f;
    private float mFlashTime = -1f;

    public LogEntry(Level level, String text) {
        this(level, text, false, null);
    }

    public LogEntry(Level level, String text, boolean clickable, String clickUrl) {
        this.level = level;
        this.text = text;
        this.timestampMs = System.currentTimeMillis();
        this.clickable = clickable;
        this.clickUrl = clickUrl;
        this.mFlashTime = 0f;
    }

    public float getDisplayAlpha() { return mDisplayAlpha; }
    public void setDisplayAlpha(float a) { mDisplayAlpha = a; }

    public float getFlashTime() { return mFlashTime; }
    public void setFlashTime(float t) { mFlashTime = t; }

    public String getTimeString() {
        long diff = System.currentTimeMillis() - timestampMs;
        if (diff < 1000) return "now";
        if (diff < 60000) return (diff / 1000) + "s";
        if (diff < 3600000) return (diff / 60000) + "m";
        return (diff / 3600000) + "h";
    }
}
