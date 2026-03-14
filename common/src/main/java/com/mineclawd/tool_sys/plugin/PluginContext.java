package com.mineclawd.tool_sys.plugin;

import net.minecraft.server.command.ServerCommandSource;

/**
 * 插件上下文接口
 * 为插件提供运行时上下文信息
 */
public interface PluginContext {
    
    /**
     * 获取服务器命令源
     */
    ServerCommandSource getSource();
    
    /**
     * 获取插件配置
     */
    PluginConfig getConfig();
    
    /**
     * 获取插件管理器
     */
    PluginManager getPluginManager();
    
    /**
     * 记录插件日志
     */
    void logInfo(String message);
    
    void logWarning(String message);
    
    void logError(String message);
}