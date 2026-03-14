package com.mineclawd.tool;

import com.google.gson.JsonObject;
import com.mineclawd.MineClawd;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表，管理所有可用的工具定义
 * 类似于LangChain的工具注册机制
 */
public final class ToolRegistry {
    private static final Map<String, ToolDefinition> REGISTRY = new ConcurrentHashMap<>();
    
    static {
        // 注册基础工具
        registerDefaultTools();
    }
    
    /**
     * 注册工具定义
     */
    public static void register(ToolDefinition tool) {
        if (tool != null && tool.name() != null && !tool.name().isBlank()) {
            REGISTRY.put(tool.name(), tool);
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
     * 根据条件过滤工具
     */
    public static List<ToolDefinition> getFiltered(boolean includeDynamicRegistry, boolean includeSearch) {
        List<ToolDefinition> tools = new ArrayList<>();
        
        for (ToolDefinition tool : REGISTRY.values()) {
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
            
            // 包含其他所有工具
            tools.add(tool);
        }
        
        return tools;
    }
    
    /**
     * 注册默认工具定义
     */
    private static void registerDefaultTools() {
        // 基础工具
        register(ToolDefinition.of(
            "ask-user-question",
            "Ask the player a clarification question with up to five preset options. Do not include Other/Skip in options; MineClawd injects Other and handles skip.",
            createQuestionParameters()
        ));
        
        register(ToolDefinition.of(
            "apply-instant-server-script",
            "Apply immediate runtime changes by executing KubeJS JavaScript via /_exec_kubejs_internal.",
            createCodeParameters("JavaScript code to execute immediately. Supports multi-line strings.")
        ));
        
        register(ToolDefinition.of(
            "execute-command",
            "Execute a Minecraft command and return command output/result.",
            createCommandParameters()
        ));
        
        register(ToolDefinition.of(
            "list_commands",
            "List available root commands. Optionally filter with mod_id (best-effort by command name/prefix).",
            createListCommandsParameters()
        ));
        
        register(ToolDefinition.of(
            "fetch_modrinth",
            "Fetch the Modrinth project page content for an installed mod id.",
            createFetchModrinthParameters()
        ));
        
        register(ToolDefinition.of(
            "fetch_url",
            "Fetch any HTTP(S) URL and return readable content. HTML is converted to Markdown.",
            createFetchUrlParameters()
        ));
        
        register(ToolDefinition.of(
            "list-files",
            "List files/directories under the server root. Supports optional path, recursive, and limit.",
            createListFilesParameters()
        ));
        
        register(ToolDefinition.of(
            "read-files",
            "Read a UTF-8 text file under the server root.",
            createPathParameters("Relative file path under the server root, for example `logs/latest.log` or `world/serverconfig/mod.json`.")
        ));
        
        register(ToolDefinition.of(
            "write-files",
            "Write or overwrite a UTF-8 file under the server root.",
            createWriteParameters()
        ));
        
        register(ToolDefinition.of(
            "copy-files",
            "Copy a file or directory under the server root.",
            createCopyMoveParameters()
        ));
        
        register(ToolDefinition.of(
            "move-files",
            "Move or rename a file or directory under the server root.",
            createCopyMoveParameters()
        ));
        
        register(ToolDefinition.of(
            "grep",
            "Search file contents using regex under the server root.",
            createGrepParameters()
        ));
        
        register(ToolDefinition.of(
            "curl",
            "Perform an HTTP request and return status/body.",
            createCurlParameters()
        ));
        
        register(ToolDefinition.of(
            "read-image",
            "Read and describe an image file under the server root using the configured vision model.",
            createReadImageParameters()
        ));
        
        register(ToolDefinition.of(
            "reload-game",
            "Run /reload and return command output plus detected KubeJS load errors.",
            createNoArgParameters()
        ));
        
        register(ToolDefinition.of(
            "sync-command-tree",
            "Refresh command tree/tab-completion for all online players. Use only after command registration changes.",
            createNoArgParameters()
        ));
        
        register(ToolDefinition.of(
            "list-assets",
            "List currently tracked persistent asset records for this player/session owner.",
            createNoArgParameters()
        ));
        
        register(ToolDefinition.of(
            "upsert-asset-record",
            "Create or update an asset tracking record for entities, items/blocks/fluids, special items, commands, or game mechanics.",
            createAssetUpsertParameters()
        ));
        
        register(ToolDefinition.of(
            "remove-asset-record",
            "Remove an obsolete asset tracking record by id/reference.",
            createAssetRemoveParameters()
        ));
        
        // 搜索工具
        register(ToolDefinition.of(
            "search",
            "Search the web via Tavily and return concise snippets with URLs. Use this when external references are needed.",
            createSearchParameters()
        ));
        
        // 动态注册工具
        register(ToolDefinition.noArg(
            "list-dynamic-content",
            "List currently active dynamic placeholder entries and free slots."
        ));
        
        register(ToolDefinition.of(
            "register-dynamic-item",
            "Claim a free dynamic item placeholder slot.",
            createDynamicItemParameters()
        ));
        
        register(ToolDefinition.of(
            "register-dynamic-block",
            "Claim a free dynamic block placeholder slot.",
            createDynamicBlockParameters()
        ));
        
        register(ToolDefinition.of(
            "register-dynamic-fluid",
            "Claim a free dynamic fluid placeholder slot.",
            createDynamicFluidParameters()
        ));
        
        register(ToolDefinition.of(
            "update-dynamic-item",
            "Update properties for an existing dynamic item slot.",
            createDynamicItemUpdateParameters()
        ));
        
        register(ToolDefinition.of(
            "update-dynamic-block",
            "Update properties for an existing dynamic block slot.",
            createDynamicBlockUpdateParameters()
        ));
        
        register(ToolDefinition.of(
            "update-dynamic-fluid",
            "Update properties for an existing dynamic fluid slot.",
            createDynamicFluidUpdateParameters()
        ));
        
        register(ToolDefinition.of(
            "unregister-dynamic-content",
            "Release a dynamic placeholder entry by type and slot.",
            createDynamicUnregisterParameters()
        ));
    }
    
    // 参数创建方法（从MineClawd.java中复制）
    private static JsonObject createNoArgParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new JsonObject());
        parameters.add("required", new JsonObject());
        return parameters;
    }
    
