# MineClawd 智能体编辑器 — 架构重构方案 v2

> 日期: 2026-05-02 | Minecraft: 1.21.1 | Loader: NeoForge | 更新: Prompt 统一模型 + Skills 系统

---

## 1. 当前架构分析

### 1.1 AgentManager (foundation/agent/AgentManager.java)

| 方面 | 当前实现 | 问题 |
|------|----------|------|
| 存储 | `mineclawd/agents/<name>/{base,dynamic_registry,asset_tracking}.md` | 纯文本文件分散，无结构化元数据 |
| 数据结构 | `record Agent(String name, String basePrompt, String dynamicRegistryPrompt, String assetTrackingPrompt)` | 无 emoji、无工具绑定、无 Persona 关联 |
| 列表 | `listAgentNames()` 扫描目录 | 仅返回名称列表 |
| 选择 | `loadActiveAgent(ownerKey)` | 单 agent 活跃模式，文件记录 |

### 1.2 PersonaManager (foundation/persona/PersonaManager.java)

Persona 本质上是**带名称的提示词文本**，以 `.md` 文件存储在 `mineclawd/souls/` 目录。当前只有名称和原始文本，没有结构化元数据。

### 1.3 Tool系统 (foundation/tool/)

| 方面 | 当前实现 |
|------|----------|
| 注册 | `ToolRegistry` 静态 Map + `registerBuiltInTools()` 硬编码 |
| 启用 | `ToolConfig` JSON 文件 (`mineclawd-tools.json`) |
| 架构 | `MineClawdTool` 接口 |

**关键缺陷**：工具不能绑定到特定 Agent。所有 Agent 共享同一工具集。

### 1.4 Prompt系统

Prompt 拼接逻辑深埋在 6600 行的 `MineClawd.java` 中，不可扩展、不可配置。Agent 的提示词、Persona、工具指引、环境信息全部在 `buildSystemPrompt()` 中硬编码拼接。

### 1.5 ChatRole (前端显示)

`ChatRole` 枚举定义了固定角色（USER/ASSISTANT/TOOL），Agent 在聊天中只能显示为固定 🤖 emoji。

---

## 2. 新架构设计

### 2.1 设计原则

1. **模块化**：每个子系统有清晰的接口和边界
2. **可扩展**：通过注册/插件模式支持第三方扩展
3. **统一数据模型**：Persona = Prompt文本，使用统一类型
4. **JSON 持久化**：Agent 用单个 JSON 文件，方便外部访问和控制
5. **MCP Ready**：接口设计预留 MCP Server 集成点
6. **Skills 系统**：Agent 可安装 Skill 模块（工具组合 + 提示词片段）

### 2.2 核心数据模型

```
foundation/
├── agent/
│   ├── AgentProfile.java              ← 智能体数据模型（JSON 持久化）
│   ├── AgentProfileManager.java       ← 智能体 CRUD
│   └── AgentManager.java              ← @Deprecated 委托
│
├── prompt/
│   ├── PromptSource.java              ← 枚举：FILE / CUSTOM_TEXT
│   ├── PromptEntry.java               ← 统一 Prompt 数据类型
│   ├── PromptPipeline.java            ← 提示词流水线
│   └── BuiltInPromptModules.java      ← 内置模块实现
│
├── tool/
│   ├── MineClawdTool.java             ← 接口不变
│   ├── ToolRegistry.java              ← 增强 per-agent 查询
│   └── ToolBinding.java               ← Agent ↔ Tool 绑定
│
├── skill/                             ← 新增：Skills 系统
│   ├── AgentSkill.java                ← Skill 接口
│   ├── SkillManifest.java             ← Skill 清单
│   └── SkillManager.java              ← Skill 管理器
│
├── mcp/                               ← 新增：MCP 预留
│   ├── McpServerProfile.java
│   └── McpIntegrationManager.java
│
└── chat/
    └── ChatRoleManager.java           ← 增强动态角色注册
```

---

## 3. 核心数据模型详细设计

### 3.1 PromptEntry — 统一 Prompt 数据类型

Persona 与 Agent 的提示词使用**同一数据类型**，支持 FILE 和 CUSTOM_TEXT 两种来源。

```java
public class PromptEntry {
    PromptSource source;      // FILE 或 CUSTOM_TEXT
    String filePath;          // 文件路径（source=FILE 时有效）
    String customText;        // 用户输入文本（source=CUSTOM_TEXT 时有效）
    
    // runtime — 最终渲染内容
    String resolvedContent;   // 由 PromptPipeline 填充
}

public enum PromptSource {
    FILE,           // 从文件读取，GUI 中只读
    CUSTOM_TEXT     // 玩家自定义文本，GUI 中可编辑
}
```

