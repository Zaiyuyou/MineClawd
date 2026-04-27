# MineClawd.java 修改指南

## 修改前的代码

### 1. `openAiTools()` 方法

**位置**: `MineClawd.java:4619-4770`

**当前实现**:
```java
private List<OpenAITool> openAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
    List<OpenAITool> tools = new ArrayList<>(List.of(
        new OpenAITool(
            TOOL_ASK_USER,
            "Ask the player a clarification question...",
            questionToolParameters()
        ),
        new OpenAITool(
            TOOL_APPLY_INSTANT_SERVER_SCRIPT,
            "Apply immediate runtime changes...",
            codeToolParameters()
        ),
        new OpenAITool(
            TOOL_EXECUTE_COMMAND,
            "Execute a Minecraft command...",
            commandToolParameters()
        ),
        // ... 几十个工具硬编码在这里
    ));
    return List.copyOf(tools);
}
```

**修改后**:
```java
private List<OpenAITool> openAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
    List<OpenAITool> tools = new ArrayList<>();
    
    // 从 ToolRegistry 获取所有工具
    for (MineClawdTool tool : ToolRegistry.getAll().values()) {
        tools.add(new ToolExecutorWrapper(tool).toOpenAITool());
    }
    
    return List.copyOf(tools);
}
```

**说明**:
- 移除所有硬编码的 `new OpenAITool(...)`
- 使用 `ToolRegistry.getAll()` 获取所有工具
- 使用 `ToolExecutorWrapper` 包装工具

---

### 2. `executeToolCallSync()` 方法

**位置**: `MineClawd.java:3240-3500`

**当前实现**:
```java
private String executeToolCallSync(ServerCommandSource source, String toolName, JsonObject args, AgentRuntime runtime) {
    if (toolName == null || toolName.isBlank()) {
        return "ERROR: Tool call is missing required `name`.";
    }
    if (runtime != null && isRuntimeInactive(runtime)) {
        return "ERROR: Request was stopped by user.";
    }
    String ownerKey = runtime == null || runtime.ownerKey() == null || runtime.ownerKey().isBlank()
            ? sessionOwnerKey(source)
            : runtime.ownerKey();
    MineClawdConfig config = MineClawdConfig.get();
    ToolExecutionResult result;
    switch (toolName) {
        case TOOL_APPLY_INSTANT_SERVER_SCRIPT:
        case LEGACY_TOOL_KUBEJS_EVAL:
            String code = readRequiredStringArg(args, "code");
            if (code == null || code.isBlank()) {
                return "ERROR: Tool call is missing required string `code`.";
            }
            result = KubeJsToolExecutor.executeInstant(source, code);
            break;
        case TOOL_EXECUTE_COMMAND:
            String command = readRequiredStringArg(args, "command");
            if (command == null || command.isBlank()) {
                return "ERROR: Tool call is missing required string `command`.";
            }
            result = KubeJsToolExecutor.executeCommand(source, command);
            break;
        case TOOL_SEARCH:
            if (!hasConfiguredTavilyKey(config)) {
                return "ERROR: Search tool is disabled. Configure `tavily-api-key` first.";
            }
            String query = readRequiredStringArg(args, "query");
            if (query == null || query.isBlank()) {
                return "ERROR: Tool call is missing required string `query`.";
            }
            result = SearchToolExecutor.searchWeb(config.tavilyApiKey, query, readOptionalIntArg(args, "max_results"));
            break;
        // ... 几十个 case
        default:
            return "ERROR: Unknown tool " + toolName;
    }
    return result.success() ? result.output() : "ERROR: " + result.output();
}
```

**修改后**:
```java
private String executeToolCallSync(ServerCommandSource source, String toolName, JsonObject args, AgentRuntime runtime) {
    if (toolName == null || toolName.isBlank()) {
        return "ERROR: Tool call is missing required `name`.";
    }
    if (runtime != null && isRuntimeInactive(runtime)) {
        return "ERROR: Request was stopped by user.";
    }
    
    // 从 ToolRegistry 查找工具
    MineClawdTool tool = ToolRegistry.get(toolName);
    if (tool == null) {
        return "ERROR: Unknown tool " + toolName;
    }
    
    // 使用包装器执行
    ToolExecutorWrapper wrapper = new ToolExecutorWrapper(tool);
    String result = wrapper.execute(source, args);
    
    return result;
}
```

**说明**:
- 移除所有 `case TOOL_*` 分支
- 使用 `ToolRegistry.get(toolName)` 查找工具
- 使用 `ToolExecutorWrapper` 执行工具

---

### 3. `buildSystemPrompt()` 方法

**位置**: `MineClawd.java:5612-5709`

