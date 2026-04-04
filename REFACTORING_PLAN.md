# MineClawd 工具系统重构计划

## 一、重构目标

### 1.1 核心目标

1. **模块化** - 允许第三方 Mod 通过简单接口添加自定义工具
2. **可扩展** - 工具注册表支持动态添加/移除工具
3. **Prompt Appendix 支持** - 工具可以提供特定的 LLM 上下文
4. **Agent 兼容** - 支持 Agent 级别的 Prompt 配置
5. **向后兼容** - 保留现有工具功能，平滑迁移

### 1.2 非目标

1. **不修改 LLM 客户端** - OpenAIClient/VertexAIClient 保持不变
2. **不修改核心执行逻辑结构** - executeToolCallSync/Async 的执行流程保持不变
3. **不移除现有工具** - 内置工具继续工作
4. **不强制使用新 API** - 现有代码继续使用旧方式

### 1.3 必须修改的地方 ⚠️

1. **`openAiTools()` 方法** - 改为从 ToolRegistry 获取工具列表
2. **`executeToolCallSync()` 方法** - 改为使用 ToolRegistry 查找并执行工具
3. **`buildSystemPrompt()` 方法** - 改为集成所有工具的 Prompt Appendix
4. **`executeToolCallAsync()` 方法** - 确保异步工具支持

## 二、当前问题分析

### 2.1 硬编码工具注册

**位置**: `MineClawd.java:4619-4770`

```java
private List<OpenAITool> openAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
    List<OpenAITool> tools = new ArrayList<>(List.of(
        new OpenAITool(TOOL_ASK_USER, "...", questionToolParameters()),
        new OpenAITool(TOOL_APPLY_INSTANT_SERVER_SCRIPT, "...", codeToolParameters()),
        // ... 几十个工具硬编码在这里
    ));
    return List.copyOf(tools);
}
```

**问题**:
- ❌ 所有工具必须在 MineClawd 源码中定义
- ❌ 无法动态添加/移除工具
- ❌ 第三方 Mod 无法添加自己的工具
- ❌ 工具列表在编译时固定

### 2.2 Switch-Case 执行分发

**位置**: `MineClawd.java:3240-3500`

```java
private String executeToolCallSync(ServerCommandSource source, String toolName, JsonObject args, ...) {
    switch (toolName) {
        case TOOL_APPLY_INSTANT_SERVER_SCRIPT:
            // ...
        case TOOL_EXECUTE_COMMAND:
            // ...
        case TOOL_LIST_FILES:
            // ...
        // ... 几十个 case
        default:
            return "ERROR: Unknown tool " + toolName;
    }
}
```

**问题**:
- ❌ 每增加一个工具都要修改这里
- ❌ Switch-case 随着工具数量增长变得臃肿
- ❌ 不利于维护和扩展

### 2.3 工具 Prompt Appendix 硬编码

**位置**: `MineClawd.java:420-450`

```java
private static final String DYNAMIC_REGISTRY_PROMPT_APPENDIX = String.join("\n",
    "*** DYNAMIC REGISTRY (RUNTIME PLACEHOLDER MODE) ***",
    "True startup registration is still impossible in-session, but you can pseudo-register",
    "content by configuring pre-registered placeholders.",
    // ...
);
```

**问题**:
- ❌ 每个工具的 Prompt Appendix 都是硬编码常量
- ❌ 无法动态生成或修改
- ❌ 无法按 Agent 配置
- ❌ 第三方工具无法添加自己的 Prompt Appendix

### 2.4 Agent Prompt 系统耦合

**位置**: `MineClawd.java:5612-5709`

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

**问题**:
- ❌ 只支持两个固定的 Prompt Appendix (dynamic_registry, asset_tracking)
- ❌ 无法支持其他工具的 Prompt Appendix
- ❌ 无法按工具分类配置

## 三、设计原则

### 3.1 简单优先

1. **最小化改动** - 只修改必要的代码
2. **向后兼容** - 保留现有 API
3. **渐进式迁移** - 允许平滑过渡

### 3.2 清晰分离

1. **工具定义** - 工具的元数据 (name, description, parameters)
2. **工具执行** - 工具的业务逻辑
3. **Prompt Appendix** - 工具的 LLM 上下文 (可选)
4. **注册管理** - 工具的生命周期管理

### 3.3 灵活性

1. **可选功能** - Prompt Appendix 是可选的
2. **Agent 配置** - 支持按 Agent 配置
3. **动态注册** - 支持运行时添加/移除工具

## 四、重构方案

### 4.1 新增文件结构

