package com.mineclawd.modules.core;

import java.util.List;

/**
 * 验证结果
 * 
 * <p>包含验证状态和消息</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ValidationResult {
    
    private final boolean isValid;
    private final String message;
    
    public ValidationResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 创建成功验证结果
     */
    public static ValidationResult success() {
        return new ValidationResult(true, "验证通过");
    }
    
    /**
     * 创建成功验证结果（带消息）
     */
    public static ValidationResult success(String message) {
        return new ValidationResult(true, message);
    }
    
    /**
     * 创建失败验证结果
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }
}