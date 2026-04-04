package com.mineclawd.foundation.config;

import net.minecraft.client.gui.screen.Screen;

/**
 * 配置屏幕工厂，使用反射来避免直接依赖 YACL
 */
public final class MineClawdConfigScreenFactory {
    
    private MineClawdConfigScreenFactory() {
    }
    
    /**
     * 创建配置屏幕
     * @param parent 父屏幕
     * @return 配置屏幕，如果 YACL 不可用则返回父屏幕
     */
    public static Screen create(Screen parent) {
        return create(parent, null);
    }
    
    /**
     * 创建配置屏幕
     * @param parent 父屏幕
     * @param initialBroadcastTarget 初始广播目标
     * @return 配置屏幕，如果 YACL 不可用则返回父屏幕
     */
    public static Screen create(Screen parent, String initialBroadcastTarget) {
        try {
            // 检查 YACL 是否可用
            Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib");
            
            // 使用反射调用 MineClawdConfigScreen 的方法
            Class<?> configScreenClass = Class.forName("com.mineclawd.foundation.config.MineClawdConfigScreen");
            
            if (initialBroadcastTarget != null) {
                return (Screen) configScreenClass.getMethod("create", Screen.class, String.class)
                        .invoke(null, parent, initialBroadcastTarget);
            } else {
                return (Screen) configScreenClass.getMethod("create", Screen.class)
                        .invoke(null, parent);
            }
        } catch (Exception e) {
            // YACL 不可用，返回父屏幕
            return parent;
        }
    }
    
    /**
     * 检查 YACL 是否可用
     * @return YACL 是否可用
     */
    public static boolean isYACLAvailable() {
        try {
            Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 从服务器同步广播目标
     * @param value 广播目标值
     */
    public static void syncBroadcastTargetFromServer(String value) {
        if (!isYACLAvailable()) {
            return;
        }
        
        try {
            Class<?> configScreenClass = Class.forName("com.mineclawd.foundation.config.MineClawdConfigScreen");
            configScreenClass.getMethod("syncBroadcastTargetFromServer", String.class)
                    .invoke(null, value);
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    /**
     * 清除广播目标服务器同步
     */
    public static void clearBroadcastTargetServerSync() {
        if (!isYACLAvailable()) {
            return;
        }
        
        try {
            Class<?> configScreenClass = Class.forName("com.mineclawd.foundation.config.MineClawdConfigScreen");
            configScreenClass.getMethod("clearBroadcastTargetServerSync")
                    .invoke(null);
        } catch (Exception e) {
            // 忽略错误
        }
    }
}