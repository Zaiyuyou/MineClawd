# MineClawd 上下文优化实施方案

## 概述

本文档详细描述了MineClawd项目中上下文管理系统的优化实施方案，旨在解决token消耗指数级增长的问题，同时保持系统的模块化和可扩展性。

## 核心问题分析

### 当前问题
1. **系统提示词重复**: 每次对话都包含完整的系统提示词
2. **工具列表冗余**: 工具描述信息在多个地方重复出现
3. **历史累积**: 会话历史无限制增长，导致token消耗爆炸
4. **缺乏智能管理**: 没有基于会话状态的上下文优化

### 根本原因
- 会话历史采用简单的线性累积模式
- 缺乏上下文修剪和压缩机制
- 系统提示词和工具列表的包含策略过于保守

## 设计理念

### 模块化原则
1. **单一职责**: 每个模块只负责一个明确的功能
2. **接口隔离**: 模块间通过明确定义的接口通信
3. **可替换性**: 模块实现可以独立替换和升级
4. **配置驱动**: 关键参数通过配置文件管理

### 可扩展性设计
1. **插件化架构**: 支持添加新的上下文优化策略
2. **策略模式**: 不同的优化策略可以灵活组合
3. **事件驱动**: 基于事件的通知机制支持功能扩展
4. **监控反馈**: 实时监控优化效果，支持动态调整

## 架构设计

### 整体架构
```
┌─────────────────────────────────────────────────────────────┐
│                   应用层 (MineClawd.java)                   │
├─────────────────────────────────────────────────────────────┤
│             上下文管理器 (ContextManager)                   │
├───────────────┬───────────────┬───────────────┬─────────────┤
│  会话状态跟踪  │  上下文修剪器  │  提示词优化器  │  工具管理器  │
│ (SessionTracker) │ (ContextPruner) │ (PromptOptimizer) │ (ToolManager) │
├───────────────┼───────────────┼───────────────┼─────────────┤
│  策略管理器    │  监控统计器    │  配置管理器    │  事件总线    │
│ (StrategyManager) │ (Monitor) │ (ConfigManager) │ (EventBus) │
└───────────────┴───────────────┴───────────────┴─────────────┘
```

### 核心模块设计

#### 1. 上下文管理器 (ContextManager)
**职责**: 协调所有上下文优化组件，提供统一的API接口

```java
public class ContextManager {
    private final SessionTracker sessionTracker;
    private final ContextPruner contextPruner;
    private final PromptOptimizer promptOptimizer;
    private final ToolManager toolManager;
    private final StrategyManager strategyManager;
    
    public OptimizedContext getOptimizedContext(String sessionId, UserRequest request) {
        // 1. 获取会话状态
        SessionState state = sessionTracker.getSessionState(sessionId);
        
        // 2. 选择优化策略
        OptimizationStrategy strategy = strategyManager.selectStrategy(state, request);
        
        // 3. 执行优化
        return strategy.optimize(state, request);
    }
}
```

#### 2. 会话状态跟踪器 (SessionTracker)
**职责**: 跟踪和管理会话的生命周期状态

```java
public class SessionTracker {
    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();
    
    public SessionState getSessionState(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> new SessionState(id));
    }
    
    public void updateSessionState(String sessionId, SessionEvent event) {
        SessionState state = getSessionState(sessionId);
        state.processEvent(event);
        
        // 发布状态变更事件
        eventBus.publish(new SessionStateChangedEvent(sessionId, state));
    }
}

public class SessionState {
    private final String sessionId;
    private SessionPhase phase = SessionPhase.INITIALIZATION;
    private Instant lastActivity;
    private int turnCount = 0;
    private UserIntent lastIntent;
    private List<ToolUsage> recentToolUsage = new ArrayList<>();
    private String lastSystemPromptHash;
    
    public boolean shouldIncludeFullSystemPrompt() {
        return phase == SessionPhase.INITIALIZATION || 
               hasSystemPromptChanged() ||
               turnCount % REINITIALIZATION_INTERVAL == 0;
    }
    
    public boolean shouldIncludeTools() {
        return phase == SessionPhase.INITIALIZATION ||
               lastIntent.requiresTools() ||
               hasRecentToolUsage();
    }
}
```

#### 3. 上下文修剪器 (ContextPruner)
**职责**: 智能修剪和压缩历史上下文

