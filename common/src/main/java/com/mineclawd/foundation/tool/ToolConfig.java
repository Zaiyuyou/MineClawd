package com.mineclawd.foundation.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineclawd.MineClawd;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具配置管理器
 * 管理所有工具的配置，支持动态配置和持久化
 */
public class ToolConfig {
    
    /**
     * 单个工具的配置项
     */
    public static class ToolConfigEntry {
        public boolean enabled = true;           // 工具是否启用
        public String category = "General";      // 工具分类
        public JsonObject customConfig = null;   // 自定义配置
        
        public ToolConfigEntry() {}
        
        public ToolConfigEntry(boolean enabled, String category) {
            this.enabled = enabled;
            this.category = category;
        }
    }
    
    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("mineclawd-tools.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, ToolConfigEntry> TOOL_CONFIGS = new HashMap<>();
    
    static {
        loadConfig();
    }
    
    /**
     * 加载工具配置
     */
    private static void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            // 配置文件不存在，创建默认配置
            saveConfig();
            return;
        }
        
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonObject configJson = JsonParser.parseReader(reader).getAsJsonObject();
            
            for (Map.Entry<String, com.google.gson.JsonElement> entry : configJson.entrySet()) {
                String toolName = entry.getKey();
                JsonObject toolConfigJson = entry.getValue().getAsJsonObject();
                
                ToolConfigEntry toolConfig = new ToolConfigEntry();
                toolConfig.enabled = toolConfigJson.has("enabled") ? 
                    toolConfigJson.get("enabled").getAsBoolean() : true;
                toolConfig.category = toolConfigJson.has("category") ? 
                    toolConfigJson.get("category").getAsString() : "General";
                toolConfig.customConfig = toolConfigJson.has("customConfig") ? 
                    toolConfigJson.get("customConfig").getAsJsonObject() : null;
                
                TOOL_CONFIGS.put(toolName, toolConfig);
            }
            
        } catch (Exception e) {
            MineClawd.LOGGER.error("Failed to load tool config: {}", CONFIG_PATH, e);
            // 加载失败时使用默认配置
            TOOL_CONFIGS.clear();
        }
    }
    
    /**
     * 保存工具配置
     */
    private static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            
            JsonObject configJson = new JsonObject();
            for (Map.Entry<String, ToolConfigEntry> entry : TOOL_CONFIGS.entrySet()) {
                JsonObject toolConfigJson = new JsonObject();
                toolConfigJson.addProperty("enabled", entry.getValue().enabled);
                toolConfigJson.addProperty("category", entry.getValue().category);
                if (entry.getValue().customConfig != null) {
                    toolConfigJson.add("customConfig", entry.getValue().customConfig);
                }
                configJson.add(entry.getKey(), toolConfigJson);
            }
            
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(configJson, writer);
            }
            
        } catch (IOException e) {
            MineClawd.LOGGER.error("Failed to save tool config: {}", CONFIG_PATH, e);
        }
    }
    
    /**
     * 获取工具配置
     */
    public static ToolConfigEntry getConfig(String toolName) {
        return TOOL_CONFIGS.get(toolName);
    }
    
    /**
     * 注册新工具时自动创建默认配置
     */
    public static void registerTool(String toolName, MineClawdTool tool) {
        if (!TOOL_CONFIGS.containsKey(toolName)) {
            // 第一次注册，创建默认配置
            ToolConfigEntry config = new ToolConfigEntry();
            config.enabled = tool.isEnabled();  // 使用工具的默认启用状态
            config.category = tool.getPromptCategory() != null ? 
                tool.getPromptCategory() : "General";
            
            TOOL_CONFIGS.put(toolName, config);
            saveConfig();  // 保存新配置
        }
    }
    
    /**
     * 更新工具配置
     */
    public static void updateConfig(String toolName, ToolConfigEntry config) {
        TOOL_CONFIGS.put(toolName, config);
        saveConfig();
    }
    
    /**
     * 检查工具是否启用
     */
    public static boolean isToolEnabled(String toolName) {
        ToolConfigEntry config = TOOL_CONFIGS.get(toolName);
        return config != null && config.enabled;
    }
    
    /**
     * 启用/禁用工具
     */
    public static void setToolEnabled(String toolName, boolean enabled) {
        ToolConfigEntry config = TOOL_CONFIGS.get(toolName);
        if (config != null) {
            config.enabled = enabled;
            saveConfig();
        }
    }
    
    /**
     * 获取所有工具配置
     */
    public static Map<String, ToolConfigEntry> getAllConfigs() {
        return new HashMap<>(TOOL_CONFIGS);
    }
}