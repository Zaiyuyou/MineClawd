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
    private final List<PluginRegistrationCallback> registrationCallbacks = new ArrayList<>();
    private final ServerCommandSource source;
    
    public PluginManager(ServerCommandSource source) {
        this.source = source;
    }
    
    /**
     * 注册插件注册回调
     */
    public void registerCallback(PluginRegistrationCallback callback) {
        if (callback != null && !registrationCallbacks.contains(callback)) {
            registrationCallbacks.add(callback);
            info("注册插件注册回调: " + callback.getClass().getName());
        }
    }
    
    /**
     * 移除插件注册回调
     */
    public void unregisterCallback(PluginRegistrationCallback callback) {
        registrationCallbacks.remove(callback);
        info("移除插件注册回调: " + callback.getClass().getName());
    }
    
    /**
     * 触发插件注册回调
     */
    private void triggerPluginRegistered(AbstractPlugin plugin) {
        for (PluginRegistrationCallback callback : registrationCallbacks) {
            try {
                callback.onPluginRegistered(plugin, this);
            } catch (Exception e) {
                warn("触发插件注册回调失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发插件卸载回调
     */
    private void triggerPluginUnregistered(AbstractPlugin plugin) {
        for (PluginRegistrationCallback callback : registrationCallbacks) {
            try {
                callback.onPluginUnregistered(plugin, this);
            } catch (Exception e) {
                warn("触发插件卸载回调失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发工具注册回调
     */
    private void triggerToolRegistered(AbstractPlugin plugin, String toolName) {
        for (PluginRegistrationCallback callback : registrationCallbacks) {
            try {
                callback.onToolRegistered(plugin, toolName, this);
            } catch (Exception e) {
                warn("触发工具注册回调失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 触发工具卸载回调
     */
    private void triggerToolUnregistered(AbstractPlugin plugin, String toolName) {
        for (PluginRegistrationCallback callback : registrationCallbacks) {
            try {
                callback.onToolUnregistered(plugin, toolName, this);
            } catch (Exception e) {
                warn("触发工具卸载回调失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 扫描并注册插件
     */
    public void scanAndRegisterPlugins() {
        info("开始扫描插件...");
        
        // 使用ServiceLoader扫描插件
        java.util.ServiceLoader<AbstractPlugin> loader = java.util.ServiceLoader.load(AbstractPlugin.class);
        System.out.println("[PluginManager] ServiceLoader.load() 返回 loader: " + loader);
        
        // 检查ServiceLoader的配置
        java.util.Iterator<AbstractPlugin> iterator = loader.iterator();
        System.out.println("[PluginManager] ServiceLoader 迭代器: " + iterator);
        
        int pluginCount = 0;
        int checkedCount = 0;
        while (iterator.hasNext()) {
            checkedCount++;
            try {
                AbstractPlugin plugin = iterator.next();
                pluginCount++;
                System.out.println("[PluginManager] 发现插件 #" + pluginCount + ": " + plugin.getClass().getName() + ", pluginInfo: " + plugin.getPluginInfo());
                registerPlugin(plugin);
            } catch (Exception e) {
                System.err.println("[PluginManager] 加载插件时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("[PluginManager] ServiceLoader扫描完成，检查了 " + checkedCount + " 个服务，共发现 " + pluginCount + " 个插件");
        
        // 如果没有找到插件，尝试手动注册MineClawd内置插件
        if (plugins.isEmpty()) {
            info("未找到外部插件，尝试注册内置插件...");
            registerBuiltInPlugins();
        }
        
        // 扫描完成后，初始化所有插件
        initializePlugins();
        
        info("插件扫描完成，共注册 " + plugins.size() + " 个插件，" + tools.size() + " 个工具");
    }
    
    /**
     * 注册内置插件（当没有找到外部插件时）
     */
    private void registerBuiltInPlugins() {
        // 这里可以注册MineClawd内置的插件
        // 目前暂时不注册任何内置插件
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
        
        // 触发插件注册回调
        triggerPluginRegistered(plugin);
        
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
        System.out.println("[PluginManager] 扫描插件 " + pluginId + " 的工具方法，共 " + methods.length + " 个方法");
        for (Method method : methods) {
            ToolExecutor toolAnnotation = method.getAnnotation(ToolExecutor.class);
            if (toolAnnotation != null) {
                System.out.println("[PluginManager] 发现工具方法: " + method.getName() + ", annotation: " + toolAnnotation.name() + ", enabled: " + toolAnnotation.enabled());
                if (toolAnnotation.enabled()) {
                    registerTool(plugin, method, toolAnnotation);
                } else {
                    System.out.println("[PluginManager] 工具方法 " + toolAnnotation.name() + " 被禁用，跳过注册");
                }
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
        System.out.println("[PluginManager] 已注册工具: " + toolName + " (category: " + annotation.category() + ")");
        info("注册工具: " + toolName + " (" + annotation.category() + ")");
        
        // 触发工具注册回调
        triggerToolRegistered(plugin, toolName);
    }
    
    /**
     * 执行工具
     */
    public String executeTool(String toolName, Map<String, Object> parameters) {
        ToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return "ERROR: 工具 " + toolName + " 不存在";
        }
        
        if (!tool.isEnabled()) {
            return "ERROR: 工具 " + toolName + " 已被禁用";
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
        System.out.println("[PluginManager] getAllTools() 返回 " + tools.size() + " 个工具");
        return tools.values();
    }
    
    /**
     * 获取所有启用的工具定义
     */
    public List<ToolDefinition> getEnabledTools() {
        return tools.values().stream()
            .filter(ToolDefinition::isEnabled)
            .toList();
    }
    
    /**
     * 获取所有禁用的工具定义
     */
    public List<ToolDefinition> getDisabledTools() {
        return tools.values().stream()
            .filter(tool -> !tool.isEnabled())
            .toList();
    }
    
    /**
     * 启用工具
     */
    public boolean enableTool(String toolName) {
        ToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return false;
        }
        tool.setEnabled(true);
        info("启用工具: " + toolName);
        return true;
    }
    
    /**
     * 禁用工具
     */
    public boolean disableTool(String toolName) {
        ToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            return false;
        }
        tool.setEnabled(false);
        info("禁用工具: " + toolName);
        return true;
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