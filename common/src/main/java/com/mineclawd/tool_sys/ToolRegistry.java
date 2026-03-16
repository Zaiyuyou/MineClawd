package com.mineclawd.tool_sys;

import com.mineclawd.tool.*;
import com.mineclawd.tool_sys.plugin.PluginToolProvider;
import com.google.gson.JsonObject;
import com.mineclawd.MineClawd;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import dev.architectury.networking.NetworkManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表，管理所有可用的工具定义
 * 使用提供者模式，支持按功能领域分类管理工具
 */
public final class ToolRegistry {
    private static final Map<String, ToolDefinition> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, ToolDefinition> VALID_REGISTRY = new ConcurrentHashMap<>();
    private static final List<ToolProvider> PROVIDERS = new ArrayList<>();
    private static final List<String> INVALID_TOOLS = new ArrayList<>();
    
    static {
        System.out.println("[ToolRegistry] 静态初始化块开始执行...");
        // 注册内置工具提供者
        registerBuiltinProviders();
        System.out.println("[ToolRegistry] 已注册 " + PROVIDERS.size() + " 个工具提供者");
        // 从提供者加载工具定义
        loadToolsFromProviders();
        System.out.println("[ToolRegistry] 已加载 " + REGISTRY.size() + " 个工具定义");
        System.out.println("[ToolRegistry] 静态初始化块执行完成");
    }
    
    /**
     * 注册内置工具提供者
     */
    private static void registerBuiltinProviders() {
        PROVIDERS.add(new FileSystemTools());
        PROVIDERS.add(new NetworkTools());
        PROVIDERS.add(new GameCommandTools());
        PROVIDERS.add(new DynamicContentTools());
        PROVIDERS.add(new AssetManagementTools());
        PROVIDERS.add(new InteractionTools());
    }
    
    /**
     * 从插件加载工具提供者
     */
    public static void loadFromPlugins(PluginToolProvider pluginToolProvider) {
        if (pluginToolProvider != null) {
            PROVIDERS.add(pluginToolProvider);
            System.out.println("[ToolRegistry] 已注册插件工具提供者: " + pluginToolProvider.getName());
        }
    }
    
    /**
     * 重新加载所有工具定义（包括插件）
     */
    public static void reloadAll(PluginToolProvider pluginToolProvider) {
        REGISTRY.clear();
        VALID_REGISTRY.clear();
        INVALID_TOOLS.clear();
        PROVIDERS.clear();
        
        // 重新注册内置提供者
        registerBuiltinProviders();
        
        // 从插件加载提供者
        loadFromPlugins(pluginToolProvider);
        
        // 重新加载工具定义
        loadToolsFromProviders();
    }
    
    /**
     * 从所有提供者加载工具定义
     */
    private static void loadToolsFromProviders() {
        System.out.println("[ToolRegistry] 开始从提供者加载工具...");
        for (ToolProvider provider : PROVIDERS) {
            System.out.println("[ToolRegistry] 从提供者 " + provider.getName() + " 加载工具...");
            List<ToolDefinition> tools = provider.getTools();
            System.out.println("[ToolRegistry] 提供者 " + provider.getName() + " 返回 " + tools.size() + " 个工具");
            for (ToolDefinition tool : tools) {
                register(tool);
            }
        }
        System.out.println("[ToolRegistry] 工具加载完成");
    }
    
    /**
     * 注册工具定义
     */
    public static void register(ToolDefinition tool) {
        System.out.println("[ToolRegistry] register() 被调用: tool=" + tool + ", tool.name()=" + (tool != null ? tool.name() : "null"));
        if (tool != null && tool.name() != null && !tool.name().isBlank()) {
            // 验证工具参数schema是否符合OpenAI规范
            OpenAISchemaValidator.ValidationResult validation = OpenAISchemaValidator.validateToolDefinition(tool);
            
            // 总是注册到完整注册表（保持向后兼容）
            REGISTRY.put(tool.name(), tool);
            System.out.println("[ToolRegistry] 已注册工具: " + tool.name() + " (有效: " + validation.isValid() + ")");
            
            if (validation.isValid()) {
                // 验证通过的工具注册到有效注册表
                VALID_REGISTRY.put(tool.name(), tool);
            } else {
                // 验证失败的工具记录到无效工具列表
                INVALID_TOOLS.add(tool.name());
                
                // 记录验证错误
                System.err.println("⚠️ 工具定义验证失败: " + tool.name());
                for (String error : validation.getErrors()) {
                    System.err.println("   - " + error);
                }
            }
        } else {
            System.out.println("[ToolRegistry] 跳过注册: tool=" + tool + ", name=" + (tool != null ? tool.name() : "null"));
        }
    }
    
    /**
     * 获取工具定义
     */
    public static ToolDefinition get(String toolName) {
        return REGISTRY.get(toolName);
    }
    
    /**
     * 注销工具定义
     */
    public static void unregister(String toolName) {
        if (toolName != null && !toolName.isBlank()) {
            ToolDefinition removed = REGISTRY.remove(toolName);
            if (removed != null) {
                VALID_REGISTRY.remove(toolName);
                INVALID_TOOLS.remove(toolName);
                System.out.println("[ToolRegistry] 已注销工具: " + toolName);
            } else {
                System.out.println("[ToolRegistry] 工具不存在，无法注销: " + toolName);
            }
        }
    }
    
