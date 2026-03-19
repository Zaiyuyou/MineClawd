# MineClawd 重构计划

## 📋 目录

- [总体重构目标](#总体重构目标)
- [分层重构计划](#分层重构计划)
  - [阶段1：统一接口层 (第3.5层)](#阶段1统一接口层-第35层)
  - [阶段2：Tool 具体实现层 (第4层)](#阶段2tool-具体实现层-第4层)
  - [阶段3：Skill 具体实现层 (第4层)](#阶段3skill-具体实现层-第4层)
  - [阶段4：Workflow 具体实现层 (第4层)](#阶段4workflow-具体实现层-第4层)
  - [阶段5：Agent 协调层 (第3层)](#阶段5agent-协调层-第3层)
  - [阶段6：AgentProtocolHandler 层 (第2层)](#阶段6agentprotocolhandler-层-第2层)
  - [阶段7：外挂插件系统设计](#阶段7外挂插件系统设计)
- [阶段8：用户自定义工作流系统 (新增)](#阶段8用户自定义工作流系统-新增)
- [阶段9：结构化数据管理系统 (新增)](#阶段9结构化数据管理系统-新增)
- [阶段10：资产管理系统增强 (新增)](#阶段10资产管理系统增强-新增)
- [阶段11：业务规则引擎层 (新增)](#阶段11业务规则引擎层-新增)
- [阶段12：用户界面适配器层 (新增)](#阶段12用户界面适配器层-新增)
- [阶段13：文档更新](#阶段13文档更新)
- [阶段14：测试验证](#阶段14测试验证)
- [外挂插件系统详细设计](#外挂插件系统详细设计)
- [外挂 Mod 示例](#外挂-mod-示例)
- [实施路线图](#实施路线图)

---

## 🎯 总体重构目标

1. **分层重构**：每层独立完成，确保向后兼容
2. **统一通信模式**：Tool/Skill/Workflow 采用相同的通信模式
3. **外挂支持**：支持第三方 Mod 通过插件形式扩展
4. **平行层通信**：制定清晰的内外通信规则

### **核心设计理念：通信模式统一**

#### **Tool/Skill/Workflow 的三种通信模式**

从通信的角度看，Tool/Skill/Workflow 应该被视为**抽象概念**，它们提供三种统一的通信方式：

##### **1. 注册通信 (Registration Communication)**
- **目的**：提供基本信息和摘要
- **方法**：`getName()`, `getDescription()`, `getSpecificInfo()`
- **用途**：向 LLM 提供组件的元数据，用于决策
- **特点**：静态信息，无需执行

##### **2. 预调用通信 (Pre-invocation Communication)**
- **目的**：返回具体的 function call 规则
- **方法**：`getParameters()`, `getContext()`, `getPrompt()`
- **用途**：定义 LLM 如何调用该组件的规则
- **特点**：定义调用接口，但不执行

##### **3. 控制通信 (Control Communication)**
- **目的**：执行实际的业务逻辑
- **方法**：`execute(Map<String, Object> context)`
- **用途**：执行组件的核心功能
- **特点**：动态执行，可能嵌套调用其他组件

#### **三种组件的通信模式对比**

| 组件 | 注册通信 | 预调用通信 | 控制通信 | 特点 |
|------|---------|-----------|---------|------|
| **Tool** | 基本信息 | OpenAI Tool Schema | 执行具体操作 | 最基础的原子操作 |
| **Workflow** | 基本信息 | 步骤定义 + 参数 | 按顺序执行步骤 | 固化的工作流模板 |
| **Skill** | 基本信息 | 工具列表 + 配置 | 生成 Workflow 并执行 | 动态的工作流生成器 |

#### **嵌套调用关系**

```
Skill.execute() → 生成 Workflow → Workflow.execute() → 执行多个 Tools
                         ↓
                    通信模式相同
                         ↓
                    但复杂度不同
```

**关键理解**：
- Skill/Workflow/Tool 在**控制通信**上是相同的（都实现 `execute()`）
- Skill/Workflow 可以在 `execute()` 中**嵌套调用**其他组件
- 但从**通信模式**上看，三者都是相同的：注册 → 预调用 → 控制

---

## 📊 分层重构计划

### **阶段1：统一接口层 (第3.5层)** ⭐⭐⭐⭐⭐

#### **目标**
- 定义统一的通信模式规范
- 确保 Skills/Workflow/Tool 有统一的通信方式
- 为外挂插件提供扩展点

#### **核心理念**
- **注册通信**：提供基本信息和摘要
- **预调用通信**：返回具体的 function call 规则
- **控制通信**：执行实际的业务逻辑

#### **操作清单**

##### **1.1 创建统一接口包**
```bash
创建目录: com.mineclawd.agent.interface
```

##### **1.2 创建核心接口**

**文件**: `com.mineclawd.agent.interface.Executable.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

import java.util.Map;

public interface Executable {
    /**
     * 控制通信方法
     * 
     * <p>执行组件的核心业务逻辑</p>
     * 
     * <p>注意：Skill/Workflow 的 execute() 可以嵌套调用其他组件</p>
     * 
     * @param context 执行上下文，包含执行时需要的各种参数和数据
     * @return 执行结果
     */
    Object execute(Map<String, Object> context);
    
    /**
     * 注册通信方法
     * 
     * <p>返回组件的名称</p>
     * 
     * @return 组件名称
     */
    String getName();
    
    /**
     * 注册通信方法
     * 
     * <p>返回组件的描述</p>
     * 
     * @return 组件描述
     */
    String getDescription();
}
```

**文件**: `com.mineclawd.agent.interface.WithContext.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

public interface WithContext {
    /**
     * 预调用通信方法
     * 
     * <p>返回组件的上下文信息，用于 LLM 理解组件的使用场景</p>
     * 
     * @param promptType 提示类型
     *                   - "base": 基础提示
     *                   - "dynamic_registry": 动态注册提示
     *                   - "asset_tracking": 资产跟踪提示
     * @return 上下文信息
     */
    String getContext(String promptType);
    
    /**
     * 预调用通信方法
     * 
     * <p>返回组件的提示信息，用于 LLM 构建调用请求</p>
     * 
     * @param promptType 提示类型
     * @return 提示信息
     */
    String getPrompt(String promptType);
}
```

**文件**: `com.mineclawd.agent.interface.WithSpecificInfo.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

public interface WithSpecificInfo {
    /**
     * 注册通信方法
     * 
     * <p>返回组件的详细信息摘要</p>
     * 
     * <p>用于向 LLM 提供组件的完整信息，帮助 LLM 决策是否使用该组件</p>
     * 
     * @return 组件详细信息
     */
    String getSpecificInfo();
}
```

##### **1.3 组合接口**

**文件**: `com.mineclawd.agent.interface.ToolInterface.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

import com.google.gson.JsonObject;

public interface ToolInterface extends Executable, WithContext, WithSpecificInfo {
    /**
     * 预调用通信方法
     * 
     * <p>返回 Tool 的参数定义（OpenAI Tool Schema）</p>
     * 
     * @return Tool 的参数定义（JSON 对象）
     */
    JsonObject getParameters();
}
```

**文件**: `com.mineclawd.agent.interface.WorkflowInterface.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;

public interface WorkflowInterface extends Executable, WithContext, WithSpecificInfo {
    /**
     * 预调用通信方法
     * 
     * <p>返回 Workflow 的步骤列表</p>
     * 
     * <p>LLM 根据步骤列表了解 Workflow 的执行流程</p>
     * 
     * @return 步骤列表
     */
    List<ToolDefinition> getSteps();
    
    /**
     * 注册通信方法
     * 
     * <p>返回 Workflow 是否已通过用户确认</p>
     * 
     * <p>用于区分固化 Workflow 和动态生成的 Workflow</p>
     * 
     * @return 是否已确认
     */
    boolean isUserConfirmed();
    
    /**
     * 注册通信方法
     * 
     * <p>设置 Workflow 的用户确认状态</p>
     * 
     * @param confirmed 是否确认
     */
    void setUserConfirmed(boolean confirmed);
}
```

**文件**: `com.mineclawd.agent.interface.SkillInterface.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;

public interface SkillInterface extends Executable, WithContext, WithSpecificInfo {
    /**
     * 预调用通信方法
     * 
     * <p>返回 Skill 包含的工具列表</p>
     * 
     * <p>LLM 根据工具列表了解 Skill 能力范围</p>
     * 
     * @return 工具列表
     */
    List<ToolDefinition> getToolList();
    
    /**
     * 控制通信方法
     * 
     * <p>生成 Workflow 并执行</p>
     * 
     * <p>注意：Skill 的 execute() 方法可以调用 generateWorkflow() 来生成 Workflow，
     * 然后执行生成的 Workflow</p>
     * 
     * @return Workflow 执行结果
     */
    WorkflowInterface generateWorkflow();
}
```

##### **1.4 创建工具类**

**文件**: `com.mineclawd.agent.interface.InterfaceRegistry.java` ✅ 已创建
```java
package com.mineclawd.agent.interface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InterfaceRegistry {
    private static final ConcurrentHashMap<String, ToolInterface> TOOLS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, WorkflowInterface> WORKFLOWS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, SkillInterface> SKILLS = new ConcurrentHashMap<>();
    
    public static void registerTool(ToolInterface tool) {
        TOOLS.put(tool.getName(), tool);
    }
    
    public static void registerWorkflow(WorkflowInterface workflow) {
        WORKFLOWS.put(workflow.getName(), workflow);
    }
    
    public static void registerSkill(SkillInterface skill) {
        SKILLS.put(skill.getName(), skill);
    }
    
    public static ToolInterface getTool(String name) {
        return TOOLS.get(name);
    }
    
    public static WorkflowInterface getWorkflow(String name) {
        return WORKFLOWS.get(name);
    }
    
    public static SkillInterface getSkill(String name) {
        return SKILLS.get(name);
    }
    
    public static List<ToolInterface> getAllTools() {
        return new ArrayList<>(TOOLS.values());
    }
    
    public static List<WorkflowInterface> getAllWorkflows() {
        return new ArrayList<>(WORKFLOWS.values());
    }
    
    public static List<SkillInterface> getAllSkills() {
        return new ArrayList<>(SKILLS.values());
    }
}
```

---

### **阶段2：Tool 具体实现层 (第4层)** ⭐⭐⭐⭐⭐

#### **目标**
- 重构现有 ToolRegistry, ToolFactory
- 实现 ToolInterface 接口
- 保持向后兼容

#### **核心理念**
- Tool 是最基础的原子操作
- 实现三种通信模式：注册、预调用、控制
- 通信规则符合 OpenAI Tool Schema

#### **操作清单**

##### **2.1 重构 ToolDefinition**

**文件**: `com.mineclawd.tool_sys.ToolDefinition.java` ✅ 已创建
```java
package com.mineclawd.tool_sys;

import com.google.gson.JsonObject;
import com.mineclawd.agent.interface.ToolInterface;

import java.util.Map;

public record ToolDefinition(
        String name,
        String description,
        JsonObject parameters
) implements ToolInterface {
    
    /**
     * 控制通信方法
     * 
     * <p>执行 Tool 的核心业务逻辑</p>
     * 
     * <p>这是 Tool 最基本的通信模式，执行具体的原子操作</p>
     * 
     * @param context 执行上下文
     * @return 执行结果
     */
    @Override
    public Object execute(Map<String, Object> context) {
        // 执行工具逻辑
        // 具体实现由各个工具类提供
        return null;
    }
    
    /**
     * 注册通信方法
     * 
     * <p>返回 Tool 的详细信息摘要</p>
     * 
     * <p>用于向 LLM 提供 Tool 的完整信息</p>
     * 
     * @return Tool 详细信息
     */
    @Override
    public String getSpecificInfo() {
        return "Tool: " + name + "\n" + description;
    }
    
    /**
     * 预调用通信方法
     * 
     * <p>返回 Tool 的上下文信息</p>
     * 
     * @param promptType 提示类型
     * @return 上下文信息
     */
    @Override
    public String getContext(String promptType) {
        // 返回上下文
        return "";
    }
    
    /**
     * 预调用通信方法
     * 
     * <p>返回 Tool 的提示信息</p>
     * 
     * @param promptType 提示类型
     * @return 提示信息
     */
    @Override
    public String getPrompt(String promptType) {
        // 返回提示
        return "";
    }
    
    /**
     * 预调用通信方法
     * 
     * <p>返回 Tool 的参数定义（OpenAI Tool Schema）</p>
     * 
     * @return 参数定义
     */
    @Override
    public JsonObject getParameters() {
        return parameters;
    }
}
```

##### **2.2 重构 ToolRegistry**

**文件**: `com.mineclawd.tool_sys.ToolRegistry.java` ✅ 已创建
```java
package com.mineclawd.tool_sys;

import com.mineclawd.agent.interface.InterfaceRegistry;
import com.mineclawd.tool.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ToolRegistry {
    private static final Map<String, ToolDefinition> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, ToolDefinition> VALID_REGISTRY = new ConcurrentHashMap<>();
    private static final List<ToolProvider> PROVIDERS = new ArrayList<>();
    private static final List<String> INVALID_TOOLS = new ArrayList<>();
    
    static {
        registerBuiltinProviders();
        loadToolsFromProviders();
    }
    
    private static void registerBuiltinProviders() {
        PROVIDERS.add(new FileSystemTools());
        PROVIDERS.add(new NetworkTools());
        PROVIDERS.add(new GameCommandTools());
        PROVIDERS.add(new DynamicContentTools());
        PROVIDERS.add(new AssetManagementTools());
        PROVIDERS.add(new InteractionTools());
    }
    
    private static void loadToolsFromProviders() {
        for (ToolProvider provider : PROVIDERS) {
            List<ToolDefinition> tools = provider.getTools();
            for (ToolDefinition tool : tools) {
                register(tool);
            }
        }
    }
    
    public static void register(ToolDefinition tool) {
        if (tool != null && tool.name() != null && !tool.name().isBlank()) {
            // 验证工具参数schema是否符合OpenAI规范
            OpenAISchemaValidator.ValidationResult validation = OpenAISchemaValidator.validateToolDefinition(tool);
            
            // 总是注册到完整注册表（保持向后兼容）
            REGISTRY.put(tool.name(), tool);
            
            if (validation.isValid()) {
                // 验证通过的工具注册到有效注册表
                VALID_REGISTRY.put(tool.name(), tool);
                
                // 注册到 InterfaceRegistry
                InterfaceRegistry.registerTool((ToolInterface) tool);
            } else {
                INVALID_TOOLS.add(tool.name());
            }
        }
    }
    
    public static ToolDefinition get(String toolName) {
        return REGISTRY.get(toolName);
    }
    
    public static ToolInterface getToolInterface(String toolName) {
        return (ToolInterface) REGISTRY.get(toolName);
    }
    
    public static List<ToolDefinition> getAll() {
        return new ArrayList<>(REGISTRY.values());
    }
    
    public static List<ToolInterface> getAllToolInterfaces() {
        List<ToolInterface> interfaces = new ArrayList<>();
        for (ToolDefinition tool : REGISTRY.values()) {
            interfaces.add((ToolInterface) tool);
        }
        return interfaces;
    }
    
    public static List<ToolDefinition> getFiltered(boolean includeDynamicRegistry, boolean includeSearch) {
        List<ToolDefinition> tools = new ArrayList<>();
        
        for (ToolDefinition tool : VALID_REGISTRY.values()) {
            String name = tool.name();
            
            // 排除动态注册工具（除非启用）
            if (name.startsWith("list-dynamic-content") || 
                name.startsWith("register-dynamic") || 
                name.startsWith("update-dynamic") || 
                name.startsWith("unregister-dynamic")) {
                if (includeDynamicRegistry) {
                    tools.add(tool);
                }
                continue;
            }
            
            // 排除搜索工具（除非启用）
            if (name.equals("search")) {
                if (includeSearch) {
                    tools.add(tool);
                }
                continue;
            }
            
            // 包含其他所有验证通过的工具
            tools.add(tool);
        }
        
        return tools;
    }
}
```

##### **2.3 重构 ToolFactory**

**文件**: `com.mineclawd.tool_sys.ToolFactory.java` ✅ 已创建
```java
package com.mineclawd.tool_sys;

import com.mineclawd.agent.interface.InterfaceRegistry;
import com.mineclawd.agent.interface.ToolInterface;
import com.mineclawd.llm.OpenAITool;
import com.mineclawd.llm.VertexAIFunction;

import java.util.ArrayList;
import java.util.List;

public final class ToolFactory {
    
    public static void registerTool(ToolDefinition tool) {
        ToolRegistry.register(tool);
    }
    
    public static void unregisterTool(String toolName) {
        ToolRegistry.unregister(toolName);
    }
    
    public static List<OpenAITool> createOpenAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        List<OpenAITool> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            tools.add(new OpenAITool(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
        return tools;
    }
    
    public static List<VertexAIFunction> createVertexTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        List<VertexAIFunction> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            tools.add(new VertexAIFunction(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
        return tools;
    }
    
    public static List<ToolInterface> createToolInterfaces(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        List<ToolInterface> interfaces = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            interfaces.add((ToolInterface) definition);
        }
        
        return interfaces;
    }
}
```

---

### **阶段3：Skill 具体实现层 (第4层)** ⭐⭐⭐⭐⭐

#### **目标**
- 创建 SkillRegistry, SkillProvider
- 实现 SkillInterface 接口
- 支持动态生成 Workflow

#### **核心理念**
- Skill 是动态的工作流生成器
- 实现三种通信模式：注册、预调用、控制
- 控制通信可以嵌套调用 Workflow 和 Tools

#### **操作清单**

##### **3.1 创建 Skill 包**
```bash
创建目录: com.mineclawd.agent.skill
```

##### **3.2 创建 SkillDefinition**

**文件**: `com.mineclawd.agent.skill.SkillDefinition.java` ✅ 已创建
```java
package com.mineclawd.agent.skill;

import com.google.gson.JsonObject;
import com.mineclawd.agent.interface.SkillInterface;
import com.mineclawd.agent.interface.WorkflowInterface;
import com.mineclawd.tool_sys.ToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkillDefinition implements SkillInterface {
    private final String name;
    private final String description;
    private final List<ToolDefinition> toolList;
    private final JsonObject configuration;
    
    public SkillDefinition(String name, String description, List<ToolDefinition> toolList, JsonObject configuration) {
        this.name = name;
        this.description = description;
        this.toolList = toolList;
        this.configuration = configuration;
    }
    
    /**
     * 控制通信方法
     * 
     * <p>执行 Skill 的核心逻辑</p>
     * 
     * <p>注意：Skill 的 execute() 可以嵌套调用 Workflow 和 Tools</p>
     * 
     * @param context 执行上下文
     * @return 执行结果
     */
    @Override
    public Object execute(Map<String, Object> context) {
        // 执行 Skill 逻辑
        // 可以调用多个 Tools
        for (ToolDefinition tool : toolList) {
            ToolInterface toolInterface = (ToolInterface) tool;
            toolInterface.execute(context);
        }
        return null;
    }
    
    /**
     * 注册通信方法
     * 
     * <p>返回 Skill 的名称</p>
     * 
     * @return Skill 名称
     */
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * 注册通信方法
     * 
     * <p>返回 Skill 的描述</p>
     * 
     * @return Skill 描述
     */
    @Override
    public String getDescription() {
        return description;
    }
    
    /**
     * 注册通信方法
     * 
     * <p>返回 Skill 的详细信息摘要</p>
     * 
     * @return Skill 详细信息
     */
    @Override
    public String getSpecificInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Skill: ").append(name).append("\n");
        info.append("Description: ").append(description).append("\n");
        info.append("Tool List:\n");
        for (ToolDefinition tool : toolList) {
            info.append("  - ").append(tool.name()).append(": ").append(tool.description()).append("\n");
        }
        return info.toString();
    }
    
    /**
     * 预调用通信方法
     * 
     * <p>返回 Skill 的上下文信息</p>
     * 
     * @param promptType 提示类型
     * @return 上下文信息
     */
    @Override
    public String getContext(String promptType) {
        // 返回 Skill 的上下文
        return "";
    }
    
    /**
     * 预调用通信方法
     * 
     * <p>返回 Skill 的提示信息</p>
     * 
     * @param promptType 提示类型
     * @return 提示信息
     */
    @Override
    public String getPrompt(String promptType) {
        // 返回 Skill 的提示
        return "";
    }
    
    /**
     * 预调用通信方法
     * 
     * <p>返回 Skill 包含的工具列表</p>
     * 
     * @return 工具列表
     */
    @Override
    public List<ToolDefinition> getToolList() {
        return toolList;
    }
    
    /**
     * 控制通信方法
     * 
     * <p>生成 Workflow 并执行</p>
     * 
     * <p>注意：Skill 的 execute() 方法可以调用 generateWorkflow() 来生成 Workflow，
     * 然后执行生成的 Workflow</p>
     * 
     * @return Workflow 接口
     */
    @Override
    public WorkflowInterface generateWorkflow() {
        // 使用 LLM 生成 Workflow
        String workflowName = "generated-" + name + "-" + System.currentTimeMillis();
        
        WorkflowInterface workflow = new WorkflowDefinition(
            workflowName,
            "Generated workflow from skill: " + name,
            new ArrayList<>(toolList),
            false // 需要用户确认
        );
        
        return workflow;
    }
}
```

##### **3.3 创建 SkillProvider 接口**

**文件**: `com.mineclawd.agent.skill.SkillProvider.java` ✅ 已创建
```java
package com.mineclawd.agent.skill;

import java.util.List;

public interface SkillProvider {
    /**
     * 注册通信方法
     * 
     * <p>返回 SkillProvider 管理的 Skill 列表</p>
     * 
     * @return Skill 列表
     */
    List<SkillDefinition> getSkills();
    
    /**
     * 注册通信方法
     * 
     * <p>返回 SkillProvider 的名称</p>
     * 
     * @return Provider 名称
     */
    String getName();
}
```

##### **3.4 创建 SkillRegistry**

**文件**: `com.mineclawd.agent.skill.SkillRegistry.java` ✅ 已创建
```java
package com.mineclawd.agent.skill;

import com.mineclawd.agent.interface.InterfaceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillRegistry {
    private static final ConcurrentHashMap<String, SkillInterface> SKILLS = new ConcurrentHashMap<>();
    private static final List<SkillProvider> PROVIDERS = new ArrayList<>();
    private static final SkillRegistry INSTANCE = new SkillRegistry();
    
    private SkillRegistry() {
        registerBuiltinProviders();
    }
    
    public static SkillRegistry getInstance() {
        return INSTANCE;
    }
    
    private void registerBuiltinProviders() {
        PROVIDERS.add(new BuiltinSkillProvider());
        loadSkillsFromProviders();
    }
    
    private void loadSkillsFromProviders() {
        for (SkillProvider provider : PROVIDERS) {
            for (SkillDefinition skill : provider.getSkills()) {
                register(skill);
            }
        }
    }
    
    public static void register(SkillDefinition skill) {
        SKILLS.put(skill.getName(), skill);
        InterfaceRegistry.registerSkill(skill);
    }
    
    public static SkillInterface getSkill(String name) {
        return SKILLS.get(name);
    }
    
    public static List<SkillInterface> getAllSkills() {
        return new ArrayList<>(SKILLS.values());
    }
    
    public static void registerProvider(SkillProvider provider) {
        PROVIDERS.add(provider);
        // 重新加载所有 Skills
        SKILLS.clear();
        loadSkillsFromProviders();
    }
}
```

##### **3.5 创建内置 SkillProvider**

**文件**: `com.mineclawd.agent.skill.BuiltinSkillProvider.java` ✅ 已创建
```java
package com.mineclawd.agent.skill;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;

import java.util.ArrayList;
import java.util.List;

public class BuiltinSkillProvider implements SkillProvider {
    @Override
    public List<SkillDefinition> getSkills() {
        List<SkillDefinition> skills = new ArrayList<>();
        
        // 示例：代码审查 Skill
        skills.add(new SkillDefinition(
            "code-review",
            "Code review skill for analyzing code quality",
            getToolListForCodeReview(),
            createConfiguration()
        ));
        
        // 示例：文件分析 Skill
        skills.add(new SkillDefinition(
            "file-analysis",
            "File analysis skill for analyzing file contents",
            getToolListForFileAnalysis(),
            createConfiguration()
        ));
        
        return skills;
    }
    
    @Override
    public String getName() {
        return "builtin";
    }
    
    private List<ToolDefinition> getToolListForCodeReview() {
        List<ToolDefinition> tools = new ArrayList<>();
        tools.add(ToolRegistry.getTool("read-files"));
        tools.add(ToolRegistry.getTool("grep"));
        return tools;
    }
    
    private List<ToolDefinition> getToolListForFileAnalysis() {
        List<ToolDefinition> tools = new ArrayList<>();
        tools.add(ToolRegistry.getTool("read-files"));
        tools.add(ToolRegistry.getTool("list-files"));
        return tools;
    }
    
    private JsonObject createConfiguration() {
        JsonObject config = new JsonObject();
        config.addProperty("llmModel", "gpt-4");
        config.addProperty("maxSteps", 10);
        return config;
    }
}
```

---

### **阶段4：Workflow 具体实现层 (第4层)** ⭐⭐⭐⭐⭐

#### **目标**
- 创建 WorkflowRegistry, WorkflowProvider
- 实现 WorkflowInterface 接口
- 支持固化 Workflow 和动态 Workflow

#### **操作清单**

##### **4.1 创建 Workflow 包**
```bash
创建目录: com.mineclawd.agent.workflow
```

##### **4.2 创建 WorkflowDefinition**

**文件**: `com.mineclawd.agent.workflow.WorkflowDefinition.java` ✅ 已创建
```java
package com.mineclawd.agent.workflow;

import com.mineclawd.agent.interface.WorkflowInterface;
import com.mineclawd.tool_sys.ToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowDefinition implements WorkflowInterface {
    private final String name;
    private final String description;
    private final List<ToolDefinition> steps;
    private boolean userConfirmed;
    
    public WorkflowDefinition(String name, String description, List<ToolDefinition> steps, boolean userConfirmed) {
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.userConfirmed = userConfirmed;
    }
    
    @Override
    public Object execute(Map<String, Object> context) {
        // 执行 Workflow 步骤
        for (ToolDefinition step : steps) {
            ToolInterface tool = (ToolInterface) step;
            tool.execute(context);
        }
        return null;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getSpecificInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Workflow: ").append(name).append("\n");
        info.append("Description: ").append(description).append("\n");
        info.append("Steps:\n");
        for (ToolDefinition step : steps) {
            info.append("  - ").append(step.name()).append(": ").append(step.description()).append("\n");
        }
        info.append("User Confirmed: ").append(userConfirmed).append("\n");
        return info.toString();
    }
    
    @Override
    public String getContext(String promptType) {
        // 返回 Workflow 的上下文
        return "";
    }
    
    @Override
    public String getPrompt(String promptType) {
        // 返回 Workflow 的提示
        return "";
    }
    
    @Override
    public List<ToolDefinition> getSteps() {
        return steps;
    }
    
    @Override
    public boolean isUserConfirmed() {
        return userConfirmed;
    }
    
    @Override
    public void setUserConfirmed(boolean confirmed) {
        this.userConfirmed = confirmed;
    }
}
```

##### **4.3 创建 WorkflowProvider 接口**

**文件**: `com.mineclawd.agent.workflow.WorkflowProvider.java` ✅ 已创建
```java
package com.mineclawd.agent.workflow;

import java.util.List;

public interface WorkflowProvider {
    List<WorkflowDefinition> getWorkflows();
    String getName();
}
```

##### **4.4 创建 WorkflowRegistry**

**文件**: `com.mineclawd.agent.workflow.WorkflowRegistry.java` ✅ 已创建
```java
package com.mineclawd.agent.workflow;

import com.mineclawd.agent.interface.InterfaceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class WorkflowRegistry {
    private static final ConcurrentHashMap<String, WorkflowInterface> WORKFLOWS = new ConcurrentHashMap<>();
    private static final List<WorkflowProvider> PROVIDERS = new ArrayList<>();
    private static final WorkflowRegistry INSTANCE = new WorkflowRegistry();
    
    private WorkflowRegistry() {
        registerBuiltinProviders();
    }
    
    public static WorkflowRegistry getInstance() {
        return INSTANCE;
    }
    
    private void registerBuiltinProviders() {
        PROVIDERS.add(new BuiltinWorkflowProvider());
        loadWorkflowsFromProviders();
    }
    
    private void loadWorkflowsFromProviders() {
        for (WorkflowProvider provider : PROVIDERS) {
            for (WorkflowDefinition workflow : provider.getWorkflows()) {
                register(workflow);
            }
        }
    }
    
    public static void register(WorkflowDefinition workflow) {
        WORKFLOWS.put(workflow.getName(), workflow);
        InterfaceRegistry.registerWorkflow(workflow);
    }
    
    public static WorkflowInterface getWorkflow(String name) {
        return WORKFLOWS.get(name);
    }
    
    public static List<WorkflowInterface> getAllWorkflows() {
        return new ArrayList<>(WORKFLOWS.values());
    }
    
    public static void registerProvider(WorkflowProvider provider) {
        PROVIDERS.add(provider);
        // 重新加载所有 Workflows
        WORKFLOWS.clear();
        loadWorkflowsFromProviders();
    }
}
```

##### **4.5 创建内置 WorkflowProvider**

**文件**: `com.mineclawd.agent.workflow.BuiltinWorkflowProvider.java` ✅ 已创建
```java
package com.mineclawd.agent.workflow;

import com.mineclawd.tool_sys.ToolDefinition;

import java.util.ArrayList;
import java.util.List;

public class BuiltinWorkflowProvider implements WorkflowProvider {
    @Override
    public List<WorkflowDefinition> getWorkflows() {
        List<WorkflowDefinition> workflows = new ArrayList<>();
        
        // 示例：部署 Workflow
        workflows.add(new WorkflowDefinition(
            "deploy",
            "Deploy project workflow",
            getDeploySteps(),
            true // 固化 Workflow，不需要用户确认
        ));
        
        // 示例：文件备份 Workflow
        workflows.add(new WorkflowDefinition(
            "backup",
            "Backup files workflow",
            getBackupSteps(),
            true // 固化 Workflow，不需要用户确认
        ));
        
        return workflows;
    }
    
    @Override
    public String getName() {
        return "builtin";
    }
    
    private List<ToolDefinition> getDeploySteps() {
        List<ToolDefinition> steps = new ArrayList<>();
        steps.add(ToolRegistry.getTool("read-files"));
        steps.add(ToolRegistry.getTool("write-files"));
        steps.add(ToolRegistry.getTool("execute-command"));
        return steps;
    }
    
    private List<ToolDefinition> getBackupSteps() {
        List<ToolDefinition> steps = new ArrayList<>();
        steps.add(ToolRegistry.getTool("list-files"));
        steps.add(ToolRegistry.getTool("copy-files"));
        return steps;
    }
}
```

---

### **阶段5：Agent 协调层 (第3层)** ⭐⭐⭐⭐

#### **目标**
- 重构 AgentManager
- 实现 Agent 协调逻辑
- 管理 Memory, Skills, Workflows, Tools

#### **操作清单**

##### **5.1 创建 Agent 类**

**文件**: `com.mineclawd.agent.Agent.java` ✅ 已创建
```java
package com.mineclawd.agent;

import com.mineclawd.agent.interface.Executable;
import com.mineclawd.agent.interface.SkillInterface;
import com.mineclawd.agent.interface.WorkflowInterface;
import com.mineclawd.agent.skill.SkillRegistry;
import com.mineclawd.agent.workflow.WorkflowRegistry;
import com.mineclawd.tool_sys.ToolRegistry;

import java.util.Map;

public class Agent {
    private final String name;
    private final String basePrompt;
    private final String dynamicRegistryPrompt;
    private final String assetTrackingPrompt;
    private final SkillRegistry skillRegistry;
    private final WorkflowRegistry workflowRegistry;
    private final ToolRegistry toolRegistry;
    private final MemoryManager memoryManager;
    
    public Agent(String name, String basePrompt, String dynamicRegistryPrompt, String assetTrackingPrompt) {
        this.name = name;
        this.basePrompt = basePrompt;
        this.dynamicRegistryPrompt = dynamicRegistryPrompt;
        this.assetTrackingPrompt = assetTrackingPrompt;
        this.skillRegistry = SkillRegistry.getInstance();
        this.workflowRegistry = WorkflowRegistry.getInstance();
        this.toolRegistry = ToolRegistry.getInstance();
        this.memoryManager = new MemoryManager();
    }
    
    // 决定使用哪个 Skills/Workflow/Tool
    public Executable decideSkillOrWorkflow(String userInput) {
        // 1. 优先匹配固化 Workflow
        WorkflowInterface workflow = workflowRegistry.getWorkflow(userInput);
        if (workflow != null && workflow.isUserConfirmed()) {
            return workflow;
        }
        
        // 2. 走 Skills 流程
        SkillInterface skill = skillRegistry.getSkill(userInput);
        if (skill != null) {
            WorkflowInterface generatedWorkflow = skill.generateWorkflow();
            
            // 等待用户确认
            if (waitForUserConfirmation(generatedWorkflow)) {
                return generatedWorkflow;
            }
        }
        
        return null;
    }
    
    // 执行方法
    public Object executeSkill(String skillName, Map<String, Object> context) {
        SkillInterface skill = skillRegistry.getSkill(skillName);
        if (skill != null) {
            return skill.execute(context);
        }
        return null;
    }
    
    public Object executeWorkflow(String workflowName, Map<String, Object> context) {
        WorkflowInterface workflow = workflowRegistry.getWorkflow(workflowName);
        if (workflow != null) {
            return workflow.execute(context);
        }
        return null;
    }
    
    public Object executeTool(String toolName, Map<String, Object> context) {
        ToolInterface tool = (ToolInterface) toolRegistry.get(toolName);
        if (tool != null) {
            return tool.execute(context);
        }
        return null;
    }
    
    // 获取信息方法
    public String getSpecificSkillInfo(String skillName) {
        SkillInterface skill = skillRegistry.getSkill(skillName);
        if (skill != null) {
            return skill.getSpecificInfo();
        }
        return "";
    }
    
    public String getSpecificWorkflowInfo(String workflowName) {
        WorkflowInterface workflow = workflowRegistry.getWorkflow(workflowName);
        if (workflow != null) {
            return workflow.getSpecificInfo();
        }
        return "";
    }
    
    public String getSpecificToolInfo(String toolName) {
        ToolInterface tool = (ToolInterface) toolRegistry.get(toolName);
        if (tool != null) {
            return tool.getSpecificInfo();
        }
        return "";
    }
    
    public String getContext(String promptType) {
        // 根据 promptType 返回上下文
        switch (promptType) {
            case "base" -> basePrompt;
            case "dynamic_registry" -> dynamicRegistryPrompt;
            case "asset_tracking" -> assetTrackingPrompt;
            default -> "";
        }
    }
    
    public String getPrompt(String promptType) {
        return getContext(promptType);
    }
    
    // 用户确认
    private boolean waitForUserConfirmation(WorkflowInterface workflow) {
        // 显示 Workflow 给用户确认
        // 用户确认后设置 userConfirmed = true
        return workflow.isUserConfirmed();
    }
    
    // Memory 管理
    public void addMemory(String key, Object value) {
        memoryManager.addMessage(new MemoryMessage(key, value));
    }
    
    public Object getMemory(String key) {
        return memoryManager.getMessage(key);
    }
}
```

##### **5.2 创建 MemoryManager**

**文件**: `com.mineclawd.agent.MemoryManager.java` ✅ 已创建
```java
package com.mineclawd.agent;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryManager {
    private final ConcurrentHashMap<String, Object> memory = new ConcurrentHashMap<>();
    private int maxHistorySize = 100;
    
    public void addMessage(MemoryMessage message) {
        memory.put(message.getKey(), message.getValue());
        
        // 限制内存大小
        if (memory.size() > maxHistorySize) {
            // 移除最早的记录
            memory.entrySet().stream().findFirst().ifPresent(entry -> memory.remove(entry.getKey()));
        }
    }
    
    public Object getMessage(String key) {
        return memory.get(key);
    }
    
    public void clear() {
        memory.clear();
    }
    
    public int getSize() {
        return memory.size();
    }
}

record MemoryMessage(String key, Object value) {}
```

##### **5.3 重构 AgentManager**

**文件**: `com.mineclawd.agent.AgentManager.java` ✅ 已创建
```java
package com.mineclawd.agent;

import com.mineclawd.MineClawd;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class AgentManager {
    public static final String DEFAULT_AGENT = "default";
    private static final String FILE_EXTENSION = ".md";
    private static final Pattern OWNER_SANITIZE = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final ConcurrentHashMap<String, Agent> AGENTS = new ConcurrentHashMap<>();
    
    private final Path agentsRoot;
    private final Path activeRoot;
    private static final String ACTIVE_FILE_EXTENSION = ".txt";
    
    public static final String PROMPT_TYPE_BASE = "base";
    public static final String PROMPT_TYPE_DYNAMIC_REGISTRY = "dynamic_registry";
    public static final String PROMPT_TYPE_ASSET_TRACKING = "asset_tracking";
    
    public AgentManager() {
        Path mineclawdRoot = Platform.getGameFolder().resolve("mineclawd");
        this.agentsRoot = mineclawdRoot.resolve("agents");
        this.activeRoot = agentsRoot.resolve(".active");
        ensureDirectory(agentsRoot);
        ensureDirectory(activeRoot);
        ensureBundledAgents();
    }
    
    public synchronized List<String> listAgentNames() {
        ensureBundledAgents();
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(agentsRoot)) {
            stream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> !name.startsWith("."))
                    .filter(name -> !name.isBlank())
                    .forEach(names::add);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to list agents in {}: {}", agentsRoot, exception.getMessage());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }
    
    public synchronized String resolveAgentName(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        String wanted = reference.trim().toLowerCase(Locale.ROOT);
        for (String name : listAgentNames()) {
            if (name.toLowerCase(Locale.ROOT).equals(wanted)) {
                return name;
            }
        }
        return null;
    }
    
    public synchronized Agent loadActiveAgent(String ownerKey) {
        ensureBundledAgents();
        String agentName = getActiveAgentName(ownerKey);
        String basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
        String dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);
        String assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);
        
        if (basePrompt == null) {
            agentName = DEFAULT_AGENT;
            basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
            dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);
            assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);
        }
        
        if (basePrompt == null) {
            basePrompt = "";
            dynamicRegistryPrompt = "";
            assetTrackingPrompt = "";
        }
        
        Agent agent = new Agent(
            agentName,
            basePrompt,
            dynamicRegistryPrompt,
            assetTrackingPrompt
        );
        
        AGENTS.put(ownerKey, agent);
        return agent;
    }
    
    public synchronized String getActiveAgentName(String ownerKey) {
        ensureBundledAgents();
        String selected = readSelectedAgent(ownerKey);
        String resolved = resolveAgentName(selected);
        if (resolved != null) {
            return resolved;
        }
        return DEFAULT_AGENT;
    }
    
    public synchronized boolean setActiveAgent(String ownerKey, String agentReference) {
        String resolved = resolveAgentName(agentReference);
        if (resolved == null) {
            return false;
        }
        writeSelectedAgent(ownerKey, resolved);
        return true;
    }
    
    public synchronized String readAgentPromptContent(String agentName, String promptType) {
        String resolved = resolveAgentName(agentName);
        if (resolved == null) {
            return null;
        }
        Path path = agentsRoot.resolve(resolved).resolve(promptType + FILE_EXTENSION);
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to read agent prompt {}: {}", path, exception.getMessage());
            return null;
        }
    }
    
    private void ensureBundledAgents() {
        ensureDefaultAgent();
        ensureDumAgent();
    }
    
    private void ensureDefaultAgent() {
        Path defaultAgentDir = agentsRoot.resolve(DEFAULT_AGENT);
        ensureDirectory(defaultAgentDir);
        
        ensureFileFromResourceIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_BASE + FILE_EXTENSION), "/mineclawd/agents/default/base.md");
        ensureFileFromResourceIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_DYNAMIC_REGISTRY + FILE_EXTENSION), "/mineclawd/agents/default/dynamic_registry.md");
        ensureFileFromResourceIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_ASSET_TRACKING + FILE_EXTENSION), "/mineclawd/agents/default/asset_tracking.md");
    }
    
    private void ensureDumAgent() {
        Path dumAgentDir = agentsRoot.resolve("dum");
        ensureDirectory(dumAgentDir);
        
        ensureFileFromResourceIfMissing(dumAgentDir.resolve(PROMPT_TYPE_BASE + FILE_EXTENSION), "/mineclawd/agents/dum/base.md");
        ensureFileFromResourceIfMissing(dumAgentDir.resolve(PROMPT_TYPE_DYNAMIC_REGISTRY + FILE_EXTENSION), "/mineclawd/agents/dum/dynamic_registry.md");
        ensureFileFromResourceIfMissing(dumAgentDir.resolve(PROMPT_TYPE_ASSET_TRACKING + FILE_EXTENSION), "/mineclawd/agents/dum/asset_tracking.md");
    }
    
    private void ensureDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create agent directory: " + path, exception);
        }
    }
    
    private void ensureFileFromResourceIfMissing(Path target, String resourcePath) {
        if (Files.isRegularFile(target)) {
            return;
        }
        try (InputStream stream = AgentManager.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                MineClawd.LOGGER.warn("Bundled agent resource not found: {}", resourcePath);
                return;
            }
            Files.createDirectories(target.getParent());
            Files.write(target, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to write bundled agent {}: {}", target, exception.getMessage());
        }
    }
    
    private String readSelectedAgent(String ownerKey) {
        Path path = activeRoot.resolve(safeOwner(ownerKey) + ACTIVE_FILE_EXTENSION);
        if (!Files.isRegularFile(path)) {
            return DEFAULT_AGENT;
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to read active agent {}: {}", path, exception.getMessage());
            return DEFAULT_AGENT;
        }
    }
    
    private void writeSelectedAgent(String ownerKey, String agentName) {
        Path path = activeRoot.resolve(safeOwner(ownerKey) + ACTIVE_FILE_EXTENSION);
        try {
            Files.writeString(path, agentName + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to persist active agent: " + path, exception);
        }
    }
    
    private String safeOwner(String ownerKey) {
        String value = ownerKey == null ? "" : ownerKey.trim();
        value = OWNER_SANITIZE.matcher(value).replaceAll("_");
        if (value.isBlank()) {
            return "unknown";
        }
        return value;
    }
    
    public Agent getAgent(String ownerKey) {
        return AGENTS.get(ownerKey);
    }
}
```

---

### **阶段6：AgentProtocolHandler 层 (第2层)** ⭐⭐⭐⭐

#### **目标**
- 重构 AgentProtocolHandler
- 调用 Agent 获取具体信息
- 构建完整的 JSON

#### **操作清单**

##### **6.1 重构 buildOpenAiRequest**

**文件**: `com.mineclawd.agent.AgentProtocolHandler.java`
```java
package com.mineclawd.agent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineclawd.agent.interface.SkillInterface;
import com.mineclawd.agent.interface.ToolInterface;
import com.mineclawd.agent.interface.WorkflowInterface;
import com.mineclawd.agent.skill.SkillRegistry;
import com.mineclawd.agent.workflow.WorkflowRegistry;
import com.mineclawd.llm.OpenAIMessage;
import com.mineclawd.llm.OpenAITool;
import com.mineclawd.llm.OpenAIToolCall;
import com.mineclawd.llm.VertexAIMessage;
import com.mineclawd.llm.VertexAIFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class AgentProtocolHandler {
    
    private AgentProtocolHandler() {
        // 私有构造函数
    }
    
    public static JsonObject buildOpenAiRequest(
            String model,
            List<OpenAIMessage> history,
            @Nullable List<OpenAITool> tools,
            boolean stream,
            Agent agent
    ) {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        if (stream) {
            body.addProperty("stream", true);
        }
        
        JsonArray messages = buildMessagesArray(history);
        body.add("messages", messages);
        
        if (tools != null && !tools.isEmpty()) {
            JsonArray toolsArray = buildToolsArray(tools);
            body.add("tools", toolsArray);
        }
        
        // 调用 Agent 获取具体信息
        JsonObject agentInfo = buildAgentInfo(agent);
        body.add("agent", agentInfo);
        
        return body;
    }
    
    private static JsonObject buildAgentInfo(Agent agent) {
        JsonObject agentInfo = new JsonObject();
        
        // 获取所有 Skills 的具体信息
        JsonArray skillsInfo = new JsonArray();
        for (SkillInterface skill : SkillRegistry.getAllSkills()) {
            JsonObject skillInfo = new JsonObject();
            skillInfo.addProperty("name", skill.getName());
            skillInfo.addProperty("info", skill.getSpecificInfo());
            skillsInfo.add(skillInfo);
        }
        agentInfo.add("skills", skillsInfo);
        
        // 获取所有 Workflows 的具体信息
        JsonArray workflowsInfo = new JsonArray();
        for (WorkflowInterface workflow : WorkflowRegistry.getAllWorkflows()) {
            JsonObject workflowInfo = new JsonObject();
            workflowInfo.addProperty("name", workflow.getName());
            workflowInfo.addProperty("info", workflow.getSpecificInfo());
            workflowsInfo.add(workflowInfo);
        }
        agentInfo.add("workflows", workflowsInfo);
        
        // 获取所有 Tools 的具体信息
        JsonArray toolsInfo = new JsonArray();
        for (ToolInterface tool : ToolRegistry.getAllToolInterfaces()) {
            JsonObject toolInfo = new JsonObject();
            toolInfo.addProperty("name", tool.getName());
            toolInfo.addProperty("info", tool.getSpecificInfo());
            toolsInfo.add(toolInfo);
        }
        agentInfo.add("tools", toolsInfo);
        
        return agentInfo;
    }
    
    private static JsonArray buildMessagesArray(List<OpenAIMessage> history) {
        JsonArray messages = new JsonArray();
        if (history == null) {
            return messages;
        }
        
        for (OpenAIMessage message : history) {
            if (message == null) {
                continue;
            }
            
            JsonObject msg = new JsonObject();
            msg.addProperty("role", message.role());
            
            if (message.contentParts() != null && !message.contentParts().isEmpty()) {
                JsonArray parts = new JsonArray();
                for (JsonObject part : message.contentParts()) {
                    if (part != null) {
                        parts.add(part.deepCopy());
                    }
                }
                msg.add("content", parts);
            } else if (message.content() != null) {
                msg.addProperty("content", message.content());
            } else {
                msg.add("content", new JsonObject());
            }
            
            if (message.toolCalls() != null && !message.toolCalls().isEmpty()) {
                JsonArray toolCalls = new JsonArray();
                for (OpenAIToolCall call : message.toolCalls()) {
                    if (call == null) {
                        continue;
                    }
                    JsonObject callObj = new JsonObject();
                    if (call.id() != null && !call.id().isBlank()) {
                        callObj.addProperty("id", call.id());
                    }
                    callObj.addProperty("type", "function");
                    
                    JsonObject function = new JsonObject();
                    function.addProperty("name", call.name());
                    function.addProperty("arguments", call.arguments() == null ? "" : call.arguments());
                    callObj.add("function", function);
                    
                    toolCalls.add(callObj);
                }
                msg.add("tool_calls", toolCalls);
            }
            
            if (message.toolCallId() != null && !message.toolCallId().isBlank()) {
                msg.addProperty("tool_call_id", message.toolCallId());
            }
            
            messages.add(msg);
        }
        
        return messages;
    }
    
    private static JsonArray buildToolsArray(List<OpenAITool> tools) {
        JsonArray toolsArray = new JsonArray();
        
        for (OpenAITool tool : tools) {
            if (tool == null) {
                continue;
            }
            
            JsonObject toolObj = new JsonObject();
            toolObj.addProperty("type", "function");
            
            JsonObject function = new JsonObject();
            function.addProperty("name", tool.name());
            
            if (tool.description() != null && !tool.description().isBlank()) {
                function.addProperty("description", tool.description());
            }
            
            if (tool.parameters() != null) {
                function.add("parameters", tool.parameters());
            }
            
            toolObj.add("function", function);
            toolsArray.add(toolObj);
        }
        
        return toolsArray;
    }
    
    // ==================== Vertex AI JSON 构建 ====================
    
    public static JsonObject buildVertexAiRequest(
            String model,
            List<VertexAIMessage> history,
            List<VertexAIFunction> tools,
            boolean stream,
            Agent agent
    ) {
        JsonObject body = new JsonObject();
        
        JsonArray contents = buildContentsArray(history);
        body.add("contents", contents);
        
        if (tools != null && !tools.isEmpty()) {
            JsonObject tool = buildToolObject(tools);
            JsonArray toolsArray = new JsonArray();
            toolsArray.add(tool);
            body.add("tools", toolsArray);
            
            JsonObject toolConfig = buildToolConfig();
            body.add("toolConfig", toolConfig);
        }
        
        // 调用 Agent 获取具体信息
        JsonObject agentInfo = buildAgentInfo(agent);
        body.add("agent", agentInfo);
        
        return body;
    }
    
    private static JsonArray buildContentsArray(List<VertexAIMessage> history) {
        JsonArray contents = new JsonArray();
        if (history == null) {
            return contents;
        }
        
        for (VertexAIMessage message : history) {
            if (message == null) {
                continue;
            }
            
            JsonObject content = new JsonObject();
            if (message.role() != null && !message.role().isBlank()) {
                content.addProperty("role", message.role());
            }
            content.add("parts", message.toJsonParts());
            contents.add(content);
        }
        
        return contents;
    }
    
    private static JsonObject buildToolObject(List<VertexAIFunction> tools) {
        JsonObject tool = new JsonObject();
        JsonArray declarations = new JsonArray();
        
        for (VertexAIFunction function : tools) {
            if (function == null) {
                continue;
            }
            
            JsonObject functionJson = new JsonObject();
            functionJson.addProperty("name", function.name());
            
            if (function.description() != null && !function.description().isBlank()) {
                functionJson.addProperty("description", function.description());
            }
            
            if (function.parameters() != null) {
                functionJson.add("parameters", function.parameters());
            }
            
            declarations.add(functionJson);
        }
        
        tool.add("functionDeclarations", declarations);
        return tool;
    }
    
    private static JsonObject buildToolConfig() {
        JsonObject toolConfig = new JsonObject();
        JsonObject functionCallingConfig = new JsonObject();
        functionCallingConfig.addProperty("mode", "AUTO");
        toolConfig.add("functionCallingConfig", functionCallingConfig);
        return toolConfig;
    }
    
    // ==================== OpenAI 响应解析 ====================
    
    public static OpenAiResponse parseOpenAiResponse(String responseBody, Agent agent) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        List<OpenAIToolCall> toolCalls = new ArrayList<>();
        
        if (responseJson.has("choices") && responseJson.get("choices").isJsonArray()) {
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                
                if (firstChoice.has("message") && firstChoice.get("message").isJsonObject()) {
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    
                    if (message.has("content") && !message.get("content").isJsonNull()) {
                        text = message.get("content").getAsString();
                    }
                    
                    if (message.has("tool_calls") && message.get("tool_calls").isJsonArray()) {
                        JsonArray messageToolCalls = message.getAsJsonArray("tool_calls");
                        for (JsonElement toolCallElement : messageToolCalls) {
                            if (toolCallElement.isJsonObject()) {
                                JsonObject toolCallObj = toolCallElement.getAsJsonObject();
                                OpenAIToolCall toolCall = parseOpenAiToolCall(toolCallObj);
                                if (toolCall != null) {
                                    toolCalls.add(toolCall);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new OpenAiResponse(text, toolCalls);
    }
    
    private static OpenAIToolCall parseOpenAiToolCall(JsonObject toolCallObj) {
        if (!toolCallObj.has("type") || !"function".equals(toolCallObj.get("type").getAsString())) {
            return null;
        }
        
        String id = null;
        if (toolCallObj.has("id") && !toolCallObj.get("id").isJsonNull()) {
            id = toolCallObj.get("id").getAsString();
        }
        
        if (!toolCallObj.has("function") || !toolCallObj.get("function").isJsonObject()) {
            return null;
        }
        
        JsonObject function = toolCallObj.getAsJsonObject("function");
        String name = function.has("name") ? function.get("name").getAsString() : null;
        String arguments = function.has("arguments") ? function.get("arguments").getAsString() : "";
        
        return new OpenAIToolCall(id, name, arguments);
    }
    
    public static OpenAiStreamData parseOpenAiStream(String responseBody) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        OpenAIToolCall toolCall = null;
        
        if (responseJson.has("choices") && responseJson.get("choices").isJsonArray()) {
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                
                if (delta.has("content") && !delta.get("content").isJsonNull()) {
                    text = delta.get("content").getAsString();
                }
                
                if (delta.has("tool_calls") && delta.get("tool_calls").isJsonArray()) {
                    JsonArray toolCalls = delta.getAsJsonArray("tool_calls");
                    if (!toolCalls.isEmpty()) {
                        JsonObject toolCallObj = toolCalls.get(0).getAsJsonObject();
                        toolCall = parseOpenAiToolCall(toolCallObj);
                    }
                }
            }
        }
        
        return new OpenAiStreamData(text, toolCall);
    }
    
    // ==================== Vertex AI 响应解析 ====================
    
    public static VertexAiResponse parseVertexAiResponse(String responseBody, Agent agent) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        List<VertexAiToolCall> toolCalls = new ArrayList<>();
        
        if (responseJson.has("candidates") && responseJson.get("candidates").isJsonArray()) {
            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (!candidates.isEmpty()) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                
                if (firstCandidate.has("content") && firstCandidate.get("content").isJsonObject()) {
                    JsonObject content = firstCandidate.getAsJsonObject("content");
                    
                    if (content.has("parts") && content.get("parts").isJsonArray()) {
                        JsonArray parts = content.getAsJsonArray("parts");
                        for (JsonElement partElement : parts) {
                            if (partElement.isJsonObject()) {
                                JsonObject part = partElement.getAsJsonObject();
                                
                                if (part.has("text") && !part.get("text").isJsonNull()) {
                                    text = part.get("text").getAsString();
                                }
                                
                                if (part.has("functionCall") && part.get("functionCall").isJsonObject()) {
                                    JsonObject functionCall = part.getAsJsonObject("functionCall");
                                    String name = functionCall.has("name") ? functionCall.get("name").getAsString() : null;
                                    JsonObject args = functionCall.has("args") ? functionCall.getAsJsonObject("args") : new JsonObject();
                                    
                                    VertexAiToolCall toolCall = new VertexAiToolCall(name, args);
                                    toolCalls.add(toolCall);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new VertexAiResponse(text, toolCalls);
    }
    
    public static VertexAiStreamData parseVertexAiStream(String responseBody) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        VertexAiToolCall toolCall = null;
        
        if (responseJson.has("candidates") && responseJson.get("candidates").isJsonArray()) {
            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (!candidates.isEmpty()) {
                JsonObject delta = candidates.get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonObject("parts")
                        .get(0).getAsJsonObject();
                
                if (delta.has("text") && !delta.get("text").isJsonNull()) {
                    text = delta.get("text").getAsString();
                }
                
                if (delta.has("functionCall") && delta.get("functionCall").isJsonObject()) {
                    JsonObject functionCall = delta.getAsJsonObject("functionCall");
                    String name = functionCall.has("name") ? functionCall.get("name").getAsString() : null;
                    JsonObject args = functionCall.has("args") ? functionCall.getAsJsonObject("args") : new JsonObject();
                    
                    toolCall = new VertexAiToolCall(name, args);
                }
            }
        }
        
        return new VertexAiStreamData(text, toolCall);
    }
    
    // ==================== 响应数据类 ====================
    
    public static class OpenAiResponse {
        private final String text;
        private final List<OpenAIToolCall> toolCalls;
        
        public OpenAiResponse(String text, List<OpenAIToolCall> toolCalls) {
            this.text = text;
            this.toolCalls = toolCalls;
        }
        
        public String text() { return text; }
        public List<OpenAIToolCall> toolCalls() { return toolCalls; }
        public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
    }
    
    public static class OpenAiStreamData {
        private final String text;
        private final OpenAIToolCall toolCall;
        
        public OpenAiStreamData(String text, OpenAIToolCall toolCall) {
            this.text = text;
            this.toolCall = toolCall;
        }
        
        public String text() { return text; }
        public OpenAIToolCall toolCall() { return toolCall; }
        public boolean hasToolCall() { return toolCall != null; }
    }
    
    public static class VertexAiResponse {
        private final String text;
        private final List<VertexAiToolCall> toolCalls;
        
        public VertexAiResponse(String text, List<VertexAiToolCall> toolCalls) {
            this.text = text;
            this.toolCalls = toolCalls;
        }
        
        public String text() { return text; }
        public List<VertexAiToolCall> toolCalls() { return toolCalls; }
        public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
    }
    
    public static class VertexAiStreamData {
        private final String text;
        private final VertexAiToolCall toolCall;
        
        public VertexAiStreamData(String text, VertexAiToolCall toolCall) {
            this.text = text;
            this.toolCall = toolCall;
        }
        
        public String text() { return text; }
        public VertexAiToolCall toolCall() { return toolCall; }
        public boolean hasToolCall() { return toolCall != null; }
    }
    
    public static class VertexAiToolCall {
        private final String name;
        private final JsonObject arguments;
        
        public VertexAiToolCall(String name, JsonObject arguments) {
            this.name = name;
            this.arguments = arguments;
        }
        
        public String name() { return name; }
        public JsonObject arguments() { return arguments; }
    }
}
```

---

### **阶段7：外挂插件系统设计** ⭐⭐⭐⭐

#### **目标**
- 设计外挂 Mod 的扩展机制
- 制定平行层通信规则
- 支持外挂的 Tool/Agent/Skill/Workflow

#### **设计原则**

1. **简单直接**：外挂 Mod 直接导入类调用
2. **插件注册**：通过插件机制注册外挂组件
3. **平行层通信**：制定清晰的接口规范

#### **操作清单**

##### **7.1 创建外挂 API 包**
```bash
创建目录: com.mineclawd.api
```

##### **7.2 创建外挂 API 接口**

**文件**: `com.mineclawd.api.MineClawdAPI.java`
```java
package com.mineclawd.api;

import com.mineclawd.agent.Agent;
import com.mineclawd.agent.interface.SkillInterface;
import com.mineclawd.agent.interface.ToolInterface;
import com.mineclawd.agent.interface.WorkflowInterface;

public interface MineClawdAPI {
    // 注册 Tool
    void registerTool(ToolInterface tool);
    
    // 注册 Workflow
    void registerWorkflow(WorkflowInterface workflow);
    
    // 注册 Skill
    void registerSkill(SkillInterface skill);
    
    // 获取 Agent
    Agent getAgent(String ownerKey);
}
```

##### **7.3 创建外挂上下文**

**文件**: `com.mineclawd.api.PluginContext.java`
```java
package com.mineclawd.api;

public class PluginContext {
    private final String pluginId;
    private final String pluginName;
    private final MineClawdAPI api;
    
    public PluginContext(String pluginId, String pluginName, MineClawdAPI api) {
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.api = api;
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public MineClawdAPI getApi() {
        return api;
    }
}
```

##### **7.4 创建外挂基类**

**文件**: `com.mineclawd.api.MineClawdPlugin.java`
```java
package com.mineclawd.api;

import com.mineclawd.agent.interface.SkillInterface;
import com.mineclawd.agent.interface.ToolInterface;
import com.mineclawd.agent.interface.WorkflowInterface;

public abstract class MineClawdPlugin {
    protected PluginContext context;
    
    public void setContext(PluginContext context) {
        this.context = context;
    }
    
    public abstract void onEnable();
    public abstract void onDisable();
    
    // 便捷方法
    protected void registerTool(ToolInterface tool) {
        context.getApi().registerTool(tool);
    }
    
    protected void registerWorkflow(WorkflowInterface workflow) {
        context.getApi().registerWorkflow(workflow);
    }
    
    protected void registerSkill(SkillInterface skill) {
        context.getApi().registerSkill(skill);
    }
}
```

##### **7.5 外挂 Mod 示例**

**文件**: `com.example.myplugin.MyPlugin.java`
```java
package com.example.myplugin;

import com.example.myplugin.tools.MyCustomTool;
import com.example.myplugin.workflows.MyCustomWorkflow;
import com.example.myplugin.skills.MyCustomSkill;
import com.mineclawd.api.MineClawdPlugin;
import com.mineclawd.api.PluginContext;

public class MyPlugin extends MineClawdPlugin {
    
    @Override
    public void onEnable() {
        // 注册自定义 Tool
        registerTool(new MyCustomTool());
        
        // 注册自定义 Workflow
        registerWorkflow(new MyCustomWorkflow());
        
        // 注册自定义 Skill
        registerSkill(new MyCustomSkill());
        
        System.out.println("MyPlugin enabled!");
    }
    
    @Override
    public void onDisable() {
        // 清理逻辑
        System.out.println("MyPlugin disabled!");
    }
}
```

##### **7.6 平行层通信规则**

**规则1：外挂调用我们的类**
```java
// 外挂 Mod 直接导入并调用
import com.mineclawd.tool_sys.ToolRegistry;
import com.mineclawd.agent.skill.SkillRegistry;

// 注册 Tool
ToolRegistry.register(new MyTool());

// 注册 Skill
SkillRegistry.register(new MySkill());
```

**规则2：我们调用外挂的类**
```java
// 通过插件注册机制
PluginManager.registerPlugin(new MyPlugin());

// 插件通过 MineClawdAPI 注册组件
public class MyPlugin extends MineClawdPlugin {
    @Override
    public void onEnable() {
        registerTool(new MyTool());
    }
}
```

**规则3：平行层通信**
```
外挂 Mod ←→ MineClawd
    ↓              ↓
导入类调用    插件注册机制
```

---

### **阶段8：文档更新** ⭐⭐⭐

#### **操作清单**

##### **8.1 更新 ARCHITECTURE.md**

**文件**: `ARCHITECTURE.md`
```markdown
## 🔄 重构计划

### **阶段1：统一接口层 (第3.5层)**
- [ ] 创建 SkillInterface, WorkflowInterface, ToolInterface
- [ ] 创建 InterfaceRegistry
- [ ] 定义统一的调用方法

### **阶段2：Tool 具体实现层 (第4层)**
- [ ] 重构 ToolDefinition 实现 ToolInterface
- [ ] 重构 ToolRegistry 支持 ToolInterface
- [ ] 重构 ToolFactory 创建 ToolInterface

### **阶段3：Skill 具体实现层 (第4层)**
- [ ] 创建 SkillDefinition 实现 SkillInterface
- [ ] 创建 SkillProvider 接口
- [ ] 创建 SkillRegistry
- [ ] 创建 BuiltinSkillProvider

### **阶段4：Workflow 具体实现层 (第4层)**
- [ ] 创建 WorkflowDefinition 实现 WorkflowInterface
- [ ] 创建 WorkflowProvider 接口
- [ ] 创建 WorkflowRegistry
- [ ] 创建 BuiltinWorkflowProvider

### **阶段5：Agent 协调层 (第3层)**
- [ ] 创建 Agent 类
- [ ] 重构 AgentManager
- [ ] 实现协调逻辑

### **阶段6：AgentProtocolHandler 层 (第2层)**
- [ ] 重构 buildOpenAiRequest
- [ ] 重构 parseOpenAiResponse

### **阶段7：外挂插件系统**
- [ ] 创建 MineClawdAPI
- [ ] 创建 MineClawdPlugin 基类
- [ ] 创建 PluginContext
- [ ] 编写外挂 Mod 示例

### **阶段8：文档更新**
- [ ] 更新 ARCHITECTURE.md
- [ ] 更新 CONTEXT_SUMMARY.md
```

---

### **阶段9：测试验证** ⭐⭐⭐

#### **操作清单**

##### **9.1 单元测试**

**文件**: `com.mineclawd.agent.skill.SkillRegistryTest.java`
```java
package com.mineclawd.agent.skill;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SkillRegistryTest {
    
    @Test
    public void testRegisterSkill() {
        // 创建测试 Skill
        List<ToolDefinition> toolList = new ArrayList<>();
        JsonObject config = new JsonObject();
        SkillDefinition skill = new SkillDefinition("test", "test", toolList, config);
        
        // 注册 Skill
        SkillRegistry.register(skill);
        
        // 验证注册结果
        SkillInterface registeredSkill = SkillRegistry.getSkill("test");
        assertNotNull(registeredSkill);
        assertEquals("test", registeredSkill.getName());
    }
    
    @Test
    public void testGenerateWorkflow() {
        // 创建测试 Skill
        List<ToolDefinition> toolList = new ArrayList<>();
        JsonObject config = new JsonObject();
        SkillDefinition skill = new SkillDefinition("test", "test", toolList, config);
        
        // 生成 Workflow
        WorkflowInterface workflow = skill.generateWorkflow();
        
        // 验证 Workflow
        assertNotNull(workflow);
        assertTrue(workflow.getName().startsWith("generated-test"));
        assertFalse(workflow.isUserConfirmed());
    }
}
```

##### **9.2 集成测试**

**文件**: `com.mineclawd.agent.AgentIntegrationTest.java`
```java
package com.mineclawd.agent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AgentIntegrationTest {
    
    @Test
    public void testAgentExecute() {
        // 创建 Agent
        Agent agent = new Agent("test", "base", "dynamic", "asset");
        
        // 执行 Tool
        Object result = agent.executeTool("read-files", new HashMap<>());
        
        // 验证结果
        assertNotNull(result);
    }
    
    @Test
    public void testAgentDecideSkillOrWorkflow() {
        // 创建 Agent
        Agent agent = new Agent("test", "base", "dynamic", "asset");
        
        // 决定使用哪个 Skills/Workflow/Tool
        Executable executable = agent.decideSkillOrWorkflow("deploy");
        
        // 验证结果
        if (executable != null) {
            assertTrue(executable instanceof WorkflowInterface);
        }
    }
}
```

---

## 📊 外挂插件系统详细设计

### **设计原则**

1. **简单直接**：外挂 Mod 直接导入类调用
2. **插件注册**：通过插件机制注册外挂组件
3. **平行层通信**：制定清晰的接口规范

### **外挂 Mod 示例**

```java
// 1. 导入 MineClawd 类
import com.mineclawd.tool_sys.ToolRegistry;
import com.mineclawd.agent.skill.SkillRegistry;
import com.mineclawd.agent.workflow.WorkflowRegistry;

// 2. 创建自定义 Tool
public class MyCustomTool implements ToolInterface {
    @Override
    public Object execute(Map<String, Object> context) {
        // 自定义逻辑
        return null;
    }
    
    @Override
    public String getName() {
        return "my-custom-tool";
    }
    
    @Override
    public String getDescription() {
        return "My custom tool";
    }
    
    @Override
    public String getSpecificInfo() {
        return "Custom tool info";
    }
    
    @Override
    public String getContext(String promptType) {
        return "";
    }
    
    @Override
    public String getPrompt(String promptType) {
        return "";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        return params;
    }
}

// 3. 创建自定义 Skill
public class MyCustomSkill implements SkillInterface {
    @Override
    public Object execute(Map<String, Object> context) {
        // 自定义逻辑
        return null;
    }
    
    @Override
    public String getName() {
        return "my-custom-skill";
    }
    
    @Override
    public String getDescription() {
        return "My custom skill";
    }
    
    @Override
    public String getSpecificInfo() {
        return "Custom skill info";
    }
    
    @Override
    public String getContext(String promptType) {
        return "";
    }
    
    @Override
    public String getPrompt(String promptType) {
        return "";
    }
    
    @Override
    public List<ToolDefinition> getToolList() {
        return Arrays.asList(
            ToolRegistry.getTool("read-files"),
            new MyCustomTool()
        );
    }
    
    @Override
    public WorkflowInterface generateWorkflow() {
        // 生成 Workflow
        return new WorkflowDefinition(
            "generated-" + getName(),
            "Generated from custom skill",
            getToolList(),
            false
        );
    }
}

// 4. 创建自定义 Workflow
public class MyCustomWorkflow implements WorkflowInterface {
    @Override
    public Object execute(Map<String, Object> context) {
        // 自定义逻辑
        return null;
    }
    
    @Override
    public String getName() {
        return "my-custom-workflow";
    }
    
    @Override
    public String getDescription() {
        return "My custom workflow";
    }
    
    @Override
    public String getSpecificInfo() {
        return "Custom workflow info";
    }
    
    @Override
    public String getContext(String promptType) {
        return "";
    }
    
    @Override
    public String getPrompt(String promptType) {
        return "";
    }
    
    @Override
    public List<ToolDefinition> getSteps() {
        return Arrays.asList(
            ToolRegistry.getTool("read-files"),
            new MyCustomTool()
        );
    }
    
    @Override
    public boolean isUserConfirmed() {
        return true;
    }
    
    @Override
    public void setUserConfirmed(boolean confirmed) {
        // 固化 Workflow，不需要用户确认
    }
}

// 5. 注册组件
public class MyPlugin {
    @Mod.EventBusSubscriber(modid = "myplugin")
    public static class Registry {
        @SubscribeEvent
        public static void onInit(FMLCommonSetupEvent event) {
            // 注册 Tool
            ToolRegistry.register(new MyCustomTool());
            
            // 注册 Skill
            SkillRegistry.register(new MyCustomSkill());
            
            // 注册 Workflow
            WorkflowRegistry.register(new MyCustomWorkflow());
        }
    }
}
```

---

## ✅ 总结

### **分层重构顺序**

1. **阶段1**：统一接口层 (第3.5层) - 定义接口规范
2. **阶段2**：Tool 具体实现层 (第4层) - 重构现有工具
3. **阶段3**：Skill 具体实现层 (第4层) - 创建技能系统
4. **阶段4**：Workflow 具体实现层 (第4层) - 创建工作流系统
5. **阶段5**：Agent 协调层 (第3层) - 重构协调逻辑
6. **阶段6**：AgentProtocolHandler 层 (第2层) - 重构协议处理
7. **阶段7**：外挂插件系统 - 制定插件规范
8. **阶段8**：文档更新 - ARCHITECTURE.md 和 CONTEXT_SUMMARY.md
9. **阶段9**：测试验证 - 确保重构正确性

### **外挂插件系统**

1. **外挂 Mod 直接调用类**：导入并调用我们的类
2. **插件注册机制**：通过插件机制注册外挂组件
3. **平行层通信规则**：制定清晰的接口规范

### **架构优势**

- ✅ 层级划分更清晰
- ✅ 职责更明确
- ✅ Skills 和 Workflow 统一接口
- ✅ Memory 位置更合理
- ✅ 统一接口层
- ✅ 易于扩展
- ✅ 易于测试
- ✅ 外挂支持

---

## 🎯 新增重构阶段

### **阶段8：用户自定义工作流系统 (新增)** ⭐⭐⭐⭐⭐

#### **目标**
- 支持用户图形化创建和编辑工作流
- 提供工作流模板库和版本管理
- 增强工作流的条件分支和循环控制能力

#### **核心功能**

##### **8.1 工作流可视化编辑器**
```java
// 新增工作流可视化编辑器接口
public interface WorkflowVisualEditor {
    WorkflowTemplate createTemplate(String name);
    void addStep(WorkflowTemplate template, WorkflowStep step);
    void removeStep(WorkflowTemplate template, int stepIndex);
    void saveTemplate(WorkflowTemplate template);
    WorkflowTemplate loadTemplate(String templateId);
}
```

##### **8.2 工作流模板系统**
```java
// 工作流模板管理器
public interface WorkflowTemplateManager {
    List<WorkflowTemplate> getAllTemplates();
    WorkflowTemplate getTemplate(String templateId);
    void saveTemplate(WorkflowTemplate template);
    void deleteTemplate(String templateId);
    List<WorkflowTemplate> searchTemplates(String keyword);
}
```

##### **8.3 条件分支引擎**
```java
// 条件分支支持
public interface ConditionalWorkflow {
    boolean evaluateCondition(String condition, Map<String, Object> context);
    void executeBranch(String branchName, Map<String, Object> context);
    List<String> getAvailableBranches();
}
```

#### **操作清单**
1. 创建 `com.mineclawd.workflow.editor` 包
2. 实现工作流可视化编辑器
3. 开发工作流模板管理系统
4. 添加条件分支和循环控制支持
5. 实现工作流版本控制

---

### **阶段9：结构化数据管理系统 (新增)** ⭐⭐⭐⭐

#### **目标**
- 建立统一的数据实体层
- 支持复杂数据关系管理
- 提供数据验证和业务规则引擎

#### **核心功能**

##### **9.1 数据实体层**
```java
// 统一数据模型接口
public interface DataEntity {
    String getId();
    String getType();
    Map<String, Object> getAttributes();
    List<DataRelationship> getRelationships();
    void validate() throws ValidationException;
}
```

##### **9.2 数据关系管理**
```java
// 数据关系定义
public interface DataRelationship {
    String getType(); // DEPENDS_ON, ASSOCIATED_WITH, COMPOSED_OF
    String getSourceEntityId();
    String getTargetEntityId();
    Map<String, Object> getProperties();
    boolean isValid();
}
```

##### **9.3 数据验证引擎**
```java
// 数据验证器
public interface DataValidator {
    ValidationResult validate(DataEntity entity);
    void addValidationRule(String fieldName, ValidationRule rule);
    List<ValidationRule> getValidationRules(String entityType);
}
```

#### **操作清单**
1. 创建 `com.mineclawd.data.entity` 包
2. 定义标准数据实体接口
3. 实现数据关系管理
4. 开发数据验证引擎
5. 集成业务规则引擎

---

### **阶段10：资产管理系统增强 (新增)** ⭐⭐⭐⭐

#### **目标**
- 建立完整的资产分类体系
- 实现资产生命周期管理
- 提供资产关系图谱和权限控制

#### **核心功能**

##### **10.1 资产分类系统**
```java
// 资产分类接口
public interface AssetCategory {
    String getName();
    String getDescription();
    List<AssetTag> getTags();
    AssetLifecycle getLifecycle();
    List<AssetCategory> getSubcategories();
}
```

##### **10.2 资产生命周期管理**
```java
// 资产生命周期状态
public enum AssetLifecycle {
    DRAFT,        // 草稿
    ACTIVE,       // 活跃
    ARCHIVED,     // 归档
    DELETED       // 删除
}

// 生命周期管理器
public interface AssetLifecycleManager {
    void transition(Asset asset, AssetLifecycle newState);
    boolean canTransition(Asset asset, AssetLifecycle newState);
    List<AssetLifecycle> getAvailableTransitions(Asset asset);
}
```

##### **10.3 资产关系图谱**
```java
// 资产关系管理器
public interface AssetRelationshipManager {
    void addRelationship(Asset source, Asset target, RelationshipType type);
    void removeRelationship(Asset source, Asset target);
    List<Asset> getRelatedAssets(Asset asset, RelationshipType type);
    AssetGraph buildGraph(Asset root, int depth);
}
```

#### **操作清单**
1. 创建 `com.mineclawd.assets.management` 包
2. 实现资产分类系统
3. 开发资产生命周期管理
4. 构建资产关系图谱
5. 添加权限控制系统

---

### **阶段11：业务规则引擎层 (新增)** ⭐⭐⭐

#### **目标**
- 提供可配置的业务规则引擎
- 支持复杂业务流程的规则管理
- 实现规则执行和监控

#### **核心功能**

##### **11.1 规则定义**
```java
// 业务规则接口
public interface BusinessRule {
    String getName();
    String getDescription();
    RuleCondition getCondition();
    RuleAction getAction();
    RulePriority getPriority();
    boolean isEnabled();
}
```

##### **11.2 规则执行器**
```java
// 规则执行引擎
public interface RuleEngine {
    RuleExecutionResult execute(RuleContext context);
    void addRule(BusinessRule rule);
    void removeRule(String ruleName);
    List<BusinessRule> getRules();
    void enableRule(String ruleName);
    void disableRule(String ruleName);
}
```

##### **11.3 业务流程监控**
```java
// 流程监控器
public interface ProcessMonitor {
    void startProcess(String processId, Map<String, Object> context);
    void updateProcess(String processId, ProcessStatus status);
    ProcessStatus getProcessStatus(String processId);
    List<ProcessLog> getProcessLogs(String processId);
}
```

#### **操作清单**
1. 创建 `com.mineclawd.business.rules` 包
2. 定义业务规则接口
3. 实现规则执行引擎
4. 开发业务流程监控
5. 集成到现有架构中

---

### **阶段12：用户界面适配器层 (新增)** ⭐⭐⭐

#### **目标**
- 提供友好的用户界面工具
- 支持可视化配置和管理
- 构建插件市场机制

#### **核心功能**

##### **12.1 工作流可视化编辑器界面**
```java
// 可视化编辑器接口
public interface WorkflowVisualEditorUI {
    void openEditor(WorkflowTemplate template);
    void saveTemplate(WorkflowTemplate template);
    void exportTemplate(WorkflowTemplate template, String format);
    void importTemplate(String filePath);
}
```

##### **12.2 资产管理系统界面**
```java
// 资产管理界面
public interface AssetManagementUI {
    void showAssetCatalog();
    void showAssetDetails(Asset asset);
    void createAsset(AssetCategory category);
    void editAsset(Asset asset);
    void deleteAsset(Asset asset);
}
```

##### **12.3 插件市场界面**
```java
// 插件市场
public interface PluginMarketplace {
    List<PluginInfo> getAvailablePlugins();
    void installPlugin(String pluginId);
    void uninstallPlugin(String pluginId);
    void updatePlugin(String pluginId);
    PluginInfo getPluginInfo(String pluginId);
}
```

#### **操作清单**
1. 创建 `com.mineclawd.ui.adapter` 包
2. 开发工作流可视化编辑器界面
3. 实现资产管理系统界面
4. 构建插件市场机制
5. 集成到 Minecraft 界面中

---

## 🗺️ 实施路线图

### **短期目标 (1-2周)**
- ✅ 完成阶段1-7的基础重构
- 🔄 实现工作流模板持久化（阶段8.2）
- 🔄 添加基础数据实体定义（阶段9.1）
- 🔄 完善资产分类系统（阶段10.1）

### **中期目标 (1-2月)**
- 🔄 开发可视化工作流编辑器（阶段8.1）
- 🔄 实现数据关系管理（阶段9.2）
- 🔄 构建资产生命周期管理（阶段10.2）
- 🔄 开发业务规则引擎（阶段11）

### **长期目标 (3-6月)**
- 🔄 实现完整的用户界面适配器（阶段12）
- 🔄 构建插件市场生态系统
- 🔄 优化性能和用户体验
- 🔄 建立社区支持体系

### **关键里程碑**
1. **里程碑1**：基础重构完成，支持插件化扩展
2. **里程碑2**：用户自定义工作流系统上线
3. **里程碑3**：结构化数据管理系统完善
4. **里程碑4**：完整的用户界面适配器发布
5. **里程碑5**：生态系统成熟，社区活跃

---

## 📈 架构改进总结

### **当前架构优势保持**
- ✅ 分层清晰，职责分明
- ✅ 统一接口，简化调用
- ✅ 插件化设计，支持扩展
- ✅ 动态生成，灵活适应

### **新增架构优势**
- 🚀 **用户友好性**：提供可视化工具和界面适配器
- 🚀 **数据管理**：增强结构化数据处理能力
- 🚀 **业务逻辑**：支持更复杂的业务流程
- 🚀 **生态系统**：构建更完善的插件生态

### **技术挑战应对**
1. **性能优化**：通过分层设计确保各层性能
2. **安全性**：保持现有的安全机制，增强权限控制
3. **兼容性**：确保向后兼容，平滑升级
4. **扩展性**：通过插件系统支持无限扩展

通过上述重构计划的完善，MineClawd 将能够更好地支持用户自定义工作流，提供更强大的结构化数据管理能力，并构建完整的生态系统。