**关键行为**：
- `source=FILE` 时：`customText` 保留文件内容副本，但在 GUI 中设为只读；切换回 CUSTOM_TEXT 时恢复可编辑，且之前输入的文本不丢失
- `resolvedContent` 在 PromptPipeline 运行时填充（读取文件内容或使用 customText）

### 3.2 Persona = PromptEntry

Persona **不再是独立实体**，而是 AgentProfile 中的一个 `PromptEntry` 字段：

```java
// 旧 PersonaManager → 简化为 Persona 预设库
// Persona 预设是一个带名称的 PromptEntry 集合，存储在 mineclawd/personas/<id>.json
public class PersonaPreset {
    String id;
    String name;
    String emoji;
    String description;
    PromptEntry prompt;        // 此处无 source 限制，任何类型皆可
}
```

Agent 中的 person 字段直接引用 `personaId`，运行时将 persona 的 prompt 内容并入提示词。

### 3.3 AgentProfile — JSON 持久化

**单个 JSON 文件**，路径：`mineclawd/agents/<id>.json`

```json
{
  "id": "my-agent",
  "name": "My Agent",
  "emoji": "🧙",
  "description": "A custom agent",
  "personaId": "default",
  
  "personaPrompt": {
    "source": "FILE",
    "filePath": "mineclawd/personas/default.md",
    "customText": ""
  },
  
  "prompts": [
    {
      "id": "main",
      "source": "CUSTOM_TEXT",
      "filePath": "",
      "customText": "You are a helpful wizard..."
    },
    {
      "id": "registry",
      "source": "FILE",
      "filePath": "mineclawd/agents/default/dynamic_registry.md",
      "customText": ""
    }
  ],
  
  "tools": {
    "mode": "WHITELIST",
    "ids": ["execute-command", "search", "read-files"]
  },
  
  "skills": ["file-operator", "web-researcher"],
  
  "mcpServers": [],
  
  "createdAt": 1714567890000,
  "updatedAt": 1714567890000
}
```

**AgentProfile.java** 数据类：

```java
public class AgentProfile {
    String id;
    String name;
    String emoji;
    String description;
    String personaId;
    PromptEntry personaPrompt;      // Persona 提示词（关联或内联）
    List<PromptEntry> prompts;      // 多段提示词（可扩展）
    ToolBindingMode toolBindingMode; // ALL / WHITELIST / BLACKLIST
    Set<String> boundToolIds;
    Set<String> skillIds;           // 已安装的 Skill
    Set<String> mcpServerIds;
    long createdAt;
    long updatedAt;
}
```

### 3.4 Skills 系统 — ZIP 归档分发

Skill 是可安装到 Agent 上的**功能模块**，以 **ZIP 归档** 格式分发。这是目前 AI 生态和 Minecraft 模组的通用做法：

| 生态 | 格式 | 说明 |
|------|------|------|
| **KubeJS** | `.zip` 数据包 | 可直接放入 `kubejs/data/` 文件夹加载 |
| **OpenAI ChatGPT** | `.zip` | `/home/oai/skills/` 目录中的 Skills |
| **Claude/Anthropic** | `.zip` | `SKILL.md` + `references/` 结构 |
| **Microsoft Copilot** | `.zip` | `PackageUrl` 指向的 GPT 模板包 |
| **MCP Bundle** | `.mcpb` (ZIP) | `manifest.json` + 可执行服务器 |

#### ZIP 包结构

```
file-operator-v1.0.0.zip
├── skill.json                    ← 必需：清单文件
├── SKILL.md                      ← 必需：主提示词 / 指令
├── references/                   ← 可选：参考文档
│   ├── usage-examples.md
│   └── api-docs.md
├── scripts/                      ← 可选：KubeJS 脚本（放入后自动加载）
│   ├── startup_scripts/
│   ├── server_scripts/
│   └── client_scripts/
└── assets/                       ← 可选：资源文件（纹理等）
    └── textures/
```

#### skill.json 清单

```json
{
  "id": "file-operator",
  "name": "File Operator",
  "description": "Advanced file operations skill for AI agents",
  "version": "1.0.0",
  "author": "MineClawd Team",
  "minModVersion": "2.0.0",
  "promptPriority": 50,
  "promptSnippet": "You have advanced file operations capabilities...",
  "recommendedTools": ["read-files", "write-files", "copy-files", "grep"],
  "dependencies": [],
  "icon": "assets/icon.png"
}
```

