package com.mineclawd.modules.core;

import java.util.Map;

/**
 * 执行结果格式
 * 
 * <p>标准化的调用结果格式，包含成功/失败状态、数据和元数据</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ExecutionResult {
    
    private final String requestId;
    private final boolean success;
    private final Object data;
    private final String message;
    private final Map<String, Object> metadata;
    
    public ExecutionResult(String requestId, boolean success, Object data,
                          String message, Map<String, Object> metadata) {
        this.requestId = requestId;
        this.success = success;
        this.data = data;
        this.message = message;
        this.metadata = metadata;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Object getData() {
        return data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    /**
     * 创建成功结果
     */
    public static ExecutionResult success(String requestId, Object data) {
        return new ExecutionResult(requestId, true, data, "执行成功", Map.of());
    }
    
    /**
     * 创建成功结果（带消息）
     */
    public static ExecutionResult success(String requestId, Object data, String message) {
        return new ExecutionResult(requestId, true, data, message, Map.of());
    }
    
    /**
     * 创建失败结果
     */
    public static ExecutionResult failure(String requestId, String message) {
        return new ExecutionResult(requestId, false, null, message, Map.of());
    }
}