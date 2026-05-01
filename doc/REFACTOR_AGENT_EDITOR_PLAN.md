# MineClawd 智能体编辑器 — 架构重构方案

> 日期: 2026-05-02 | Minecraft: 1.21.1 | Loader: NeoForge

---

## 1. 当前架构分析

### 1.1 AgentManager (foundation/agent/AgentManager.java)

| 方面 | 当前实现 | 问题 |
|------|----------|------|
| 存储 | `mineclawd/agents/<name>/*.md` 文件系统 | 纯文本文件，无结构化元数据 |
| 数据结构 | `record Agent(String name, String basePrompt, String dynamicRegistryPrompt, String assetTrackingPrompt)` | 无 emoji、无工具绑定、无 Persona 关联 |
| 列表 | `listAgentNames()` 扫描目录 | 仅返回名称列表 |
| 选择 | `loadActiveAgent(ownerKey)` | 单 agent 活跃模式，文件记录 |

**关键缺陷**：Agent 没有自己的元数据文件。emoji、描述、工具白名单、关联 Persona 都无法表达。

### 1.2 PersonaManager (foundation/persona/PersonaManager.java)

| 方面 | 当前实现 | 问题 |
|------|----------|------|
| 存储 | `mineclawd/souls/<name>.md` | 单个 .md 文件，无元数据 |
| 数据结构 | `record Persona(String name, String content)` | 只有名称和原始文本 |
| 管理 | 仅支持 `list` / `load` / `setActive` | 无 create/update/delete |
| 关联 | 独立于 Agent 运行 | Agent 和 Persona 无显式绑定关系 |

### 1.3 Tool系统 (foundation/tool/)

| 方面 | 当前实现 | 问题 |
|------|----------|------|
| 注册 | `ToolRegistry` 静态 Map + 硬编码 `registerBuiltInTools()` | 全局注册，不区分 agent |
| 启用 | `ToolConfig` JSON 文件 + `isToolEnabled()` | 全局启用/禁用，无 per-agent 过滤 |
| 分组 | `getPromptCategory()` | 分类仅用于 prompt 显示 |
| 架构 | `MineClawdTool` 接口 | 接口设计良好，模块化程度高 |

**关键缺陷**：工具不能绑定到特定 Agent。所有 Agent 共享同一工具集。

### 1.4 Prompt系统

| 组件 | 来源 |
|------|------|
| base prompt | Agent.basePrompt() → base.md |
| persona | Persona.content() → soul.md |
| 工具指引 | `RevealedToolPromptSystem.buildSystemPrompt()` |
| 环境信息 | `MineClawd.java:buildSystemPrompt()` 中拼接 |

**关键缺陷**：Prompt 组合逻辑深埋在 6600 行的 MineClawd.java 中，不可扩展、不可配置。

### 1.5 ChatRole (前端显示)

`ChatRole` 枚举定义了 USER/ASSISTANT/TOOL 的 avatar emoji、气泡颜色、对齐方式。前端的 MessageBubble 通过 `ChatRoleManager` 查找角色配置。

**当前局限**：Agent 在聊天中显示为固定的 ASSISTANT 角色(🤖)，不能自定义 emoji 或显示名称。

---

## 2. 新架构设计

### 2.1 设计原则

1. **模块化**：每个子系统有清晰的接口和边界
2. **可扩展**：通过注册/插件模式支持第三方扩展
3. **MCP Ready**：接口设计预留 MCP Server 集成点
4. **前后端分离**：数据层通过 Service 层暴露给 GUI
5. **渐进式兼容**：旧 API 标记弃用，不破坏现有功能

### 2.2 核心模块架构

