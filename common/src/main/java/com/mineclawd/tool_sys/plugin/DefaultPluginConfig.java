package com.mineclawd.tool_sys.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认插件配置实现
 */
public class DefaultPluginConfig implements PluginConfig {
    
    private final String pluginId;
    private final Map<String, Object> configMap;
    
    public DefaultPluginConfig(String pluginId) {
        this.pluginId = pluginId;
        this.configMap = new HashMap<>();
        
        // 加载默认配置
        loadDefaults();
    }
    
    private void loadDefaults() {
        // 这里可以从文件加载配置
        // 暂时使用内存配置
        configMap.put("enabled", true);
    }
    
    @Override
    public String getPluginId() {
        return pluginId;
    }
    
    @Override
    public String getString(String key, String defaultValue) {
        Object value = configMap.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    @Override
    public int getInt(String key, int defaultValue) {
        Object value = configMap.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = configMap.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    @Override
    public void setString(String key, String value) {
        configMap.put(key, value);
    }
    
    @Override
    public void setInt(String key, int value) {
        configMap.put(key, value);
    }
    
    @Override
    public void setBoolean(String key, boolean value) {
        configMap.put(key, value);
    }
    
    @Override
    public Map<String, Object> getAll() {
        return new HashMap<>(configMap);
    }
    
    @Override
    public void save() {
        // 这里应该将配置保存到文件
        System.out.println("保存插件 " + pluginId + " 配置");
    }
}