```java
public class ContextPruner {
    private final TokenEstimator tokenEstimator;
    private final ContextCompressor compressor;
    
    public List<OpenAIMessage> pruneHistory(List<OpenAIMessage> history, int maxTokens) {
        int currentTokens = tokenEstimator.estimate(history);
        
        if (currentTokens <= maxTokens) {
            return history;
        }
        
        // 应用修剪策略
        return applyPruningStrategy(history, maxTokens);
    }
    
    private List<OpenAIMessage> applyPruningStrategy(List<OpenAIMessage> history, int maxTokens) {
        PruningStrategy strategy = selectPruningStrategy(history);
        
        switch (strategy) {
            case RECENT_FIRST:
                return pruneRecentFirst(history, maxTokens);
            case IMPORTANCE_WEIGHTED:
                return pruneByImportance(history, maxTokens);
            case COMPRESSION:
                return compressHistory(history, maxTokens);
            default:
                return pruneRecentFirst(history, maxTokens);
        }
    }
}
```

#### 4. 提示词优化器 (PromptOptimizer)
**职责**: 优化系统提示词和用户提示词的内容和结构

```java
public class PromptOptimizer {
    private final PromptTemplateManager templateManager;
    private final VariableResolver variableResolver;
    
    public String optimizeSystemPrompt(String basePrompt, SessionState state, UserIntent intent) {
        // 选择适当的提示词模板
        PromptTemplate template = templateManager.selectTemplate("system", state, intent);
        
        // 解析变量
        Map<String, Object> variables = variableResolver.resolveVariables(state, intent);
        
        // 生成优化的提示词
        return template.render(variables);
    }
    
    public String optimizeUserPrompt(String userMessage, SessionState state) {
        // 基于会话状态优化用户消息
        if (state.getTurnCount() > 0 && !state.requiresExplicitContext()) {
            return compressUserMessage(userMessage, state);
        }
        return userMessage;
    }
}
```

#### 5. 工具管理器 (ToolManager)
**职责**: 管理工具列表的生成和优化

```java
public class ToolManager {
    private final ToolRegistry toolRegistry;
    private final ToolDescriptionOptimizer descriptionOptimizer;
    
    public List<OpenAITool> getOptimizedTools(SessionState state, UserIntent intent) {
        if (!state.shouldIncludeTools() && !intent.requiresTools()) {
            return Collections.emptyList();
        }
        
        // 基于意图选择相关工具
        List<MineClawdTool> relevantTools = selectRelevantTools(intent);
        
        // 优化工具描述
        return relevantTools.stream()
            .map(this::optimizeToolDescription)
            .collect(Collectors.toList());
    }
    
    private OpenAITool optimizeToolDescription(MineClawdTool tool) {
        ToolDescription description = descriptionOptimizer.optimize(tool, getCurrentContext());
        return new OpenAITool(tool.getName(), description);
    }
}
```

## 实施计划

### 第一阶段：基础架构 (1-2周)

#### 目标
建立核心模块框架，实现基本的上下文优化功能

#### 具体任务
1. **创建基础包结构**
   ```
   com.mineclawd.foundation.context
   ├── ContextManager.java
   ├── session/
   │   ├── SessionTracker.java
   │   └── SessionState.java
   ├── pruning/
   │   ├── ContextPruner.java
   │   └── strategies/
   ├── prompt/
   │   ├── PromptOptimizer.java
   │   └── templates/
   └── tools/
       └── ToolManager.java
   ```

2. **实现SessionTracker和SessionState**
   - 会话生命周期跟踪
   - 基本的状态判断逻辑
   - 事件发布机制

3. **集成到MineClawd.java**
   - 替换原有的ensureOpenAiHistory调用
   - 添加ContextManager初始化
   - 基本的错误处理

#### 交付物
- 可运行的基础上下文优化框架
- 会话状态跟踪功能
- 基本的提示词优化

### 第二阶段：核心功能 (2-3周)

#### 目标
实现智能的上下文修剪和工具管理

#### 具体任务
1. **完善ContextPruner**
   - 实现多种修剪策略
   - 添加token估算功能
   - 实现上下文压缩

2. **增强PromptOptimizer**
   - 模板管理系统
   - 变量解析器
   - 自适应提示词生成

3. **优化ToolManager**
   - 基于意图的工具选择
   - 工具描述优化
   - 动态工具包含策略

4. **添加配置管理**
   - 可配置的优化参数
   - 策略选择配置
   - 性能阈值设置

#### 交付物
- 完整的上下文优化功能
- 可配置的优化策略
- 性能监控基础

### 第三阶段：高级优化 (2-3周)

#### 目标
实现高级优化功能和性能监控

#### 具体任务
1. **实现StrategyManager**
   - 动态策略选择
   - A/B测试框架
   - 自适应优化

2. **添加监控统计**
   - Token使用统计
   - 优化效果分析
   - 性能指标收集

3. **实现事件总线**
   - 模块间松耦合通信
   - 插件扩展支持
   - 调试和日志

4. **性能优化和测试**
   - 压力测试
   - 内存优化
   - 并发处理

#### 交付物
- 生产级的上下文优化系统
- 完整的监控和统计功能
- 可扩展的插件架构

