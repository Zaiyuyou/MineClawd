package com.mineclawd.tool_sys.plugin;

import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.*;

/**
 * MineClawd插件集成接口
 * 将插件系统与MineClawd核心系统集成
 */
public class MineClawdPluginIntegration implements ToolProvider {
    
    private final PluginManager pluginManager;
    private boolean initialized = false;
    
    public MineClawdPluginIntegration(ServerCommandSource source) {
        this.pluginManager = new PluginManager(source);
    }
    
    /**
     * 初始化插件系统
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        pluginManager.scanAndRegisterPlugins();
        initialized = true;
        
        System.out.println("[MineClawdPluginIntegration] 插件系统初始化完成");
    }
    
    /**
     * 手动注册插件（供MineClawd调用）
     */
    public void registerPlugin(AbstractPlugin plugin) {
        pluginManager.registerPlugin(plugin);
    }
    
    /**
     * 执行插件工具
     */
    public String executePluginTool(String toolName, Map<String, Object> parameters) {
        if (!initialized) {
            return "ERROR: 插件系统未初始化";
        }
        
        return pluginManager.executeTool(toolName, parameters);
    }
    
    /**
     * 获取所有插件工具定义（实现ToolProvider接口）
     */
    @Override
    public List<ToolDefinition> getTools() {
        if (!initialized) {
            return Collections.emptyList();
        }
        
        List<ToolDefinition> toolDefinitions = new ArrayList<>();
        
        for (com.mineclawd.tool_sys.plugin.ToolDefinition pluginTool : pluginManager.getAllTools()) {
            // 将插件工具定义转换为MineClawd工具定义
            ToolDefinition toolDef = convertToMineClawdToolDefinition(pluginTool);
            if (toolDef != null) {
                toolDefinitions.add(toolDef);
            }
        }
        
        return toolDefinitions;
    }
    
    /**
     * 将插件工具定义转换为MineClawd工具定义
     */
    private ToolDefinition convertToMineClawdToolDefinition(com.mineclawd.tool_sys.plugin.ToolDefinition pluginTool) {
        try {
            // 创建JSON参数对象
            com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
            com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
            com.google.gson.JsonArray required = new com.google.gson.JsonArray();
            
            for (com.mineclawd.tool_sys.plugin.ToolDefinition.ParameterDefinition param : pluginTool.getParameters()) {
                // 添加参数属性
                com.google.gson.JsonObject paramSchema = createParameterSchema(param);
                properties.add(param.getName(), paramSchema);
                
                // 如果是必需参数，添加到required数组
                if (param.isRequired()) {
                    required.add(param.getName());
                }
            }
            
            parameters.addProperty("type", "object");
            parameters.add("properties", properties);
            parameters.add("required", required);
            
            // 创建工具定义
            return ToolDefinition.of(
                pluginTool.getName(),
                pluginTool.getDescription(),
                parameters
            );
            
        } catch (Exception e) {
            System.err.println("转换工具定义失败: " + pluginTool.getName() + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建参数模式
     */
    private com.google.gson.JsonObject createParameterSchema(com.mineclawd.tool_sys.plugin.ToolDefinition.ParameterDefinition param) {
        com.google.gson.JsonObject schema = new com.google.gson.JsonObject();
        schema.addProperty("type", param.getType().name().toLowerCase());
        schema.addProperty("description", param.getDescription());
        
        if (!param.getDefaultValue().isEmpty()) {
            schema.addProperty("default", param.getDefaultValue());
        }
        
        return schema;
    }
    
    /**
     * 工具执行包装器
     */
    private String executeToolWrapper(Map<String, Object> args) {
        // 这里需要从args中提取工具名称和参数
        // 简化实现：假设第一个参数是工具名称
        String toolName = (String) args.get("tool_name");
        if (toolName == null) {
            return "ERROR: 缺少工具名称参数";
        }
        
        Map<String, Object> parameters = new HashMap<>(args);
        parameters.remove("tool_name");
        
        return executePluginTool(toolName, parameters);
    }
    
    /**
     * 获取插件管理器（供其他模块使用）
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
    /**
     * 检查插件系统是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 重新加载插件系统
     */
    public void reload() {
        // 这里可以实现插件重新加载逻辑
        System.out.println("[MineClawdPluginIntegration] 重新加载插件系统");
    }
}