package com.mineclawd.modules.router;

import com.mineclawd.modules.core.ExecutionResult;

/**
 * 路由结果
 * 
 * <p>Schema路由操作的结果</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class RouteResult {
    
    private final boolean success;
    private final ExecutionResult executionResult;
    private final String message;
    private final String errorCode;
    
    public RouteResult(boolean success, ExecutionResult executionResult, 
                      String message, String errorCode) {
        this.success = success;
        this.executionResult = executionResult;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public ExecutionResult getExecutionResult() {
        return executionResult;
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
    public static RouteResult success(ExecutionResult executionResult) {
        return new RouteResult(true, executionResult, "路由成功", null);
    }
    
    /**
     * 创建失败结果
     */
    public static RouteResult failure(String message) {
        return new RouteResult(false, null, message, "ROUTE_ERROR");
    }
    
    /**
     * 创建失败结果（带错误码）
     */
    public static RouteResult failure(String message, String errorCode) {
        return new RouteResult(false, null, message, errorCode);
    }
}