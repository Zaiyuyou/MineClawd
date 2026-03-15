# MineClawd 工具系统重构 - 对话上下文总结

## 当前状态（2026-03-14 15:30）

### 最近完成的工作（2026-03-14 15:30）

**工具验证系统重大安全改进**

1. **OpenAI API Schema验证系统**
   - 新增`OpenAISchemaValidator.java` - 完整的工具定义验证器
   - 新增`ParameterFactory.java` - JSON驱动的参数schema生成工厂
   - 修复了`list-assets`工具中`properties`字段错误设置为数组的问题

2. **智能工具过滤机制**
   - `ToolRegistry.java`现在支持自动验证工具schema
   - 只有验证通过的工具才会被发送到LLM，确保基础聊天功能不受影响
   - 维护两个独立的注册表：完整注册表（向后兼容）和有效注册表（验证通过）

3. **玩家状态通知系统**
   - 玩家加入游戏时自动发送工具验证状态消息
   - 包含详细的工具统计信息（总计、有效、无效工具数量）
   - 问题工具列表和服务器日志指引

4. **网络消息发送修复**
   - 修正了`RegistryByteBuf`的类路径错误
   - 使用正确的网络消息发送机制（参考`DynamicContentRegistry.java`）
   - 确保消息能够正确发送到玩家

### 已完成的工作

### 已完成的工作

1. **工具系统初步解耦**
   - 将OpenAITools和vertexTools从MineClawd.java中解耦
   - 创建了`tool_sys`包，包含核心架构类：
     - `ToolDefinition.java` - 工具定义记录类
     - `ToolProvider.java` - 工具提供者接口
     - `ToolRegistry.java` - 工具注册表
     - `ToolFactory.java` - LLM工具工厂

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
   - 创建了`plugin`包，包含：
     - `ToolPlugin.java` - 工具插件接口
     - `PluginManager.java` - 插件管理器
     - `ToolExecutor.java` - 工具执行器接口
     - `ToolExecutorManager.java` - 工具执行器管理器
     - `AbstractToolPlugin.java` - 抽象插件基类

4. **API接口规范**
   - 创建了`api`包，包含：
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
1. **工具验证系统实现**
   - 实现OpenAI API Schema验证器
   - 创建参数工厂支持JSON驱动参数生成
   - 实现智能工具过滤机制
   - 添加玩家状态通知系统

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

### 最近的对话重点（2026-03-14 15:30）
- **问题发现**：`list-assets`工具存在400错误，`properties`字段错误设置为数组
- **解决方案**：实现OpenAI API Schema验证系统，确保工具定义符合规范
- **安全改进**：智能工具过滤机制，防止无效工具影响LLM基础功能
- **用户体验**：玩家状态通知系统，提供详细的工具验证信息
- **技术修复**：修正`RegistryByteBuf`类路径错误，确保网络消息正常发送

## 后续讨论建议

当继续讨论时，可以基于以下方面展开：
1. **插件系统完善**：修复`ToolExecutorInitializer.java`的编译错误
2. **工具执行器实现**：完成所有内置工具的执行器开发
3. **动态加载机制**：实现真正的插件动态注册和卸载
4. **性能监控**：添加工具执行性能监控和优化
5. **安全增强**：进一步强化插件系统的安全性机制

---
*此文档由AI助手根据对话内容自动生成，记录了当前的技术状态和后续工作方向。*