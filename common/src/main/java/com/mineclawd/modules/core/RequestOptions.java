package com.mineclawd.modules.core;

/**
 * 请求选项
 * 
 * <p>定义执行请求的各种选项</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class RequestOptions {
    
    private final boolean async;
    private final int timeout;
    private final int retryCount;
    private final boolean validateParameters;
    private final boolean logExecution;
    
    public RequestOptions(boolean async, int timeout, int retryCount, 
                        boolean validateParameters, boolean logExecution) {
        this.async = async;
        this.timeout = timeout;
        this.retryCount = retryCount;
        this.validateParameters = validateParameters;
        this.logExecution = logExecution;
    }
    
    public boolean isAsync() {
        return async;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public boolean shouldValidateParameters() {
        return validateParameters;
    }
    
    public boolean shouldLogExecution() {
        return logExecution;
    }
    
    /**
     * 默认选项
     */
    public static final RequestOptions DEFAULT = new RequestOptions(false, 30000, 0, true, true);
    
    /**
     * 异步选项
     */
    public static final RequestOptions ASYNC = new RequestOptions(true, 0, 0, true, true);
    
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
        private boolean async = false;
        private int timeout = 30000;
        private int retryCount = 0;
        private boolean validateParameters = true;
        private boolean logExecution = true;
        
        public Builder async(boolean async) {
            this.async = async;
            return this;
        }
        
        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }
        
        public Builder validateParameters(boolean validateParameters) {
            this.validateParameters = validateParameters;
            return this;
        }
        
        public Builder logExecution(boolean logExecution) {
            this.logExecution = logExecution;
            return this;
        }
        
        public RequestOptions build() {
            return new RequestOptions(async, timeout, retryCount, validateParameters, logExecution);
        }
    }
}