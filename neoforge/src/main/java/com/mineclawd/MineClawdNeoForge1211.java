package com.mineclawd;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.client.gui.screen.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(MineClawd.MOD_ID)
public final class MineClawdNeoForge1211 {
    public MineClawdNeoForge1211(IEventBus modEventBus, ModContainer modContainer) {
        MineClawd.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            if (hasClass("dev.isxander.yacl3.api.YetAnotherConfigLib")) {
                modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                        (IConfigScreenFactory) (container, parent) -> createConfigScreen(parent));
            }
            MineClawdNeoForge1211Client.init(modEventBus);
        });
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className, false, MineClawdNeoForge1211.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Screen createConfigScreen(Screen parent) {
        try {
            Class<?> cls = Class.forName("com.mineclawd.foundation.config.MineClawdConfigScreenFactory");
            return (Screen) cls.getMethod("create", Screen.class).invoke(null, parent);
        } catch (ReflectiveOperationException ignored) {
            return parent;
        }
    }
}