```
MineClawd_RE/common/src/main/java/com/mineclawd/foundation/tool/
├── MineClawdTool.java              # 工具接口
├── ToolExecutionResult.java        # 执行结果
├── ToolRegistry.java               # 工具注册表
├── ToolExecutorWrapper.java        # 执行器包装
└── prompt/
    ├── ToolPromptManager.java      # Prompt 管理器
    └── ToolPromptCategory.java     # Prompt 分类
```

### 4.2 必须修改的文件 ⚠️

```
MineClawd_RE/common/src/main/java/com/mineclawd/
└── MineClawd.java                  # 必须修改 4 个方法
```

**修改内容**:
1. `openAiTools()` - 从 ToolRegistry 获取工具
2. `executeToolCallSync()` - 使用 ToolRegistry 查找工具
3. `executeToolCallAsync()` - 确保异步工具支持
4. `buildSystemPrompt()` - 集成所有工具的 Prompt Appendix

### 4.2 核心接口定义

```java
// MineClawd/src/main/java/com/mineclawd/foundation/tool/MineClawdTool.java
package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;
import net.minecraft.server.command.ServerCommandSource;

/**
 * MineClawd 工具扩展接口
 */
public interface MineClawdTool {
    
    /**
     * 工具的唯一标识符
     */
    String getName();
    
    /**
     * 给 LLM 的描述
     */
    String getDescription();
    
    /**
     * 参数的 JSON Schema 定义
     */
    JsonObject getParameters();
    
    /**
     * 执行工具
     */
    ToolExecutionResult execute(ServerCommandSource source, JsonObject args);
    
    /**
     * 可选: 工具的 Prompt Appendix
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
 */
public class ToolRegistry {
    private static final Map<String, MineClawdTool> TOOLS = new ConcurrentHashMap<>();
    private static final Map<String, String> TOOL_PROMPT_APPENDIXES = new ConcurrentHashMap<>();
    private static final Map<String, String> TOOL_PROMPT_CATEGORIES = new ConcurrentHashMap<>();
    
    static {
        registerBuiltInTools();
    }
    
    /**
     * 注册内置工具
     */
    private static void registerBuiltInTools() {
        // 可以在这里注册内置工具，或者留空完全由 Mod 注册
    }
    
    /**
     * 注册一个工具
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
        
        // 注册 Prompt Appendix
        String appendix = tool.getPromptAppendix();
        if (appendix != null && !appendix.isBlank()) {
            TOOL_PROMPT_APPENDIXES.put(name, appendix);
        }
        
        // 注册 Prompt Category
        String category = tool.getPromptCategory();
        if (category != null && !category.isBlank()) {
            TOOL_PROMPT_CATEGORIES.put(name, category);
        }
        
        MineClawd.LOGGER.info("Registered tool: {}", name);
    }
    
    /**
     * 注销一个工具
     */
    public static boolean unregister(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        
        boolean removed = TOOLS.remove(name) != null;
        TOOL_PROMPT_APPENDIXES.remove(name);
        TOOL_PROMPT_CATEGORIES.remove(name);
        
        return removed;
    }
    
    /**
     * 获取工具
     */
    public static MineClawdTool get(String name) {
        return TOOLS.get(name);
    }
    
    /**
     * 获取所有工具
     */
    public static Map<String, MineClawdTool> getAll() {
        return Collections.unmodifiableMap(TOOLS);
    }
    
    /**
     * 获取工具的 Prompt Appendix
     */
    public static String getPromptAppendix(String name) {
        return TOOL_PROMPT_APPENDIXES.get(name);
    }
    
    /**
     * 获取所有 Prompt Appendix
     */
    public static Map<String, String> getAllPromptAppendixes() {
        return Collections.unmodifiableMap(TOOL_PROMPT_APPENDIXES);
    }
    
    /**
     * 获取工具的 Prompt Category
     */
    public static String getPromptCategory(String name) {
        return TOOL_PROMPT_CATEGORIES.get(name);
    }
}
```

### 4.4 执行器包装

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
 */
public class ToolExecutorWrapper {
    private final MineClawdTool tool;
    
    public ToolExecutorWrapper(MineClawdTool tool) {
        this.tool = tool;
    }
    
