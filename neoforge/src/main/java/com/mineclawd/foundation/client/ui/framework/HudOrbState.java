package com.mineclawd.foundation.client.ui.framework;

public class HudOrbState {

    private static volatile boolean sVisible;
    private static volatile boolean sHudShowing;
    private static boolean sInitialized;
    private static int sX;
    private static int sY;
    private static int sSize = 0;

    private static float sSavedXNorm, sSavedYNorm, sSavedWNorm, sSavedHNorm;
    private static boolean sHasSavedPos;
    private static int sSavedSidebarW;
    private static boolean sSavedSidebarCollapsed;

    public static void show() { sVisible = true; }
    public static void hide() { sVisible = false; }
    public static boolean isVisible() { return sVisible; }

    public static void setHudRequested(boolean showing) { sHudShowing = showing; }
    public static void hideHud() { sHudShowing = false; }
    public static boolean isHudShowing() { return sHudShowing; }

    public static int getX() { return sX; }
    public static int getY() { return sY; }
    public static int getSize() { return sSize > 0 ? sSize : 40; }
    public static void setSize(int size) { sSize = size; }
    public static boolean isInitialized() { return sInitialized; }
    public static void setInitialized() { sInitialized = true; }
    public static void setPosition(int x, int y) { sX = x; sY = y; }

    public static void saveWindowPos(float x, float y, float w, float h) {
        sSavedXNorm = x; sSavedYNorm = y; sSavedWNorm = w; sSavedHNorm = h;
        sHasSavedPos = true;
    }
    public static boolean hasSavedWindowPos() { return sHasSavedPos; }
    public static void clearSavedWindowPos() { sHasSavedPos = false; }
    public static float getSavedXNorm() { return sSavedXNorm; }
    public static float getSavedYNorm() { return sSavedYNorm; }
    public static float getSavedWNorm() { return sSavedWNorm; }
    public static float getSavedHNorm() { return sSavedHNorm; }

    public static void saveSidebarState(int width, boolean collapsed) {
        sSavedSidebarW = width;
        sSavedSidebarCollapsed = collapsed;
    }
    public static boolean hasSavedSidebarState() { return sSavedSidebarW > 0; }
    public static int getSavedSidebarW() { return sSavedSidebarW; }
    public static boolean getSavedSidebarCollapsed() { return sSavedSidebarCollapsed; }
}