### 第四阶段：集成和优化 (1-2周)

#### 目标
系统集成、性能调优和文档完善

#### 具体任务
1. **系统集成测试**
   - 端到端功能测试
   - 回归测试
   - 性能基准测试

2. **性能调优**
   - 内存使用优化
   - 响应时间优化
   - 并发性能优化

3. **文档和示例**
   - API文档
   - 配置指南
   - 最佳实践

#### 交付物
- 稳定可用的上下文优化系统
- 完整的文档和示例
- 性能优化报告

## 技术细节

### 关键算法实现

#### 1. 上下文修剪算法
```java
public class RecentFirstPruner implements PruningStrategy {
    @Override
    public List<OpenAIMessage> prune(List<OpenAIMessage> history, int maxTokens) {
        List<OpenAIMessage> result = new ArrayList<>();
        int currentTokens = 0;
        
        // 从最新消息开始添加
        for (int i = history.size() - 1; i >= 0; i--) {
            OpenAIMessage message = history.get(i);
            int messageTokens = estimateTokens(message);
            
            if (currentTokens + messageTokens <= maxTokens) {
                result.add(0, message); // 保持顺序
                currentTokens += messageTokens;
            } else {
                break;
            }
        }
        
        return result;
    }
}
```

#### 2. 会话状态机
```java
public enum SessionPhase {
    INITIALIZATION,      // 会话初始化阶段
    TOOL_DISCOVERY,     // 工具探索阶段
    ACTIVE_WORK,        // 活跃工作阶段
    MAINTENANCE,        // 维护阶段
    SUMMARY             // 总结阶段
}

public class SessionStateMachine {
    public SessionPhase transition(SessionEvent event, SessionState currentState) {
        return switch (currentState.getPhase()) {
            case INITIALIZATION -> handleInitialization(event, currentState);
            case TOOL_DISCOVERY -> handleToolDiscovery(event, currentState);
            case ACTIVE_WORK -> handleActiveWork(event, currentState);
            case MAINTENANCE -> handleMaintenance(event, currentState);
            case SUMMARY -> handleSummary(event, currentState);
        };
    }
}
```

### 配置系统设计

#### 1. 配置文件结构
```yaml
context:
  optimization:
    enabled: true
    maxTokens: 8000
    pruningStrategy: "recent_first"
    
  session:
    reinitializationInterval: 10
    toolInclusionThreshold: 0.7
    
  prompts:
    system:
      templates:
        initialization: "templates/system/init.mustache"
        active: "templates/system/active.mustache"
        summary: "templates/system/summary.mustache"
```

#### 2. 动态配置更新
```java
public class DynamicConfigManager {
    private final ConfigProvider configProvider;
    private final List<ConfigListener> listeners = new ArrayList<>();
    
    public void updateConfig(String key, Object value) {
        configProvider.update(key, value);
        
        // 通知监听器
        listeners.forEach(listener -> listener.onConfigChanged(key, value));
    }
}
```

## 风险评估和缓解措施

### 技术风险

#### 1. 性能开销
- **风险**: 上下文优化可能增加处理延迟
- **缓解**: 
  - 实现异步处理
  - 添加性能监控
  - 优化算法复杂度

#### 2. 状态一致性
- **风险**: 会话状态可能在不同模块间不一致
- **缓解**:
  - 实现原子操作
  - 添加状态验证
  - 完善的错误恢复

#### 3. 内存使用
- **风险**: 状态跟踪可能增加内存消耗
- **缓解**:
  - 实现内存限制
  - 添加垃圾回收策略
  - 监控内存使用

### 业务风险

#### 1. 功能回归
- **风险**: 优化可能影响现有功能
- **缓解**:
  - 充分的单元测试
  - 渐进式部署
  - 功能开关控制

#### 2. 用户体验
- **风险**: 过度优化可能影响对话连贯性
- **缓解**:
  - 用户可配置的优化级别
  - A/B测试验证
  - 用户反馈收集

## 成功指标

### 技术指标
1. **Token减少率**: 目标减少50-70%的token消耗
2. **响应时间**: 优化后响应时间增加不超过20%
3. **内存使用**: 内存增加控制在10%以内
4. **错误率**: 功能错误率低于0.1%

### 业务指标
1. **对话长度**: 支持更长的多轮对话
2. **用户体验**: 用户满意度调查得分
3. **成本节约**: API调用成本降低比例

## 总结

本实施方案基于模块化和可扩展的设计理念，通过分阶段的实施计划，逐步构建一个智能、高效的上下文优化系统。该系统将显著改善MineClawd的token使用效率，同时为未来的功能扩展奠定坚实的基础。

实施过程中将重点关注性能、稳定性和用户体验，确保优化效果的同时不牺牲系统功能。