```
foundation/
├── agent/
│   ├── AgentProfile.java          ← 智能体数据模型（新）
│   ├── AgentProfileManager.java   ← 智能体 CRUD（新，取代 AgentManager）
│   └── AgentManager.java          ← 标记 @Deprecated，委托给 AgentProfileManager
│
├── persona/
│   ├── PersonaProfile.java        ← Persona 数据模型（新）
│   ├── PersonaProfileManager.java ← Persona CRUD（新，扩展 PersonaManager）
│   └── PersonaManager.java        ← 保留，增加 create/update/delete
│
├── tool/
│   ├── MineClawdTool.java         ← 接口不变
│   ├── ToolRegistry.java          ← 增强：支持 per-agent 查询
│   ├── ToolBinding.java           ← 新增：Agent ↔ Tool 绑定关系
│   └── ToolBindingManager.java    ← 新增：绑定持久化管理
│
├── prompt/
│   ├── PromptModule.java          ← 新增：模块化提示词接口
│   ├── PromptPipeline.java        ← 新增：提示词流水线
│   ├── BuiltInPromptModules.java  ← 新增：内置模块实现
│   └── (RevealedToolPromptSystem.java ← 保持，内部适配)
│
├── mcp/                           ← 新增：MCP 支持（接口层，为未来预留）
│   ├── McpServerProfile.java
│   └── McpIntegrationManager.java
│
└── chat/
    ├── ChatRole.java              ← 扩展：支持动态 Agent 角色
    └── ChatRoleManager.java       ← 增强：支持 agent 驱动的角色注册
```

---

## 3. 阶段实施计划

### 第一阶段：数据模型重构（Foundation Layer）

#### 3.1 AgentProfile — 新 Agent 数据模型

```java
public class AgentProfile {
    String id;                    // 唯一标识符
    String name;                  // 显示名称
    String emoji;                 // Emoji 头像
    String description;           // 简短描述
    String personaId;             // 关联 Persona ID
    
    // 提示词（多种来源）
    PromptSource promptSource;    // FILE 或 CUSTOM
    String customPrompt;          // 用户自定义提示词
    
    // 工具绑定
    ToolBindingMode toolBindingMode; // ALL / WHITELIST / BLACKLIST
    Set<String> boundToolIds;     // 绑定的工具 ID
    
    // MCP（预留）
    Set<String> mcpServerIds;     // 关联的 MCP 服务器
    
    // 元数据
    long createdAt;
    long updatedAt;
}
```

**写入格式** (`mineclawd/agents/<id>/profile.json`):
```json
{
  "id": "my-agent",
  "name": "My Agent",
  "emoji": "🧙",
  "description": "A custom agent",
  "personaId": "yuki",
  "prompt": {
    "source": "CUSTOM",
    "customPrompt": "You are a helpful wizard..."
  },
  "tools": {
    "mode": "WHITELIST",
    "ids": ["execute-command", "search", "read-files"]
  },
  "mcpServers": [],
  "createdAt": 1714567890000,
  "updatedAt": 1714567890000
}
```

#### 3.2 PersonaProfile — 新 Persona 数据模型

```java
public class PersonaProfile {
    String id;
    String name;
    String emoji;
    String description;
    String content;       // Markdown 内容
    long createdAt;
    long updatedAt;
}
```

**写入格式** (`mineclawd/personas/<id>/profile.json` + `<id>.md`):
- `profile.json`: 元数据（name, emoji, description）
- `<id>.md`: Persona 提示词内容

#### 3.3 ToolBinding — 工具绑定管理

```java
public class ToolBinding {
    String agentId;
    ToolBindingMode mode;  // ALL, WHITELIST, BLACKLIST
    Set<String> toolIds;
}
```

持久化：存储在 Agent 的 `profile.json` 中，不单独文件。

#### 3.4 PromptModule — 模块化提示词

```java
public interface PromptModule {
    String getId();
    String buildPrompt(AgentProfile agent, PersonaProfile persona, 
                       Map<String, MineClawdTool> tools, 
                       SessionData session);
    int getPriority();  // 排序优先级
}
```

内置模块：
1. `BasePromptModule` — Agent 基础提示词
2. `PersonaPromptModule` — Persona 上下文
3. `ToolPromptModule` — 工具列表 + 使用指引
4. `EnvironmentPromptModule` — 环境信息
5. `SessionPromptModule` — 会话上下文