#### AgentSkill 接口

```java
public interface AgentSkill {
    String getId();
    String getName();
    String getDescription();
    String getVersion();
    String getPromptSnippet();
    int getPromptPriority();
    Set<String> getRecommendedToolIds();
    
    // ZIP 中的可选内容
    default Path getExtractedPath() { return null; }  // 解压根目录
    default List<Path> getScriptFiles() { return List.of(); }  // KubeJS 脚本
}
```

#### SkillManager — ZIP 安装/卸载流程

```java
public class SkillManager {
    Path skillsRoot;          // mineclawd/skills/         ← ZIP 文件存放处
    Path skillsExtractRoot;   // mineclawd/skills/.extracted/ ← 解压根
    
    // 扫描目录中的 .zip 文件
    List<AgentSkill> listAvailableSkills();
    
    // 安装到指定 Agent
    void installSkill(String agentId, String skillId);
    void uninstallSkill(String agentId, String skillId);
    
    // 列出 Agent 已安装的 Skill
    List<AgentSkill> getInstalledSkills(String agentId);
    
    // ZIP 管理
    AgentSkill installFromZip(Path zipFile);     // 将 ZIP 放入 skills/ 并注册
    boolean validateZip(Path zipFile);            // 校验 ZIP 合法性
    boolean extractSkill(Path zipFile);           // 解压到 .extracted/<id>/
}
```

**安装流程**：
1. 用户将 `file-operator-v1.0.0.zip` 放入 `mineclawd/skills/`
2. `SkillManager` 扫描到新 ZIP，校验 `skill.json` 合法性
3. 解压到 `mineclawd/skills/.extracted/file-operator/`
4. 可选的 `scripts/` 中的 KubeJS 脚本自动复制到 `kubejs/` 对应目录
5. Agent 的 `skillIds` 中引用此 skill id
6. 运行时 PromptPipeline 的 `SkillsPromptModule` 读取 `SKILL.md` 注入提示词

**卸载流程**：
1. 从 Agent 的 `skillIds` 移除引用
2. 删除解压目录
3. 删除 ZIP 文件（可选）

#### Skill 来源

| 来源 | 路径 | 示例 |
|------|------|------|
| 内置 Skill | `mod jar 中打包的资源文件` | `file-operator.zip` |
| 用户手动安装 | 放入 `mineclawd/skills/` 目录 | 下载的 `.zip` 文件 |
| 未来：MCP Server 发布 | 通过 MCP 协议分发 | `.mcpb` 格式 |
| 未来：Skill 市场 | 联机下载 | 仓库/网站托管 |

### 3.5 GUI — Prompt 编辑交互

Prompt 编辑区域设计（Agent 编辑器中每个 PromptEntry 都是一个独立编辑单元）：

```
── Prompt: main ──────────────────────
[ ▼ File-based  ]  ← 下拉菜单选择来源
  ○ File-based (base.md)
  ● Custom text

┌──────────────────────────────────┐
│ [文件模式: 只读显示文件内容...]   │
│ [或]                             │
│ [自定义模式: 可编辑文本框...]      │
└──────────────────────────────────┘
                                     ← 切换来源时文本不丢失
────────────── [Save] [Cancel] ────── ← 仅保存当前 Prompt
```

**交互逻辑**：
1. 下拉选择 `File-based` → 文本框显示文件内容，**只读**（`setEnabled(false)`）
2. 下拉选择 `Custom text` → 文本框可编辑，显示之前输入的文本（切换时**不丢失**）
3. 底部 Save/Cancel 按钮仅对当前 Prompt 生效，不影响 Agent 其他字段

### 3.6 AgentProfileManager — JSON 持久化

```java
public class AgentProfileManager {
    Path agentsRoot;  // mineclawd/agents/
    
    List<AgentProfile> listAgents();
    AgentProfile getAgent(String id);
    AgentProfile createAgent(AgentProfile profile);
    AgentProfile updateAgent(AgentProfile profile);
    void deleteAgent(String id);
    
    AgentProfile getActiveAgent(String ownerKey);
    void setActiveAgent(String ownerKey, String agentId);
}
```

