# ⚠️ 重要通知：必须修改 MineClawd.java

## 问题说明

仅创建 `ToolRegistry`、`MineClawdTool` 等新类**无法实现功能**！

### 原因分析

当前 MineClawd.java 的工具系统是**硬编码**的：

1. **`openAiTools()` 方法** - 硬编码了所有工具
   ```java
   private List<OpenAITool> openAiTools(...) {
       List<OpenAITool> tools = new ArrayList<>(List.of(
           new OpenAITool(TOOL_ASK_USER, "...", ...),
           new OpenAITool(TOOL_APPLY_INSTANT_SERVER_SCRIPT, "...", ...),
           // ... 硬编码所有工具
       ));
       return List.copyOf(tools);
   }
   ```

2. **`executeToolCallSync()` 方法** - 使用 switch-case 分发
   ```java
   private String executeToolCallSync(...) {
       switch (toolName) {
           case TOOL_APPLY_INSTANT_SERVER_SCRIPT:
               // ...
           case TOOL_EXECUTE_COMMAND:
               // ...
       }
   }
   ```

3. **`buildSystemPrompt()` 方法** - 硬编码了 Prompt Appendix
   ```java
   private String buildSystemPrompt(...) {
       // ...
       prompt.append("\n\n").append(DYNAMIC_REGISTRY_PROMPT_APPENDIX);
       prompt.append("\n\n").append(ASSET_TRACKING_PROMPT_APPENDIX);
       // ...
   }
   ```

### 为什么新类无法工作

| 新类 | 作用 | 问题 |
|------|------|------|
| `ToolRegistry` | 存储工具 | 没有人调用 `ToolRegistry.getAll()` |
| `MineClawdTool` | 定义工具 | 没有人调用 `tool.execute()` |
| `ToolExecutorWrapper` | 包装工具 | 没有人使用它 |
| `ToolPromptManager` | 管理 Prompt | 没有人调用它 |

### 必须修改的地方

#### 1. `openAiTools()` 方法

**当前实现**:
```java
private List<OpenAITool> openAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
    List<OpenAITool> tools = new ArrayList<>(List.of(
        new OpenAITool(TOOL_ASK_USER, "...", questionToolParameters()),
        new OpenAITool(TOOL_APPLY_INSTANT_SERVER_SCRIPT, "...", codeToolParameters()),
        // ... 硬编码
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

#### 2. `executeToolCallSync()` 方法

**当前实现**:
```java
private String executeToolCallSync(ServerCommandSource source, String toolName, JsonObject args, ...) {
    switch (toolName) {
        case TOOL_APPLY_INSTANT_SERVER_SCRIPT:
            String code = readRequiredStringArg(args, "code");
            result = KubeJsToolExecutor.executeInstant(source, code);
            break;
        case TOOL_EXECUTE_COMMAND:
            String command = readRequiredStringArg(args, "command");
            result = KubeJsToolExecutor.executeCommand(source, command);
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
private String executeToolCallSync(ServerCommandSource source, String toolName, JsonObject args, ...) {
    // 从 ToolRegistry 查找工具
    MineClawdTool tool = ToolRegistry.get(toolName);
    if (tool == null) {
        return "ERROR: Unknown tool " + toolName;
    }
    
    // 使用包装器执行
    ToolExecutorWrapper wrapper = new ToolExecutorWrapper(tool);
    return wrapper.execute(source, args);
}
```

#### 3. `buildSystemPrompt()` 方法

**当前实现**:
```java
private String buildSystemPrompt(...) {
    // ...
    if (dynamicRegistryEnabled && agent.hasDynamicRegistryPrompt()) {
        prompt.append("\n\n").append(agent.dynamicRegistryPrompt());
    } else if (dynamicRegistryEnabled) {
        prompt.append("\n\n").append(DYNAMIC_REGISTRY_PROMPT_APPENDIX);
    }
    
    if (agent.hasAssetTrackingPrompt()) {
        prompt.append("\n\n").append(agent.assetTrackingPrompt());
    } else {
        prompt.append("\n\n").append(ASSET_TRACKING_PROMPT_APPENDIX);
    }
    return prompt.toString();
}
```

**修改后**:
```java
private String buildSystemPrompt(...) {
    String basePrompt = buildBasePrompt(source, config, ownerKey, dynamicRegistryEnabled, session);
    
    StringBuilder prompt = new StringBuilder(basePrompt);
    
    // 添加所有工具的 Prompt Appendix
    for (var entry : ToolRegistry.getAllPromptAppendixes().entrySet()) {
        String toolName = entry.getKey();
        String appendix = entry.getValue();
        
        prompt.append("\n\nTool: ").append(toolName).append("\n");
        prompt.append(appendix);
    }
    
    return prompt.toString();
}
```

## 实现流程

### 步骤 1: 创建基础设施 ✅ (已完成)

- [x] `MineClawdTool.java` - 工具接口
- [x] `ToolExecutionResult.java` - 执行结果
- [x] `ToolRegistry.java` - 工具注册表
- [x] `ToolExecutorWrapper.java` - 执行器包装
- [x] `ToolPromptManager.java` - Prompt 管理器
- [x] `ToolPromptCategory.java` - Prompt 分类

### 步骤 2: 修改 MineClawd.java ⚠️ (必须完成)

- [ ] 修改 `openAiTools()` 方法
- [ ] 修改 `executeToolCallSync()` 方法
- [ ] 修改 `buildSystemPrompt()` 方法
- [ ] 修改 `executeToolCallAsync()` 方法

### 步骤 3: 迁移内置工具

- [ ] 将现有内置工具转换为 `MineClawdTool`
- [ ] 在 `ToolRegistry` 中注册
- [ ] 测试所有工具功能

## 结论

**仅创建新类无法实现功能！**

必须修改 `MineClawd.java` 的 4 个方法，才能让 ToolRegistry 中的工具被 LLM 使用。

### 为什么必须修改

| 问题 | 影响 |
|------|------|
| `openAiTools()` 不修改 | LLM 无法知道新工具的存在 |
| `executeToolCallSync()` 不修改 | 新工具无法被执行 |
| `buildSystemPrompt()` 不修改 | 新工具的 Prompt Appendix 无法注入 |

### 类比

这就像：
- 创建了新的"工具类"（汽车）
- 但没有"驾驶方法"（开车）
- 汽车再好，不驾驶就无法使用

## 下一步行动

**立即执行**：开始修改 `MineClawd.java` 的 4 个方法

参考实现：
- `openAiTools()` - 从 ToolRegistry 获取工具
- `executeToolCallSync()` - 使用 ToolRegistry 查找工具
- `buildSystemPrompt()` - 集成所有工具的 Prompt Appendix
- `executeToolCallAsync()` - 确保异步工具支持

---

**文档版本**: 1.0  
**最后更新**: 2026-03-29  
**作者**: 在与有