**当前实现**:
```java
private String buildSystemPrompt(
    ServerCommandSource source,
    MineClawdConfig config,
    String ownerKey,
    boolean dynamicRegistryEnabled,
    SessionData session
) {
    String configured = config == null ? "" : config.systemPrompt;
    Agent agent = AGENT_MANAGER.loadActiveAgent(ownerKey);
    Persona persona = PERSONA_MANAGER.loadActivePersona(ownerKey);
    
    // 优先级：agent prompt > 配置prompt > 硬编码默认值
    String basePrompt = agent.hasBasePrompt() 
            ? agent.basePrompt() 
            : (configured == null || configured.isBlank() 
                ? BASE_SYSTEM_PROMPT 
                : configured.trim());
    
    Path serverRoot = WorkspaceFileToolExecutor.serverRoot(source);
    if (serverRoot == null) {
        serverRoot = Platform.getGameFolder().toAbsolutePath().normalize();
    }
    Path serverScriptsPath = serverRoot.resolve("kubejs").resolve("server_scripts").normalize();
    String serverScriptsToolPath = WorkspaceFileToolExecutor.displayPath(serverRoot, serverScriptsPath);
    if (serverScriptsToolPath.isBlank()
            || ".".equals(serverScriptsToolPath)
            || serverScriptsToolPath.contains(":")
            || serverScriptsToolPath.startsWith("/")) {
        serverScriptsToolPath = "kubejs/server_scripts";
    }

    String env = buildEnvironmentInfo();
    String installedMods = buildInstalledModsInfo();
    StringBuilder prompt = new StringBuilder(basePrompt);
    if (!env.isBlank()) {
        prompt.append("\n\nEnvironment:\n").append(env);
    }
    if (!installedMods.isBlank()) {
        prompt.append("\n\nInstalled mods:\n").append(installedMods);
    }
    if (hasConfiguredTavilyKey(config)) {
        prompt.append("\n\nSearch tool status:\n")
                .append("`search` is enabled (Tavily API key is configured). Use it whenever external facts are uncertain.");
    } else {
        prompt.append("\n\nSearch tool status:\n")
                .append("`search` is disabled because `tavily-api-key` is not configured. ")
                .append("If the player asks for web search, explain this and ask an operator to configure `tavily-api-key`.");
    }
    prompt.append("\n\nPersona context:\n")
            .append("Project and tool identity remains MineClawd even if the persona uses a different name.\n")
            .append("Active soul: ").append(persona.name()).append("\n");
    if (persona.content() == null || persona.content().isBlank()) {
        prompt.append("Soul instructions are empty. Use default MineClawd behavior.");
    } else {
        prompt.append("Follow these soul instructions:\n")
                .append(persona.content().trim());
    }
    if (session != null) {
        Path workspacePath = SESSION_MANAGER.ensureSessionWorkspace(ownerKey, session.id());
        String workspaceToolPath = WorkspaceFileToolExecutor.displayPath(serverRoot, workspacePath);
        if (workspaceToolPath.isBlank()
                || ".".equals(workspaceToolPath)
                || workspaceToolPath.contains(":")
                || workspaceToolPath.startsWith("/")) {
            workspaceToolPath = "mineclawd/sessions/<owner>/<session>/workspace";
        }
        prompt.append("\n\nSession context:\n")
                .append("Current session id: ").append(session.id()).append("\n")
                .append("Current session token: ").append(session.commandToken()).append("\n")
                .append("File tools path rules:\n")
                .append("- Use server-root-relative paths only (no drive letters, no leading slash).\n")
                .append("- server-scripts: ").append(serverScriptsToolPath).append("\n")
                .append("- workspace: ").append(workspaceToolPath).append("\n")
                .append("Uploaded files/images for this session are stored in workspace.\n")
                .append("For persistent callback scripts, prefer the stable session id when using `mineclawd.requestWithSession`.\n")
                .append("If this session is removed later, callback requests using it will fail safely.");
    } else {
        prompt.append("\n\nFile path context:\n")
                .append("Use server-root-relative paths in file tools (no drive letters, no leading slash).\n")
                .append("Most-used path: server-scripts = ").append(serverScriptsToolPath).append("\n")
                .append("Session workspace path becomes available on session-backed requests.");
    }
    if (dynamicRegistryEnabled && agent.hasDynamicRegistryPrompt()) {
        prompt.append("\n\n")
                .append(agent.dynamicRegistryPrompt());
    } else if (dynamicRegistryEnabled) {
        prompt.append("\n\n")
                .append(DYNAMIC_REGISTRY_PROMPT_APPENDIX);
    }
    
    if (agent.hasAssetTrackingPrompt()) {
        prompt.append("\n\n")
                .append(agent.assetTrackingPrompt());
    } else {
        prompt.append("\n\n")
                .append(ASSET_TRACKING_PROMPT_APPENDIX);
    }
    return prompt.toString();
}
```

