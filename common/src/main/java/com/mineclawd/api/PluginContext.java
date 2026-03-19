package com.mineclawd.api;

/**
 * 插件上下文
 * 为外挂插件提供运行时上下文信息
 * 
 * <p>插件上下文包含：</p>
 * <ul>
 *   <li>pluginId: 插件ID</li>
 *   <li>pluginName: 插件名称</li>
 *   <li>api: MineClawd API 实例</li>
 * </ul>
 * 
 * <p>外挂插件通过此上下文获取 MineClawd API 实例，从而调用 MineClawd 的功能</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public class PluginContext {
    
    // ==================== 字段 ====================
    
    /**
     * 插件ID
     */
    private final String pluginId;
    
    /**
     * 插件名称
     */
    private final String pluginName;
    
    /**
     * MineClawd API 实例
     */
    private final MineClawdAPI api;
    
    // ==================== 构造函数 ====================
    
    /**
     * 创建插件上下文
     * 
     * @param pluginId 插件ID
     * @param pluginName 插件名称
     * @param api MineClawd API 实例
     */
    public PluginContext(String pluginId, String pluginName, MineClawdAPI api) {
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.api = api;
    }
    
    // ==================== Getter 方法 ====================
    
    /**
     * 获取插件ID
     * 
     * @return 插件ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * 获取插件名称
     * 
     * @return 插件名称
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     * 获取 MineClawd API 实例
     * 
     * @return MineClawd API 实例
     */
    public MineClawdAPI getApi() {
        return api;
    }
}
