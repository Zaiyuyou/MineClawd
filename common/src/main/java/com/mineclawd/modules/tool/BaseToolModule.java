package com.mineclawd.modules.tool;

import com.mineclawd.modules.core.*;
import java.util.Map;

/**
 * 基础Tool模块实现
 * 
 * <p>简化版Tool模块实现，只包含核心功能</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public abstract class BaseToolModule implements ToolModule {
    
    protected final String moduleId;
    protected final String moduleName;
    protected final String moduleDescription;
    protected final LLMDescription llmDescription;
    protected final MCPDescription mcpDescription;
    
    public BaseToolModule(String moduleName, String moduleDescription, 
                         LLMDescription llmDescription, MCPDescription mcpDescription) {
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
        this.llmDescription = llmDescription;
        this.mcpDescription = mcpDescription;
        this.moduleId = "tool-" + moduleName.replace(" ", "-").toLowerCase();
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
            // 验证参数
            ValidationResult validation = validateParameters(request.getParameters());
            if (!validation.isValid()) {
                return ExecutionResult.failure(request.getRequestId(), 
                    "参数验证失败: " + validation.getMessage());
            }
            
            // 执行具体逻辑（由子类实现）
            Object result = executeToolLogic(request.getParameters(), request.getContext());
            
            return ExecutionResult.success(request.getRequestId(), result);
        } catch (Exception e) {
            return ExecutionResult.failure(request.getRequestId(), 
                "Tool执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行具体的工具逻辑（由子类实现）
     */
    protected abstract Object executeToolLogic(Map<String, Object> parameters, Map<String, Object> context);
}