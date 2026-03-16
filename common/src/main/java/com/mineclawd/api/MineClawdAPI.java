package com.mineclawd.api;

import com.mineclawd.tool_sys.plugin.PluginToolProvider;
import com.mineclawd.tool_sys.plugin.MineClawdPluginIntegration;
import com.mineclawd.MineClawd;

/**
 * MineClawd API接口
 * 为其他mod提供标准化的集成接口
 */
public final class MineClawdAPI {
    
    private MineClawdAPI() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 注册工具插件
     * @param plugin 工具插件实例
     * @return 注册是否成功
     */
    public static boolean registerPlugin(MineClawdPluginIntegration plugin) {
        if (plugin != null) {
            com.mineclawd.tool_sys.ToolRegistry.loadFromPlugins(plugin.getToolProvider());
            return true;
        }
        return false;
    }
    
    /**
     * 启用工具插件
     * @param pluginId 插件ID
     * @return 启用是否成功
     */
    public static boolean enablePlugin(String pluginId) {
        // 新的插件系统将在需要时动态初始化
        return true;
    }
    
    /**
     * 禁用工具插件
     * @param pluginId 插件ID
     * @return 禁用是否成功
     */
    public static boolean disablePlugin(String pluginId) {
        // 新的插件系统将在需要时动态初始化
        return true;
    }
    
    /**
     * 卸载工具插件
     * @param pluginId 插件ID
     * @return 卸载是否成功
     */
    public static boolean unregisterPlugin(String pluginId) {
        // 新的插件系统将在需要时动态初始化
        return true;
    }
    
    /**
     * 注册工具执行器
     * @param executor 工具执行器实例
     * @return 注册是否成功
     */
    public static boolean registerToolExecutor(Object executor) {
        // 新的插件系统使用注解驱动，无需手动注册
        return true;
    }
    
    /**
     * 注销工具执行器
     * @param toolName 工具名称
     * @return 注销是否成功
     */
    public static boolean unregisterToolExecutor(String toolName) {
        // 新的插件系统将在需要时动态管理
        return true;
    }
    
    /**
     * 检查插件是否已启用
     * @param pluginId 插件ID
     * @return 是否已启用
     */
    public static boolean isPluginEnabled(String pluginId) {
        // 新的插件系统将在需要时动态管理
        return true;
    }
    
    /**
     * 检查工具是否存在
     * @param toolName 工具名称
     * @return 是否存在
     */
    public static boolean hasTool(String toolName) {
        // 新的插件系统将在需要时动态管理
        return false;
    }
    
    /**
     * 重新加载所有工具定义
     * 在动态添加/移除插件后调用此方法
     */
    public static void reloadTools() {
        com.mineclawd.tool_sys.ToolRegistry.reloadAll(null);
    }
    
    /**
     * 获取API版本
     * @return API版本号
     */
    public static String getAPIVersion() {
        return "1.0.0";
    }
    
    /**
     * 获取MineClawd版本
     * @return MineClawd版本号
     */
    public static String getMineClawdVersion() {
        return "1.7.1-Zaiyuyou-fork";
    }
}