# MineClawd 多智能体协作架构设计

## 概述

本文档描述了MineClawd项目中引入多智能体协作架构的设计思路和技术细节，旨在优化提示词管理和工具调用流程。

## 核心问题

当前`MineClawd.java`中的`buildSystemPrompt`方法承担了过多的职责：
- 系统提示词构建
- 工具列表管理
- 会话上下文处理
- 环境信息整合

这导致了代码复杂度高、维护困难、提示词优化空间有限的问题。

## 架构目标

1. **职责分离**：将复杂的提示词构建逻辑分解到专门的智能体
2. **智能优化**：引入语义意图理解，动态优化提示词内容
3. **生命周期管理**：合理管理不同智能体的状态和资源
4. **可扩展性**：支持未来添加更多专业智能体

## 多智能体架构设计

### 智能体分层结构

```
┌─────────────────────────────────────────┐
│           用户请求入口                   │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     语义意图理解智能体                  │
│  (SemanticIntentAgent)                 │
│  • 生命周期: 请求级别                   │
│  • 职责: 分析用户意图、工具使用模式      │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│       核心会话协调智能体                │
│    (CoreSessionAgent)                   │
│    • 生命周期: 伴随会话                  │
│    • 职责: 维护上下文、协调其他智能体     │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         工具管理智能体                   │
│      (ToolManagementAgent)              │
│      • 生命周期: 无状态单次调用          │
│      • 职责: 工具提示词生成、调用处理     │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│              LLM API调用                 │
└─────────────────────────────────────────┘
```

### 智能体详细设计

#### 1. 语义意图理解智能体 (SemanticIntentAgent)

**生命周期策略**: 请求级别（单次调用即用即销）

**核心职责**:
- 分析用户请求的语义意图
- 识别工具使用模式和复杂度
- 生成工具提示词优化策略
- 管理`tools`角色的发言内容

**实现细节**:
```java
public class SemanticIntentAgent {
    public IntentAnalysis analyzeIntent(String userMessage, SessionContext context) {
        // 规则基础意图分类
        IntentType intentType = ruleBasedIntentClassification(userMessage);
        
        // AI辅助的意图精炼（可选）
        if (intentType == IntentType.AMBIGUOUS) {
            intentType = aiBasedIntentRefinement(userMessage, context);
        }
        
        // 工具需求分析
        ToolUsageRequirement toolReq = analyzeToolRequirements(userMessage, context);
        
        return new IntentAnalysis(intentType, toolReq);
    }
    
    public ToolPromptStrategy generateToolStrategy(IntentAnalysis intent) {
        return switch (intent.getType()) {
            case SIMPLE_QUERY -> buildMinimalToolStrategy();
            case COMPLEX_TASK -> buildComprehensiveToolStrategy();
            case TOOL_HEAVY -> buildToolFocusedStrategy();
            default -> buildBalancedStrategy();
        };
    }
}
```

#### 2. 核心会话协调智能体 (CoreSessionAgent)

**生命周期策略**: 伴随会话

**核心职责**:
- 维护会话上下文和状态
- 协调其他智能体的协作
- 处理复杂的多轮对话逻辑
- 管理会话级别的优化策略

**实现细节**:
```java
public class CoreSessionAgent {
    private final String sessionId;
    private final SessionState state;
    
    public AgentResponse processRequest(UserRequest request, IntentAnalysis intent) {
        // 基于意图更新会话状态
        state.updateBasedOnIntent(intent);
        
        // 协调工具智能体生成提示词
        ToolManagementAgent toolAgent = new ToolManagementAgent();
        ToolPrompt toolPrompt = toolAgent.generatePrompt(intent, state);
        
        // 构建完整的系统提示词
        String systemPrompt = buildSystemPrompt(request, intent, toolPrompt);
        
        return new AgentResponse(systemPrompt, toolPrompt.getTools());
    }
}
```

#### 3. 工具管理智能体 (ToolManagementAgent)

**生命周期策略**: 无状态单次调用

**核心职责**:
- 提供工具列表和详细描述
- 基于意图生成优化的工具提示词
- 处理工具调用请求和参数验证
- 管理工具调用结果

**实现细节**:
```java
public class ToolManagementAgent {
    public ToolPrompt generatePrompt(IntentAnalysis intent, SessionState state) {
        // 基于意图选择工具展示策略
        ToolDisplayStrategy strategy = selectDisplayStrategy(intent);
        
        // 生成优化的工具列表
        List<ToolDescription> tools = generateOptimizedToolList(strategy, state);
        
        // 构建工具使用指南
        String toolGuidance = buildToolGuidance(intent, tools);
        
        return new ToolPrompt(tools, toolGuidance, strategy);
    }
}
```

## 智能体协作流程

### 标准请求处理流程

