package com.mineclawd.modules.api;

/**
 * 更新结果
 * 
 * <p>配置更新操作的结果</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class UpdateResult {
    
    private final boolean success;
    private final String message;
    private final String errorCode;
    
    public UpdateResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }
    
    public boolean isSuccess() {
        return success;
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
    public static UpdateResult success(String message) {
        return new UpdateResult(true, message, null);
    }
    
    /**
     * 创建失败结果
     */
    public static UpdateResult failure(String message) {
        return new UpdateResult(false, message, "UPDATE_ERROR");
    }
    
    /**
     * 创建失败结果（带错误码）
     */
    public static UpdateResult failure(String message, String errorCode) {
        return new UpdateResult(false, message, errorCode);
    }
}