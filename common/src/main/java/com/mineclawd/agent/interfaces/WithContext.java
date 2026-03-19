package com.mineclawd.agent.interfaces;

/**
 * 上下文接口
 * 定义了获取组件上下文和提示的方法
 * 
 * <p>此接口用于获取组件在不同场景下的上下文信息和提示信息</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface WithContext {
    
    /**
     * 获取组件的上下文信息
     * 
     * <p>上下文信息用于向LLM提供组件的使用场景和相关信息</p>
     * 
     * @param promptType 提示类型
     *                   - "base": 基础提示
     *                   - "dynamic_registry": 动态注册提示
     *                   - "asset_tracking": 资产跟踪提示
     *                   - 其他自定义提示类型
     * @return 组件的上下文信息
     */
    String getContext(String promptType);
    
    /**
     * 获取组件的提示信息
     * 
     * <p>提示信息用于指导LLM如何使用该组件</p>
     * 
     * @param promptType 提示类型
     *                   - "base": 基础提示
     *                   - "dynamic_registry": 动态注册提示
     *                   - "asset_tracking": 资产跟踪提示
     *                   - 其他自定义提示类型
     * @return 组件的提示信息
     */
    String getPrompt(String promptType);
}
