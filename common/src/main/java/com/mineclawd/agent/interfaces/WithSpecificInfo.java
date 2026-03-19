package com.mineclawd.agent.interfaces;

/**
 * 具体信息接口
 * 定义了获取组件详细信息的方法
 * 
 * <p>此接口用于获取组件的详细信息，通常用于向用户展示或提供给LLM参考</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface WithSpecificInfo {
    
    /**
     * 获取组件的具体信息
     * 
     * <p>返回组件的详细信息，包括名称、描述、参数等</p>
     * 
     * @return 组件的具体信息
     */
    String getSpecificInfo();
}
