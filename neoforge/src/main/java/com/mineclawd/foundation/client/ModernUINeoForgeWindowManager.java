package com.mineclawd.foundation.client;

import com.mineclawd.foundation.client.ui.framework.HudOrbState;
import com.mineclawd.foundation.client.ui.framework.ModernHudFragment;
import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.MinecraftClient;

public class ModernUINeoForgeWindowManager {

    public static void toggleFloatingWindow() {
        if (HudOrbState.isHudShowing()) {
            // GUI is open → minimize to orb
            HudOrbState.hideHud();
            HudOrbState.show();
            MinecraftClient.getInstance().setScreen(null);
            return;
        }

        if (HudOrbState.isVisible()) {
            // Orb is visible → open GUI
            HudOrbState.hide();
            HudOrbState.setHudRequested(true);
            MuiModApi.openScreen(new ModernHudFragment());
            return;
        }

        // First time → open GUI
        HudOrbState.setHudRequested(true);
        HudOrbState.hide();
        MuiModApi.openScreen(new ModernHudFragment());
    }
}
