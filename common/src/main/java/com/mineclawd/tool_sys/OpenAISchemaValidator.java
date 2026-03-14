package com.mineclawd.tool_sys;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * OpenAI工具定义规范验证器
 * 确保工具定义符合OpenAI函数调用API标准
 */
public final class OpenAISchemaValidator {
    
    private OpenAISchemaValidator() {
        // 私有构造函数
    }
    
    /**
     * 验证工具参数定义是否符合OpenAI规范
     */
    public static ValidationResult validateToolDefinition(ToolDefinition tool) {
        ValidationResult result = new ValidationResult();
        
        if (tool == null) {
            result.addError("工具定义不能为null");
            return result;
        }
        
        JsonObject parameters = tool.parameters();
        
        // 检查基本结构
        validateBasicStructure(parameters, result);
        
        // 检查required字段格式
        validateRequiredField(parameters, result);
        
        // 检查properties定义
        validateProperties(parameters, result);
        
        return result;
    }
    
    /**
     * 验证基本结构
     */
    private static void validateBasicStructure(JsonObject parameters, ValidationResult result) {
        if (!parameters.has("type")) {
            result.addError("缺少必需的'type'字段");
        } else if (!"object".equals(parameters.get("type").getAsString())) {
            result.addError("'type'字段必须是'object'");
        }
        
        if (!parameters.has("properties")) {
            result.addError("缺少必需的'properties'字段");
        }
        
        if (!parameters.has("required")) {
            result.addError("缺少必需的'required'字段");
        }
    }
    
    /**
     * 验证required字段格式
     */
    private static void validateRequiredField(JsonObject parameters, ValidationResult result) {
        if (parameters.has("required")) {
            try {
                // required必须是JSON数组
                if (!parameters.get("required").isJsonArray()) {
                    result.addError("'required'字段必须是JSON数组，而不是JSON对象");
                }
            } catch (Exception e) {
                result.addError("'required'字段格式错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 验证properties定义
     */
    private static void validateProperties(JsonObject parameters, ValidationResult result) {
        if (parameters.has("properties") && parameters.get("properties").isJsonObject()) {
            JsonObject properties = parameters.get("properties").getAsJsonObject();
            
            for (String propertyName : properties.keySet()) {
                JsonObject property = properties.get(propertyName).getAsJsonObject();
                
                // 检查参数类型
                if (!property.has("type")) {
                    result.addError("参数'" + propertyName + "'缺少'type'字段");
                }
                
                // 检查参数描述
                if (!property.has("description")) {
                    result.addWarning("参数'" + propertyName + "'建议添加'description'字段");
                }
                
                // 验证参数类型值
                if (property.has("type")) {
                    String type = property.get("type").getAsString();
                    if (!isValidType(type)) {
                        result.addError("参数'" + propertyName + "'的类型'" + type + "'无效");
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否为有效的JSON Schema类型
     */
    private static boolean isValidType(String type) {
        return "string".equals(type) || 
               "number".equals(type) || 
               "integer".equals(type) || 
               "boolean".equals(type) || 
               "object".equals(type) || 
               "array".equals(type);
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public java.util.List<String> getErrors() {
            return new java.util.ArrayList<>(errors);
        }
        
        public java.util.List<String> getWarnings() {
            return new java.util.ArrayList<>(warnings);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            if (!errors.isEmpty()) {
                sb.append("错误:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("警告:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            
            if (isValid()) {
                sb.append("验证通过，符合OpenAI工具定义规范");
            }
            
            return sb.toString();
        }
    }
}