---

### 第二阶段：服务层实现

#### 3.5 AgentProfileManager

```
listAgents()          → List<AgentProfile>
getAgent(id)          → AgentProfile
createAgent(profile)  → AgentProfile
updateAgent(profile)  → AgentProfile
deleteAgent(id)       → void
getActiveAgent(owner) → AgentProfile (替代 loadActiveAgent)
setActiveAgent(owner, id) → void
```

#### 3.6 PersonaProfileManager

```
listPersonas()         → List<PersonaProfile>
createPersona(profile) → PersonaProfile
updatePersona(profile) → PersonaProfile
deletePersona(id)      → void
```

#### 3.7 ToolRegistry 增强

```java
// 新增方法
getToolsForAgent(agentId) → Map<String, MineClawdTool>
// 根据 Agent 的 ToolBinding 过滤
```

#### 3.8 PromptPipeline

```java
public class PromptPipeline {
    List<PromptModule> modules;
    
    String build(AgentProfile agent, ...) {
        // 按优先级排序所有模块
        // 依次调用 buildPrompt() 拼接
    }
    
    void registerModule(PromptModule module);
    void unregisterModule(String id);
}
```

---

### 第三阶段：GUI 层

#### 3.9 Agent 编辑界面

**侧边栏（Tab 2 — Agents）**：
```
Agent List Header [+ 新建按钮]
┌─────────────────────┐
│ 🧙 My Agent         │  ← 卡片：emoji + name + description
│ A custom agent...   │
├─────────────────────┤
│ 🤖 Default Agent    │
│ System default      │
├─────────────────────┤
│ 🧪 Test Agent       │
│ For testing         │
└─────────────────────┘
```

右键菜单：复制 / 删除

**主内容区 — 编辑表单**：
```
┌─────────────────────────────────┐
│ Agent Editor                     │
│                                  │
│  Emoji: [🧙]  Name: [My Agent]  │
│  Description: [A custom agent]   │
│                                  │
│ ── Persona ───────────────────── │
│  [yuki ▼]                        │
│                                  │
│ ── System Prompt ─────────────── │
│  ○ File-based (base.md)          │
│  ● Custom text                   │
│  ┌─────────────────────────┐     │
│  │ You are a helpful...    │     │
│  │ (多行编辑框)             │     │
│  └─────────────────────────┘     │
│                                  │
│ ── Available Tools ──────────── │
│  ☑ execute-command   [KubeJS]   │
│  ☑ search            [Web]      │
│  ☐ read-files        [Files]    │
│  ☐ write-files       [Files]    │
│  ☑ apply-script      [KubeJS]   │
│                                  │
│ ──────────────────────────────── │
│  [Save]  [Cancel]                │
└─────────────────────────────────┘
```

#### 3.10 ChatRole 动态注册

Agent 激活时，向 `ChatRoleManager` 注册一个动态角色：
```java
ChatRoleConfig agentRole = new ChatRoleConfig(
    "agent_" + agent.id,
    ChatRole.ASSISTANT,       // 基类
    agent.emoji,              // 自定义 emoji
    "#F3E5F5",                // 气泡颜色
    Alignment.LEFT,
    agent.name,               // 显示名称
    ""
);
ChatRoleManager.getInstance().registerRole(agentRole);
```

这样 Agent 在聊天中显示自定义 emoji 和名称。

---

## 4. 文件清单

### 新增文件

