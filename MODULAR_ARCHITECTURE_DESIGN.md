# MineClawd 模块化解耦架构设计

## 📋 目录

- [设计目标](#设计目标)
- [核心设计理念](#核心设计理念)
- [格式化接口规范](#格式化接口规范)
- [模块化解耦架构](#模块化解耦架构)
- [动态注册和发现机制](#动态注册和发现机制)
- [UI和LLM调用接口](#ui和llm调用接口)
- [实施计划](#实施计划)

---

## 🎯 设计目标

### **核心设计原则**
1. **完全解耦**：Tool、Skill、Workflow 完全独立，互不依赖
2. **格式化接口**：通过标准化的接口规范进行通信
3. **动态注册**：支持运行时动态注册和发现
4. **自由调用**：LLM和UI可以通过统一接口自由调用任何模块
5. **实时修改**：支持运行时动态修改模块配置和行为

### **技术目标**
- 实现真正的模块化解耦
- 提供标准化的格式化接口
- 支持动态注册和发现机制
- 提供统一的调用接口给LLM和UI
- 支持实时修改和热更新

---

## 🏗️ 核心设计理念

### **模块化架构概览**

```
┌─────────────────────────────────────────────────────────────────────┐
│ 调用层 (LLM/UI/Player)                                              │
│ • 统一的格式化调用接口                                              │
│ • 支持同步/异步调用                                                 │
│ • 提供调用结果回调                                                  │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ 格式化接口调用
                              │
┌─────────────────────────────────────────────────────────────────────┐
│ 模块管理层                                                          │
│ • 模块注册中心                                                      │
│ • 模块发现服务                                                      │
│ • 调用路由分发                                                      │
│ • 生命周期管理                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              ↑
                              │ 模块接口调用
                              │
┌─────────────┬─────────────┬─────────────┬─────────────┬─────────────┐
│   Tool模块  │  Skill模块  │ Workflow模块│  Asset模块  │  Data模块   │
│ • 原子操作  │ • 动态生成  │ • 流程执行  │ • 资源管理  │ • 数据操作  │
│ • 独立实现  │ • 独立实现  │ • 独立实现  │ • 独立实现  │ • 独立实现  │
└─────────────┴─────────────┴─────────────┴─────────────┴─────────────┘
```

### **设计原则**
1. **接口隔离**：每个模块只依赖格式化接口，不依赖其他模块
2. **单一职责**：每个模块专注于单一功能领域
3. **开放封闭**：对扩展开放，对修改封闭
4. **依赖倒置**：高层模块不依赖低层模块，都依赖抽象接口

---

## 📝 格式化接口规范

### **1. 基础模块接口**

```java
package com.mineclawd.modules.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基础模块接口
 * 所有模块都必须实现此接口
 */
public interface BaseModule {
    
    /**
     * 模块唯一标识
     */
    String getModuleId();
    
    /**
     * 模块名称
     */
    String getModuleName();
    
    /**
     * 模块版本
     */
    String getModuleVersion();
    
    /**
     * 模块描述
     */
    String getModuleDescription();
    
    /**
     * 模块类型
     */
    ModuleType getModuleType();
    
    /**
     * 获取模块元数据
     */
    ModuleMetadata getMetadata();
    
    /**
     * 获取模块能力描述（用于LLM理解）
     */
    ModuleCapability getCapability();
    
    /**
     * 验证模块配置
     */
    ValidationResult validateConfiguration(Map<String, Object> config);
    
    /**
     * 获取模块状态
     */
    ModuleStatus getStatus();
}
```

### **2. 可执行模块接口**

```java
package com.mineclawd.modules.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 可执行模块接口
 * 支持执行操作的模块实现此接口
 */
public interface ExecutableModule extends BaseModule {
    
    /**
     * 执行模块操作
     * 
     * @param request 执行请求
     * @return 执行结果
     */
    ExecutionResult execute(ExecutionRequest request);
    
    /**
     * 异步执行模块操作
     * 
     * @param request 执行请求
     * @return 异步执行结果
     */
    CompletableFuture<ExecutionResult> executeAsync(ExecutionRequest request);
    
    /**
     * 获取执行参数规范
     */
    ParameterSchema getParameterSchema();
    
    /**
     * 验证执行参数
     */
    ValidationResult validateParameters(Map<String, Object> parameters);
    
    /**
     * 获取执行示例
     */
    List<ExecutionExample> getExecutionExamples();
}
```

### **3. 格式化数据定义**

#### **执行请求格式**
```java
package com.mineclawd.modules.core;

import java.util.Map;

/**
 * 执行请求格式
 * 标准化的调用请求格式
 */
public class ExecutionRequest {
    
    private final String requestId;           // 请求ID
    private final String moduleId;           // 目标模块ID
    private final String operation;          // 操作类型
    private final Map<String, Object> parameters; // 参数
    private final Map<String, Object> context;    // 上下文信息
    private final RequestOptions options;    // 请求选项
    
    public ExecutionRequest(String requestId, String moduleId, String operation,
                           Map<String, Object> parameters, Map<String, Object> context,
                           RequestOptions options) {
        this.requestId = requestId;
        this.moduleId = moduleId;
        this.operation = operation;
        this.parameters = parameters;
        this.context = context;
        this.options = options;
    }
    
    // getters...
}
```

#### **执行结果格式**
```java
package com.mineclawd.modules.core;

import java.util.Map;

/**
 * 执行结果格式
 * 标准化的调用结果格式
 */
public class ExecutionResult {
    
    private final String requestId;           // 对应的请求ID
    private final boolean success;           // 是否成功
    private final Object data;               // 结果数据
    private final String message;            // 结果消息
    private final Map<String, Object> metadata; // 元数据
    private final List<ExecutionError> errors; // 错误信息
    
    public ExecutionResult(String requestId, boolean success, Object data,
                          String message, Map<String, Object> metadata,
                          List<ExecutionError> errors) {
        this.requestId = requestId;
        this.success = success;
        this.data = data;
        this.message = message;
        this.metadata = metadata;
        this.errors = errors;
    }
    
    // 静态工厂方法
    public static ExecutionResult success(String requestId, Object data) {
        return new ExecutionResult(requestId, true, data, "Success", Map.of(), List.of());
    }
    
    public static ExecutionResult failure(String requestId, String message, List<ExecutionError> errors) {
        return new ExecutionResult(requestId, false, null, message, Map.of(), errors);
    }
    
    // getters...
}
```

---

## 🔧 模块化解耦架构

### **1. Tool模块设计**

```java
package com.mineclawd.modules.tool;

import com.mineclawd.modules.core.*;
import java.util.Map;

/**
 * Tool模块接口
 * 原子操作模块，提供具体的功能实现
 */
public interface ToolModule extends ExecutableModule {
    
    /**
     * 获取Tool的OpenAI Schema
     */
    OpenAIToolSchema getOpenAISchema();
    
    /**
     * 获取Tool的Vertex AI Schema
     */
    VertexAIFunctionSchema getVertexAISchema();
    
    /**
     * 获取Tool分类
     */
    ToolCategory getCategory();
    
    /**
     * 获取Tool标签
     */
    List<String> getTags();
    
    /**
     * 验证Tool权限
     */
    PermissionResult checkPermission(UserInfo user, Map<String, Object> context);
}
```

### **2. Skill模块设计**

```java
package com.mineclawd.modules.skill;

import com.mineclawd.modules.core.*;
import java.util.Map;

/**
 * Skill模块接口
 * 动态工作流生成模块
 */
public interface SkillModule extends ExecutableModule {
    
    /**
     * 生成Workflow
     */
    WorkflowGenerationResult generateWorkflow(WorkflowGenerationRequest request);
    
    /**
     * 获取Skill依赖的Tool列表
     */
    List<String> getDependentToolIds();
    
    /**
     * 获取Skill配置选项
     */
    SkillConfiguration getConfigurationOptions();
    
    /**
     * 更新Skill配置
     */
    UpdateResult updateConfiguration(Map<String, Object> newConfig);
}
```

### **3. Workflow模块设计**

```java
package com.mineclawd.modules.workflow;

import com.mineclawd.modules.core.*;
import java.util.Map;

/**
 * Workflow模块接口
 * 工作流执行模块
 */
public interface WorkflowModule extends ExecutableModule {
    
    /**
     * 获取Workflow步骤定义
     */
    List<WorkflowStep> getSteps();
    
    /**
     * 获取Workflow变量定义
     */
    Map<String, VariableDefinition> getVariableDefinitions();
    
    /**
     * 获取Workflow条件定义
     */
    List<ConditionDefinition> getConditionDefinitions();
    
    /**
     * 验证Workflow完整性
     */
    ValidationResult validateWorkflow();
    
    /**
     * 导出Workflow定义
     */
    WorkflowDefinition exportDefinition();
    
    /**
     * 导入Workflow定义
     */
    ImportResult importDefinition(WorkflowDefinition definition);
}
```

### **4. 模块实现示例**

#### **具体Tool模块实现**
```java
package com.mineclawd.modules.tool.impl;

import com.mineclawd.modules.tool.ToolModule;
import com.mineclawd.modules.core.*;
import java.util.Map;

/**
 * 文件读取Tool模块
 */
public class FileReadToolModule implements ToolModule {
    
    private static final String MODULE_ID = "file-read-tool";
    
    @Override
    public String getModuleId() { return MODULE_ID; }
    
    @Override
    public String getModuleName() { return "文件读取工具"; }
    
    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        // 解析参数
        String filePath = (String) request.getParameters().get("file_path");
        
        try {
            // 执行文件读取逻辑
            String content = readFileContent(filePath);
            
            return ExecutionResult.success(request.getRequestId(), content);
        } catch (Exception e) {
            return ExecutionResult.failure(request.getRequestId(), 
                "文件读取失败: " + e.getMessage(), List.of());
        }
    }
    
    @Override
    public ParameterSchema getParameterSchema() {
        return ParameterSchema.builder()
            .addParameter("file_path", "string", "文件路径", true)
            .addParameter("encoding", "string", "文件编码", false, "UTF-8")
            .build();
    }
    
    @Override
    public OpenAIToolSchema getOpenAISchema() {
        return OpenAIToolSchema.builder()
            .name("read_file")
            .description("读取文件内容")
            .parameters(getParameterSchema().toOpenAISchema())
            .build();
    }
    
    // 其他接口实现...
}
```

---

## 🔄 动态注册和发现机制

### **1. 模块注册中心**

```java
package com.mineclawd.modules.registry;

import com.mineclawd.modules.core.BaseModule;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 模块注册中心
 * 负责模块的动态注册、发现和管理
 */
public interface ModuleRegistry {
    
    /**
     * 注册模块
     */
    RegistrationResult registerModule(BaseModule module);
    
    /**
     * 注销模块
     */
    UnregistrationResult unregisterModule(String moduleId);
    
    /**
     * 获取模块
     */
    BaseModule getModule(String moduleId);
    
    /**
     * 获取所有模块
     */
    List<BaseModule> getAllModules();
    
    /**
     * 根据类型获取模块
     */
    List<BaseModule> getModulesByType(ModuleType type);
    
    /**
     * 搜索模块
     */
    List<BaseModule> searchModules(String keyword, ModuleType type);
    
    /**
     * 监听模块注册事件
     */
    void addRegistrationListener(ModuleRegistrationListener listener);
    
    /**
     * 获取模块统计信息
     */
    ModuleStatistics getStatistics();
}
```

### **2. 模块发现服务**

```java
package com.mineclawd.modules.discovery;

import com.mineclawd.modules.core.BaseModule;
import java.util.List;

/**
 * 模块发现服务
 * 负责自动发现和加载模块
 */
public interface ModuleDiscoveryService {
    
    /**
     * 扫描模块目录
     */
    DiscoveryResult scanModuleDirectory(String directoryPath);
    
    /**
     * 从JAR文件加载模块
     */
    DiscoveryResult loadModulesFromJar(String jarPath);
    
    /**
     * 从类路径加载模块
     */
    DiscoveryResult loadModulesFromClasspath();
    
    /**
     * 热加载模块
     */
    HotLoadResult hotLoadModule(String modulePath);
    
    /**
     * 热卸载模块
     */
    HotUnloadResult hotUnloadModule(String moduleId);
    
    /**
     * 获取发现配置
     */
    DiscoveryConfiguration getConfiguration();
    
    /**
     * 更新发现配置
     */
    UpdateResult updateConfiguration(DiscoveryConfiguration config);
}
```

---

## 🖥️ UI和LLM调用接口

### **1. 统一调用接口**

```java
package com.mineclawd.modules.api;

import com.mineclawd.modules.core.ExecutionRequest;
import com.mineclawd.modules.core.ExecutionResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 统一调用接口
 * 为LLM和UI提供统一的模块调用入口
 */
public interface UnifiedModuleAPI {
    
    /**
     * 调用模块
     */
    ExecutionResult callModule(ExecutionRequest request);
    
    /**
     * 异步调用模块
     */
    CompletableFuture<ExecutionResult> callModuleAsync(ExecutionRequest request);
    
    /**
     * 批量调用模块
     */
    List<ExecutionResult> callModules(List<ExecutionRequest> requests);
    
    /**
     * 获取模块列表
     */
    List<ModuleInfo> getAvailableModules();
    
    /**
     * 获取模块详情
     */
    ModuleDetail getModuleDetail(String moduleId);
    
    /**
     * 搜索模块
     */
    List<ModuleInfo> searchModules(ModuleSearchCriteria criteria);
    
    /**
     * 获取调用历史
     */
    List<CallHistory> getCallHistory(String moduleId, int limit);
    
    /**
     * 订阅模块事件
     */
    Subscription subscribeToModuleEvents(String moduleId, ModuleEventListener listener);
}
```

### **2. LLM专用接口**

```java
package com.mineclawd.modules.api.llm;

import com.mineclawd.modules.core.BaseModule;
import java.util.List;

/**
 * LLM专用接口
 * 为LLM提供优化的模块调用接口
 */
public interface LLMModuleAPI {
    
    /**
     * 获取所有模块的OpenAI Schema
     */
    List<OpenAIToolSchema> getAllOpenAISchemas();
    
    /**
     * 获取模块的OpenAI Schema
     */
    OpenAIToolSchema getOpenAISchema(String moduleId);
    
    /**
     * 执行LLM函数调用
     */
    LLMExecutionResult executeFunctionCall(LLMFunctionCall call);
    
    /**
     * 批量执行函数调用
     */
    List<LLMExecutionResult> executeFunctionCalls(List<LLMFunctionCall> calls);
    
    /**
     * 获取LLM可用的模块列表
     */
    List<LLMModuleInfo> getLLMAvailableModules();
    
    /**
     * 验证LLM函数调用
     */
    ValidationResult validateFunctionCall(LLMFunctionCall call);
}
```

### **3. UI专用接口**

```java
package com.mineclawd.modules.api.ui;

import com.mineclawd.modules.core.BaseModule;
import java.util.List;

/**
 * UI专用接口
 * 为UI提供优化的模块管理接口
 */
public interface UIModuleAPI {
    
    /**
     * 获取模块的可视化配置
     */
    UIModuleConfiguration getUIConfiguration(String moduleId);
    
    /**
     * 更新模块配置
     */
    UpdateResult updateModuleConfiguration(String moduleId, UIModuleConfiguration config);
    
    /**
     * 获取模块的可视化界面
     */
    UIModuleInterface getUIModuleInterface(String moduleId);
    
    /**
     * 执行模块操作（UI友好格式）
     */
    UIExecutionResult executeModuleOperation(UIExecutionRequest request);
    
    /**
     * 获取模块执行历史（UI友好格式）
     */
    List<UIExecutionHistory> getUIExecutionHistory(String moduleId);
    
    /**
     * 导出模块配置
     */
    ExportResult exportModuleConfiguration(String moduleId);
    
    /**
     * 导入模块配置
     */
    ImportResult importModuleConfiguration(String moduleId, ImportData data);
}
```

### **4. 使用示例**

#### **LLM调用示例**
```java
// 初始化LLM API
LLMModuleAPI llmAPI = new DefaultLLMModuleAPI(moduleRegistry);

// 获取所有可用的模块Schema
List<OpenAIToolSchema> schemas = llmAPI.getAllOpenAISchemas();

// LLM返回函数调用
LLMFunctionCall call = new LLMFunctionCall("file-read-tool", Map.of("file_path", "/path/to/file"));

// 执行函数调用
LLMExecutionResult result = llmAPI.executeFunctionCall(call);

// 处理结果
if (result.isSuccess()) {
    String fileContent = (String) result.getData();
    System.out.println("文件内容: " + fileContent);
}
```

#### **UI调用示例**
```java
// 初始化UI API
UIModuleAPI uiAPI = new DefaultUIModuleAPI(moduleRegistry);

// 获取模块的可视化配置
UIModuleConfiguration config = uiAPI.getUIConfiguration("file-read-tool");

// 在UI中显示配置选项
for (UIConfigOption option : config.getOptions()) {
    // 渲染配置界面
    renderConfigOption(option);
}

// 用户执行操作
UIExecutionRequest request = new UIExecutionRequest("file-read-tool", "read", 
    Map.of("file_path", userSelectedPath));

// 执行操作
UIExecutionResult result = uiAPI.executeModuleOperation(request);

// 显示结果
if (result.isSuccess()) {
    showSuccessMessage(result.getMessage());
    displayFileContent(result.getData());
} else {
    showErrorMessage(result.getErrorMessage());
}
```

---

## 🗺️ 实施计划

### **阶段1：基础架构实现 (2-3周)**
1. 实现格式化接口规范
2. 开发模块注册中心
3. 实现基础模块接口
4. 创建统一调用接口

### **阶段2：核心模块实现 (3-4周)**
1. 实现Tool模块系统
2. 实现Skill模块系统
3. 实现Workflow模块系统
4. 开发模块发现服务

### **阶段3：调用接口实现 (2周)**
1. 实现LLM专用接口
2. 实现UI专用接口
3. 开发调用路由和分发
4. 实现错误处理和监控

### **阶段4：集成和优化 (2周)**
1. 集成到现有MineClawd架构
2. 性能优化和测试
3. 文档和示例编写
4. 用户体验优化

### **关键里程碑**
1. **里程碑1**：基础架构完成，支持模块注册
2. **里程碑2**：核心模块系统实现完成
3. **里程碑3**：LLM和UI调用接口上线
4. **里程碑4**：完整的模块化系统发布

---

## 📈 设计优势

### **技术优势**
1. **完全解耦**：模块之间零依赖，独立开发和部署
2. **标准化接口**：统一的格式化接口，便于扩展和维护
3. **动态注册**：支持运行时动态加载和卸载模块
4. **多调用方支持**：同时支持LLM、UI、玩家等多种调用方式
5. **实时修改**：支持运行时动态修改模块配置和行为

### **业务优势**
1. **灵活性**：LLM和UI可以自由调用任何模块
2. **可扩展性**：轻松添加新的模块类型和功能
3. **可维护性**：模块独立，便于调试和问题排查
4. **用户体验**：提供友好的UI接口和LLM优化接口
5. **生态系统**：为第三方模块开发提供标准化接口

### **解决的关键问题**
✅ **解耦问题**：通过格式化接口实现完全解耦
✅ **调用问题**：提供统一的调用接口给LLM和UI
✅ **扩展问题**：支持动态注册和发现新模块
✅ **修改问题**：支持运行时实时修改模块配置
✅ **集成问题**：与现有架构平滑集成

通过这套模块化解耦架构，MineClawd将能够实现真正的模块化设计，让Tool、Skill、Workflow等模块通过格式化接口自由调用、响应和修改，为LLM和玩家提供强大的扩展能力！