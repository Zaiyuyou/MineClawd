package com.mineclawd.foundation.client;

import com.mineclawd.foundation.client.ui.framework.HudOrbState;
import com.mineclawd.foundation.client.ui.framework.ModernHudFragment;
import com.mojang.blaze3d.systems.RenderSystem;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "mineclawd", value = Dist.CLIENT)
public class HudOrbHandler {

    private static final Identifier ORB_ICON =
            Identifier.of("mineclawd", "textures/gui/icon-simplified.png");
    private static final int ORB_ICON_TEXTURE_SIZE = 64;

    private static boolean sDragging;
    private static int sDragOffX;
    private static int sDragOffY;
    private static boolean sPressed;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();
        DrawContext ctx = event.getGuiGraphics();

        if (!HudOrbState.isVisible()) return;

        int size = HudOrbState.getSize();
        int x = HudOrbState.getX();
        int y = HudOrbState.getY();

        if (!HudOrbState.isInitialized()) {
            int sw = window.getScaledWidth();
            int sh = window.getScaledHeight();
            size = Math.max(32, Math.min(sw, sh) / 20);
            HudOrbState.setSize(size);
            x = sw - size - 16;
            y = sh - size - 16;
            HudOrbState.setPosition(x, y);
            HudOrbState.setInitialized();
        }

        int cx = x + size / 2;
        int cy = y + size / 2;
        int radius = size / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        drawSmoothCircle(ctx, cx, cy, radius, 0xCC1C2532);
        drawSmoothCircle(ctx, cx, cy, radius - 3, 0x224A6DC9);
        drawSmoothRing(ctx, cx, cy, radius - 2, radius, 0xCC4A6DC9);

