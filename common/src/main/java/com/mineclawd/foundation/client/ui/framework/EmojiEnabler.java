package com.mineclawd.foundation.client.ui.framework;

import icyllis.modernui.graphics.text.FontFamily;
import icyllis.modernui.mc.ModernUIClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class EmojiEnabler {

    private static boolean sInited;
    private static byte[] sFontData;

    public static void ensure() {
        if (sInited) return;
        sInited = true;

        try {
            var rm = MinecraftClient.getInstance().getResourceManager();
            var fontLoc = Identifier.of("mineclawd", "font/NotoEmoji.ttf");
            var opt = rm.getResource(fontLoc);
            if (opt.isPresent()) {
                try (var is = opt.get().getInputStream()) {
                    sFontData = is.readAllBytes();
                }
            }
        } catch (Exception ignored) {
        }

        if (ModernUIClient.sFallbackFontFamilyList == null) {
            ModernUIClient.sFallbackFontFamilyList = new ArrayList<>();
        }
        if (ModernUIClient.sFirstFontFamily == null) {
            ModernUIClient.sFirstFontFamily = "";
        }
        if (ModernUIClient.sFontRegistrationList == null) {
            ModernUIClient.sFontRegistrationList = java.util.List.of();
        }

        applyFont();
    }

    public static void onResourcesReady() {
        applyFont();
    }

    private static void applyFont() {
        if (sFontData == null) return;

        try {
            var is = new ByteArrayInputStream(sFontData);
            FontFamily[] families = FontFamily.createFamilies(is, true);
            for (var family : families) {
                registerFont(family);
                String name = family.getFamilyName();
                var fbList = ModernUIClient.sFallbackFontFamilyList;
                if (fbList instanceof ArrayList list && !list.contains(name)) {
                    list.addFirst(name);
                }
            }
        } catch (Exception ignored) {
        }

        scheduleReload(0);
    }

    private static void registerFont(FontFamily family) {
        try {
            Class<?> mgr = Class.forName("icyllis.modernui.mc.FontResourceManager");
            Method getInstance = mgr.getMethod("getInstance");
            Object instance = getInstance.invoke(null);
            Method onFontRegistered = mgr.getMethod("onFontRegistered", FontFamily.class);
            onFontRegistered.invoke(instance, family);
        } catch (Exception ignored) {
        }
    }

    private static void scheduleReload(int attempt) {
        if (attempt > 60) return;
        MinecraftClient.getInstance().execute(() -> {
            try {
                ModernUIClient.getInstance().reloadTypeface();
                ModernUIClient.getInstance().reloadFontStrike();
            } catch (Exception ignored) {
                if (attempt < 60) {
                    scheduleReload(attempt + 1);
                }
            }
        });
    }
}
