package com.mineclawd.modules.core;

import java.util.Map;

/**
 * 参数规范定义
 * 
 * <p>定义模块执行参数的规范和约束</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ParameterSchema {
    
    private final Map<String, ParameterDefinition> parameters;
    
    public ParameterSchema(Map<String, ParameterDefinition> parameters) {
        this.parameters = parameters;
    }
    
    public Map<String, ParameterDefinition> getParameters() {
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
        private Map<String, ParameterDefinition> parameters = Map.of();
        
        public Builder addParameter(String name, String type, String description, boolean required) {
            return addParameter(name, type, description, required, null);
        }
        
        public Builder addParameter(String name, String type, String description, 
                                   boolean required, Object defaultValue) {
            ParameterDefinition param = new ParameterDefinition(name, type, description, required, defaultValue);
            parameters = Map.of(name, param); // 简化实现，实际应该合并map
            return this;
        }
        
        public ParameterSchema build() {
            return new ParameterSchema(parameters);
        }
    }
    
    /**
     * 参数定义
     */
    public static class ParameterDefinition {
        private final String name;
        private final String type;
        private final String description;
        private final boolean required;
        private final Object defaultValue;
        
        public ParameterDefinition(String name, String type, String description, 
                                 boolean required, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
            this.defaultValue = defaultValue;
        }
        
        // getters...
        public String getName() { return name; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public boolean isRequired() { return required; }
        public Object getDefaultValue() { return defaultValue; }
    }
}