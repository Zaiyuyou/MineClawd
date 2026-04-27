package com.mineclawd.foundation.tool;

import com.mineclawd.MineClawd;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表
 * 管理所有注册的工具
 */
public class ToolRegistry {
    private static final Map<String, MineClawdTool> TOOLS = new ConcurrentHashMap<>();
    private static final Map<String, String> TOOL_PROMPT_APPENDIXES = new ConcurrentHashMap<>();
    private static final Map<String, String> TOOL_PROMPT_CATEGORIES = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> TOOL_ENABLED_STATUS = new ConcurrentHashMap<>();
    
    static {
        registerBuiltInTools();
    }
    
    /**
     * 注册内置工具
     * 可以在这里注册 MineClawd 内置的工具
     * 或者留空, 完全由 Mod 动态注册
     */
    private static void registerBuiltInTools() {
        // 先注册系统工具
        register(new com.mineclawd.foundation.tool.prompt.ToolInfoRequestTool());
        register(new com.mineclawd.foundation.tool.prompt.ToolInfoRequestTool.ListToolsTool());
        
        // 注册KubeJS工具
        register(new com.mineclawd.buildin.kubejs.KubeJsTools.ExecuteCommandTool());
        register(new com.mineclawd.buildin.kubejs.KubeJsTools.ApplyInstantServerScriptTool());
        register(new com.mineclawd.buildin.kubejs.KubeJsTools.ListServerScriptsTool());
        register(new com.mineclawd.buildin.kubejs.KubeJsTools.AskUserQuestionTool());
        
        // 注册文件操作工具
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.ListFilesTool());
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.ReadFilesTool());
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.WriteFilesTool());
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.CopyFilesTool());
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.MoveFilesTool());
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.GrepTool());
        register(new com.mineclawd.buildin.files.WorkspaceFileTool.CurlTool());
        
        // 注册网络工具
        register(new com.mineclawd.buildin.web.WebTools.SearchTool());
        
        // 注册Mod文档工具
        register(new com.mineclawd.buildin.mod.ModDocsTool.FetchUrlTool());
        register(new com.mineclawd.buildin.mod.ModDocsTool.FetchModrinthTool());
        register(new com.mineclawd.buildin.mod.ModDocsTool.ListCommandsTool());
        
        // 注册KubeJS管理工具
        register(new com.mineclawd.buildin.kubejs.KubeJsTools.ReloadGameTool());
        register(new com.mineclawd.buildin.kubejs.KubeJsTools.SyncCommandTreeTool());
        
        // 注册动态内容工具
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.ListDynamicContentTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.RegisterDynamicItemTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.RegisterDynamicBlockTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.RegisterDynamicFluidTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.UpdateDynamicItemTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.UpdateDynamicBlockTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.UpdateDynamicFluidTool());
        register(new com.mineclawd.buildin.dynamic.DynamicContentTool.UnregisterDynamicContentTool());
        
        // 注册资产管理工具
        register(new com.mineclawd.buildin.assets.AssetManagementTools.ListAssetsTool());
        register(new com.mineclawd.buildin.assets.AssetManagementTools.UpsertAssetRecordTool());
        register(new com.mineclawd.buildin.assets.AssetManagementTools.RemoveAssetRecordTool());
        
        MineClawd.LOGGER.info("Registered {} built-in tools", TOOLS.size());
    }
    
    /**
     * 注册一个工具
     * @param tool 工具实例
     * @throws IllegalArgumentException 如果工具名称已存在
     */
    public static void register(MineClawdTool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("Tool cannot be null");
        }
        
        String name = tool.getName();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tool name cannot be null or blank");
        }
        
        if (TOOLS.containsKey(name)) {
            throw new IllegalArgumentException("Tool already registered: " + name);
        }
        
        // 使用ToolConfig管理工具配置
        ToolConfig.registerTool(name, tool);
        boolean enabled = ToolConfig.isToolEnabled(name);
        
        TOOLS.put(name, tool);
        TOOL_ENABLED_STATUS.put(name, enabled);
        
        if (enabled) {
            // 只注册启用的工具的 Prompt Appendix
            String appendix = tool.getPromptAppendix();
            if (appendix != null && !appendix.isBlank()) {
                TOOL_PROMPT_APPENDIXES.put(name, appendix);
            }
            
            // 注册 Prompt Category
            String category = tool.getPromptCategory();
            if (category != null && !category.isBlank()) {
                TOOL_PROMPT_CATEGORIES.put(name, category);
            }
        }
        
        MineClawd.LOGGER.info("Registered tool: {} (enabled: {})", name, enabled);
    }
    
    /**
     * 注销一个工具
     * @param name 工具名称
     * @return 是否成功
     */
    public static boolean unregister(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        
        boolean removed = TOOLS.remove(name) != null;
        TOOL_PROMPT_APPENDIXES.remove(name);
        TOOL_PROMPT_CATEGORIES.remove(name);
        TOOL_ENABLED_STATUS.remove(name);
        
        return removed;
    }
    
    /**
     * 重新加载所有内置工具
     * 用于支持热重载和动态更新
     */
    public static void reloadBuiltInTools() {
        // 清理所有现有工具
        clearAllTools();
        
        // 重新注册内置工具
        registerBuiltInTools();
        
        MineClawd.LOGGER.info("Reloaded built-in tools, total: {}", TOOLS.size());
    }
    
    /**
     * 清理所有工具
     * 用于重载或重置场景
     */
    public static void clearAllTools() {
        int count = TOOLS.size();
        TOOLS.clear();
        TOOL_PROMPT_APPENDIXES.clear();
        TOOL_PROMPT_CATEGORIES.clear();
        TOOL_ENABLED_STATUS.clear();
        
        MineClawd.LOGGER.info("Cleared all tools, removed: {}", count);
    }
    
    /**
     * 启用一个工具
     * @param name 工具名称
     * @return 是否成功
     */
    public static boolean enable(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        
        MineClawdTool tool = TOOLS.get(name);
        if (tool == null) {
            return false;
        }
        
        // 使用ToolConfig管理工具状态
        ToolConfig.setToolEnabled(name, true);
        TOOL_ENABLED_STATUS.put(name, true);
        
        String appendix = tool.getPromptAppendix();
        if (appendix != null && !appendix.isBlank()) {
            TOOL_PROMPT_APPENDIXES.put(name, appendix);
        }
        
        String category = tool.getPromptCategory();
        if (category != null && !category.isBlank()) {
            TOOL_PROMPT_CATEGORIES.put(name, category);
        }
        
        MineClawd.LOGGER.info("Enabled tool: {}", name);
        return true;
    }
    
    /**
     * 禁用一个工具
     * @param name 工具名称
     * @return 是否成功
     */
    public static boolean disable(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        
        MineClawdTool tool = TOOLS.get(name);
        if (tool == null) {
            return false;
        }
        
        // 使用ToolConfig管理工具状态
        ToolConfig.setToolEnabled(name, false);
        TOOL_ENABLED_STATUS.put(name, false);
        TOOL_PROMPT_APPENDIXES.remove(name);
        TOOL_PROMPT_CATEGORIES.remove(name);
        
        MineClawd.LOGGER.info("Disabled tool: {}", name);
        return true;
    }
    
    /**
     * 获取工具是否启用
     * @param name 工具名称
     * @return 是否启用, 不存在返回 false
     */
    public static boolean isEnabled(String name) {
        return TOOL_ENABLED_STATUS.getOrDefault(name, false);
    }
    
    /**
     * 获取工具
     * @param name 工具名称
     * @return 工具实例, 不存在返回 null
     */
    public static MineClawdTool get(String name) {
        return TOOLS.get(name);
    }
    
    /**
     * 获取所有工具
     * @return 工具映射
     */
    public static Map<String, MineClawdTool> getAll() {
        return Collections.unmodifiableMap(TOOLS);
    }
    
    /**
     * 获取所有启用的工具
     * @return 启用的工具映射
     */
    public static Map<String, MineClawdTool> getAllEnabled() {
        Map<String, MineClawdTool> enabledTools = new LinkedHashMap<>();
        TOOLS.forEach((name, tool) -> {
            if (isEnabled(name)) {
                enabledTools.put(name, tool);
            }
        });
        return Collections.unmodifiableMap(enabledTools);
    }
    
    /**
     * 获取所有启用的工具描述
     * @return 工具名称 -> 工具描述的映射
     */
    public static Map<String, String> getEnabledToolDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();
        TOOLS.forEach((name, tool) -> {
            if (isEnabled(name)) {
                descriptions.put(name, tool.getDescription());
            }
        });
        return Collections.unmodifiableMap(descriptions);
    }
    
    /**
     * 获取所有启用的工具 Prompt Appendix
     * @return 工具名称 -> Prompt Appendix 的映射
     */
    public static Map<String, String> getEnabledToolAppendixes() {
        return Collections.unmodifiableMap(TOOL_PROMPT_APPENDIXES);
    }
    
    /**
     * 获取工具的 Prompt Category
     * @param name 工具名称
     * @return Prompt Category 名称, 不存在返回 null
     */
    public static String getPromptCategory(String name) {
        return TOOL_PROMPT_CATEGORIES.get(name);
    }
    
    /**
     * 获取工具数量
     * @return 工具数量
     */
    public static int size() {
        return TOOLS.size();
    }
    
    /**
     * 获取启用的工具数量
     * @return 启用的工具数量
     */
    public static int enabledSize() {
        return (int) TOOL_ENABLED_STATUS.values().stream().filter(Boolean::booleanValue).count();
    }
    
    public static ToolStatusDescriptor getToolStatusDescriptor(String name, String toolName, JsonObject args) {
        MineClawdTool tool = TOOLS.get(name);
        if (tool != null) {
            return tool.getToolStatusDescriptor(toolName, args);
        }
        return new ToolStatusDescriptor("Completed task step", "Tool: " + (toolName == null ? "" : toolName));
    }
}
