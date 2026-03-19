package com.mineclawd.modules.core;

/**
 * LLM描述信息
 * 
 * <p>用于Function Call的LLM可理解描述</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class LLMDescription {
    
    private final String functionName;
    private final String description;
    private final String parameters;
    
    public LLMDescription(String functionName, String description, String parameters) {
        this.functionName = functionName;
        this.description = description;
        this.parameters = parameters;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getParameters() {
        return parameters;
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
        private String functionName;
        private String description;
        private String parameters = "{}";
        
        public Builder functionName(String functionName) {
            this.functionName = functionName;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder parameters(String parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public LLMDescription build() {
            if (functionName == null) {
                functionName = "unnamed_function";
            }
            if (description == null) {
                description = "No description provided";
            }
            
            return new LLMDescription(functionName, description, parameters);
        }
    }
}