package com.mineclawd.foundation.client.ui.framework;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Reactive IMBlocker compatibility layer.
 * 
 * Zero compile-time dependency on IMBlocker. Detects IMBlocker at runtime
 * via reflection and registers ModernUI's Screen class in IMBlocker's
 * bakedScreenWhitelist.
 * 
 * IMBlocker checks Minecraft.setScreen() via mixin. On each screen change,
 * it calls isScreenInWhitelist(screen) to decide whether to enable IME.
 * By adding our SimpleScreen class to the whitelist, IME will be enabled
 * whenever our HUD is shown.
 */
public class IMBlockerCompat {

    private static boolean sChecked;
    private static boolean sActive;

    /**
     * Call when our Fragment opens (e.g. in onAttach/onCreate).
     * Registers our Screen class with IMBlocker's baked whitelist.
     */
    public static void onScreenOpened(Object screen) {
        if (!sChecked) {
            sChecked = true;
            sActive = checkIMBlocker();
        }
        if (!sActive || screen == null) return;

        // Reflectively call IMBlockerConfig.isScreenInWhitelist to check
        // if our screen is already registered. If not, add to baked set.
        try {
            Class<?> configClass = Class.forName("io.github.reserveword.imblocker.common.IMBlockerConfig");
            Field instanceField = configClass.getDeclaredField("INSTANCE");
            Object config = instanceField.get(null);
            Method isInWhitelist = configClass.getMethod("isScreenInWhitelist", Object.class);

            if (!(boolean) isInWhitelist.invoke(config, screen)) {
                // Get the bakedScreenWhitelist set and add our screen class
                Field bakedField = configClass.getDeclaredField("bakedScreenWhitelist");
                bakedField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Set<Class<?>> whitelist = (Set<Class<?>>) bakedField.get(null);
                whitelist.add(screen.getClass());

                // Tell IMBlocker's container to prefer IME on
                setContainerPreferredState(true);
            }
        } catch (Exception ignored) {
            // IMBlocker not loaded or API changed
        }
    }

    /**
     * Call when our Fragment closes (e.g. in onDestroy).
     */
    public static void onScreenClosed() {
        if (!sActive) return;
        setContainerPreferredState(false);
    }

    private static boolean checkIMBlocker() {
        try {
            Class.forName("io.github.reserveword.imblocker.common.IMBlockerConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void setContainerPreferredState(boolean preferred) {
        try {
            Class<?> focusContainer = Class.forName("io.github.reserveword.imblocker.common.gui.FocusContainer");
            Field minecraftField = focusContainer.getDeclaredField("MINECRAFT");
            Object minecraftContainer = minecraftField.get(null);
            Method setPref = focusContainer.getMethod("setPreferredState", boolean.class);
            setPref.invoke(minecraftContainer, preferred);
        } catch (Exception ignored) {}
    }
}
