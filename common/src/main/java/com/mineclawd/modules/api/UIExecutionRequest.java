package com.mineclawd.modules.api;

import java.util.Map;

/**
 * UI执行请求
 * 
 * <p>UI友好的模块执行请求格式</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class UIExecutionRequest {
    
    private final String moduleId;
    private final String operation;
    private final Map<String, Object> parameters;
    private final Map<String, Object> context;
    
    public UIExecutionRequest(String moduleId, String operation, 
                             Map<String, Object> parameters, Map<String, Object> context) {
        this.moduleId = moduleId;
        this.operation = operation;
        this.parameters = parameters;
        this.context = context;
    }
    
    public String getModuleId() {
        return moduleId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private String moduleId;
        private String operation = "execute";
        private Map<String, Object> parameters = Map.of();
        private Map<String, Object> context = Map.of();
        
        public Builder moduleId(String moduleId) {
            this.moduleId = moduleId;
            return this;
        }
        
        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }
        
        public UIExecutionRequest build() {
            if (moduleId == null) {
                throw new IllegalArgumentException("moduleId不能为空");
            }
            
            return new UIExecutionRequest(moduleId, operation, parameters, context);
        }
    }
}