    /**
     * 执行工具
     */
    public String execute(ServerCommandSource source, JsonObject args) {
        if (tool == null) {
            return "ERROR: Tool is null";
        }
        
        try {
            var result = tool.execute(source, args);
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

### 4.5 MineClawd.java 修改

#### 4.5.1 openAiTools() 方法

**修改前**:
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
    
    // 从注册表获取所有工具
    for (MineClawdTool tool : ToolRegistry.getAll().values()) {
        tools.add(new ToolExecutorWrapper(tool).toOpenAITool());
    }
    
    return List.copyOf(tools);
}
```

#### 4.5.2 executeToolCallSync() 方法

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
    MineClawdTool tool = ToolRegistry.get(toolName);
    if (tool == null) {
        return "ERROR: Unknown tool " + toolName;
    }
    
    ToolExecutorWrapper wrapper = new ToolExecutorWrapper(tool);
    return wrapper.execute(source, args);
}
```

#### 4.5.3 buildSystemPrompt() 方法

**修改前**:
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

**⚠️ 注意**: 这些修改是**必须的**，否则 ToolRegistry 中的工具无法被 LLM 使用！

### 4.6 Agent Manager 增强

```java
// MineClawd/src/main/java/com/mineclawd/foundation/agent/AgentManager.java
public record Agent(
    String name,
    String basePrompt,
    String dynamicRegistryPrompt,
    String assetTrackingPrompt,
    Map<String, String> customToolPrompts  // 新增
) {
    public boolean hasCustomToolPrompt(String toolName) {
        return customToolPrompts != null && customToolPrompts.containsKey(toolName);
    }
    
    public String getCustomToolPrompt(String toolName) {
        return customToolPrompts != null ? customToolPrompts.get(toolName) : null;
    }
}
```

## 五、重构步骤

### 阶段 1: 基础设施 (1-2 天)

#### 任务 1.1: 创建核心接口

- [x] 创建 `MineClawdTool.java` 接口
- [x] 创建 `ToolExecutionResult.java` 记录类
- [x] 创建 `ToolRegistry.java` 注册表
- [x] 创建 `ToolExecutorWrapper.java` 包装类

#### 任务 1.2: 创建 Prompt 管理器

- [x] 创建 `ToolPromptManager.java`
- [x] 创建 `ToolPromptCategory.java` 枚举
- [x] 实现 Prompt 分类和管理逻辑

#### 任务 1.3: 编写使用示例

- [x] 编写 `TOOL_EXTENSION_USAGE.md` 文档

#### 任务 1.4: 单元测试 (可选，后续补充)

- [ ] 编写单元测试
- [ ] 验证工具注册/注销
- [ ] 验证 Prompt Appendix 注册

### 阶段 2: MineClawd 集成 (2-3 天) ⚠️ 必须完成

#### 任务 2.0: 理解必须修改的原因 ⚠️

- [ ] 阅读 `IMPORTANT_NOTICE.md` 了解为什么必须修改 MineClawd.java
- [ ] 阅读 `CODE_CHANGES_GUIDE.md` 了解具体的修改内容
- [ ] 确认理解 ToolRegistry 和 MineClawd.java 的关系

#### 任务 2.1: 修改 openAiTools()

- [ ] 修改 `openAiTools()` 方法
- [ ] 从 ToolRegistry 获取工具
- [ ] 测试工具列表生成

#### 任务 2.2: 修改 executeToolCallSync()

- [ ] 修改 `executeToolCallSync()` 方法
- [ ] 使用 ToolRegistry 查找工具
- [ ] 测试工具执行

#### 任务 2.3: 修改 buildSystemPrompt()

- [ ] 修改 `buildSystemPrompt()` 方法
- [ ] 集成 ToolRegistry 的 Prompt Appendix
- [ ] 测试 Prompt 生成

#### 任务 2.4: 修改 executeToolCallAsync()

- [ ] 修改 `executeToolCallAsync()` 方法
- [ ] 确保异步工具支持
- [ ] 测试异步工具执行

### 阶段 3: Agent 增强 (1-2 天)

#### 任务 3.1: Agent 级别 Prompt 配置

- [ ] 增强 `Agent` record
- [ ] 添加 `customToolPrompts` 字段
- [ ] 实现 Prompt 查找逻辑

#### 任务 3.2: Agent 文件格式

- [ ] 定义 Agent 文件格式
- [ ] 支持自定义工具 Prompt
- [ ] 测试 Agent 加载

### 阶段 4: 内置工具迁移 (3-5 天)

#### 任务 4.1: 迁移文件工具

- [ ] `WorkspaceFileToolExecutor` → `MineClawdTool`
- [ ] 注册到 ToolRegistry
- [ ] 测试所有文件操作

#### 任务 4.2: 迁移 KubeJS 工具

- [ ] `KubeJsToolExecutor` → `MineClawdTool`
- [ ] 注册到 ToolRegistry
- [ ] 测试脚本执行

#### 任务 4.3: 迁移 Mod 文档工具

- [ ] `ModDocsToolExecutor` → `MineClawdTool`
- [ ] 注册到 ToolRegistry
- [ ] 测试 Mod 文档获取

#### 任务 4.4: 迁移动态内容工具

- [ ] `DynamicContentToolExecutor` → `MineClawdTool`
- [ ] 注册到 ToolRegistry
- [ ] 测试动态内容注册

#### 任务 4.5: 迁移其他工具

- [ ] `SearchToolExecutor` → `MineClawdTool`
- [ ] `ModDocsToolExecutor` → `MineClawdTool`
- [ ] 其他内置工具

### 阶段 5: 文档和示例 (2-3 天)

#### 任务 5.1: 开发者指南

- [ ] 编写 Mod 开发者指南
- [ ] 提供完整示例
- [ ] 提供 KubeJS 脚本示例

#### 任务 5.2: 迁移指南

- [ ] 编写迁移指南
- [ ] 提供旧代码示例
- [ ] 提供新代码示例

#### 任务 5.3: API 文档

- [ ] 更新 API 文档
- [ ] 提供 Javadoc
- [ ] 提供代码示例

## 六、向后兼容性

### 6.1 保留现有 API

- [ ] `TOOL_*` 常量继续保留
- [ ] `executeToolCallSync()` 保持签名
- [ ] `openAiTools()` 保持签名

### 6.2 旧工具继续工作

- [ ] 硬编码工具继续工作
- [ ] 不强制迁移现有工具
- [ ] 允许混合使用新旧方式

### 6.3 渐进式迁移

- [ ] 允许逐步迁移工具
- [ ] 不要求一次性全部迁移
- [ ] 支持新旧工具共存

## 七、风险和缓解

### 7.1 风险 1: 性能影响

**风险**: ToolRegistry 查找可能比 switch-case 慢

**缓解**:
- 使用 ConcurrentHashMap (O(1) 查找)
- 工具数量少 (通常 < 50)
- 性能差异可忽略

### 7.2 风险 2: 兼容性问题

**风险**: 修改 MineClawd 可能影响现有 Mod

**缓解**:
- 保留现有 API
- 提供迁移指南
- 允许混合使用

### 7.3 风险 3: 复杂性增加

**风险**: 新系统更复杂

**缓解**:
- 简单的接口设计
- 清晰的文档
- 渐进式迁移

## 八、成功标准

### 8.1 功能性

- [ ] 第三方 Mod 可以添加自定义工具
- [ ] 工具 Prompt Appendix 正确注入
- [ ] Agent 级别 Prompt 配置工作
- [ ] 所有内置工具继续工作

### 8.2 非功能性

- [ ] 性能无明显下降
- [ ] 代码可维护性提升
- [ ] 文档完整清晰
- [ ] 向后兼容

### 8.3 质量

- [ ] 单元测试覆盖 > 80%
- [ ] 集成测试通过
- [ ] 代码审查通过
- [ ] 文档完整

## 九、后续扩展

### 9.1 工具分类

```java
public enum ToolCategory {
    INFORMATION,        // 信息查询
    EXECUTION,          // 执行操作
    FILE_MANAGEMENT,    // 文件管理
    GAME_CONTROL,       // 游戏控制
    CUSTOM              // 自定义
}
```

### 9.2 工具权限

```java
public interface PermissionAwareTool {
    int getRequiredPermissionLevel();
}
```

### 9.3 工具依赖

```java
public interface DependencyAwareTool {
    List<String> getRequiredMods();
}
```

### 9.4 工具元数据

```java
public record ToolMetadata(
    String version,
    String author,
    String license,
    List<String> tags
) {}
```

## 十、参考实现

### 10.1 动态注册工具

```java
// 在 Mod 初始化时
@Override
public void onInitialize() {
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
            // 返回 JSON Schema
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            // 实现逻辑
        }
    });
}
```

### 10.2 带 Prompt Appendix 的工具

```java
ToolRegistry.register(new MineClawdTool() {
    @Override
    public String getPromptAppendix() {
        return "*** CRAFTING TOOLS ***\n\nUse these tools for crafting.";
    }
    
    @Override
    public String getPromptCategory() {
        return "crafting";
    }
    
    // ... 其他方法
});
```

## 十一、Agent 层重构计划

### 11.1 核心问题

#### 11.1.1 功能模块的可扩展性

**问题描述**：

当前架构中，Agent 只有 `dynamicRegistryPrompt` 和 `assetTrackingPrompt` 两个字段，这意味着：

1. **只有两个功能模块** - 动态注册和资产追踪
2. **无法添加新的功能模块** - 需要修改 Agent record
3. **功能模块是硬编码的** - 无法动态配置

**问题示例**：

```java
// 如果想添加一个新的功能模块 "web_search"
public record Agent(String name, 
                   String basePrompt, 
                   String dynamicRegistryPrompt,
                   String assetTrackingPrompt,
                   String webSearchPrompt) {  // ❌ 需要修改 Agent record
    // ...
}
```

**影响**：

- ❌ 第三方 Mod 无法添加自己的功能模块
- ❌ 新功能需要修改核心代码
- ❌ 不符合开闭原则（对扩展开放，对修改关闭）

#### 11.1.2 控制粒度

**问题描述**：

当前架构中，Agent 的控制粒度是"功能模块级别"：

1. **功能模块** - 一组相关的工具
2. **模块 Prompt** - 整个模块的 Prompt Appendix

**问题示例**：

```
动态注册模块 (dynamicRegistryPrompt)
├── list-dynamic-content
├── register-dynamic-item
├── register-dynamic-block
├── register-dynamic-fluid
├── update-dynamic-item
├── update-dynamic-block
├── update-dynamic-fluid
└── unregister-dynamic-content
```

**问题**：

- ❌ 无法单独控制模块中的某个工具
- ❌ 无法为单个工具提供自定义 Prompt
- ❌ 无法根据上下文动态启用/禁用工具

#### 11.1.3 控制层级

**问题描述**：

当前架构中，Prompt 的注入层级是"并列的"：

```
Base Prompt (Agent)
    ↓
Environment (Hardcoded)
    ↓
Installed Mods (Hardcoded)
    ↓
Search Tool Status (Hardcoded)
    ↓
Persona Context (Hardcoded)
    ↓
Session Context (Hardcoded)
    ↓
File Path Context (Hardcoded)
    ↓
Dynamic Registry Prompt (Agent) ⭐
    ↓
Asset Tracking Prompt (Agent) ⭐
```

**问题**：

- ❌ 所有 Prompt 都是并列的，没有层级关系
- ❌ 无法对 Prompt 进行分组和组织
- ❌ 无法根据条件动态调整 Prompt 结构

### 11.2 设计目标

1. **功能模块可扩展** - 支持动态添加新的功能模块
2. **工具可见性控制** - 支持按工具、分类、模块控制工具可见性
3. **Prompt 展现控制** - 支持自定义 Prompt 展现方式
4. **向后兼容** - 保留现有功能，平滑迁移

### 11.3 推荐方案：功能模块 Map

#### 11.3.1 Agent 结构

```java
public record Agent(String name, 
                   String basePrompt,
                   Map<String, String> modulePrompts) {  // 功能模块 Prompt Map
    
    public boolean hasBasePrompt() {
        return basePrompt != null && !basePrompt.isBlank();
    }
    
    public String getModulePrompt(String moduleName) {
        return modulePrompts != null ? modulePrompts.get(moduleName) : null;
    }
    
    public boolean hasModulePrompt(String moduleName) {
        String prompt = getModulePrompt(moduleName);
        return prompt != null && !prompt.isBlank();
    }
}
```

#### 11.3.2 MineClawd.java 修改

```java
private String buildSystemPrompt(...) {
    // ... 现有代码 ...
    
    // 功能模块 Prompt (Agent 控制)
    Map<String, String> modulePrompts = agent.modulePrompts();
    if (modulePrompts != null) {
        modulePrompts.forEach((moduleName, prompt) -> {
            if (prompt != null && !prompt.isBlank()) {
                prompt.append("\n\n").append(prompt);
            }
        });
    }
    
    return prompt.toString();
}
```

### 11.4 实现计划

#### 11.4.1 阶段 1: Agent 层架构设计（进行中）

- [x] 分析当前架构
- [x] 识别核心问题
- [x] 设计解决方案
- [x] 撰写架构文档 (`AGENT_ARCHITECTURE.md`)

#### 11.4.2 阶段 2: Agent 层实现

- [ ] 修改 Agent record，使用 Map 替代固定字段
- [ ] 修改 AgentManager，支持功能模块管理
- [ ] 修改 MineClawd.java，使用新的 Agent 结构
- [ ] 测试和验证

#### 11.4.3 阶段 3: 文档和示例

- [ ] 撰写 Agent 层架构文档
- [ ] 提供示例代码

---

**文档版本**: 1.0  
**最后更新**: 2026-03-31  
**作者**: 在与有
