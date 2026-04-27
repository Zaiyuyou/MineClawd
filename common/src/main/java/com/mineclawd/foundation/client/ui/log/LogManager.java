package com.mineclawd.foundation.client.ui.log;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LogManager {

    private static final int MAX_ENTRIES = 100;
    private static final List<LogEntry> sEntries = new ArrayList<>();
    private static Consumer<LogEntry> sListener;

    public static void setListener(Consumer<LogEntry> listener) {
        sListener = listener;
    }

    public static void clearListener() {
        sListener = null;
    }

    public static void info(String text) {
        add(new LogEntry(LogEntry.Level.INFO, text));
    }

    public static void success(String text) {
        add(new LogEntry(LogEntry.Level.SUCCESS, text));
    }

    public static void warn(String text) {
        add(new LogEntry(LogEntry.Level.WARN, text));
    }

    public static void error(String text) {
        add(new LogEntry(LogEntry.Level.ERROR, text));
    }

    public static void tool(String text) {
        add(new LogEntry(LogEntry.Level.TOOL, text));
    }

    public static void info(String text, boolean clickable, String url) {
        add(new LogEntry(LogEntry.Level.INFO, text, clickable, url));
    }

    public static void success(String text, boolean clickable, String url) {
        add(new LogEntry(LogEntry.Level.SUCCESS, text, clickable, url));
    }

    public static void error(String text, boolean clickable, String url) {
        add(new LogEntry(LogEntry.Level.ERROR, text, clickable, url));
    }

    private static synchronized void add(LogEntry entry) {
        sEntries.add(entry);
        if (sEntries.size() > MAX_ENTRIES) {
            sEntries.remove(0);
        }
        if (sListener != null) {
            sListener.accept(entry);
        }
    }

    public static synchronized List<LogEntry> getEntries() {
        return new ArrayList<>(sEntries);
    }

    public static synchronized void clear() {
        sEntries.clear();
    }
}
