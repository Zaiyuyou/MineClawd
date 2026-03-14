package com.mineclawd.tool_sys.plugin;

import java.util.Map;

/**
 * 插件配置接口
 */
public interface PluginConfig {
    
    /**
     * 获取插件ID
     */
    String getPluginId();
    
    /**
     * 获取配置值
     */
    String getString(String key, String defaultValue);
    
    int getInt(String key, int defaultValue);
    
    boolean getBoolean(String key, boolean defaultValue);
    
    /**
     * 设置配置值
     */
    void setString(String key, String value);
    
    void setInt(String key, int value);
    
    void setBoolean(String key, boolean value);
    
    /**
     * 获取所有配置
     */
    Map<String, Object> getAll();
    
    /**
     * 保存配置
     */
    void save();
}