    private static JsonObject createQuestionParameters() {
        // 简化的参数创建，实际实现需要从MineClawd.java复制完整逻辑
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject question = new JsonObject();
        question.addProperty("type", "string");
        question.addProperty("description", "Question for the player. Keep concise.");
        properties.add("question", question);
        
        JsonObject options = new JsonObject();
        options.addProperty("type", "array");
        options.addProperty("description", "Preset options (1-5 items).");
        JsonObject item = new JsonObject();
        item.addProperty("type", "string");
        options.add("items", item);
        options.addProperty("minItems", 1);
        options.addProperty("maxItems", 5);
        properties.add("options", options);
        
        parameters.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("question", "");
        parameters.add("required", required);
        
        return parameters;
    }
    
    // 其他参数创建方法（简化版，实际需要完整实现）
    private static JsonObject createCodeParameters(String description) {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject code = new JsonObject();
        code.addProperty("type", "string");
        code.addProperty("description", description);
        properties.add("code", code);
        
        parameters.add("properties", properties);
        
        JsonObject required = new JsonObject();
        required.addProperty("code", "");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static JsonObject createCommandParameters() {
        return createCodeParameters("Minecraft command to run, with or without leading slash.");
    }
    
    private static JsonObject createListCommandsParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject modId = new JsonObject();
        modId.addProperty("type", "string");
        modId.addProperty("description", "Optional installed mod id filter, for example `kubejs`.");
        properties.add("mod_id", modId);
        
        parameters.add("properties", properties);
        parameters.add("required", new JsonObject());
        
        return parameters;
    }
    
    // 其他参数方法...（为简洁起见，这里只展示关键方法）
    private static JsonObject createPathParameters(String description) {
        return createCodeParameters(description);
    }
    
    private static JsonObject createWriteParameters() {
        return createCodeParameters("File content to write.");
    }
    
    private static JsonObject createCopyMoveParameters() {
        return createCodeParameters("Source and destination paths.");
    }
    
    private static JsonObject createGrepParameters() {
        return createCodeParameters("Regex pattern to search for.");
    }
    
    private static JsonObject createCurlParameters() {
        return createCodeParameters("HTTP request details.");
    }
    
    private static JsonObject createReadImageParameters() {
        return createCodeParameters("Image file path.");
    }
    
    private static JsonObject createFetchModrinthParameters() {
        return createCodeParameters("Installed mod id to resolve a Modrinth page for.");
    }
    
    private static JsonObject createFetchUrlParameters() {
        return createCodeParameters("HTTP(S) URL to fetch.");
    }
    
    private static JsonObject createListFilesParameters() {
        return createCodeParameters("Directory path and options.");
    }
    
    private static JsonObject createSearchParameters() {
        return createCodeParameters("Web search query.");
    }
    
    private static JsonObject createAssetUpsertParameters() {
        return createCodeParameters("Asset record details.");
    }
    
    private static JsonObject createAssetRemoveParameters() {
        return createCodeParameters("Asset record id to remove.");
    }
    
    private static JsonObject createDynamicItemParameters() {
        return createCodeParameters("Dynamic item properties.");
    }
    
    private static JsonObject createDynamicBlockParameters() {
        return createCodeParameters("Dynamic block properties.");
    }
    
    private static JsonObject createDynamicFluidParameters() {
        return createCodeParameters("Dynamic fluid properties.");
    }
    
    private static JsonObject createDynamicItemUpdateParameters() {
        return createCodeParameters("Updated dynamic item properties.");
    }
    
    private static JsonObject createDynamicBlockUpdateParameters() {
        return createCodeParameters("Updated dynamic block properties.");
    }
    
    private static JsonObject createDynamicFluidUpdateParameters() {
        return createCodeParameters("Updated dynamic fluid properties.");
    }
    
    private static JsonObject createDynamicUnregisterParameters() {
        return createCodeParameters("Dynamic content type and slot to unregister.");
    }
}