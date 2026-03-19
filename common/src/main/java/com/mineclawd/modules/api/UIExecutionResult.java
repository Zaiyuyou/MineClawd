package com.mineclawd.modules.api;

import com.mineclawd.modules.core.ExecutionResult;

/**
 * UI执行结果
 * 
 * <p>UI友好的模块执行结果格式</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class UIExecutionResult {
    
    private final boolean success;
    private final Object data;
    private final String message;
    private final String errorCode;
    
    public UIExecutionResult(boolean success, Object data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
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
    
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 创建成功结果
     */
    public static UIExecutionResult success(Object data) {
        return new UIExecutionResult(true, data, "执行成功", null);
    }
    
    /**
     * 创建成功结果（带消息）
     */
    public static UIExecutionResult success(Object data, String message) {
        return new UIExecutionResult(true, data, message, null);
    }
    
    /**
     * 创建失败结果
     */
    public static UIExecutionResult failure(String message) {
        return new UIExecutionResult(false, null, message, "UNKNOWN_ERROR");
    }
    
    /**
     * 创建失败结果（带错误码）
     */
    public static UIExecutionResult failure(String message, String errorCode) {
        return new UIExecutionResult(false, null, message, errorCode);
    }
    
    /**
     * 从ExecutionResult转换
     */
    public static UIExecutionResult fromExecutionResult(ExecutionResult executionResult) {
        if (executionResult.isSuccess()) {
            return UIExecutionResult.success(executionResult.getData(), executionResult.getMessage());
        } else {
            // 简化错误处理，直接使用消息作为错误码
            String errorCode = "EXECUTION_ERROR";
            String message = executionResult.getMessage();
            if (message != null && message.contains(":")) {
                String[] parts = message.split(":", 2);
                if (parts.length > 1) {
                    errorCode = parts[0].trim();
                    message = parts[1].trim();
                }
            }
            return UIExecutionResult.failure(message, errorCode);
        }
    }
}