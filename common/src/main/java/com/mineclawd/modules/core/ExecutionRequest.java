package com.mineclawd.modules.core;

import java.util.Map;

/**
 * 执行请求格式
 * 
 * <p>标准化的调用请求格式，用于模块间的统一通信</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ExecutionRequest {
    
    private final String requestId;
    private final String moduleId;
    private final String operation;
    private final Map<String, Object> parameters;
    private final Map<String, Object> context;
    private final RequestOptions options;
    
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
    
    public String getRequestId() {
        return requestId;
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
    
    public RequestOptions getOptions() {
        return options;
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
        private String requestId;
        private String moduleId;
        private String operation = "execute";
        private Map<String, Object> parameters = Map.of();
        private Map<String, Object> context = Map.of();
        private RequestOptions options = RequestOptions.DEFAULT;
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
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
        
        public Builder options(RequestOptions options) {
            this.options = options;
            return this;
        }
        
        public ExecutionRequest build() {
            if (requestId == null) {
                requestId = generateRequestId();
            }
            if (moduleId == null) {
                throw new IllegalArgumentException("moduleId不能为空");
            }
            
            return new ExecutionRequest(requestId, moduleId, operation, parameters, context, options);
        }
        
        private String generateRequestId() {
            return "req_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        }
    }
}