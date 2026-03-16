# MineClawd Agent 架构重构 - 对话上下文总结

## 当前状态（2026-03-16）

### 最近完成的工作（2026-03-16）

**Agent 架构设计完成**

1. **完整架构层级设计**
   - 创建了7层架构体系
   - 定义了各层职责和接口
   - 设计了统一接口层（第3.5层）
   - 完善了数据流向和调用关系

2. **Skills vs Workflow 路由设计**
   - 明确了 Skills 是动态的工作流生成器
   - 明确了 Workflow 是固化的工作流模板
   - 设计了5个场景的路由逻辑
   - 定义了用户确认 Workflow 的流程

3. **架构文档创建**
   - 创建了 `ARCHITECTURE.md` 完整架构说明文档
   - 包含架构概览、核心概念、各层职责
   - 包含 Skills vs Workflow 路由章节
   - 包含重构计划

4. **AgentProtocolHandler 创建**
   - 创建了 `AgentProtocolHandler.java` 类
   - 实现了 JSON 构建和解析功能
   - 提供了 OpenAI 和 Vertex AI 的支持

### 已完成的工作

1. **工具系统初步解耦**
   - 将 OpenAI Tools 和 Vertex Tools 从 MineClawd.java 中解耦
   - 创建了 `tool_sys` 包，包含核心架构类：
     - `ToolDefinition.java` - 工具定义记录类
     - `ToolProvider.java` - 工具提供者接口
     - `ToolRegistry.java` - 工具注册表
     - `ToolFactory.java` - LLM 工具工厂

2. **工具分类重构**
   - 将工具按功能领域分为6个类别：
     - `FileSystemTools.java` - 文件系统工具
     - `NetworkTools.java` - 网络工具
     - `GameCommandTools.java` - 游戏命令工具
     - `DynamicContentTools.java` - 动态内容工具
     - `AssetManagementTools.java` - 资产管理工具
     - `InteractionTools.java` - 交互工具

3. **插件化架构设计**
   - 设计了完整的插件化工具系统架构
   - 创建了 `plugin` 包，包含：
     - `ToolPlugin.java` - 工具插件接口
     - `PluginManager.java` - 插件管理器
     - `ToolExecutor.java` - 工具执行器接口
     - `ToolExecutorManager.java` - 工具执行器管理器
     - `AbstractToolPlugin.java` - 抽象插件基类

4. **API接口规范**
   - 创建了 `api` 包，包含：
     - `MineClawdAPI.java` - 为其他mod提供标准化集成接口

### 当前架构问题

1. **已修复的问题** ✅
   - ~~`ToolRegistry.java`中的`RegistryByteBuf`类路径错误~~ - **已修复**
   - ~~`list-assets`工具schema不符合OpenAI API规范~~ - **已修复**
   - ~~工具验证机制缺失，无效工具会影响LLM功能~~ - **已修复**

2. **待解决的编译错误**
   - `ToolExecutorInitializer.java`中存在类路径引用错误
   - `ToolExecutionResult`类路径需要修正

3. **架构局限性**
   - 当前工具系统仍然存在硬编码依赖
   - 缺乏真正的动态加载机制
   - 与其他mod的联动机制尚未完全实现

## 技术架构设计

### 核心设计理念

```
MineClawd Core
    ↓
Plugin System (插件系统)
    ↓
Tool Registry (工具注册表)
    ↓
Tool Executors (工具执行器)
    ↓
External Mod Integration (外部mod集成)
```

### 关键接口

1. **ToolPlugin接口**
   - 定义插件的生命周期管理
   - 支持依赖关系和冲突检测
   - 提供工具定义注册机制

2. **ToolExecutor接口**
   - 将工具执行逻辑从MineClawd.java中完全解耦
   - 支持参数验证和分类管理
   - 为第三方mod提供标准化执行接口

3. **MineClawdAPI接口**
   - 提供标准化的mod集成接口
   - 支持动态插件注册和卸载
   - 提供工具状态查询功能

## 下一步工作

### 已完成的高优先级工作 ✅
1. **Agent 架构设计**
   - 完整7层架构设计
   - Skills vs Workflow 路由设计
   - 架构文档创建
   - AgentProtocolHandler 创建

2. **Schema错误修复**
   - 修复`list-assets`工具的`properties`字段错误
   - 确保所有工具定义符合OpenAI API规范
   - 修正`RegistryByteBuf`类路径问题

### 当前高优先级
3. **修复编译错误**
   - 修正`ToolExecutorInitializer.java`中的类路径引用
   - 确保插件化架构能够正常编译运行

4. **完善工具执行器**
   - 实现所有内置工具的执行器
   - 确保向后兼容性

5. **动态加载机制**
   - 实现插件的动态注册和卸载
   - 支持运行时工具定义更新

### 中优先级
6. **第三方mod集成示例**
   - 创建示例插件，演示如何集成其他mod
   - 提供详细的开发文档

7. **配置管理**
   - 实现插件配置系统
   - 支持插件的启用/禁用配置

### 低优先级
8. **性能优化**
   - 工具执行的性能监控
   - 插件加载的优化

## 技术挑战

1. **类加载器隔离**
   - 需要解决不同mod之间的类加载器冲突
   - 确保插件系统的稳定性

2. **版本兼容性**
   - 处理不同版本mod之间的兼容性问题
   - 提供版本检测和回退机制

3. **安全性**
   - 确保插件系统的安全性
   - 防止恶意插件的执行

## 对话历史要点

- **用户需求**：实现高度解耦、模块化的工具系统，支持与其他mod的联动
- **技术目标**：类似LangChain的模块化架构，支持动态插件加载
- **当前进展**：基础架构设计完成，工具验证系统已实现，解决了关键的安全问题

### 最近的对话重点（2026-03-16）
- **架构设计**：完整7层 Agent 架构设计
- **Skills vs Workflow**：明确 Skills 是动态的工作流生成器，Workflow 是固化的工作流模板
- **路由设计**：设计了5个场景的路由逻辑
- **架构文档**：创建了 ARCHITECTURE.md 完整架构说明文档
- **AgentProtocolHandler**：创建了 AgentProtocolHandler.java 类

## 后续讨论建议

当继续讨论时，可以基于以下方面展开：
1. **重构统一接口层**：实现 SkillInterface, WorkflowInterface, ToolInterface
2. **重构具体实现层**：实现 Skills 和 Workflow 的具体实现
3. **重构 Agent 层**：协调 LLM, Memory, Skills/Workflow/Tool
4. **重构 Handler**：JSON 构建和解析
5. **重构 LLM Client**：HTTP 通信
6. **修复编译错误**：修正 ToolExecutorInitializer.java 的类路径引用
7. **实现工具执行器**：完成所有内置工具的执行器开发
8. **动态加载机制**：实现真正的插件动态注册和卸载

---
*此文档由AI助手根据对话内容自动生成，记录了当前的技术状态和后续工作方向。*