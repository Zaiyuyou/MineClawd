package com.mineclawd.modules.router;

import java.util.Map;

/**
 * 执行上下文
 * 
 * <p>包含schema执行的上下文信息</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ExecutionContext {
    
    private final Map<String, Object> context;
    
    public ExecutionContext(Map<String, Object> context) {
        this.context = context;
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
        private Map<String, Object> context = Map.of();
        
        public Builder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }
        
        public ExecutionContext build() {
            return new ExecutionContext(context);
        }
    }
}