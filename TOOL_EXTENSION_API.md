# MineClawd Tool 扩展接口设计文档

## 一、Tool 的完整构成

### 1.1 数据结构定义

#### OpenAI Tool
```java
// OpenAITool.java
public record OpenAITool(String name, String description, JsonObject parameters) {
}
```

#### Vertex AI Function
```java
// VertexAIFunction.java
public record VertexAIFunction(String name, String description, JsonObject parameters) {
}
```

#### 工具调用对象
```java
// OpenAIToolCall.java
public record OpenAIToolCall(String id, String name, String arguments) {
}

// VertexAIToolCall.java
public record VertexAIToolCall(String name, JsonObject args) {
}
```

### 1.2 核心要素

每个 Tool 必须包含三个核心要素：

1. **name** (String)
   - 工具的唯一标识符
   - 建议格式: `mod_id:tool_name` (例如: `my_mod:craft_item`)
   - LLM 用来识别和选择工具

2. **description** (String)
   - 给 LLM 的说明文本
   - 告诉 LLM 在什么场景下使用这个工具
   - 需要清晰描述工具的用途、参数要求和返回值

3. **parameters** (JsonObject)
   - JSON Schema 格式的参数定义
   - 告诉 LLM 如何正确调用这个工具
   - 定义参数的类型、必需性、描述等

### 1.3 参数格式示例

```java
private static JsonObject questionToolParameters() {
    JsonObject question = new JsonObject();
    question.addProperty("type", "object");
    question.addProperty("description", "The question to ask.");
    
    JsonArray required = new JsonArray();
    required.add("question");
    question.add("required", required);
    
    JsonObject properties = new JsonObject();
    
    JsonObject questionProp = new JsonObject();
    questionProp.addProperty("type", "string");
    questionProp.addProperty("description", "The question to ask.");
    properties.add("question", questionProp);
    
    JsonObject optionsProp = new JsonObject();
    optionsProp.addProperty("type", "array");
    optionsProp.addProperty("description", "Preset options for the answer.");
    JsonArray items = new JsonArray();
    JsonObject item = new JsonObject();
    item.addProperty("type", "string");
    items.add(item);
    optionsProp.add("items", items);
    properties.add("options", optionsProp);
    
    question.add("properties", properties);
    return question;
}
```

## 二、Tool 的完整生命周期

```
┌─────────────────────────────────────────────────────────────┐
│ 阶段 1: 定义 Tool (任何 Mod 可以做)                           │
│ - 创建 Tool 实现类                                           │
│ - 实现 name, description, parameters                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 2: 注册 Tool (通过统一接口注册到 MineClawd)              │
│ - 注册到全局 Tool 注册表                                     │
│ - 检查 name 唯一性                                           │
│ - 存储 Tool 执行器引用                                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 3: LLM 请求 (OpenAIClient/VertexAIClient)               │
│ - 将所有注册的 Tool 包含在 API 请求中                        │
│ - LLM 根据 description 决定调用哪个工具                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 4: 接收响应 (OpenAIClient.parseResponse)                 │
│ - 解析 LLM 返回的 tool_calls 数组                            │
│ - 提取: id, name, arguments                                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 5: 分发执行 (MineClawd.executeToolCallSync)              │
│ - 根据 toolName 查找注册的 Tool 执行器                       │
│ - 解析 arguments 为 JsonObject                               │
│ - 调用执行器处理                                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 6: 返回结果                                             │
│ - 成功: 返回执行结果字符串                                   │
│ - 失败: 返回 ERROR: 错误信息                                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 7: 包装消息 (OpenAIMessage.tool())                      │
│ - 使用 toolCallId 包装结果                                   │
│ - 发送给 LLM 进行下一轮处理                                  │
└─────────────────────────────────────────────────────────────┘
```

## 三、当前实现分析

### 3.1 硬编码注册 (问题所在)

**位置**: `MineClawd.java:4619-4770`

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
            codeToolParameters("JavaScript code...")
        ),
        // ... 所有工具都在这里硬编码
    ));
    return List.copyOf(tools);
}
```

**问题**:
- ❌ 所有工具必须修改 MineClawd 源码
- ❌ 无法动态扩展
- ❌ 其他 Mod 无法添加自己的工具
- ❌ 工具列表在编译时固定

### 3.2 Switch-Case 执行 (性能瓶颈)

**位置**: `MineClawd.java:3240-3500`

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
        case TOOL_LIST_FILES:
            result = WorkspaceFileToolExecutor.listFiles(source, ...);
            break;
        // ... 几十个 case
        default:
            return "ERROR: Unknown tool " + toolName;
    }
    return result.success() ? result.output() : "ERROR: " + result.output();
}
```

