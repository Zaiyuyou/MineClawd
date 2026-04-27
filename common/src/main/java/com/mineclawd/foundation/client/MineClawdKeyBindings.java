package com.mineclawd.foundation.client;

/**
 * MineClawd 快捷键常量定义类
 * 只定义常量，不包含任何注册逻辑
 * 注册逻辑完全由平台模块负责
 */
public class MineClawdKeyBindings {
    
    // ModernUI 界面快捷键的键名常量
    public static final String OPEN_MODERNUI_SCREEN_KEY = "key.mineclawd.open_modernui";
    public static final String OPEN_MODERNUI_SCREEN_CATEGORY = "category.mineclawd.general";
    
    /**
     * 打开 ModernUI 悬浮窗口的方法名
     * 平台模块需要实现这个方法
     */
    public static final String OPEN_MODERNUI_METHOD = "openModernUIScreen";
    
    /**
     * 日志记录方法
     * 平台模块调用这个方法来记录按键事件
     */
    public static void logKeyPress() {
        System.out.println("F7键按下 - ModernUI窗口切换请求已发送");
    }
}