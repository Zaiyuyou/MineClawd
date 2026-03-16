package com.mineclawd.tool_sys.plugin;

import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单的插件工具提供者
 * 插件只需创建这个类的实例并添加工具即可
 */
public class PluginToolProvider implements ToolProvider {
    
    private final String pluginName;
    private final List<ToolDefinition> tools = new ArrayList<>();
    
    public PluginToolProvider(String pluginName) {
        this.pluginName = pluginName;
    }
    
    @Override
    public List<ToolDefinition> getTools() {
        return new ArrayList<>(tools);
    }
    
    @Override
    public String getName() {
        return pluginName;
    }
    
    /**
     * 添加工具定义
     */
    public void addTool(ToolDefinition tool) {
        if (tool != null && tool.name() != null && !tool.name().isBlank()) {
            tools.add(tool);
        }
    }
    
    /**
     * 添加工具（简化版本）
     */
    public void addTool(String name, String description, com.google.gson.JsonObject parameters) {
        addTool(ToolDefinition.of(name, description, parameters));
    }
}
