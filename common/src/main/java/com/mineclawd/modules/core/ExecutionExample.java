package com.mineclawd.modules.core;

import java.util.Map;

/**
 * 执行示例
 * 
 * <p>提供模块执行的示例，用于文档和LLM理解</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ExecutionExample {
    
    private final String name;
    private final String description;
    private final Map<String, Object> parameters;
    private final Object expectedResult;
    
    public ExecutionExample(String name, String description, 
                          Map<String, Object> parameters, Object expectedResult) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.expectedResult = expectedResult;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public Object getExpectedResult() {
        return expectedResult;
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
        private String name;
        private String description;
        private Map<String, Object> parameters = Map.of();
        private Object expectedResult;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder expectedResult(Object expectedResult) {
            this.expectedResult = expectedResult;
            return this;
        }
        
        public ExecutionExample build() {
            if (name == null) {
                name = "Unnamed Example";
            }
            if (description == null) {
                description = "No description provided";
            }
            
            return new ExecutionExample(name, description, parameters, expectedResult);
        }
    }
}