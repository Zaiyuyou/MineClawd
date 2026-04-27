package com.mineclawd;

import com.mineclawd.buildin.dynamic.DynamicContentNeoForge1211Client;
import com.mineclawd.foundation.client.ui.framework.EmojiEnabler;
import icyllis.modernui.mc.ModernUIClient;
import net.neoforged.bus.api.IEventBus;

public final class MineClawdNeoForge1211Client {

    static {
        ModernUIClient.sUseColorEmoji = true;
        EmojiEnabler.ensure();
    }

    private MineClawdNeoForge1211Client() {
    }

    public static void init(IEventBus modEventBus) {
        MineClawdClientNetworking.init();
        DynamicContentNeoForge1211Client.init(modEventBus);

        System.out.println("MineClawd NeoForge客户端初始化完成");
    }
}