package com.mineclawd.tool_sys.plugin;

import com.mineclawd.tool_sys.plugin.annotation.ToolPlugin;
import com.mineclawd.tool_sys.plugin.annotation.ToolExecutor;
import net.minecraft.server.command.ServerCommandSource;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件管理器
 * 负责插件的扫描、注册、管理和工具执行
 */
public class PluginManager {
    
    private final Map<String, AbstractPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();
    private final Map<String, PluginConfig> pluginConfigs = new ConcurrentHashMap<>();
    private final ServerCommandSource source;
    
    public PluginManager(ServerCommandSource source) {
        this.source = source;
    }
    
    /**
     * 扫描并注册插件
     */
    public void scanAndRegisterPlugins() {
        // 这里应该实现类路径扫描逻辑
        // 暂时使用手动注册的方式
        
        info("开始扫描插件...");
        
        // 扫描完成后，初始化所有插件
        initializePlugins();
        
        info("插件扫描完成，共注册 " + plugins.size() + " 个插件，" + tools.size() + " 个工具");
    }
    
    /**
     * 手动注册插件（用于测试和开发）
     */
    public void registerPlugin(AbstractPlugin plugin) {
        ToolPlugin pluginInfo = plugin.getPluginInfo();
        if (pluginInfo == null) {
            warn("插件 " + plugin.getClass().getName() + " 缺少 @ToolPlugin 注解，跳过注册");
            return;
        }
        
        String pluginId = pluginInfo.id();
        if (plugins.containsKey(pluginId)) {
            warn("插件 " + pluginId + " 已存在，跳过重复注册");
            return;
        }
        
        // 创建插件配置
        PluginConfig config = new DefaultPluginConfig(pluginId);
        pluginConfigs.put(pluginId, config);
        
        // 创建插件上下文
        PluginContext context = new DefaultPluginContext(source, this, config);
        
        // 初始化插件
        plugin.onEnable(context);
        plugins.put(pluginId, plugin);
        
        // 扫描插件中的工具方法
        scanPluginTools(plugin);
        
        info("注册插件: " + pluginInfo.name() + " (" + pluginId + ")");
    }
    
    /**
     * 扫描插件中的工具方法
     */
    private void scanPluginTools(AbstractPlugin plugin) {
        ToolPlugin pluginInfo = plugin.getPluginInfo();
        String pluginId = pluginInfo.id();
        
        Method[] methods = plugin.getClass().getDeclaredMethods();
        for (Method method : methods) {
            ToolExecutor toolAnnotation = method.getAnnotation(ToolExecutor.class);
            if (toolAnnotation != null && toolAnnotation.enabled()) {
                registerTool(plugin, method, toolAnnotation);
            }
        }
        
        info("插件 " + pluginId + " 注册了 " + 
             methods.length + " 个方法，其中 " + 
             countPluginTools(pluginId) + " 个工具方法");
    }
    
    /**
     * 注册工具方法
     */
    private void registerTool(AbstractPlugin plugin, Method method, ToolExecutor annotation) {
        String toolName = annotation.name();
        
        if (tools.containsKey(toolName)) {
            warn("工具 " + toolName + " 已存在，跳过重复注册");
            return;
        }
        
        ToolDefinition toolDefinition = new ToolDefinition(
            toolName,
            annotation.description(),
            annotation.category(),
            method,
            plugin,
            annotation.parameterMode()
        );
        
        tools.put(toolName, toolDefinition);
        info("注册工具: " + toolName + " (" + annotation.category() + ")");
    }
    
    /**
     * 执行工具
     */
    public String executeTool(String toolName, Map<String, Object> parameters) {
        ToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return "ERROR: 工具 " + toolName + " 不存在";
        }
        
        try {
            Method method = tool.getMethod();
            Object pluginInstance = tool.getPluginInstance();
            
            // 根据参数模式处理参数
            Object[] args = prepareArguments(method, parameters, tool.getParameterMode());
            
            // 执行工具方法
            Object result = method.invoke(pluginInstance, args);
            
            return result != null ? result.toString() : "工具执行完成";
            
        } catch (Exception e) {
            error("执行工具 " + toolName + " 时发生错误: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * 准备方法参数
     */
    private Object[] prepareArguments(Method method, Map<String, Object> parameters, 
                                   ToolExecutor.ParameterMode parameterMode) {
        // 简化实现：直接将参数Map传递给方法
        // 实际实现应该根据参数模式进行转换
        return new Object[]{parameters};
    }
    
    /**
     * 获取所有工具定义
     */
    public Collection<ToolDefinition> getAllTools() {
        return tools.values();
    }
    
    /**
     * 获取插件工具数量
     */
    private int countPluginTools(String pluginId) {
        return (int) tools.values().stream()
            .filter(tool -> {
                AbstractPlugin plugin = (AbstractPlugin) tool.getPluginInstance();
                return plugin.getPluginId().equals(pluginId);
            })
            .count();
    }
    
    /**
     * 初始化所有插件
     */
    private void initializePlugins() {
        // 这里可以添加依赖关系检查和初始化顺序控制
        plugins.values().forEach(plugin -> {
            // 插件已经在注册时初始化了
        });
    }
    
    // 日志方法
    private void info(String message) {
        System.out.println("[PluginManager] " + message);
    }
    
    private void warn(String message) {
        System.out.println("[PluginManager] WARN: " + message);
    }
    
    private void error(String message) {
        System.err.println("[PluginManager] ERROR: " + message);
    }
}