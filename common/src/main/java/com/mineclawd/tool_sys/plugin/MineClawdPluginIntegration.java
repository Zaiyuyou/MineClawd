package com.mineclawd.tool_sys.plugin;

import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的插件集成器
 * 插件只需创建 PluginToolProvider 并添加工具即可
 */
public class MineClawdPluginIntegration implements ToolProvider {
    
    private final String pluginName;
    private final PluginToolProvider toolProvider;
    private boolean initialized = false;
    
    public MineClawdPluginIntegration(String pluginName) {
        this.pluginName = pluginName;
        this.toolProvider = new PluginToolProvider(pluginName);
    }
    
    public void initialize() {
        initialized = true;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 添加工具
     */
    public void addTool(String name, String description, com.google.gson.JsonObject parameters) {
        toolProvider.addTool(name, description, parameters);
    }
    
    /**
     * 添加工具
     */
    public void addTool(ToolDefinition tool) {
        toolProvider.addTool(tool);
    }
    
    /**
     * 获取插件名称
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     * 获取工具提供者
     */
    public PluginToolProvider getToolProvider() {
        return toolProvider;
    }
    
    /**
     * 获取所有工具定义（实现ToolProvider接口）
     */
    @Override
    public List<ToolDefinition> getTools() {
        return toolProvider.getTools();
    }
    
    @Override
    public String getName() {
        return pluginName;
    }
}
