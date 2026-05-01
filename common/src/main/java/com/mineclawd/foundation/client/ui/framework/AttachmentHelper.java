package com.mineclawd.foundation.client.ui.framework;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AttachmentHelper {

    private static final int MAX_ATTACHMENTS = 12;
    private static final AtomicBoolean sDialogOpen = new AtomicBoolean(false);

    private static final List<Path> sPendingFiles = new ArrayList<>();
    private static Consumer<List<Path>> sCallback;

    public static void setCallback(Consumer<List<Path>> callback) {
        sCallback = callback;
    }

    public static void clearCallback() {
        sCallback = null;
    }

    public static List<Path> getPendingFiles() {
        synchronized (sPendingFiles) {
            return List.copyOf(sPendingFiles);
        }
    }

    public static void clearPendingFiles() {
        synchronized (sPendingFiles) {
            sPendingFiles.clear();
        }
    }

    public static void addPendingFile(Path path) {
        synchronized (sPendingFiles) {
            if (sPendingFiles.size() >= MAX_ATTACHMENTS) return;
            if (!sPendingFiles.contains(path)) {
                sPendingFiles.add(path);
            }
        }
    }

    public static void openFileDialog(Runnable onComplete) {
        if (!sDialogOpen.compareAndSet(false, true)) return;

        Thread picker = new Thread(() -> {
            List<Path> selected = new ArrayList<>();
            try {
                if (!GraphicsEnvironment.isHeadless()) {
                    var dialog = new FileDialog((Frame) null, "Select files", FileDialog.LOAD);
                    dialog.setMultipleMode(true);
                    dialog.setVisible(true);
                    File[] files = dialog.getFiles();
                    if (files != null) {
                        for (var f : files) {
                            if (f != null && f.isFile()) {
                                selected.add(f.toPath().toAbsolutePath().normalize());
                            }
                        }
                    }
                    dialog.dispose();
                }
            } catch (Exception ignored) {
            }
            sDialogOpen.set(false);
            var mc = net.minecraft.client.MinecraftClient.getInstance();
            mc.execute(() -> {
                boolean added = false;
                for (var p : selected) {
                    if (Files.isRegularFile(p)) {
                        addPendingFile(p);
                        added = true;
                    }
                }
                if (added && sCallback != null) {
                    sCallback.accept(getPendingFiles());
                }
                if (onComplete != null) onComplete.run();
            });
        }, "mclawd-attach");
        picker.setDaemon(true);
        picker.start();
    }
}
