package com.mineclawd.modules.kubejs;

import com.mineclawd.modules.core.*;
import com.mineclawd.kubejs.KubeJSOfflineToolExecutor;
import java.util.Map;

/**
 * KubeJSOffline模块
 * 
 * <p>提供KubeJSOffline知识库查询和调试功能</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class KubeJSOfflineModule implements ExecutableModule {
    
    private final String moduleId;
    private final String moduleName;
    private final String moduleDescription;
    private final LLMDescription llmDescription;
    private final MCPDescription mcpDescription;
    
    public KubeJSOfflineModule() {
        this.moduleId = "kubejs-offline";
        this.moduleName = "KubeJSOffline工具";
        this.moduleDescription = "提供KubeJSOffline知识库查询和调试功能";
        this.llmDescription = createLLMDescription();
        this.mcpDescription = createMCPDescription();
    }
    
    @Override
    public String getModuleId() {
        return moduleId;
    }
    
    @Override
    public String getModuleName() {
        return moduleName;
    }
    
    @Override
    public ModuleType getModuleType() {
        return ModuleType.TOOL;
    }
    
    @Override
    public String getModuleDescription() {
        return moduleDescription;
    }
    
    @Override
    public LLMDescription getLLMDescription() {
        return llmDescription;
    }
    
    @Override
    public MCPDescription getMCPDescription() {
        return mcpDescription;
    }
    
    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        try {
            String operation = request.getOperation();
            Map<String, Object> parameters = request.getParameters();
            
            switch (operation) {
                case "check_availability":
                    return executeCheckAvailability();
                case "check_documentation":
                    return executeCheckDocumentation();
                case "generate_knowledge_base":
                    return executeGenerateKnowledgeBase();
                case "query_class":
                    String className = (String) parameters.get("class_name");
                    return executeQueryClass(className);
                case "query_event":
                    String eventName = (String) parameters.get("event_name");
                    return executeQueryEvent(eventName);
                case "debug_info":
                    return executeDebugInfo();
                default:
                    return ExecutionResult.failure(request.getRequestId(), 
                        "未知操作: " + operation);
            }
        } catch (Exception e) {
            return ExecutionResult.failure(request.getRequestId(), 
                "KubeJSOffline执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查KubeJSOffline可用性
     */
    private ExecutionResult executeCheckAvailability() {
        boolean isAvailable = KubeJSOfflineToolExecutor.isKubeJSOfflineAvailable();
        
        if (isAvailable) {
            return ExecutionResult.success("check_availability", 
                Map.of("available", true, "message", "KubeJSOffline mod已安装"));
        } else {
            return ExecutionResult.success("check_availability", 
                Map.of("available", false, "message", "KubeJSOffline mod未安装"));
        }
    }
    
    /**
     * 检查文档是否已生成
     */
    private ExecutionResult executeCheckDocumentation() {
        boolean isGenerated = KubeJSOfflineToolExecutor.isDocumentationGenerated();
        
        if (isGenerated) {
            return ExecutionResult.success("check_documentation", 
                Map.of("generated", true, "message", "KubeJSOffline文档已生成"));
        } else {
            return ExecutionResult.success("check_documentation", 
                Map.of("generated", false, "message", "KubeJSOffline文档未生成，请运行: /kubejs offline_docs generate"));
        }
    }
    
    /**
     * 生成知识库
     */
    private ExecutionResult executeGenerateKnowledgeBase() {
        KubeJSOfflineToolExecutor.ToolExecutionResult result = KubeJSOfflineToolExecutor.generateKnowledgeBase();
        
        return ExecutionResult.success("generate_knowledge_base", 
            Map.of("success", result.success(), "message", result.output()));
    }
    
    /**
     * 查询类信息
     */
    private ExecutionResult executeQueryClass(String className) {
        if (className == null || className.trim().isEmpty()) {
            return ExecutionResult.failure("query_class", "类名不能为空");
        }
        
        // 这里需要调用实际的查询方法
        // 由于KubeJSOfflineToolExecutor.queryClassInfo方法未完全实现，我们返回模拟结果
        String message = "查询类信息: " + className + "\n" +
                        "KubeJSOffline查询功能需要完整的实现";
        
        return ExecutionResult.success("query_class", 
            Map.of("class_name", className, "message", message));
    }
    
    /**
     * 查询事件信息
     */
    private ExecutionResult executeQueryEvent(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            return ExecutionResult.failure("query_event", "事件名不能为空");
        }
        
        // 这里需要调用实际的查询方法
        // 由于KubeJSOfflineToolExecutor.queryEventInfo方法未完全实现，我们返回模拟结果
        String message = "查询事件信息: " + eventName + "\n" +
                        "KubeJSOffline查询功能需要完整的实现";
        
        return ExecutionResult.success("query_event", 
            Map.of("event_name", eventName, "message", message));
    }
    
    /**
     * 获取调试信息
     */
    private ExecutionResult executeDebugInfo() {
        KubeJSOfflineToolExecutor.ToolExecutionResult result = KubeJSOfflineToolExecutor.getDebugInfo();
        
        return ExecutionResult.success("debug_info", 
            Map.of("success", result.success(), "message", result.output()));
    }
    
    /**
     * 创建LLM描述
     */
    private LLMDescription createLLMDescription() {
        return LLMDescription.builder()
            .functionName("kubejsoffline_tool")
            .description("KubeJSOffline知识库查询和调试工具")
            .parameters("{\"operation\": \"string\", \"parameters\": {\"class_name\": \"string\", \"event_name\": \"string\"}}")
            .build();
    }
    
    /**
     * 创建MCP描述
     */
    private MCPDescription createMCPDescription() {
        return MCPDescription.builder()
            .toolName("kubejsoffline")
            .description("KubeJSOffline知识库查询工具")
            .inputSchema("{\"type\": \"object\", \"properties\": {\"operation\": {\"type\": \"string\", \"enum\": [\"check_availability\", \"check_documentation\", \"generate_knowledge_base\", \"query_class\", \"query_event\", \"debug_info\"]}, \"parameters\": {\"type\": \"object\", \"properties\": {\"class_name\": {\"type\": \"string\"}, \"event_name\": {\"type\": \"string\"}}}}}")
            .outputSchema("{\"type\": \"object\", \"properties\": {\"success\": {\"type\": \"boolean\"}, \"message\": {\"type\": \"string\"}, \"data\": {\"type\": \"object\"}}}")
            .build();
    }
}