**问题**:
- ❌ 每增加一个工具都要修改这里
- ❌ Switch-case 随着工具数量增长变得臃肿
- ❌ 不利于维护和扩展

### 3.3 工具调用分发

**位置**: `MineClawd.java:3118-3135`

```java
private CompletableFuture<Void> executeOpenAiToolCallsSequential(...) {
    OpenAIToolCall call = toolCalls.get(index);
    JsonObject args = parseToolArguments(call.arguments()); // 解析JSON参数
    
    return executeToolCallAsync(source, call.name(), args, runtime)
        .handle((output, throwable) -> {
            String finalOutput = output;
            if (throwable != null) {
                finalOutput = "ERROR: " + summarizeThrowable(throwable);
            }
            
            // 将结果添加到历史记录
            String toolCallId = call.id();
            results.add(OpenAIMessage.tool(toolCallId, finalOutput));
            outputs.add(finalOutput);
            signatures.add(call.name() + ":" + args);
            return null;
        });
}
```

**关键点**:
- 解析参数: `parseToolArguments()` 将 JSON 字符串转为 JsonObject
- 执行工具: `executeToolCallAsync()` / `executeToolCallSync()`
- 包装结果: `OpenAIMessage.tool(toolCallId, finalOutput)`

## 四、模块化扩展接口设计

### 4.1 核心接口定义

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/MineClawdTool.java
package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;
import net.minecraft.server.command.ServerCommandSource;

/**
 * MineClawd 工具扩展接口
 * 任何 Mod 都可以实现此接口来提供自定义工具
 */
public interface MineClawdTool {
    
    /**
     * 工具的唯一标识符
     * 建议格式: "mod_id:tool_name"
     * 例如: "my_mod:craft_item", "my_mod:check_status"
     */
    String getName();
    
    /**
     * 给 LLM 的描述
     * 告诉 LLM 在什么场景下使用这个工具
     * 需要清晰描述工具的用途、参数要求和返回值
     */
    String getDescription();
    
    /**
     * 参数的 JSON Schema 定义
     * 定义工具接受的参数类型、必需性、描述等
     */
    JsonObject getParameters();
    
    /**
     * 执行工具
     * @param source 命令源
     * @param args 工具参数 (已解析为 JsonObject)
     * @return 工具执行结果
     */
    ToolExecutionResult execute(ServerCommandSource source, JsonObject args);
    
    /**
     * 可选: 工具执行前的检查
     * @param source 命令源
     * @return true 如果可以执行, false 如果不能执行
     */
    default boolean canExecute(ServerCommandSource source) {
        return true;
    }
    
    /**
     * 可选: 工具执行后的回调
     * @param source 命令源
     * @param success 是否成功
     * @param output 输出结果
     */
    default void onAfterExecute(ServerCommandSource source, boolean success, String output) {
    }
}
```

### 4.2 工具执行结果

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/ToolExecutionResult.java
package com.mineclawd.foundation.tool;

/**
 * 工具执行结果
 */
public record ToolExecutionResult(boolean success, String output) {
    public static ToolExecutionResult success(String output) {
        return new ToolExecutionResult(true, output);
    }
    
    public static ToolExecutionResult failure(String error) {
        return new ToolExecutionResult(false, error);
    }
}
```

