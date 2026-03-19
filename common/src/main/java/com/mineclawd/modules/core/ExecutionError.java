package com.mineclawd.modules.core;

/**
 * 执行错误
 * 
 * <p>包含错误的详细信息</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ExecutionError {
    
    private final String code;
    private final String message;
    private final String field;
    private final Object value;
    
    public ExecutionError(String code, String message, String field, Object value) {
        this.code = code;
        this.message = message;
        this.field = field;
        this.value = value;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
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
        private String code;
        private String message;
        private String field;
        private Object value;
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder field(String field) {
            this.field = field;
            return this;
        }
        
        public Builder value(Object value) {
            this.value = value;
            return this;
        }
        
        public ExecutionError build() {
            if (code == null) {
                code = "UNKNOWN_ERROR";
            }
            if (message == null) {
                message = "未知错误";
            }
            
            return new ExecutionError(code, message, field, value);
        }
    }
}