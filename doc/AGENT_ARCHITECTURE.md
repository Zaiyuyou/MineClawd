# MineClawd Agent 层架构设计

## 一、Agent 层的核心职责

### 1.1 Agent 的定义

Agent 是 MineClawd 的核心抽象层，负责：

1. **工具选择** - 决定哪些工具对 LLM 可见
2. **Prompt 管理** - 控制工具 Prompt 的展现方式
3. **上下文注入** - 管理 Agent 特定的上下文信息
4. **行为控制** - 定义 Agent 的行为模式和规则

### 1.2 Agent 与 Tool 的关系

```
┌─────────────────────────────────────────────────────────────┐
│                      Agent Layer                            │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Agent (name, basePrompt, modulePrompts, ...)        │  │
│  │  - 控制工具可见性                                      │  │
│  │  - 控制 Prompt 展现                                    │  │
│  │  - 管理上下文                                          │  │
│  └───────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  AgentManager (加载、管理所有 Agent)                   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                      Tool Layer                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ToolRegistry (注册所有工具)                           │  │
│  │  - MineClawdTool 接口                                 │  │
│  │  - 工具启用/禁用控制                                   │  │
│  │  - Prompt Appendix 管理                                │  │
│  └───────────────────────────────────────────────────────┘  │
│                           ↓                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ToolPromptManager (管理自定义 Prompt)                │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 二、当前架构分析

### 2.2 Agent 的 Prompt 字段

```java
public record Agent(String name, 
                   String basePrompt, 
                   String dynamicRegistryPrompt,  // 动态注册模块 Prompt
                   String assetTrackingPrompt) {    // 资产追踪模块 Prompt
    // ...
}
```

**关键点**：
- 每个 Prompt 是一个完整的 Markdown 文档
- 文档中包含该模块所有工具的描述
- 工具列表直接写在 Prompt 中，不需要分类

**示例**：

```markdown
// ── DYNAMIC REGISTRY ──────────────────────────────────────────────────────
*** DYNAMIC REGISTRY (RUNTIME PLACEHOLDER MODE) ***
True startup registration is still impossible in-session, but you can pseudo-register
content by configuring pre-registered placeholders.

Tools:
  `list-dynamic-content`    Inspect used and free slots for items/blocks/fluids.
  `register-dynamic-item`   Claim a free item slot.
    Params: name, material_item (vanilla item id), throwable.
  // ... 更多工具

Rules:
  1. If the user does not specify a slot, call register tools without `slot` to auto-pick.
  2. Registered placeholders appear in creative tabs; unregistered slots stay hidden.
  // ... 更多规则
```

### 2.3 Prompt 的注入逻辑

```java
private String buildSystemPrompt(...) {
    // ... 现有代码 ...
    
    // 动态注册模块 Prompt (Agent 控制) ⭐
    if (dynamicRegistryEnabled && agent.hasDynamicRegistryPrompt()) {
        prompt.append("\n\n").append(agent.dynamicRegistryPrompt());
    } else if (dynamicRegistryEnabled) {
        prompt.append("\n\n").append(DYNAMIC_REGISTRY_PROMPT_APPENDIX);
    }
    
    // 资产追踪模块 Prompt (Agent 控制) ⭐
    if (agent.hasAssetTrackingPrompt()) {
        prompt.append("\n\n").append(agent.assetTrackingPrompt());
    } else {
        prompt.append("\n\n").append(ASSET_TRACKING_PROMPT_APPENDIX);
    }
    
    return prompt.toString();
}
```

**关键点**：
- Agent 通过 `hasDynamicRegistryPrompt()` 和 `hasAssetTrackingPrompt()` 控制 Prompt 的注入
- 如果 Agent 有自定义 Prompt，使用 Agent 的 Prompt
- 如果 Agent 没有自定义 Prompt，使用硬编码的默认 Prompt
- Prompt 是完整的 Markdown 文档，包含所有相关工具的描述

## 三、核心问题分析

### 3.1 问题 1：功能模块的可扩展性

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

**根本原因**：

- Agent record 的字段是硬编码的
- AgentManager 中的 `loadActiveAgent()` 方法硬编码了字段读取
- MineClawd.java 中的 `buildSystemPrompt()` 方法硬编码了字段使用

**解决方案**：

1. **使用 Map 存储功能模块 Prompt**
2. **AgentManager 支持动态读取任意字段**
3. **MineClawd.java 支持动态注入任意功能模块 Prompt**

### 3.2 问题 2：控制粒度

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

**说明**：

- 这是设计选择，不是问题
- 功能模块级别的控制已经足够
- 工具级别的控制可以通过 Prompt 内容实现

### 3.3 问题 3：控制层级

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

**说明**：

- 这是设计选择，不是问题
- 并列的层级已经足够简单明了
- 复杂的层级结构会增加复杂度

### 3.4 问题 4：硬编码耦合

**问题描述**：

Agent 层中的方法和函数硬编码深度耦合了这两个功能模块：

1. **Agent record** - 硬编码了字段
2. **AgentManager** - 硬编码了字段读取
3. **MineClawd.java** - 硬编码了字段使用

**问题示例**：

```java
// Agent record - 硬编码字段
public record Agent(String name, 
                   String basePrompt, 
                   String dynamicRegistryPrompt,  // ❌ 硬编码
                   String assetTrackingPrompt) {    // ❌ 硬编码
    // ...
}

