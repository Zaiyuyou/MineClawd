package com.mineclawd.foundation.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * NeoForge专用的按键绑定注册类
 * 使用单例模式确保只注册一次
 */
@EventBusSubscriber(modid = "mineclawd", value = Dist.CLIENT)
public class MineClawdNeoForgeKeyBindings {
    
    // ModernUI 界面快捷键 - 使用 F7 键
    private static final KeyBinding OPEN_MODERNUI_SCREEN = new KeyBinding(
        com.mineclawd.foundation.client.MineClawdKeyBindings.OPEN_MODERNUI_SCREEN_KEY,
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_F7,
        com.mineclawd.foundation.client.MineClawdKeyBindings.OPEN_MODERNUI_SCREEN_CATEGORY
    );
    
    // 使用原子操作确保线程安全
    private static volatile boolean registered = false;
    private static final Object registrationLock = new Object();
    
    /**
     * 注册按键映射 - 使用双重检查锁定确保只注册一次
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        // 第一重检查：快速检查是否已注册
        if (!registered) {
            synchronized (registrationLock) {
                // 第二重检查：在锁内再次检查
                if (!registered) {
                    // 注册ModernUI界面快捷键
                    event.register(OPEN_MODERNUI_SCREEN);
                    
                    registered = true;
                    System.out.println("✅ ModernUI快捷键已成功注册到NeoForge");
                }
            }
        }
    }
    
    /**
     * 客户端每帧调用，处理按键事件
     */
    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        // 检查ModernUI快捷键是否被按下
        while (OPEN_MODERNUI_SCREEN.wasPressed()) {
            // 记录按键事件
            com.mineclawd.foundation.client.MineClawdKeyBindings.logKeyPress();
            
            // 直接调用neoforge模块的窗口管理器
            ModernUINeoForgeWindowManager.toggleFloatingWindow();
        }
    }
    
    /**
     * 获取ModernUI快捷键实例（供其他模块使用）
     */
    public static KeyBinding getModernUIKeyBinding() {
        return OPEN_MODERNUI_SCREEN;
    }
}