| # | 路径 | 用途 |
|---|------|------|
| 1 | `foundation/agent/AgentProfile.java` | Agent 完整数据模型 |
| 2 | `foundation/agent/AgentProfileManager.java` | Agent CRUD + 持久化 |
| 3 | `foundation/persona/PersonaProfile.java` | Persona 数据模型 |
| 4 | `foundation/persona/PersonaProfileManager.java` | Persona CRUD + 持久化 |
| 5 | `foundation/tool/ToolBindingManager.java` | Agent↔Tool 绑定管理 |
| 6 | `foundation/prompt/PromptModule.java` | 提示词模块接口 |
| 7 | `foundation/prompt/PromptPipeline.java` | 提示词流水线引擎 |
| 8 | `foundation/prompt/BuiltInPromptModules.java` | 内置模块实现 |
| 9 | `foundation/mcp/McpServerProfile.java` | MCP 服务器数据模型 |
| 10 | `foundation/mcp/McpIntegrationManager.java` | MCP 管理器接口 |
| 11 | `foundation/client/ui/AgentEditFragment.java` | Agent 编辑 Fragment |
| 12 | `foundation/client/ui/AgentCardView.java` | Agent 卡片组件 |

### 修改文件

| # | 路径 | 改动 |
|---|------|------|
| 1 | `foundation/agent/AgentManager.java` | 标记 @Deprecated，委托新实现 |
| 2 | `foundation/persona/PersonaManager.java` | 增加 create/update/delete |
| 3 | `foundation/tool/ToolRegistry.java` | 增加 per-agent 过滤方法 |
| 4 | `foundation/chat/ChatRoleManager.java` | 增强动态角色注册 |
| 5 | `foundation/chat/ChatRole.java` | 无变更（已够用） |
| 6 | `ModernHudFragment.java` | Tab 2 接入 Agent 编辑界面 |
| 7 | `MineClawd.java` | 适配新 Agent/Persona API |

---

## 5. 向后兼容策略

1. **AgentManager** 标记 `@Deprecated`，`loadActiveAgent()` 内部从 `AgentProfileManager` 获取并转换为旧 `Agent` record
2. **PersonaManager** 增加新方法，旧方法保持签名不变
3. **ToolRegistry** 增加新方法，旧方法行为不变
4. **`mineclawd/agents/<name>`** 旧目录结构仍可读取，但写入时使用新 `profile.json` 格式
5. **`mineclawd/souls/<name>.md`** 旧格式仍可读取，新创建的使用 `personas/<id>/` 目录结构

---

## 6. MCP 集成预留

```
MCP (Model Context Protocol) 接口定义：

McpServerProfile {
    String id;
    String name;
    String url;          // MCP Server 端点
    String apiKey;       // 认证密钥
    List<String> tools;  // 该 MCP 暴露的工具
    boolean enabled;
}

mcpServers 目录：mineclawd/mcp/<id>/profile.json
```

Agent 的 `mcpServerIds` 字段引用启用的 MCP 服务器。
此阶段**仅定义接口**，不实现实际的 MCP 协议连接。

---

## 7. 实施顺序

```
Week 1: 数据模型 + Manager 重构
  Day 1-2: AgentProfile + AgentProfileManager
  Day 3:   PersonaProfile + PersonaProfileManager  
  Day 4:   ToolBindingManager + ToolRegistry 增强
  Day 5:   测试 + 修复兼容性

Week 2: Prompt 系统重构
  Day 1-2: PromptModule 接口 + PromptPipeline
  Day 3:   BuiltInPromptModules 实现
  Day 4:   集成到 MineClawd.java
  Day 5:   测试 + 修复

Week 3: GUI 实现
  Day 1-2: AgentEditorFragment 基础布局
  Day 3-4: 表单逻辑 + Tool 勾选
  Day 5:   侧边栏 Agent 卡片列表

Week 4: 集成 + 测试
  Day 1-2: 集成到 ModernHudFragment Tab 2
  Day 3:   MCP 接口预留
  Day 4-5: 全面测试 + Bug 修复
```

---

## 8. 风险与缓解

| 风险 | 缓解 |
|------|------|
| AgentProfile改变影响LLM prompt输出 | PromptPipeline 抽象隔离变更 |
| 旧数据迁移 | 读取时兼容旧格式，写入时新格式 |
| GUI复杂性高 | 分步实现，先基础编辑后高级功能 |
| 与现有MineClawd.java耦合 | 通过 Service 层解耦 |
