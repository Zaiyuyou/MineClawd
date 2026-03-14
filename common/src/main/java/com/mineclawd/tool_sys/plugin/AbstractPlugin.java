package com.mineclawd.tool_sys.plugin;

import com.mineclawd.tool_sys.plugin.annotation.ToolPlugin;

/**
 * 抽象插件基类
 * 提供插件生命周期管理和基础功能
 */
public abstract class AbstractPlugin {
    
    protected PluginContext context;
    protected PluginConfig config;
    
    /**
     * 插件初始化
     */
    public void onEnable(PluginContext context) {
        this.context = context;
        this.config = context.getConfig();
    }
    
    /**
     * 插件禁用
     */
    public void onDisable() {
        // 子类可以重写此方法进行清理
    }
    
    /**
     * 获取插件信息
     */
    public ToolPlugin getPluginInfo() {
        return this.getClass().getAnnotation(ToolPlugin.class);
    }
    
    /**
     * 获取插件ID
     */
    public String getPluginId() {
        ToolPlugin info = getPluginInfo();
        return info != null ? info.id() : this.getClass().getSimpleName();
    }
    
    /**
     * 记录日志的便捷方法
     */
    protected void info(String message) {
        context.logInfo("[" + getPluginId() + "] " + message);
    }
    
    protected void warn(String message) {
        context.logWarning("[" + getPluginId() + "] " + message);
    }
    
    protected void error(String message) {
        context.logError("[" + getPluginId() + "] " + message);
    }
}