// AgentManager - 硬编码读取
public synchronized Agent loadActiveAgent(String ownerKey) {
    String basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
    String dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);  // ❌ 硬编码
    String assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);      // ❌ 硬编码
    // ...
}

// MineClawd.java - 硬编码使用
private String buildSystemPrompt(...) {
    // ...
    if (dynamicRegistryEnabled && agent.hasDynamicRegistryPrompt()) {  // ❌ 硬编码
        prompt.append("\n\n").append(agent.dynamicRegistryPrompt());
    } else if (dynamicRegistryEnabled) {
        prompt.append("\n\n").append(DYNAMIC_REGISTRY_PROMPT_APPENDIX);
    }
    
    if (agent.hasAssetTrackingPrompt()) {  // ❌ 硬编码
        prompt.append("\n\n").append(agent.assetTrackingPrompt());
    } else {
        prompt.append("\n\n").append(ASSET_TRACKING_PROMPT_APPENDIX);
    }
    // ...
}
```

**影响**：

- ❌ 无法添加新的功能模块
- ❌ 需要修改核心代码
- ❌ 不符合开闭原则

**解决方案**：

1. **使用 Map 存储功能模块 Prompt**
2. **AgentManager 支持动态读取任意字段**
3. **MineClawd.java 支持动态注入任意功能模块 Prompt**

### 3.5 问题 5：AgentManager 的硬编码

**问题描述**：

AgentManager 中的 `loadActiveAgent()` 方法硬编码了字段读取：

```java
public synchronized Agent loadActiveAgent(String ownerKey) {
    String basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
    String dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);
    String assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);
    // ...
}
```

**问题**：

- ❌ 无法动态添加新的功能模块
- ❌ 需要修改 AgentManager
- ❌ 需要修改 Agent record

**解决方案**：

1. **使用 Map 存储功能模块 Prompt**
2. **AgentManager 返回 Map 而不是固定字段**
3. **MineClawd.java 动态注入功能模块 Prompt**

### 3.6 问题 6：MineClawd.java 的硬编码

**问题描述**：

MineClawd.java 中的 `buildSystemPrompt()` 方法硬编码了字段使用：

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
    // ...
}
```

**问题**：

- ❌ 无法动态添加新的功能模块
- ❌ 需要修改 MineClawd.java
- ❌ 不符合开闭原则

**解决方案**：

1. **使用 Map 存储功能模块 Prompt**
2. **MineClawd.java 动态注入功能模块 Prompt**

### 3.7 问题 7：PROMPT_TYPE 常量的硬编码

**问题描述**：

AgentManager 中的 PROMPT_TYPE 常量硬编码了功能模块类型：

```java
public static final String PROMPT_TYPE_BASE = "base";
public static final String PROMPT_TYPE_DYNAMIC_REGISTRY = "dynamic_registry";
public static final String PROMPT_TYPE_ASSET_TRACKING = "asset_tracking";
```

**问题**：

- ❌ 无法动态添加新的功能模块
- ❌ 需要修改 AgentManager
- ❌ 不符合开闭原则

**解决方案**：

1. **移除 PROMPT_TYPE 常量**
2. **使用动态字段名**

## 四、设计目标

### 4.1 核心目标

1. **功能模块可扩展** - 支持动态添加新的功能模块
2. **工具可见性控制** - 支持按工具、分类、模块控制工具可见性
3. **Prompt 展现控制** - 支持自定义 Prompt 展现方式
4. **向后兼容** - 保留现有功能，平滑迁移

### 4.2 非目标

1. **不改变 Tool 层架构** - Tool 层保持独立
2. **不修改 LLM 客户端** - OpenAIClient/VertexAIClient 保持不变
3. **不移除现有功能** - 现有功能继续工作

## 五、设计方案

### 5.1 方案 1：功能模块 Map（推荐）

**核心思想**：使用 Map 存储功能模块 Prompt，支持动态添加

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

**优点**：
- ✅ 支持动态添加功能模块
- ✅ 无需修改 Agent record
- ✅ 第三方 Mod 可以添加自己的功能模块
- ✅ 符合开闭原则

**缺点**：
- ❌ 没有类型安全
- ❌ 模块名称是字符串，容易出错

### 5.2 方案 2：功能模块接口化

**核心思想**：定义功能模块接口，支持动态注册