### 4.3 工具注册表

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/ToolRegistry.java
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
    private static final Map<String, MineClawdTool> TOOLS_BY_NAME = new ConcurrentHashMap<>();
    
    static {
        // 注册内置工具
        registerBuiltInTools();
    }
    
    /**
     * 注册内置工具
     */
    private static void registerBuiltInTools() {
        // 这里可以注册 MineClawd 内置的工具
        // 或者留空, 完全由 Mod 动态注册
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
        
        TOOLS.put(name, tool);
        TOOLS_BY_NAME.put(name, tool);
        
        MineClawd.LOGGER.info("Registered tool: {}", name);
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
        
        return TOOLS.remove(name) != null;
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
     * 检查工具是否存在
     * @param name 工具名称
     * @return 是否存在
     */
    public static boolean contains(String name) {
        return TOOLS.containsKey(name);
    }
    
    /**
     * 获取工具数量
     * @return 工具数量
     */
    public static int size() {
        return TOOLS.size();
    }
}
```

### 4.4 执行器包装类

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/ToolExecutorWrapper.java
package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;
import com.mineclawd.MineClawd;
import com.mineclawd.foundation.llm.OpenAITool;
import com.mineclawd.foundation.llm.VertexAIFunction;
import net.minecraft.server.command.ServerCommandSource;

/**
 * 工具执行器包装类
 * 将 MineClawdTool 转换为 LLM 客户端可用的格式
 */
public class ToolExecutorWrapper {
    private final MineClawdTool tool;
    
    public ToolExecutorWrapper(MineClawdTool tool) {
        this.tool = tool;
    }
    
    /**
     * 执行工具
     * @param source 命令源
     * @param args 工具参数
     * @return 执行结果字符串
     */
    public String execute(ServerCommandSource source, JsonObject args) {
        if (tool == null) {
            return "ERROR: Tool is null";
        }
        
        try {
            // 执行前检查
            if (!tool.canExecute(source)) {
                return "ERROR: Tool cannot be executed in current context";
            }
            
            // 执行工具
            var result = tool.execute(source, args);
            
            // 执行后回调
            tool.onAfterExecute(source, result.success(), result.output());
            
            // 返回结果
            return result.success() ? result.output() : "ERROR: " + result.output();
            
        } catch (Exception e) {
            MineClawd.LOGGER.error("Tool execution failed: {}", tool.getName(), e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * 转换为 OpenAI Tool
     */
    public OpenAITool toOpenAITool() {
        return new OpenAITool(
            tool.getName(),
            tool.getDescription(),
            tool.getParameters()
        );
    }
    
    /**
     * 转换为 Vertex AI Function
     */
    public VertexAIFunction toVertexAIFunction() {
        return new VertexAIFunction(
            tool.getName(),
            tool.getDescription(),
            tool.getParameters()
        );
    }
}
```

### 4.5 异步工具支持 (可选)

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/AsyncToolExecutor.java
package com.mineclawd.foundation.tool;

import java.util.concurrent.CompletableFuture;

/**
 * 异步工具执行器接口
 * 适用于需要耗时操作的工具
 */
public interface AsyncToolExecutor {
    
    /**
     * 异步执行工具
     */
    CompletableFuture<String> executeAsync(
        ServerCommandSource source, 
        JsonObject args, 
        AgentRuntime runtime
    );
}
```

## 五、使用示例

### 5.1 基础工具示例

```java
// my_mod/src/main/java/com/mymod/tools/CraftItemTool.java
package com.mymod.tools;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import net.minecraft.server.command.ServerCommandSource;

public class CraftItemTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:craft_item";
    }
    
    @Override
    public String getDescription() {
        return "Craft an item using a recipe. " +
               "Use this when the player wants to craft something. " +
               "Requires: recipe_id (string), count (optional integer)";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject recipeId = new JsonObject();
        recipeId.addProperty("type", "string");
        recipeId.addProperty("description", "The recipe identifier (e.g., 'minecraft:diamond_sword')");
        properties.add("recipe_id", recipeId);
        
        JsonObject count = new JsonObject();
        count.addProperty("type", "integer");
        count.addProperty("description", "Number of items to craft (optional, default: 1)");
        count.addProperty("minimum", 1);
        count.addProperty("maximum", 64);
        properties.add("count", count);
        
        params.add("properties", properties);
        
        JsonArray required = new JsonArray();
        required.add("recipe_id");
        params.add("required", required);
        
        return params;
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String recipeId = args.has("recipe_id") ? args.get("recipe_id").getAsString() : null;
        int count = args.has("count") ? args.get("count").getAsInt() : 1;
        
        if (recipeId == null || recipeId.isBlank()) {
            return ToolExecutionResult.failure("recipe_id is required");
        }
        
        try {
            // 执行合成逻辑
            // 例如: 调用 KubeJS 或执行命令
            String output = executeCrafting(source, recipeId, count);
            return ToolExecutionResult.success(output);
        } catch (Exception e) {
            return ToolExecutionResult.failure("Failed to craft item: " + e.getMessage());
        }
    }
    
    private String executeCrafting(ServerCommandSource source, String recipeId, int count) {
        // 实际的合成逻辑
        return "Successfully crafted " + count + " of " + recipeId;
    }
}
```

### 5.2 注册工具

```java
// my_mod/src/main/java/com/mymod/MyMod.java
package com.mymod;