文件结构：
```
mineclawd/
├── agents/
│   ├── default.json
│   ├── my-agent.json
│   └── .active/
│       └── <owner>.txt
├── personas/
│   ├── default.json
│   ├── yuki.json
│   └── (对应的 .md 文件在指定路径)
├── skills/
│   ├── file-operator-v1.0.0.zip      ← ZIP 归档
│   ├── web-researcher-v2.1.0.zip
│   └── .extracted/                   ← 自动解压缓存
│       ├── file-operator/
│       │   ├── skill.json
│       │   ├── SKILL.md
│       │   └── references/
│       └── web-researcher/
│           └── ...
└── mcp/
    └── ...
```

### 3.7 PromptPipeline

```java
public class PromptPipeline {
    List<PromptModule> modules;
    
    String build(AgentProfile agent, SessionData session, ...) {
        // 1. 解析所有 PromptEntry（读取文件 / 使用 customText）
        // 2. 按优先级排序 PromptModule
        // 3. 依次调用 buildPrompt() 拼接
        // 4. 注入环境变量、会话上下文
    }
    
    void registerModule(PromptModule module);
    void unregisterModule(String id);
}
```

内置 PromptModule：
1. `SystemPromptModule` — Agent 的系统级指示（来自 `prompts` 列表）
2. `PersonaPromptModule` — Persona 上下文（来自 `personaPrompt`）
3. `ToolPromptModule` — 工具列表（根据 `toolBindingMode` 过滤）
4. `SkillsPromptModule` — 已安装 Skills 的提示词片段
5. `EnvironmentPromptModule` — 环境/版本信息
6. `SessionPromptModule` — 会话上下文（workspace 路径、session id）
7. `MCPPromptModule` — MCP Server 暴露的工具列表

---

## 4. 文件清单

### 新增文件

| # | 路径 | 用途 |
|---|------|------|
| 1 | `foundation/agent/AgentProfile.java` | Agent 数据模型 |
| 2 | `foundation/agent/AgentProfileManager.java` | Agent JSON CRUD |
| 3 | `foundation/prompt/PromptSource.java` | 来源枚举 |
| 4 | `foundation/prompt/PromptEntry.java` | 统一 Prompt 数据类型 |
| 5 | `foundation/prompt/PromptModule.java` | 提示词模块接口 |
| 6 | `foundation/prompt/PromptPipeline.java` | 提示词流水线 |
| 7 | `foundation/prompt/BuiltInPromptModules.java` | 内置模块实现 |
| 8 | `foundation/tool/ToolBinding.java` | 工具绑定模型 |
| 9 | `foundation/skill/AgentSkill.java` | Skill 接口 |
| 10 | `foundation/skill/SkillManifest.java` | `skill.json` 清单数据模型 |
| 11 | `foundation/skill/SkillManager.java` | ZIP 安装/卸载/校验管理器 |
| 12 | `foundation/mcp/McpServerProfile.java` | MCP 配置模型 |
| 13 | `foundation/mcp/McpIntegrationManager.java` | MCP 管理器 |
| 14 | `foundation/client/ui/AgentEditFragment.java` | Agent 编辑 Fragment |
| 15 | `foundation/client/ui/AgentCardView.java` | Agent 卡片组件 |
| 16 | `foundation/client/ui/PromptEditorView.java` | Prompt 编辑组件（单选+文本框+保存） |

### 修改文件

| # | 路径 | 改动 |
|---|------|------|
| 1 | `foundation/agent/AgentManager.java` | @Deprecated，委托 AgentProfileManager |
| 2 | `foundation/persona/PersonaManager.java` | 简化为 PersonaPreset 管理器 |
| 3 | `foundation/tool/ToolRegistry.java` | 增加 `getToolsForAgent()` |
| 4 | `foundation/chat/ChatRoleManager.java` | 增强动态角色注册 |
| 5 | `foundation/client/ui/framework/ModernHudFragment.java` | Tab 2 接入 Agent 编辑 |
| 6 | `MineClawd.java` | 适配 AgentProfile + PromptPipeline |

---

## 5. GUI — Agent 编辑器原型

### 侧边栏（Tab 2）

```
[💬] [🧙] [📁] [🔧] [📦] [⚙]
      ↑
  当前 Tab

Agent List          [+ 新建]
┌────────────────────┐
│ 🧙 My Agent        │  ← 卡片
│ A custom agent... │
├────────────────────┤
│ 🤖 Default Agent   │
│ System default     │
├────────────────────┤
│ 🧪 Test Agent      │
│ For testing...     │
└────────────────────┘
      右键菜单: [复制] [删除]
```