**修改后**:
```java
private String buildSystemPrompt(
    ServerCommandSource source,
    MineClawdConfig config,
    String ownerKey,
    boolean dynamicRegistryEnabled,
    SessionData session
) {
    String configured = config == null ? "" : config.systemPrompt;
    Agent agent = AGENT_MANAGER.loadActiveAgent(ownerKey);
    Persona persona = PERSONA_MANAGER.loadActivePersona(ownerKey);
    
    // 优先级：agent prompt > 配置prompt > 硬编码默认值
    String basePrompt = agent.hasBasePrompt() 
            ? agent.basePrompt() 
            : (configured == null || configured.isBlank() 
                ? BASE_SYSTEM_PROMPT 
                : configured.trim());
    
    Path serverRoot = WorkspaceFileToolExecutor.serverRoot(source);
    if (serverRoot == null) {
        serverRoot = Platform.getGameFolder().toAbsolutePath().normalize();
    }
    Path serverScriptsPath = serverRoot.resolve("kubejs").resolve("server_scripts").normalize();
    String serverScriptsToolPath = WorkspaceFileToolExecutor.displayPath(serverRoot, serverScriptsPath);
    if (serverScriptsToolPath.isBlank()
            || ".".equals(serverScriptsToolPath)
            || serverScriptsToolPath.contains(":")
            || serverScriptsToolPath.startsWith("/")) {
        serverScriptsToolPath = "kubejs/server_scripts";
    }

    String env = buildEnvironmentInfo();
    String installedMods = buildInstalledModsInfo();
    StringBuilder prompt = new StringBuilder(basePrompt);
    if (!env.isBlank()) {
        prompt.append("\n\nEnvironment:\n").append(env);
    }
    if (!installedMods.isBlank()) {
        prompt.append("\n\nInstalled mods:\n").append(installedMods);
    }
    if (hasConfiguredTavilyKey(config)) {
        prompt.append("\n\nSearch tool status:\n")
                .append("`search` is enabled (Tavily API key is configured). Use it whenever external facts are uncertain.");
    } else {
        prompt.append("\n\nSearch tool status:\n")
                .append("`search` is disabled because `tavily-api-key` is not configured. ")
                .append("If the player asks for web search, explain this and ask an operator to configure `tavily-api-key`.");
    }
    prompt.append("\n\nPersona context:\n")
            .append("Project and tool identity remains MineClawd even if the persona uses a different name.\n")
            .append("Active soul: ").append(persona.name()).append("\n");
    if (persona.content() == null || persona.content().isBlank()) {
        prompt.append("Soul instructions are empty. Use default MineClawd behavior.");
    } else {
        prompt.append("Follow these soul instructions:\n")
                .append(persona.content().trim());
    }
    if (session != null) {
        Path workspacePath = SESSION_MANAGER.ensureSessionWorkspace(ownerKey, session.id());
        String workspaceToolPath = WorkspaceFileToolExecutor.displayPath(serverRoot, workspacePath);
        if (workspaceToolPath.isBlank()
                || ".".equals(workspaceToolPath)
                || workspaceToolPath.contains(":")
                || workspaceToolPath.startsWith("/")) {
            workspaceToolPath = "mineclawd/sessions/<owner>/<session>/workspace";
        }
        prompt.append("\n\nSession context:\n")
                .append("Current session id: ").append(session.id()).append("\n")
                .append("Current session token: ").append(session.commandToken()).append("\n")
                .append("File tools path rules:\n")
                .append("- Use server-root-relative paths only (no drive letters, no leading slash).\n")
                .append("- server-scripts: ").append(serverScriptsToolPath).append("\n")
                .append("- workspace: ").append(workspaceToolPath).append("\n")
                .append("Uploaded files/images for this session are stored in workspace.\n")
                .append("For persistent callback scripts, prefer the stable session id when using `mineclawd.requestWithSession`.\n")
                .append("If this session is removed later, callback requests using it will fail safely.");
    } else {
        prompt.append("\n\nFile path context:\n")
                .append("Use server-root-relative paths in file tools (no drive letters, no leading slash).\n")
                .append("Most-used path: server-scripts = ").append(serverScriptsToolPath).append("\n")
                .append("Session workspace path becomes available on session-backed requests.");
    }
    
    // ✅ 新增：添加所有工具的 Prompt Appendix
    for (var entry : ToolRegistry.getAllPromptAppendixes().entrySet()) {
        String toolName = entry.getKey();
        String appendix = entry.getValue();
        
        prompt.append("\n\nTool: ").append(toolName).append("\n");
        prompt.append(appendix);
    }
    
    return prompt.toString();
}
```

**说明**:
- 移除 `DYNAMIC_REGISTRY_PROMPT_APPENDIX` 和 `ASSET_TRACKING_PROMPT_APPENDIX` 的硬编码
- 使用 `ToolRegistry.getAllPromptAppendixes()` 动态获取所有工具的 Prompt Appendix
- 这样所有工具都可以提供自己的 Prompt Appendix

