package com.mineclawd.tool_sys;

import com.mineclawd.llm.OpenAITool;
import com.mineclawd.llm.VertexAIFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具工厂类，管理所有工具定义
 */
public final class ToolFactory {
    
    private static final List<ToolDefinition> BUILTIN_TOOLS = new ArrayList<>();
    
    static {
        registerBuiltinTools();
    }
    
    /**
     * 注册内置工具
     */
    private static void registerBuiltinTools() {
        // 文件系统工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "read-files",
            "Read one or more files and return their contents.",
            createFileReadParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "write-files",
            "Write content to one or more files.",
            createFileWriteParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "list-files",
            "List files and directories in a specified path.",
            createListFilesParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "move-files",
            "Move or rename files and directories.",
            createMoveFilesParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "copy-files",
            "Copy files and directories.",
            createCopyFilesParameters()
        ));
        
        // 网络工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "curl",
            "Make HTTP requests to fetch data from URLs.",
            createCurlParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "fetch_url",
            "Fetch content from a URL.",
            createFetchUrlParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "fetch_modrinth",
            "Fetch mod information from Modrinth.",
            createFetchModrinthParameters()
        ));
        
        // 命令工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "execute-command",
            "Execute a Minecraft command and return command output/result.",
            createCommandParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "list_commands",
            "List available root commands. Optionally filter with mod_id (best-effort by command name/prefix).",
            createListCommandsParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "reload-game",
            "Run /reload and return command output plus detected KubeJS load errors.",
            createNoArgParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "sync-command-tree",
            "Refresh command tree/tab-completion for all online players. Use only after command registration changes.",
            createNoArgParameters()
        ));
        
        // 动态内容工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "register-dynamic-item",
            "Register a new dynamic item.",
            createRegisterDynamicItemParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "register-dynamic-block",
            "Register a new dynamic block.",
            createRegisterDynamicBlockParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "register-dynamic-fluid",
            "Register a new dynamic fluid.",
            createRegisterDynamicFluidParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "update-dynamic-item",
            "Update an existing dynamic item.",
            createUpdateDynamicItemParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "update-dynamic-block",
            "Update an existing dynamic block.",
            createUpdateDynamicBlockParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "update-dynamic-fluid",
            "Update an existing dynamic fluid.",
            createUpdateDynamicFluidParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "unregister-dynamic-content",
            "Unregister dynamic content (item/block/fluid).",
            createUnregisterDynamicContentParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "list-dynamic-content",
            "List all registered dynamic content.",
            createListDynamicContentParameters()
        ));
        
        // 资产管理工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "remove-asset-record",
            "Remove an asset record from the database.",
            createRemoveAssetRecordParameters()
        ));
        
        // 交互工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "ask-user-question",
            "Ask the user a question and wait for their response.",
            createAskUserQuestionParameters()
        ));
        
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "read-image",
            "Read an image file and return its content.",
            createReadImageParameters()
        ));
        
        // KubeJS工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "apply-instant-server-script",
            "Apply a KubeJS server script instantly without restart.",
            createApplyInstantServerScriptParameters()
        ));
        
        // 搜索工具
        BUILTIN_TOOLS.add(ToolDefinition.of(
            "grep",
            "Search for patterns in files.",
            createGrepParameters()
        ));
    }
    
    /**
     * 注册工具定义到ToolRegistry
     */
    public static void registerTool(ToolDefinition tool) {
        ToolRegistry.register(tool);
        System.out.println("[ToolFactory] 已注册工具: " + tool.name());
    }
    
    /**
     * 注销工具定义
     */
    public static void unregisterTool(String toolName) {
        ToolRegistry.unregister(toolName);
        System.out.println("[ToolFactory] 已注销工具: " + toolName);
    }
    
    /**
     * 获取所有内置工具定义
     */
    public static List<ToolDefinition> getBuiltinTools() {
        return new ArrayList<>(BUILTIN_TOOLS);
    }
    
    /**
     * 创建OpenAI工具列表
     */
    public static List<OpenAITool> createOpenAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        System.out.println("[ToolFactory] createOpenAiTools() 被调用: dynamicRegistryEnabled=" + dynamicRegistryEnabled + ", searchEnabled=" + searchEnabled);
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        System.out.println("[ToolFactory] ToolRegistry.getFiltered() 返回 " + definitions.size() + " 个工具定义");
        List<OpenAITool> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            System.out.println("[ToolFactory] 添加工具: " + definition.name());
            tools.add(new OpenAITool(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
        System.out.println("[ToolFactory] createOpenAiTools() 返回 " + tools.size() + " 个工具");
        return tools;
    }
    
    /**
     * 创建VertexAI工具列表
     */
    public static List<VertexAIFunction> createVertexTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        List<VertexAIFunction> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            tools.add(new VertexAIFunction(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
        return tools;
    }
    
    // 参数创建方法
    private static com.google.gson.JsonObject createNoArgParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new com.google.gson.JsonObject());
        parameters.add("required", new com.google.gson.JsonArray());
        return parameters;
    }
    
    private static com.google.gson.JsonObject createCommandParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject command = new com.google.gson.JsonObject();
        command.addProperty("type", "string");
        command.addProperty("description", "Minecraft command to run, with or without leading slash.");
        properties.add("command", command);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("command");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createListCommandsParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject modId = new com.google.gson.JsonObject();
        modId.addProperty("type", "string");
        modId.addProperty("description", "Optional installed mod id filter, for example `kubejs`.");
        properties.add("mod_id", modId);
        
        parameters.add("properties", properties);
        parameters.add("required", new com.google.gson.JsonArray());
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createFileReadParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject paths = new com.google.gson.JsonObject();
        paths.addProperty("type", "array");
        paths.addProperty("description", "Array of file paths to read.");
        properties.add("paths", paths);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("paths");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createFileWriteParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject files = new com.google.gson.JsonObject();
        files.addProperty("type", "array");
        files.addProperty("description", "Array of file objects with path and content.");
        properties.add("files", files);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("files");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createListFilesParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject path = new com.google.gson.JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "Path to list files from.");
        properties.add("path", path);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("path");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createMoveFilesParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject source = new com.google.gson.JsonObject();
        source.addProperty("type", "string");
        source.addProperty("description", "Source file path.");
        properties.add("source", source);
        
        com.google.gson.JsonObject destination = new com.google.gson.JsonObject();
        destination.addProperty("type", "string");
        destination.addProperty("description", "Destination file path.");
        properties.add("destination", destination);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("source");
        required.add("destination");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createCopyFilesParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject source = new com.google.gson.JsonObject();
        source.addProperty("type", "string");
        source.addProperty("description", "Source file path.");
        properties.add("source", source);
        
        com.google.gson.JsonObject destination = new com.google.gson.JsonObject();
        destination.addProperty("type", "string");
        destination.addProperty("description", "Destination file path.");
        properties.add("destination", destination);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("source");
        required.add("destination");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createCurlParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject url = new com.google.gson.JsonObject();
        url.addProperty("type", "string");
        url.addProperty("description", "URL to make request to.");
        properties.add("url", url);
        
        com.google.gson.JsonObject method = new com.google.gson.JsonObject();
        method.addProperty("type", "string");
        method.addProperty("description", "HTTP method (GET, POST, PUT, DELETE, etc.).");
        properties.add("method", method);
        
        com.google.gson.JsonObject headers = new com.google.gson.JsonObject();
        headers.addProperty("type", "object");
        headers.addProperty("description", "Request headers.");
        properties.add("headers", headers);
        
        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
        body.addProperty("type", "string");
        body.addProperty("description", "Request body for POST/PUT requests.");
        properties.add("body", body);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("url");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createFetchUrlParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject url = new com.google.gson.JsonObject();
        url.addProperty("type", "string");
        url.addProperty("description", "URL to fetch.");
        properties.add("url", url);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("url");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createFetchModrinthParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject query = new com.google.gson.JsonObject();
        query.addProperty("type", "string");
        query.addProperty("description", "Search query for mod.");
        properties.add("query", query);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("query");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createRegisterDynamicItemParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject itemId = new com.google.gson.JsonObject();
        itemId.addProperty("type", "string");
        itemId.addProperty("description", "Unique item ID.");
        properties.add("item_id", itemId);
        
        com.google.gson.JsonObject name = new com.google.gson.JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "Item name.");
        properties.add("name", name);
        
        com.google.gson.JsonObject description = new com.google.gson.JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "Item description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("item_id");
        required.add("name");
        required.add("description");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createRegisterDynamicBlockParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject blockId = new com.google.gson.JsonObject();
        blockId.addProperty("type", "string");
        blockId.addProperty("description", "Unique block ID.");
        properties.add("block_id", blockId);
        
        com.google.gson.JsonObject name = new com.google.gson.JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "Block name.");
        properties.add("name", name);
        
        com.google.gson.JsonObject description = new com.google.gson.JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "Block description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("block_id");
        required.add("name");
        required.add("description");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createRegisterDynamicFluidParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject fluidId = new com.google.gson.JsonObject();
        fluidId.addProperty("type", "string");
        fluidId.addProperty("description", "Unique fluid ID.");
        properties.add("fluid_id", fluidId);
        
        com.google.gson.JsonObject name = new com.google.gson.JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "Fluid name.");
        properties.add("name", name);
        
        com.google.gson.JsonObject description = new com.google.gson.JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "Fluid description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("fluid_id");
        required.add("name");
        required.add("description");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createUpdateDynamicItemParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject itemId = new com.google.gson.JsonObject();
        itemId.addProperty("type", "string");
        itemId.addProperty("description", "Item ID to update.");
        properties.add("item_id", itemId);
        
        com.google.gson.JsonObject name = new com.google.gson.JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "New item name.");
        properties.add("name", name);
        
        com.google.gson.JsonObject description = new com.google.gson.JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "New item description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("item_id");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createUpdateDynamicBlockParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject blockId = new com.google.gson.JsonObject();
        blockId.addProperty("type", "string");
        blockId.addProperty("description", "Block ID to update.");
        properties.add("block_id", blockId);
        
        com.google.gson.JsonObject name = new com.google.gson.JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "New block name.");
        properties.add("name", name);
        
        com.google.gson.JsonObject description = new com.google.gson.JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "New block description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("block_id");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createUpdateDynamicFluidParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject fluidId = new com.google.gson.JsonObject();
        fluidId.addProperty("type", "string");
        fluidId.addProperty("description", "Fluid ID to update.");
        properties.add("fluid_id", fluidId);
        
        com.google.gson.JsonObject name = new com.google.gson.JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "New fluid name.");
        properties.add("name", name);
        
        com.google.gson.JsonObject description = new com.google.gson.JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "New fluid description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("fluid_id");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createUnregisterDynamicContentParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject contentId = new com.google.gson.JsonObject();
        contentId.addProperty("type", "string");
        contentId.addProperty("description", "Content ID to unregister.");
        properties.add("content_id", contentId);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("content_id");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createListDynamicContentParameters() {
        return createNoArgParameters();
    }
    
    private static com.google.gson.JsonObject createRemoveAssetRecordParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject recordId = new com.google.gson.JsonObject();
        recordId.addProperty("type", "string");
        recordId.addProperty("description", "Asset record ID to remove.");
        properties.add("record_id", recordId);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("record_id");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createAskUserQuestionParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject question = new com.google.gson.JsonObject();
        question.addProperty("type", "string");
        question.addProperty("description", "Question to ask the user.");
        properties.add("question", question);
        
        com.google.gson.JsonObject timeout = new com.google.gson.JsonObject();
        timeout.addProperty("type", "integer");
        timeout.addProperty("description", "Timeout in seconds (optional).");
        properties.add("timeout", timeout);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("question");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createReadImageParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject path = new com.google.gson.JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "Path to image file.");
        properties.add("path", path);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("path");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createApplyInstantServerScriptParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject script = new com.google.gson.JsonObject();
        script.addProperty("type", "string");
        script.addProperty("description", "KubeJS server script to execute.");
        properties.add("script", script);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("script");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private static com.google.gson.JsonObject createGrepParameters() {
        com.google.gson.JsonObject parameters = new com.google.gson.JsonObject();
        parameters.addProperty("type", "object");
        
        com.google.gson.JsonObject properties = new com.google.gson.JsonObject();
        com.google.gson.JsonObject pattern = new com.google.gson.JsonObject();
        pattern.addProperty("type", "string");
        pattern.addProperty("description", "Regex pattern to search for.");
        properties.add("pattern", pattern);
        
        com.google.gson.JsonObject path = new com.google.gson.JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "Path to search in.");
        properties.add("path", path);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("pattern");
        required.add("path");
        parameters.add("required", required);
        
        return parameters;
    }
}