import com.mineclawd.foundation.tool.ToolRegistry;
import com.mymod.tools.CraftItemTool;
import net.fabricmc.api.ModInitializer;

public class MyMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        // 注册工具
        ToolRegistry.register(new CraftItemTool());
        
        MineClawd.LOGGER.info("MyMod tools registered");
    }
}
```

### 5.3 KubeJS 脚本工具示例

```javascript
// kubejs/server_scripts/mineclawd/tools.js
// MineClawd 会自动扫描并注册这些脚本中的工具

// 定义一个工具
server.registerMineClawdTool({
    name: 'kubejs:custom_tool',
    description: 'A custom tool implemented in KubeJS script',
    parameters: {
        type: 'object',
        properties: {
            input: { type: 'string', description: 'Input parameter' }
        },
        required: ['input']
    },
    execute: function(source, args) {
        try {
            // 执行 KubeJS 逻辑
            let result = 'Executed with input: ' + args.input;
            return { success: true, output: result };
        } catch (e) {
            return { success: false, output: 'Error: ' + e.message };
        }
    }
});
```

## 六、集成到 MineClawd

### 6.1 修改 openAiTools 方法

**修改前**:
```java
private List<OpenAITool> openAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
    List<OpenAITool> tools = new ArrayList<>(List.of(
        new OpenAITool(TOOL_ASK_USER, "...", questionToolParameters()),
        new OpenAITool(TOOL_APPLY_INSTANT_SERVER_SCRIPT, "...", codeToolParameters()),
        // ... 硬编码所有工具
    ));
    return List.copyOf(tools);
}
```

**修改后**:
```java
private List<OpenAITool> openAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
    List<OpenAITool> tools = new ArrayList<>();
    
    // 从注册表获取所有工具
    for (MineClawdTool tool : ToolRegistry.getAll().values()) {
        tools.add(new ToolExecutorWrapper(tool).toOpenAITool());
    }
    
    // 添加条件性工具
    if (searchEnabled) {
        tools.add(new ToolExecutorWrapper(new SearchTool()).toOpenAITool());
    }
    
    return List.copyOf(tools);
}
```

### 6.2 修改 executeToolCallSync 方法

**修改前**:
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
    if (toolName == null || toolName.isBlank()) {
        return "ERROR: Tool name is required";
    }
    
    // 从注册表获取工具
    MineClawdTool tool = ToolRegistry.get(toolName);
    if (tool == null) {
        return "ERROR: Unknown tool " + toolName;
    }
    
    // 执行工具
    ToolExecutorWrapper wrapper = new ToolExecutorWrapper(tool);
    return wrapper.execute(source, args);
}
```

### 6.3 异步工具支持

```java
private CompletableFuture<String> executeToolCallAsync(
    ServerCommandSource source,
    String toolName,
    JsonObject args,
    AgentRuntime runtime
) {
    MineClawdTool tool = ToolRegistry.get(toolName);
    if (tool == null) {
        return CompletableFuture.completedFuture("ERROR: Unknown tool " + toolName);
    }
    
    // 检查是否是异步工具
    if (tool instanceof AsyncToolExecutor asyncTool) {
        return asyncTool.executeAsync(source, args, runtime);
    }
    
    // 同步工具
    return CompletableFuture.completedFuture(executeToolCallSync(source, toolName, args, runtime));
}
```

## 七、优势总结

### 7.1 模块化
- ✅ 其他 Mod 可以独立开发 Tool
- ✅ 无需修改 MineClawd 源码
- ✅ 每个 Tool 独立打包和分发

### 7.2 动态性
- ✅ 支持运行时注册/注销 Tool
- ✅ 支持热加载
- ✅ 支持条件性注册 (根据 Mod 是否安装)

### 7.3 类型安全
- ✅ 通过接口保证所有 Tool 的一致性
- ✅ 编译时检查
- ✅ IDE 自动补全

### 7.4 错误隔离
- ✅ 一个 Tool 失败不影响其他 Tool
- ✅ 独立的错误处理
- ✅ 详细的错误日志

### 7.5 可测试性
- ✅ 每个 Tool 可以独立测试
- ✅ 易于单元测试
- ✅ 易于模拟和存根