```java
public interface ToolModule {
    String getName();
    String getDescription();
    boolean isEnabled(Agent agent, ConfigContext context);
    String getPrompt(Agent agent, ConfigContext context);
    List<MineClawdTool> getTools(ConfigContext context);
    default void onEnable(Agent agent, ConfigContext context) {}
    default void onDisable(Agent agent, ConfigContext context) {}
}

public record Agent(String name, 
                   String basePrompt,
                   List<ToolModule> modules) {
    
    public boolean hasBasePrompt() {
        return basePrompt != null && !basePrompt.isBlank();
    }
    
    public List<ToolModule> getEnabledModules(ConfigContext context) {
        return modules.stream()
            .filter(module -> module.isEnabled(this, context))
            .collect(Collectors.toList());
    }
    
    public String generateModulePrompts(ConfigContext context) {
        StringBuilder sb = new StringBuilder();
        for (ToolModule module : getEnabledModules(context)) {
            String prompt = module.getPrompt(this, context);
            if (prompt != null && !prompt.isBlank()) {
                sb.append("\n\n").append(prompt);
            }
        }
        return sb.toString();
    }
}
```

**优点**：
- ✅ 完全动态化
- ✅ 支持复杂的逻辑
- ✅ 易于扩展
- ✅ 符合面向对象设计

**缺点**：
- ❌ 实现复杂
- ❌ 学习成本高
- ❌ 性能开销

## 六、推荐方案

### 6.1 推荐方案：功能模块 Map

**理由**：

1. **简洁明了** - 易于理解和维护
2. **灵活控制** - 支持动态添加功能模块
3. **易于扩展** - 第三方 Mod 可以添加自己的功能模块
4. **向后兼容** - 保留现有功能
5. **功能模块级别控制已足够** - 不需要工具级别和分类级别的控制

**实现方案**：

```java
// 1. 修改 Agent record
public record Agent(String name, 
                   String basePrompt,
                   Map<String, String> modulePrompts) {
    
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

// 2. 修改 AgentManager
public synchronized Agent loadActiveAgent(String ownerKey) {
    String agentName = getActiveAgentName(ownerKey);
    String basePrompt = readAgentPromptContent(agentName, "base");
    
    // 动态读取所有功能模块 Prompt
    Map<String, String> modulePrompts = new LinkedHashMap<>();
    modulePrompts.put("dynamic_registry", readAgentPromptContent(agentName, "dynamic_registry"));
    modulePrompts.put("asset_tracking", readAgentPromptContent(agentName, "asset_tracking"));
    // 可以动态添加更多功能模块
    
    if (basePrompt == null) {
        agentName = DEFAULT_AGENT;
        basePrompt = readAgentPromptContent(agentName, "base");
        
        modulePrompts.clear();
        modulePrompts.put("dynamic_registry", readAgentPromptContent(agentName, "dynamic_registry"));
        modulePrompts.put("asset_tracking", readAgentPromptContent(agentName, "asset_tracking"));
    }
    
    if (basePrompt == null) {
        basePrompt = "";
        modulePrompts.clear();
    }
    
    return new Agent(agentName, basePrompt, modulePrompts);
}

// 3. 修改 MineClawd.java
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

## 七、工作计划

### 7.1 阶段一：Agent 层架构设计（进行中）

- [x] 分析当前架构
- [x] 识别核心问题
- [x] 设计解决方案
- [x] 撰写架构文档

### 7.2 阶段二：Agent 层实现

- [ ] 修改 Agent record，使用 Map 替代固定字段
- [ ] 修改 AgentManager，支持功能模块管理
- [ ] 修改 MineClawd.java，使用新的 Agent 结构
- [ ] 测试和验证

### 7.3 阶段三：Tool 层适配

- [ ] 修改 ToolRegistry，支持分类管理
- [ ] 修改 ToolPromptManager，支持分类过滤
- [ ] 测试和验证

### 7.4 阶段四：文档和示例

- [ ] 撰写 Agent 层架构文档
- [ ] 撰写 Tool 层架构文档
- [ ] 提供示例代码

## 八、总结

### 8.1 核心问题

1. **功能模块的可扩展性** - 如何支持动态添加新的功能模块？
2. **控制粒度** - 如何支持多种控制粒度（模块、分类、工具）？
3. **控制层级** - 如何支持灵活的 Prompt 结构？

### 8.2 解决方案

1. **功能模块 Map** - 使用 Map 存储功能模块 Prompt
2. **工具分类控制** - 支持按分类控制工具可见性
3. **自定义工具 Prompt** - 支持为单个工具提供自定义 Prompt

### 8.3 推荐方案

**功能模块 Map + 工具分类控制**

- 简洁明了
- 灵活控制
- 易于扩展
- 向后兼容

### 8.4 下一步

1. 完成 Agent 层架构设计文档
2. 实现 Agent 层功能
3. 适配 Tool 层
4. 测试和验证