### 主内容区

```
┌─────────────────────────────────────┐
│ [🧙] Agent Editor                    │
│                                      │
┌─ Basic Info ────────────────────────┐
│ Emoji: [🧙]     Name: [My Agent]   │
│ Description: [A custom agent...]    │
└─────────────────────────────────────┘

┌─ Persona ───────────────────────────┐
│ [default ▼]                         │
│ ┌─────────────────────────────┐     │
│ │ [FILE] Persona content...   │     │
│ │ (只读)                       │     │
│ └─────────────────────────────┘     │
│ ──────────── [Save] [Cancel] ───── │
└─────────────────────────────────────┘

┌─ Prompts ───────────────────────────┐
│ ┌─ main ──────────────────────────┐ │
│ │ [▼ Custom text]                 │ │
│ │ ┌─────────────────────────┐     │ │
│ │ │ You are a helpful...    │     │ │
│ │ │ (可编辑文本)             │     │ │
│ │ └─────────────────────────┘     │ │
│ │ ──────── [Save] [Cancel] ───── │ │
│ └─────────────────────────────────┘ │
│ [+ Add Prompt]                      │
└─────────────────────────────────────┘

┌─ Tools ────────────────────────────┐
│ Mode: [▼ Whitelist]                │
│ ☑ execute-command   [KubeJS]       │
│ ☑ search            [Web]          │
│ ☐ read-files        [Files]        │
│ ☐ write-files       [Files]        │
│ ☑ apply-script      [KubeJS]       │
└─────────────────────────────────────┘

┌─ Skills ───────────────────────────┐
│ [+ Install Skill]  [▼ file-operator│
│                       web-research]│
│ Installed:                         │
│   📦 file-operator v1.0.0  [✕]    │
│   📦 web-researcher v1.0.0 [✕]    │
└─────────────────────────────────────┘

═══════════════════════════════════════
        [Save Agent] [Cancel]
```

---

## 6. 向后兼容策略

| 旧路径 | 新路径 | 兼容方式 |
|--------|--------|----------|
| `mineclawd/agents/<name>/base.md` | `mineclawd/agents/<name>.json` → prompts[0] | AgentProfileManager 读取时自动迁移 |
| `mineclawd/agents/<name>/dynamic_registry.md` | `mineclawd/agents/<name>.json` → prompts[1] | 同上 |
| `mineclawd/souls/<name>.md` | `mineclawd/personas/<name>.json` | PersonaPreset 兼容读取旧 .md |
| `AgentManager.loadActiveAgent()` | `AgentProfileManager.getActiveAgent()` | 委托模式，返回旧 Agent record |
| `PersonaManager.loadActivePersona()` | `AgentProfileManager.getActiveAgent().personaPrompt` | 委托模式 |

---

## 7. 实施顺序

```
Phase 1 — 数据模型层
  PromptSource / PromptEntry           ← 统一 Prompt 类型
  AgentProfile                          ← JSON 数据模型
  ToolBinding                           ← 工具绑定模型

Phase 2 — 服务管理层
  AgentProfileManager                   ← JSON CRUD + 旧格式兼容
  PromptPipeline + PromptModule         ← 提示词流水线
  SkillManager + AgentSkill             ← Skills 系统

Phase 3 — GUI
  PromptEditorView                      ← Prompt 编辑组件（dropdown + textarea + save）
  AgentCardView                         ← 侧边栏卡片
  AgentEditFragment                     ← 主编辑界面

Phase 4 — 集成
  ModernHudFragment Tab 2 接入
  MineClawd.java 适配 PromptPipeline
  ChatRoleManager 动态注册

Phase 5 — 扩展
  MCP 接口定义
  Skill 市场（目录扫描）
  PersonaPreset 管理界面
```

---

## 8. 风险与缓解

| 风险 | 缓解 |
|------|------|
| JSON 迁移破坏旧数据 | 读取时兼容旧目录格式，写入始终新格式 |
| ZIP 安全性（ZIP Slip 攻击等） | `SkillManager.validateZip()` 限制路径穿越，仅提取预设目录结构 |
| Skills 系统过度设计 | 初期只做 ZIP 解压 + Prompt 注入，不做运行时脚本加载 |
| PromptPipeline 影响 LLM 输出质量 | 每个 Module 独立可开关，可降级回旧逻辑 |
| MCP 协议复杂 | 此阶段仅定义数据模型，不实现协议连接 |
