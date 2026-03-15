package com.mineclawd.tool_sys.plugin;

/**
 * 插件注册回调接口
 * 允许插件主动向MineClawd注册自己
 */
public interface PluginRegistrationCallback {
    
    /**
     * 当插件被注册时调用
     * 
     * @param plugin 插件实例
     * @param pluginManager 插件管理器
     */
    void onPluginRegistered(AbstractPlugin plugin, PluginManager pluginManager);
    
    /**
     * 当插件被卸载时调用
     * 
     * @param plugin 插件实例
     * @param pluginManager 插件管理器
     */
    void onPluginUnregistered(AbstractPlugin plugin, PluginManager pluginManager);
    
    /**
     * 当插件工具被注册时调用
     * 
     * @param plugin 插件实例
     * @param toolName 工具名称
     * @param pluginManager 插件管理器
     */
    void onToolRegistered(AbstractPlugin plugin, String toolName, PluginManager pluginManager);
    
    /**
     * 当插件工具被卸载时调用
     * 
     * @param plugin 插件实例
     * @param toolName 工具名称
     * @param pluginManager 插件管理器
     */
    void onToolUnregistered(AbstractPlugin plugin, String toolName, PluginManager pluginManager);
}