```java
public class MineClawdMultiAgentSystem {
    public AgentResponse handleUserRequest(String sessionId, UserRequest request) {
        // 阶段1: 意图分析（单次调用智能体）
        SemanticIntentAgent intentAgent = new SemanticIntentAgent();
        IntentAnalysis intent = intentAgent.analyze(request, getSessionContext(sessionId));
        
        // 阶段2: 工具策略生成
        ToolPromptStrategy toolStrategy = intentAgent.generateToolStrategy(intent);
        
        // 阶段3: 核心会话处理（伴随会话智能体）
        CoreSessionAgent sessionAgent = getSessionAgent(sessionId);
        AgentResponse response = sessionAgent.processRequest(request, intent, toolStrategy);
        
        // 阶段4: 工具调用处理（无状态智能体）
        if (response.requiresToolExecution()) {
            ToolManagementAgent toolAgent = new ToolManagementAgent();
            ToolExecutionResult result = toolAgent.executeTools(response.getToolCalls());
            response = sessionAgent.handleToolResults(result);
        }
        
        return response;
    }
}
```

### 工具提示词优化策略

#### 基于意图的工具展示

| 意图类型 | 工具展示策略 | 提示词优化 |
|---------|-------------|-----------|
| 简单查询 | 最小化工具列表 | 只显示核心工具 |
| 复杂任务 | 全面工具列表 | 详细工具说明 |
| 工具密集型 | 工具聚焦策略 | 突出相关工具 |
| 文件操作 | 文件工具优先 | 文件操作指南 |

#### 会话感知的工具管理

- **新会话**: 提供完整的工具介绍和指南
- **持续会话**: 基于历史优化工具提示词
- **工具探索阶段**: 强调工具发现和试用
- **熟练使用阶段**: 精简工具描述，聚焦功能

## 技术实现细节

### 智能体生命周期管理

#### 1. 请求级别智能体 (Request-Scoped)
- **创建时机**: 每次用户请求时
- **销毁时机**: 请求处理完成后
- **状态管理**: 无状态或请求级别状态
- **适用场景**: 语义意图分析、工具策略生成

#### 2. 会话级别智能体 (Session-Bound)
- **创建时机**: 会话开始时
- **销毁时机**: 会话结束时
- **状态管理**: 会话级别状态持久化
- **适用场景**: 核心会话协调、上下文管理

#### 3. 无状态智能体 (Stateless)
- **创建时机**: 按需创建
- **销毁时机**: 调用完成后立即销毁
- **状态管理**: 完全无状态
- **适用场景**: 工具管理、功能操作

### 智能体间通信机制

#### 数据传递接口
```java
public interface AgentMessage {
    String getSessionId();
    AgentType getSourceAgent();
    AgentType getTargetAgent();
    Object getPayload();
    Instant getTimestamp();
}

public class IntentAnalysisMessage implements AgentMessage {
    private final IntentAnalysis intent;
    private final ToolUsagePattern pattern;
    // ... 实现细节
}
```

#### 协调模式
- **请求-响应模式**: 智能体间的直接调用
- **事件驱动模式**: 通过事件总线进行松耦合通信
- **工作流模式**: 定义智能体协作的固定流程

### 集成到现有架构

#### MineClawd.java的改造
```java
private int handleRequest(ServerCommandSource source, String request, RequestOptions options) {
    // 现有逻辑...
    
    // 替换原有的buildSystemPrompt调用
    // String systemPrompt = buildSystemPrompt(source, config, ownerKey, runtime.dynamicRegistryEnabled(), session);
    
    // 使用多智能体系统
    AgentResponse agentResponse = multiAgentSystem.processRequest(
        sessionId, 
        new UserRequest(request, source, options)
    );
    
    String systemPrompt = agentResponse.getSystemPrompt();
    List<OpenAITool> tools = agentResponse.getTools();
    
    // 继续现有逻辑...
}
```

## 优势与收益

### 1. 代码可维护性
- 职责清晰分离
- 智能体独立测试
- 模块化架构

### 2. 提示词优化
- 动态意图分析
- 个性化工具提示
- 会话感知优化

### 3. 扩展性
- 易于添加新智能体
- 支持功能扩展
- 架构灵活性

### 4. 性能优化
- 智能体按需创建
- 资源合理分配
- 避免不必要的计算

## 实施计划

### 第一阶段：基础架构
1. 定义智能体接口和基础类
2. 实现智能体管理器
3. 创建语义意图理解智能体原型

### 第二阶段：核心功能
1. 实现核心会话协调智能体
2. 开发工具管理智能体
3. 集成到MineClawd主流程

### 第三阶段：优化完善
1. 添加智能体间通信机制
2. 实现高级优化策略
3. 性能测试和调优

## 风险评估

### 技术风险
- 智能体协作复杂度
- 状态管理挑战
- 性能开销

### 缓解措施
- 渐进式实施
- 充分的单元测试
- 性能监控和优化

## 总结

多智能体协作架构为MineClawd提供了更加智能和灵活的提示词管理方案。通过合理的智能体职责划分和生命周期管理，可以在保持系统性能的同时，实现真正的智能优化。

这种架构不仅解决了当前代码复杂度过高的问题，还为未来的功能扩展奠定了坚实的基础。