        int iconPad = size / 5;
        int iconSize = size - iconPad * 2;
        if (iconSize > 0) {
            ctx.drawTexture(
                    ORB_ICON,
                    x + iconPad,
                    y + iconPad,
                    iconSize,
                    iconSize,
                    0.0f, 0.0f,
                    ORB_ICON_TEXTURE_SIZE, ORB_ICON_TEXTURE_SIZE,
                    ORB_ICON_TEXTURE_SIZE, ORB_ICON_TEXTURE_SIZE
            );
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void drawSmoothCircle(DrawContext ctx, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        int r2 = radius * radius;
        float a = (color >> 24 & 0xFF) / 255f;
        int rgb = color & 0x00FFFFFF;
        for (int dy = -radius; dy <= radius; dy++) {
            float edgeX = (float) Math.sqrt(Math.max(0, r2 - dy * dy));
            int x0 = (int) edgeX;
            int fillColor = color;
            if (x0 > 0) {
                ctx.fill(cx - x0, cy + dy, cx + x0 + 1, cy + dy + 1, fillColor);
            }
            float frac = edgeX - x0;
            if (frac > 0.001f && x0 >= 0) {
                int alpha = Math.round(a * frac * 255);
                int edgeColor = rgb | (Math.min(alpha, 255) << 24);
                int x1 = x0 + 1;
                ctx.fill(cx - x1, cy + dy, cx - x0, cy + dy + 1, edgeColor);
                ctx.fill(cx + x0 + 1, cy + dy, cx + x1 + 1, cy + dy + 1, edgeColor);
            }
        }
    }

    private static void drawSmoothRing(DrawContext ctx, int cx, int cy, int innerR, int outerR, int color) {
        if (outerR <= 0 || innerR >= outerR) return;
        float a = (color >> 24 & 0xFF) / 255f;
        int rgb = color & 0x00FFFFFF;
        for (int dy = -outerR; dy <= outerR; dy++) {
            float outerEdge = (float) Math.sqrt(Math.max(0, outerR * outerR - dy * dy));
            int outerX = (int) outerEdge;

            int innerDrawR = Math.max(0, innerR);
            float innerEdge = (float) Math.sqrt(Math.max(0, innerDrawR * innerDrawR - dy * dy));
            int innerX = (int) Math.ceil(innerEdge);

            int fillColor = color;
            if (outerX >= innerX) {
                ctx.fill(cx - outerX, cy + dy, cx - innerX, cy + dy + 1, fillColor);
                ctx.fill(cx + innerX, cy + dy, cx + outerX + 1, cy + dy + 1, fillColor);
            }

            float outerFrac = outerEdge - (int) outerEdge;
            if (outerFrac > 0.001f) {
                int alpha = Math.round(a * outerFrac * 255);
                int edgeColor = rgb | (Math.min(alpha, 255) << 24);
                ctx.fill(cx - outerX - 1, cy + dy, cx - outerX, cy + dy + 1, edgeColor);
                ctx.fill(cx + outerX + 1, cy + dy, cx + outerX + 2, cy + dy + 1, edgeColor);
            }

            float innerFrac = (int) Math.ceil(innerEdge) - innerEdge;
            if (innerFrac > 0.001f && innerX > 0) {
                int alpha = Math.round(a * innerFrac * 255);
                int edgeColor = rgb | (Math.min(alpha, 255) << 24);
                ctx.fill(cx - innerX, cy + dy, cx - innerX + 1, cy + dy + 1, edgeColor);
                ctx.fill(cx + innerX - 1, cy + dy, cx + innerX, cy + dy + 1, edgeColor);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseButtonPost(InputEvent.MouseButton.Post event) {
        if (event.getButton() != 0) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();

        double mx = mc.mouse.getX() * window.getScaledWidth() / window.getWidth();
        double my = mc.mouse.getY() * window.getScaledHeight() / window.getHeight();

        if (!HudOrbState.isVisible()) return;

        int ox = HudOrbState.getX();
        int oy = HudOrbState.getY();
        int os = HudOrbState.getSize();

        int cx = ox + os / 2;
        int cy = oy + os / 2;
        int radius = os / 2;
        double dx = mx - cx;
        double dy = my - cy;
        boolean overOrb = (dx * dx + dy * dy) <= (radius * radius);

        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (overOrb) {
                sPressed = true;
                sDragOffX = (int) mx - ox;
                sDragOffY = (int) my - oy;
                sDragging = false;
            }
            return;
        }

        if (event.getAction() == GLFW.GLFW_RELEASE && sPressed) {
            sPressed = false;
            if (!sDragging) {
                HudOrbState.hide();
                HudOrbState.setHudRequested(true);
                mc.send(() -> MuiModApi.openScreen(new ModernHudFragment()));
            } else {
                snapOrb();
                sDragging = false;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!HudOrbState.isVisible() || !sPressed) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = mc.getWindow();

        double mx = mc.mouse.getX() * window.getScaledWidth() / window.getWidth();
        double my = mc.mouse.getY() * window.getScaledHeight() / window.getHeight();

        int newX = (int) mx - sDragOffX;
        int newY = (int) my - sDragOffY;

        if (!sDragging) {
            int dx = newX - HudOrbState.getX();
            int dy = newY - HudOrbState.getY();
            if (dx * dx + dy * dy > 4) {
                sDragging = true;
            }
        }

        HudOrbState.setPosition(newX, newY);
    }

    private static void snapOrb() {
        Window window = MinecraftClient.getInstance().getWindow();
        int x = HudOrbState.getX();
        int y = HudOrbState.getY();
        int os = HudOrbState.getSize();
        int sw = window.getScaledWidth();
        int sh = window.getScaledHeight();
        int snap = 48;

        int cx = x + os / 2;
        int cy = y + os / 2;

        if (cy < snap) y = 8;
        else if (cy > sh - snap) y = sh - os - 8;
        if (cx < snap) x = 8;
        else if (cx > sw - snap) x = sw - os - 8;

        HudOrbState.setPosition(x, y);
    }
}
