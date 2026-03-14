package com.mineclawd.tool_sys;

import com.mineclawd.tool.*;
import com.mineclawd.tool_sys.plugin.MineClawdPluginIntegration;
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
        // 注册内置工具提供者
        registerBuiltinProviders();
        // 从提供者加载工具定义
        loadToolsFromProviders();
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
     * 从插件管理器加载工具提供者
     */
    public static void loadFromPlugins() {
        // 使用新的插件集成系统
        // 插件工具将通过 MineClawdPluginIntegration 提供
        // 这里暂时留空，由 MineClawd 主类负责初始化插件系统
    }
    
    /**
     * 重新加载所有工具定义（包括插件）
     */
    public static void reloadAll() {
        REGISTRY.clear();
        VALID_REGISTRY.clear();
        INVALID_TOOLS.clear();
        PROVIDERS.clear();
        
        // 重新注册内置提供者
        registerBuiltinProviders();
        
        // 从插件加载提供者
        loadFromPlugins();
        
        // 重新加载工具定义
        loadToolsFromProviders();
    }
    
    /**
     * 从所有提供者加载工具定义
     */
    private static void loadToolsFromProviders() {
        for (ToolProvider provider : PROVIDERS) {
            for (ToolDefinition tool : provider.getTools()) {
                register(tool);
            }
        }
    }
    
    /**
     * 注册工具定义
     */
    public static void register(ToolDefinition tool) {
        if (tool != null && tool.name() != null && !tool.name().isBlank()) {
            // 验证工具参数schema是否符合OpenAI规范
            OpenAISchemaValidator.ValidationResult validation = OpenAISchemaValidator.validateToolDefinition(tool);
            
            // 总是注册到完整注册表（保持向后兼容）
            REGISTRY.put(tool.name(), tool);
            
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
        }
    }
    
    /**
     * 获取工具定义
     */
    public static ToolDefinition get(String toolName) {
        return REGISTRY.get(toolName);
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