### 7.6 可扩展性
- ✅ 支持同步和异步执行
- ✅ 支持前置检查和后置回调
- ✅ 支持参数验证

## 八、实施步骤

### 阶段 1: 创建核心接口
1. 创建 `MineClawdTool.java` 接口
2. 创建 `ToolExecutionResult.java` 记录类
3. 创建 `ToolRegistry.java` 注册表

### 阶段 2: 实现包装器
1. 创建 `ToolExecutorWrapper.java`
2. 实现 `toOpenAITool()` 方法
3. 实现 `toVertexAIFunction()` 方法

### 阶段 3: 修改 MineClawd
1. 修改 `openAiTools()` 方法
2. 修改 `vertexTools()` 方法
3. 修改 `executeToolCallSync()` 方法
4. 修改 `executeToolCallAsync()` 方法

### 阶段 4: 迁移内置工具
1. 将现有内置工具转换为实现 `MineClawdTool`
2. 在 `ToolRegistry` 中注册
3. 测试所有工具功能

### 阶段 5: 文档和示例
1. 编写 Mod 开发者指南
2. 提供示例 Tool
3. 提供 KubeJS 脚本示例

## 九、工具 Prompt Appendix 系统

### 9.1 动态注册工具的 Prompt Appendix

**参考实现**: `MineClawd.java:420-450`

```java
private static final String DYNAMIC_REGISTRY_PROMPT_APPENDIX = String.join("\n",
    "*** DYNAMIC REGISTRY (RUNTIME PLACEHOLDER MODE) ***",
    "True startup registration is still impossible in-session, but you can pseudo-register",
    "content by configuring pre-registered placeholders.",
    "",
    "Tools:",
    "  `list-dynamic-content`    Inspect used and free slots for items/blocks/fluids.",
    "  `register-dynamic-item`   Claim a free item slot.",
    "  `register-dynamic-block`  Claim a free block slot.",
    "  `register-dynamic-fluid`  Claim a free fluid slot.",
    // ...
);
```

**关键点**:
1. **Prompt Appendix 是可选的** - 只有需要告诉 LLM 特定功能的工具才需要
2. **与工具绑定** - 每个工具可以有自己的 Prompt Appendix
3. **Agent 级别配置** - 可以在 Agent 中配置是否启用

### 9.2 Agent Prompt 系统

**参考实现**: `MineClawd.java:5612-5709`

```java
private String buildSystemPrompt(
    ServerCommandSource source,
    MineClawdConfig config,
    String ownerKey,
    boolean dynamicRegistryEnabled,
    SessionData session
) {
    // 1. 基础 Prompt (优先级: agent > config > default)
    String basePrompt = agent.hasBasePrompt() 
        ? agent.basePrompt() 
        : (configured == null || configured.isBlank() 
            ? BASE_SYSTEM_PROMPT 
            : configured.trim());
    
    StringBuilder prompt = new StringBuilder(basePrompt);
    
    // 2. 环境信息
    if (!env.isBlank()) {
        prompt.append("\n\nEnvironment:\n").append(env);
    }
    
    // 3. 已安装的 Mod
    if (!installedMods.isBlank()) {
        prompt.append("\n\nInstalled mods:\n").append(installedMods);
    }
    
    // 4. 搜索工具状态
    if (hasConfiguredTavilyKey(config)) {
        prompt.append("\n\nSearch tool status:\n").append("...");;
    }
    
    // 5. 人格上下文
    prompt.append("\n\nPersona context:\n").append("...");
    
    // 6. 会话上下文
    if (session != null) {
        prompt.append("\n\nSession context:\n").append("...");
    }
    
    // 7. 动态注册 Prompt Appendix (可选)
    if (dynamicRegistryEnabled && agent.hasDynamicRegistryPrompt()) {
        prompt.append("\n\n").append(agent.dynamicRegistryPrompt());
    } else if (dynamicRegistryEnabled) {
        prompt.append("\n\n").append(DYNAMIC_REGISTRY_PROMPT_APPENDIX);
    }
    
    // 8. 资产跟踪 Prompt Appendix (可选)
    if (agent.hasAssetTrackingPrompt()) {
        prompt.append("\n\n").append(agent.assetTrackingPrompt());
    } else {
        prompt.append("\n\n").append(ASSET_TRACKING_PROMPT_APPENDIX);
    }
    
    return prompt.toString();
}
```