---

### 4. `executeToolCallAsync()` 方法

**位置**: `MineClawd.java:3218-3235`

**当前实现**:
```java
private CompletableFuture<Void> executeToolCallAsync(
    ServerCommandSource source,
    List<OpenAIToolCall> toolCalls,
    List<OpenAIMessage> history,
    List<String> outputs,
    List<String> signatures,
    AgentRuntime runtime,
    int index
) {
    if (index >= toolCalls.size()) {
        return CompletableFuture.completedFuture(null);
    }
    
    OpenAIToolCall call = toolCalls.get(index);
    JsonObject args = parseToolArguments(call.arguments());
    
    return executeToolCallAsync(source, call.name(), args, runtime)
        .handle((output, throwable) -> {
            String finalOutput = output;
            if (throwable != null) {
                finalOutput = "ERROR: " + summarizeThrowable(throwable);
            }
            
            String toolCallId = call.id();
            history.add(OpenAIMessage.tool(toolCallId, finalOutput));
            outputs.add(finalOutput);
            signatures.add(call.name() + ":" + args);
            
            return executeToolCallAsync(
                source, toolCalls, history, outputs, signatures, runtime, index + 1
            ).join();
        });
}
```

**修改后**:
```java
private CompletableFuture<Void> executeToolCallAsync(
    ServerCommandSource source,
    List<OpenAIToolCall> toolCalls,
    List<OpenAIMessage> history,
    List<String> outputs,
    List<String> signatures,
    AgentRuntime runtime,
    int index
) {
    if (index >= toolCalls.size()) {
        return CompletableFuture.completedFuture(null);
    }
    
    OpenAIToolCall call = toolCalls.get(index);
    JsonObject args = parseToolArguments(call.arguments());
    
    return executeToolCallAsync(source, call.name(), args, runtime)
        .handle((output, throwable) -> {
            String finalOutput = output;
            if (throwable != null) {
                finalOutput = "ERROR: " + summarizeThrowable(throwable);
            }
            
            String toolCallId = call.id();
            history.add(OpenAIMessage.tool(toolCallId, finalOutput));
            outputs.add(finalOutput);
            signatures.add(call.name() + ":" + args);
            
            return executeToolCallAsync(
                source, toolCalls, history, outputs, signatures, runtime, index + 1
            ).join();
        });
}
```

**说明**:
- `executeToolCallAsync()` 方法本身不需要修改
- 它调用的 `executeToolCallSync()` 已经修改为使用 ToolRegistry
- 所以这个方法会自动支持新工具

---

## 修改后的效果

### 1. 工具注册

**第三方 Mod 注册工具**:
```java
ToolRegistry.register(new MineClawdTool() {
    @Override
    public String getName() {
        return "my_mod:craft_item";
    }
    
    @Override
    public String getDescription() {
        return "Craft an item using a recipe";
    }
    
    @Override
    public JsonObject getParameters() {
        // 参数定义...
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        // 执行逻辑...
    }
    
    @Override
    public String getPromptAppendix() {
        return "*** CRAFTING TOOLS ***\n\nUse these tools for crafting.";
    }
});
```

**效果**:
- ✅ LLM 可以看到这个工具
- ✅ LLM 可以调用这个工具
- ✅ LLM 可以使用这个工具的 Prompt Appendix

### 2. 工具执行

**LLM 调用工具**:
```json
{
    "tool_name": "my_mod:craft_item",
    "arguments": {
        "recipe_id": "minecraft:diamond_sword",
        "count": 1
    }
}
```

**执行流程**:
1. `executeToolCallSync()` 从 `ToolRegistry.get("my_mod:craft_item")` 查找工具
2. 使用 `ToolExecutorWrapper` 执行工具
3. 返回执行结果

### 3. Prompt Appendix

**效果**:
- ✅ 所有工具的 Prompt Appendix 都会被注入到系统 Prompt 中
- ✅ LLM 可以看到工具的额外上下文信息
- ✅ 工具可以动态添加自己的 Prompt Appendix

---

## 总结

**必须修改的 4 个方法**:

1. ✅ `openAiTools()` - 从 ToolRegistry 获取工具
2. ✅ `executeToolCallSync()` - 使用 ToolRegistry 查找工具
3. ✅ `buildSystemPrompt()` - 集成所有工具的 Prompt Appendix
4. ✅ `executeToolCallAsync()` - 无需修改（自动支持）

**修改后的效果**:

- ✅ 第三方 Mod 可以注册自定义工具
- ✅ LLM 可以看到和调用新工具
- ✅ 新工具可以提供自己的 Prompt Appendix
- ✅ 工具列表动态生成，无需硬编码