    /**
     * 获取所有工具定义
     */
    public static List<ToolDefinition> getAll() {
        return new ArrayList<>(REGISTRY.values());
    }
    
    /**
     * 根据条件过滤工具（只返回验证通过的工具）
     */
    public static List<ToolDefinition> getFiltered(boolean includeDynamicRegistry, boolean includeSearch) {
        List<ToolDefinition> tools = new ArrayList<>();
        
        for (ToolDefinition tool : VALID_REGISTRY.values()) {
            String name = tool.name();
            
            // 排除动态注册工具（除非启用）
            if (name.startsWith("list-dynamic-content") || 
                name.startsWith("register-dynamic") || 
                name.startsWith("update-dynamic") || 
                name.startsWith("unregister-dynamic")) {
                if (includeDynamicRegistry) {
                    tools.add(tool);
                }
                continue;
            }
            
            // 排除搜索工具（除非启用）
            if (name.equals("search")) {
                if (includeSearch) {
                    tools.add(tool);
                }
                continue;
            }
            
            // 包含其他所有验证通过的工具
            tools.add(tool);
        }
        
        return tools;
    }
    
    /**
     * 获取所有验证通过的工具定义（用于调试）
     */
    public static List<ToolDefinition> getAllValid() {
        return new ArrayList<>(VALID_REGISTRY.values());
    }
    
    /**
     * 获取无效工具列表
     */
    public static List<String> getInvalidTools() {
        return new ArrayList<>(INVALID_TOOLS);
    }
    
    /**
     * 检查是否有无效工具
     */
    public static boolean hasInvalidTools() {
        return !INVALID_TOOLS.isEmpty();
    }
    
    /**
     * 向玩家发送工具验证状态消息
     */
    public static void sendToolValidationWarning(net.minecraft.server.network.ServerPlayerEntity player) {
        int totalTools = REGISTRY.size();
        int validTools = VALID_REGISTRY.size();
        int invalidToolsCount = INVALID_TOOLS.size();
        
        String message;
        
        if (hasInvalidTools()) {
            java.util.List<String> invalidTools = getInvalidTools();
            
            // 构建警告消息
            message = "⚠️ 发现 " + invalidToolsCount + " 个无效工具定义，这些工具将不会被发送到LLM\\n" +
                     "受影响的工具: " + String.join(", ", invalidTools) + "\\n" +
                     "请检查服务器日志获取详细信息\\n" +
                     "工具统计: 总计 " + totalTools + " 个，有效 " + validTools + " 个，无效 " + invalidToolsCount + " 个";
        } else {
            // 构建成功消息
            message = "✅ 所有工具定义验证通过，可以正常使用\\n" +
                     "工具统计: 总计 " + totalTools + " 个工具全部有效";
        }
        
        // 使用正确的网络消息发送方法（参考DynamicContentRegistry）
        var buf = new net.minecraft.network.RegistryByteBuf(io.netty.buffer.Unpooled.buffer(), player.getServerWorld().getRegistryManager());
        buf.writeString(message);
        try {
            // 使用现有的网络消息发送机制
            dev.architectury.networking.NetworkManager.sendToPlayer(player, com.mineclawd.MineClawdNetworking.SYNC_DYNAMIC_CONTENT, buf);
            com.mineclawd.MineClawd.LOGGER.info("已向玩家 {} 发送工具验证状态消息", player.getName().getString());
        } catch (Throwable throwable) {
            com.mineclawd.MineClawd.LOGGER.warn(
                    "[ToolRegistry] 发送工具验证状态消息失败 {}: {}",
                    player.getName().getString(),
                    throwable.getMessage()
            );
        }
    }
    
    /**
     * 检查工具定义验证结果
     */
    public static void checkToolValidation() {
        if (hasInvalidTools()) {
            java.util.List<String> invalidTools = getInvalidTools();
            
            // 记录到日志
            com.mineclawd.MineClawd.LOGGER.warn("发现 {} 个无效工具定义，这些工具将不会被发送到LLM", invalidTools.size());
            for (String toolName : invalidTools) {
                com.mineclawd.MineClawd.LOGGER.warn("无效工具: {}", toolName);
                
                // 获取工具定义以获取详细错误信息
                ToolDefinition tool = get(toolName);
                if (tool != null) {
                    OpenAISchemaValidator.ValidationResult result = OpenAISchemaValidator.validateToolDefinition(tool);
                    
                    for (String error : result.getErrors()) {
                        com.mineclawd.MineClawd.LOGGER.warn("  - {}", error);
                    }
                }
            }
        } else {
            com.mineclawd.MineClawd.LOGGER.info("所有工具定义验证通过，可以正常使用");
        }
        
        // 记录工具加载统计信息
        int totalTools = REGISTRY.size();
        int validTools = VALID_REGISTRY.size();
        int invalidToolsCount = INVALID_TOOLS.size();
        
        com.mineclawd.MineClawd.LOGGER.info("工具加载统计: 总计 {} 个工具，有效 {} 个，无效 {} 个", 
            totalTools, validTools, invalidToolsCount);
    }
    
    /**
     * 注册默认工具定义
     * 现在工具定义由各个工具提供者类管理
     */
    private static void registerDefaultTools() {
        // 工具定义已迁移到各个工具提供者类中
        // 此方法现在为空，保持向后兼容性
    }
}