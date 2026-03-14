package com.mineclawd.tool_sys;

import java.util.List;

/**
 * 工具提供者接口
 * 定义工具类需要实现的方法，用于提供工具定义
 */
public interface ToolProvider {
    
    /**
     * 获取该提供者管理的所有工具定义
     * @return 工具定义列表
     */
    List<ToolDefinition> getTools();
    
    /**
     * 获取提供者名称（用于日志和调试）
     * @return 提供者名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}