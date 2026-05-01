package com.mineclawd.foundation.client;

import icyllis.modernui.text.Editable;
import icyllis.modernui.view.View;

/**
 * Bridges character input from Minecraft's Screen.charTyped to ModernUI EditText.
 * Bypasses ModernUI's findFocus() pipeline by directly injecting chars on the UI thread.
 */
public class CharInputBridge {

    private static View sTargetView;
    private static Editable sTargetEditable;
    private static volatile boolean sActive;

    public static void activate(View view, Editable editable) {
        sTargetView = view;
        sTargetEditable = editable;
        sActive = true;
    }

    public static void deactivate() {
        sTargetView = null;
        sTargetEditable = null;
        sActive = false;
    }

    /**
     * Called from mixin or directly to handle char input.
     * Posts to the View's handler (ModernUI UI thread) for proper invalidation.
     */
    public static boolean onCharTyped(char ch) {
        if (!sActive || sTargetEditable == null || sTargetView == null) {
            return false;
        }
        final Editable target = sTargetEditable;
        final View view = sTargetView;
        view.post(() -> {
            if (target != null) {
                int sel = Math.max(0, target.length());
                target.insert(sel, String.valueOf(ch));
            }
        });
        return true;
    }
}