### 9.3 Agent Manager 系统

**参考实现**: `AgentManager.java:205`

```java
public record Agent(
    String name,
    String basePrompt,
    String dynamicRegistryPrompt,
    String assetTrackingPrompt
) {
    public boolean hasBasePrompt() {
        return basePrompt != null && !basePrompt.isBlank();
    }
    
    public boolean hasDynamicRegistryPrompt() {
        return dynamicRegistryPrompt != null && !dynamicRegistryPrompt.isBlank();
    }
    
    public boolean hasAssetTrackingPrompt() {
        return assetTrackingPrompt != null && !assetTrackingPrompt.isBlank();
    }
}
```

### 9.4 工具 Prompt Appendix 设计

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/MineClawdTool.java
public interface MineClawdTool {
    
    // ... 现有方法 ...
    
    /**
     * 可选: 工具的 Prompt Appendix
     * 给 LLM 的额外上下文信息
     */
    default String getPromptAppendix() {
        return null;
    }
    
    /**
     * 可选: 工具所属的 Prompt Category
     */
    default String getPromptCategory() {
        return null;
    }
}
```

### 9.5 ToolRegistry 增强

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/ToolRegistry.java
public class ToolRegistry {
    private static final Map<String, MineClawdTool> TOOLS = new ConcurrentHashMap<>();
    private static final Map<String, String> TOOL_PROMPT_APPENDIXES = new ConcurrentHashMap<>();
    
    public static void register(MineClawdTool tool) {
        // ... 现有逻辑 ...
        
        // 注册 Prompt Appendix
        String appendix = tool.getPromptAppendix();
        if (appendix != null && !appendix.isBlank()) {
            TOOL_PROMPT_APPENDIXES.put(tool.getName(), appendix);
        }
    }
    
    public static String getPromptAppendix(String toolName) {
        return TOOL_PROMPT_APPENDIXES.get(toolName);
    }
    
    public static Map<String, String> getAllPromptAppendixes() {
        return Collections.unmodifiableMap(TOOL_PROMPT_APPENDIXES);
    }
}
```

### 9.6 构建系统 Prompt

```java
// MineClawd/src/main/java/com/mineclawd/MineClawd.java
private String buildSystemPromptWithTools(
    ServerCommandSource source,
    MineClawdConfig config,
    String ownerKey,
    boolean dynamicRegistryEnabled,
    SessionData session
) {
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

## 十、未来扩展

### 10.1 工具分类
```java
public enum ToolCategory {
    INFORMATION,      // 信息查询
    EXECUTION,        // 执行操作
    FILE_MANAGEMENT,  // 文件管理
    GAME_CONTROL,     // 游戏控制
    CUSTOM            // 自定义
}
```

### 10.2 工具权限
```java
public interface PermissionAwareTool {
    int getRequiredPermissionLevel();
}
```

### 10.3 工具依赖
```java
public interface DependencyAwareTool {
    List<String> getRequiredMods();
}
```

### 10.4 工具元数据
```java
public record ToolMetadata(
    String version,
    String author,
    String license,
    List<String> tags
) {}
```

## 十、参考实现

### 10.1 内置工具迁移示例

```java
// 原来的硬编码工具
new OpenAITool(
    TOOL_APPLY_INSTANT_SERVER_SCRIPT,
    "Apply immediate runtime changes by executing KubeJS JavaScript...",
    codeToolParameters()
)

// 迁移后
ToolRegistry.register(new MineClawdTool() {
    @Override
    public String getName() {
        return "mineclawd:apply_instant_server_script";
    }
    
    @Override
    public String getDescription() {
        return "Apply immediate runtime changes by executing KubeJS JavaScript via /_exec_kubejs_internal. " +
               "Use for one-off operations: inventory inspection/editing, nearby block changes, " +
               "entity queries, or any ad-hoc server action.";
    }
    
    @Override
    public JsonObject getParameters() {
        return codeToolParameters("JavaScript code to execute immediately.");
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String code = args.has("code") ? args.get("code").getAsString() : null;
        if (code == null || code.isBlank()) {
            return ToolExecutionResult.failure("code is required");
        }
        
        var result = KubeJsToolExecutor.executeInstant(source, code);
        return new ToolExecutionResult(result.success(), result.output());
    }
});
```

---

**文档版本**: 1.0  
**最后更新**: 2026-03-29  
**作者**: 在与有
