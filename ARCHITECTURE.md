# MineClawd Agent 架构说明

## 📋 目录

- [架构概览](#架构概览)
- [核心概念](#核心概念)
- [架构层级](#架构层级)
- [各层职责](#各层职责)
- [数据流向](#数据流向)
- [架构优势](#架构优势)
- [Skills vs Workflow 路由](#skills-vs-workflow-路由)
- [重构计划](#重构计划)

---

## 🏗️ 架构概览

```
┌─────────────────────────────────────────────────────────────────────┐
│ 第0层：LLM API                                                      │
│ (OpenAI API, Vertex AI API)                                        │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ HTTP 请求/响应
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 第1层：LLM Client                                                   │
│ (OpenAIClient, VertexAIClient)                                     │
│ • HTTP 通信                                                         │
│ • 请求/响应序列化                                                  │
│ • 错误处理                                                          │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ JSON 请求/响应
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 第2层：AgentProtocolHandler (JSON 构建和解析)                       │
│ • 构建 JSON 请求体                                                  │
│ • 解析 JSON 响应体                                                  │
│ • 调用 Agent 获取具体信息                                           │
│ • 调用 Prompt 构建器提供摘要信息                                    │
│ • 构建完整的 JSON (包含具体信息)                                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ 请求具体信息 + 调用 Prompt 构建器
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 第3层：Agent 层 (协调者)                                            │
│ • 协调 LLM, Memory, Skills/Workflow/Tool                            │
│ • 管理 Memory (会话记忆)                                            │
│ • 调用第四层的方法获取具体信息                                      │
│ • 决定使用哪个 Skills/Workflow/Tool                                 │
│ • 用户确认 Workflow 后才开始调用 Tools                              │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ 调用方法
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 第3.5层：统一接口层 (新增)                                          │
│ • Skills/Workflow/Tool 统一接口                                     │
│   ├─ SkillInterface                                                 │
│   ├─ WorkflowInterface                                              │
│   └─ ToolInterface                                                  │
│ • 统一的调用方法                                                    │
│   ├─ execute()                                                      │
│   ├─ getSpecificInfo()                                              │
│   ├─ getContext()                                                   │
│   └─ getPrompt()                                                    │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ 调用方法
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 第4层：具体实现层 (第四层)                                          │
│ • Skills 具体实现 (Skills = Workflow + 配置)                       │
│   ├─ SkillRegistry                                                  │
│   ├─ SkillProvider                                                  │
│   └─ Skill 具体实现 (基于 Workflow)                                 │
│ • Workflow 具体实现 (Workflows = 动态工作流)                       │
│   ├─ WorkflowRegistry                                               │
│   ├─ WorkflowProvider                                               │
│   └─ Workflow 具体实现                                              │
│ • Tool 具体实现 (Tools = 可执行的操作)                              │
│   ├─ ToolRegistry                                                   │
│   ├─ ToolProvider                                                   │
│   ├─ Tool 具体实现                                                  │
│   ├─ FunctionCall 具体实现                                          │
│   └─ MCP 具体实现                                                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ 数据对象
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 第5层：素材源 (最高层)                                              │
│ • Player (玩家)                                                     │
│ • Session (会话)                                                    │
│ • Persona (角色)                                                    │
│ • Assets (资产)                                                     │
│ • 被 Tool 操作的对象层                                             │
│   ├─ Filesystem                                                    │
│   ├─ MinecraftWorld                                                │
│   └─ ...                                                           │
│ • 具体的资产这些有形的东西                                          │
│   ├─ Files                                                         │
│   ├─ Directories                                                   │
│   └─ ...                                                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 核心概念

### **Function Calling (函数调用)**

- **本质**：LLM 的原生能力
- **作用**：让模型自主判断是否需要调用外部函数
- **特点**：生成结构化的函数调用请求
- **关系**：**基础**，后面两个能够存在的基础

### **MCP (Model Context Protocol)**

- **本质**：由 Anthropic 推动的开放标准
- **作用**：为 LLM 应用提供标准化接口
- **特点**：统一工具与数据接入，分布式协作
- **关系**：Function Calling 的**标准化扩展**

### **Skills (技能)**

- **本质**：动态的工作流生成器
- **组成**：ToolList + 生成规则
- **特点**：
  - 动态的工作流生成器
  - LLM 决定 Workflow
  - 用户确认 Workflow 后才开始调用 Tools
  - 用户可以编辑固化 Workflow
- **关系**：Skills 让 LLM 动态生成 Workflow

### **Workflow (工作流)**

- **本质**：固化的工作流模板
- **作用**：预定义的流程，直接调用 Tools
- **特点**：
  - 固化的工作流模板
  - 预定义的流程
  - 直接调用 Tools
  - 不需要用户确认

### **Tool (工具)**

- **本质**：可执行的操作
- **作用**：让 LLM "改变"外部状态
- **特点**：name + description + parameters
- **关系**：Workflow 的**底层实现**，Workflow 可以调用多个 Tool

### **Agent (智能体)**

- **本质**：协调者
- **作用**：协调 LLM, Memory, Skills/Workflow/Tool
- **特点**：
  - 不执行具体的业务逻辑
  - 负责协调 LLM, Memory, Skills/Workflow/Tool
  - 决定使用哪个 Skills/Workflow/Tool
  - 用户确认 Workflow 后才开始调用 Tools
  - 管理 Memory (会话记忆)

---

## 🏗️ 架构层级

### **第0层：LLM API**

- **职责**：实际的 LLM 服务
- **组件**：
  - OpenAI API
  - Vertex AI API
- **特点**：
  - HTTP 通信
  - 接收 JSON 请求
  - 返回 JSON 响应

### **第1层：LLM Client**

- **职责**：HTTP 通信
- **组件**：
  - OpenAIClient
  - VertexAIClient
- **特点**：
  - HTTP 通信
  - 请求/响应序列化
  - 错误处理

### **第2层：AgentProtocolHandler (JSON 构建和解析)**

- **职责**：JSON 构建和解析
- **组件**：
  - AgentProtocolHandler
- **特点**：
  - 构建 JSON 请求
  - 解析 JSON 响应
  - 调用 Agent 获取具体信息
  - 调用 Prompt 构建器提供摘要信息
  - 不包含业务逻辑
  - 不包含 prompt 构建逻辑

### **第3层：Agent 层 (协调者)**

- **职责**：协调 LLM, Memory, Skills/Workflow/Tool
- **组件**：
  - Agent
  - MemoryManager
- **特点**：
  - 协调 LLM, Memory, Skills/Workflow/Tool
  - 管理 Memory (会话记忆)
  - 决定使用哪个 Skills/Workflow/Tool
  - 调用第四层的方法获取具体信息

### **第3.5层：统一接口层 (新增)**

- **职责**：提供统一的接口
- **组件**：
  - SkillInterface
  - WorkflowInterface
  - ToolInterface
- **特点**：
  - Skills/Workflow/Tool 都实现统一接口
  - 统一的调用方法：execute(), getSpecificInfo(), getContext(), getPrompt()
  - 横向划分：Skills, Workflow, Tool 可以横向划分

### **第4层：具体实现层 (第四层)**

- **职责**：具体的实现
- **组件**：
  - Skills 具体实现
  - Workflow 具体实现
  - Tool 具体实现
- **特点**：
  - 具体的实现
  - 提供具体的方法
  - 被 Agent 层调用

### **第5层：素材源 (最高层)**

- **职责**：数据源
- **组件**：
  - Player
  - Session
  - Persona
  - Assets
  - 被 Tool 操作的对象层
  - 具体的资产这些有形的东西
- **特点**：
  - 数据源，不包含逻辑
  - 可以动态变化
  - 包含被 Tool 操作的对象层
  - 包含具体的资产这些有形的东西

---

## 📊 各层职责

### **第5层：素材源 (最高层)**

| 组件 | 作用 | 数据类型 |
|------|------|----------|
| Player | 玩家信息 | UUID, Name, Permissions |
| Session | 会话状态 | History, Context, Metadata |
| Persona | 角色设定 | Name, Description, Personality |
| Assets | 资产信息 | Name, Path, Content, Metadata |
| 被 Tool 操作的对象层 | 被 Tool 操作的对象 | Filesystem, MinecraftWorld, ... |
| 具体的资产这些有形的东西 | 具体的资产 | Files, Directories, ... |

### **第4层：具体实现层 (第四层)**

| 组件 | 作用 | 说明 |
|------|------|------|
| **Skills 具体实现** | **动态的工作流生成器** | ToolList + 生成规则，LLM 决定 Workflow |
| **Workflow 具体实现** | **固化的工作流模板** | 预定义的流程，直接调用 Tools |
| **Tool 具体实现** | **可执行的操作** | name + description + parameters |

### **第3.5层：统一接口层 (新增)**

| 组件 | 作用 | 说明 |
|------|------|------|
| **Skills/Workflow/Tool 统一接口** | **统一的接口** | Skills, Workflow, Tool 都实现统一接口 |
| **统一的调用方法** | **统一的调用方法** | execute(), getSpecificInfo(), getContext(), getPrompt() |

### **第3层：Agent 层 (协调者)**

| 功能 | 说明 |
|------|------|
| **协调 LLM, Memory, Skills/Workflow/Tool** | 负责对 LLM, Memory, Skills/Workflow/Tool 进行调用 |
| **管理 Memory (会话记忆)** | Memory 是会话记忆，由 Agent 层管理 |
| **决定使用哪个 Skills/Workflow/Tool** | 决定使用哪个 Skills/Workflow/Tool |
| **用户确认 Workflow 后才开始调用 Tools** | 用户确认 Workflow 后才开始调用 Tools |
| **调用第四层的方法获取具体信息** | 调用第四层的 Skills/Workflow、Tool 等 |

### **第2层：AgentProtocolHandler (JSON 构建和解析)**

| 功能 | 说明 |
|------|------|
| **构建 JSON 请求** | 构建 messages 和 skills/workflows/tools 数组 |
| **解析 JSON 响应** | 解析 text 和 tool_calls |
| **调用 Agent 获取具体信息** | 调用 Agent 获取具体信息 |
| **调用 Prompt 构建器提供摘要信息** | Prompt 构建器提供摘要信息 |
| **构建完整的 JSON (包含具体信息)** | 构建完整的 JSON |

### **第1层：LLM Client**

| 功能 | 说明 |
|------|------|
| **HTTP 通信** | 发送请求，接收响应 |
| **错误处理** | 处理网络错误、超时等 |

### **第0层：LLM API**

| 功能 | 说明 |
|------|------|
| **实际的 LLM 服务** | OpenAI API, Vertex AI API |

---

## 🔄 数据流向

```
┌─────────────────────────────────────────────────────────────────────┐
│ 数据流：从上到下                                                    │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│ 第5层：素材源                                                       │
└─────────────────────────────────────────────────────────────────────┘
Player, Session, Persona, Assets, 被 Tool 操作的对象层, 具体的资产这些有形的东西
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 第4层：具体实现层 (第四层)                                          │
└─────────────────────────────────────────────────────────────────────┘
1. Skills 具体实现 (Skills = Agent 可调用的可执行能力模块)
   ├─ SkillRegistry.getSkill("code-review") → CodeReviewSkill
   └─ CodeReviewSkill.execute() → 执行 Skill (instructions + scripts + resources)
2. Workflow 具体实现 (Workflows = 预定义流程)
   ├─ WorkflowRegistry.getWorkflow("deploy") → DeployWorkflow
   └─ DeployWorkflow.execute() → 调用多个 Skill 完成任务
3. Tool 具体实现 (Tools = 可执行的操作)
   ├─ ToolRegistry.getTool("read-files") → ReadFilesTool
   ├─ FunctionCall 具体实现 → 调用外部函数
   └─ MCP 具体实现 → 调用外部系统
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 第3.5层：统一接口层 (新增)                                          │
└─────────────────────────────────────────────────────────────────────┘
1. Skills/Workflow/Tool 统一接口
   ├─ SkillInterface.execute()
   ├─ WorkflowInterface.execute()
   └─ ToolInterface.execute()
2. 统一的调用方法
   ├─ getSpecificInfo()
   ├─ getContext()
   └─ getPrompt()
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 第3层：Agent 层 (协调者)                                            │
└─────────────────────────────────────────────────────────────────────┘
1. 协调 LLM, Memory, Skills/Workflow/Tool
2. 管理 Memory (会话记忆)
3. 决定使用哪个 Skills/Workflow/Tool
4. 调用第四层的方法获取具体信息
   ├─ skillInterface.getSpecificInfo()
   ├─ workflowInterface.getSpecificInfo()
   └─ toolInterface.getSpecificInfo()
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 第2层：AgentProtocolHandler (JSON 构建和解析)                       │
└─────────────────────────────────────────────────────────────────────┘
1. 调用 Agent 获取具体信息
   ├─ agent.getSpecificSkillInfo(skills)
   ├─ agent.getSpecificWorkflowInfo(workflows)
   ├─ agent.getSpecificToolInfo(tools)
   └─ agent.getContext(promptType)
2. 调用 Prompt 构建器提供摘要信息
   └─ promptBuilder.buildMessages(...)
3. 构建 JSON 请求
   ├─ buildOpenAiRequest(model, messages, skills/workflows/tools, stream, agent)
   └─ 返回: {model, messages, tools}
4. 解析 JSON 响应
   ├─ parseOpenAiResponse(responseBody, agent)
   └─ 返回: {text, toolCalls}
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 第1层：LLM Client                                                   │
└─────────────────────────────────────────────────────────────────────┘
HTTP POST /v1/chat/completions
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 第0层：LLM API                                                      │
└─────────────────────────────────────────────────────────────────────┘
响应: {choices: [{message: {tool_calls: [...]}}]}
```

---

## 🎯 架构优势

### **1. 层级划分更清晰**

- **第5层**：数据源
- **第4层**：具体的实现
- **第3.5层**：统一的接口
- **第3层**：协调者
- **第2层**：JSON 构建和解析
- **第1层**：HTTP 通信
- **第0层**：实际的 LLM 服务

### **2. 职责更明确**

- **第3层**：协调者，只负责协调
- **第3.5层**：统一的接口，只负责提供统一的接口
- **第4层**：具体的实现，只负责具体的实现
- **第2层**：JSON 构建和解析，只负责 JSON 构建和解析

### **3. Skills 和 Workflow 统一接口**

- **Skills 和 Workflow 可以统一接口**
- **具体的调用逻辑在 Agent 里面对 Skills 的配置**
- **Skills 和 Workflow 只需要一次**
- **层级内可以做横向划分**

### **4. Memory 位置更合理**

- **Memory 放在第3层**，由 Agent 层管理

### **5. Prompt 构建器位置更合理**

- **Prompt 构建器放在第2层**，由 Handler 调用

### **6. 统一接口层**

- **Skills/Workflow/Tool 都实现统一接口**
- **统一的调用方法**：execute(), getSpecificInfo(), getContext(), getPrompt()

### **7. 易于扩展**

- **添加新功能只需修改第4层**
- **第3.5层 (统一接口层)** 不需要修改
- **第3层 (Agent 层)** 不需要修改
- **第2层 (Handler)** 不需要修改
- **第1层 (LLM Client)** 不需要修改

### **8. 易于测试**

- **可以独立测试每一层**
- **Mock 第4层即可测试第3.5层 (统一接口层)**
- **Mock 第3.5层即可测试第3层 (Agent 层)**
- **Mock 第3层即可测试第2层 (Handler)**
- **Mock LLM Client 即可测试 Handler**

---

## 🔄 重构计划

### **阶段1：统一接口层**

- [ ] 创建统一接口层 (第3.5层)
- [ ] 定义 SkillInterface, WorkflowInterface, ToolInterface
- [ ] 实现统一的调用方法：execute(), getSpecificInfo(), getContext(), getPrompt()

### **阶段2：重构具体实现层**

- [ ] 重构 Skills 具体实现
- [ ] 重构 Workflow 具体实现
- [ ] 重构 Tool 具体实现
- [ ] 实现统一接口

### **阶段3：重构 Agent 层**

- [ ] 重构 Agent 层，协调 LLM, Memory, Skills/Workflow/Tool
- [ ] 管理 Memory (会话记忆)
- [ ] 决定使用哪个 Skills/Workflow/Tool
- [ ] 调用第四层的方法获取具体信息

### **阶段4：重构 Handler**

- [ ] 重构 Handler，JSON 构建和解析
- [ ] 调用 Agent 获取具体信息
- [ ] 调用 Prompt 构建器提供摘要信息
- [ ] 构建完整的 JSON (包含具体信息)

### **阶段5：重构 LLM Client**

- [ ] 重构 LLM Client，HTTP 通信
- [ ] 请求/响应序列化
- [ ] 错误处理

---

## 🎯 Skills vs Workflow 路由

### **路由原则**

Skills 和 Workflow 在代码调用上是有逻辑封装关系的，但在路由上是并列的。优先级如下：

1. **优先匹配固化 Workflow**：如果已经匹配到有固化的 Workflow，就直接拿来用
2. **走 Skills 流程**：如果没有匹配到固化 Workflow，就走 Skills 流程
3. **LLM 决定 Workflow**：Skills 让 LLM 动态生成 Workflow
4. **用户确认 Workflow**：让用户确认 Workflow
5. **开始调用 Tools**：确认之后再走 Workflow，调用 Tools

### **路由流程图**

```
┌─────────────────────────────────────────────────────────────────────┐
│ 用户输入                                                            │
└─────────────────────────────────────────────────────────────────────┘
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ Agent 决定使用哪个 Skills/Workflow/Tool                             │
└─────────────────────────────────────────────────────────────────────┘
   ↓
┌─────────────────────────────────────────────────────────────────────┐
│ 是否匹配到固化 Workflow？                                            │
└─────────────────────────────────────────────────────────────────────┘
   ├─ 是 → 直接使用固化 Workflow
   │      ↓
   │   开始调用 Tools
   │
   └─ 否 → 走 Skills 流程
          ↓
      查询 ToolList
          ↓
      生成 Workflow
          ↓
      用户确认 Workflow
          ↓
      开始调用 Tools
```

### **场景 1：使用固化 Workflow**

**场景描述**：用户执行一个已知的、固定的流程

**示例**：
- 用户输入："部署项目"
- 系统匹配到固化 Workflow："deploy"
- 直接使用固化 Workflow
- 开始调用 Tools

**流程**：
1. 用户输入："部署项目"
2. Agent 匹配到固化 Workflow："deploy"
3. 直接使用固化 Workflow
4. Workflow 调用 Tools：read-files, write-files, execute-command
5. 开始执行部署流程

**特点**：
- 固化 Workflow 已经预定义好
- 不需要用户确认
- 直接开始调用 Tools

### **场景 2**：使用 Skills 生成 Workflow

**场景描述**：用户执行一个未知的、需要 LLM 动态生成的流程

**示例**：
- 用户输入："帮我分析这个项目的代码质量"
- 系统没有匹配到固化 Workflow
- 走 Skills 流程，LLM 决定 Workflow
- 用户确认 Workflow
- 开始调用 Tools

**流程**：
1. 用户输入："帮我分析这个项目的代码质量"
2. Agent 没有匹配到固化 Workflow
3. 走 Skills 流程，LLM 决定 Workflow
4. LLM 决定 Workflow：read-files → analyze-code → write-files
5. 用户确认 Workflow
6. 开始调用 Tools

**特点**：
- LLM 动态生成 Workflow
- 需要用户确认
- 灵活，可以根据用户需求调整

### **场景 3：用户编辑固化 Workflow**

**场景描述**：用户对固化 Workflow 进行编辑，然后固化保存

**示例**：
- 用户输入："部署项目"
- 系统匹配到固化 Workflow："deploy"
- 用户对固化 Workflow 进行编辑
- 用户固化保存 Workflow
- 开始调用 Tools

**流程**：
1. 用户输入："部署项目"
2. Agent 匹配到固化 Workflow："deploy"
3. 用户对固化 Workflow 进行编辑
4. 用户固化保存 Workflow
5. 开始调用 Tools

**特点**：
- 用户可以编辑固化 Workflow
- 用户可以固化保存 Workflow
- 灵活，可以根据用户需求调整

### **场景 4**：用户编辑 Skills 生成的 Workflow

**场景描述**：用户对 Skills 生成的 Workflow 进行编辑，然后固化保存

**示例**：
- 用户输入："帮我分析这个项目的代码质量"
- 系统没有匹配到固化 Workflow
- 走 Skills 流程，LLM 决定 Workflow
- 用户对 Workflow 进行编辑
- 用户固化保存 Workflow
- 开始调用 Tools

**流程**：
1. 用户输入："帮我分析这个项目的代码质量"
2. Agent 没有匹配到固化 Workflow
3. 走 Skills 流程，LLM 决定 Workflow
4. LLM 决定 Workflow：read-files → analyze-code → write-files
5. 用户对 Workflow 进行编辑
6. 用户固化保存 Workflow
7. 开始调用 Tools

**特点**：
- 用户可以编辑 Skills 生成的 Workflow
- 用户可以固化保存 Workflow
- 灵活，可以根据用户需求调整

### **场景 5**：用户取消 Workflow

**场景描述**：用户取消 Workflow，不开始调用 Tools

**示例**：
- 用户输入："帮我分析这个项目的代码质量"
- 系统没有匹配到固化 Workflow
- 走 Skills 流程，LLM 决定 Workflow
- 用户取消 Workflow
- 不开始调用 Tools

**流程**：
1. 用户输入："帮我分析这个项目的代码质量"
2. Agent 没有匹配到固化 Workflow
3. 走 Skills 流程，LLM 决定 Workflow
4. LLM 决定 Workflow：read-files → analyze-code → write-files
5. 用户取消 Workflow
6. 不开始调用 Tools

**特点**：
- 用户可以取消 Workflow
- 不开始调用 Tools
- 灵活，可以根据用户需求调整

### **路由总结**

| 场景 | 是否匹配固化 Workflow | 是否需要用户确认 | 是否开始调用 Tools |
|------|---------------------|----------------|------------------|
| 场景 1：使用固化 Workflow | 是 | 否 | 是 |
| 场景 2：使用 Skills 生成 Workflow | 否 | 是 | 用户确认后开始 |
| 场景 3：用户编辑固化 Workflow | 是 | 是 | 用户确认后开始 |
| 场景 4：用户编辑 Skills 生成的 Workflow | 否 | 是 | 用户确认后开始 |
| 场景 5：用户取消 Workflow | 是/否 | 是 | 否 |

---

## 📋 总结

优化后的架构：

1. **第5层 (素材源)**：Player, Session, Persona, Assets, 被 Tool 操作的对象层, 具体的资产这些有形的东西
2. **第4层 (具体实现层)**：**Skills 具体实现, Workflow 具体实现, Tool 具体实现**
3. **第3.5层 (统一接口层)**：**Skills/Workflow/Tool 统一接口**
4. **第3层 (Agent 层)**：**协调者**，协调 LLM, Memory, Skills/Workflow/Tool，管理 Memory
5. **第2层 (Handler)**：JSON 构建和解析，**调用 Agent 获取具体信息**，**调用 Prompt 构建器提供摘要信息**
6. **第1层 (LLM Client)**：HTTP 通信
7. **第0层 (LLM API)**：实际的 LLM 服务

**核心特点**：

- **Skills**：动态的工作流生成器
- **Workflow**：固化的工作流模板
- **统一接口层**：Skills/Workflow/Tool 都实现统一接口
- **Agent**：协调者，协调 LLM, Memory, Skills/Workflow/Tool，管理 Memory
- **Prompt 构建器**：由 Handler 调用，提供摘要信息

**优化后的优势**：

- ✅ 层级划分更清晰
- ✅ 职责更明确
- ✅ Skills 和 Workflow 统一接口
- ✅ Memory 位置更合理
- ✅ Prompt 构建器位置更合理
- ✅ 统一接口层
- ✅ 易于扩展
- ✅ 易于测试
