package com.mineclawd.modules.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 可执行模块接口
 * 
 * <p>支持执行操作的模块实现此接口</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public interface ExecutableModule extends BaseModule {
    
    /**
     * 执行模块操作
     * 
     * @param request 执行请求
     * @return 执行结果
     */
    ExecutionResult execute(ExecutionRequest request);
    
    /**
     * 异步执行模块操作（可选实现）
     * 
     * @param request 执行请求
     * @return 异步执行结果
     */
    default CompletableFuture<ExecutionResult> executeAsync(ExecutionRequest request) {
        return CompletableFuture.supplyAsync(() -> execute(request));
    }
    
    /**
     * 获取执行参数规范（可选实现）
     * 
     * @return 参数规范定义
     */
    default ParameterSchema getParameterSchema() {
        return ParameterSchema.builder().build();
    }
    
    /**
     * 验证执行参数（可选实现）
     * 
     * @param parameters 参数映射
     * @return 验证结果
     */
    default ValidationResult validateParameters(Map<String, Object> parameters) {
        return ValidationResult.success();
    }
    
    /**
     * 获取执行示例（可选实现）
     * 
     * @return 执行示例列表
     */
    default ExecutionExample[] getExecutionExamples() {
        return new ExecutionExample[0];
    }
}