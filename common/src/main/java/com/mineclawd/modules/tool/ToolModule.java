package com.mineclawd.modules.tool;

import com.mineclawd.modules.core.*;

/**
 * Tool模块接口
 * 
 * <p>原子操作模块，提供具体的功能实现</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public interface ToolModule extends ExecutableModule {
    
    /**
     * 获取Tool分类（可选实现）
     */
    default ToolCategory getCategory() {
        return ToolCategory.OTHER;
    }
    
    /**
     * 获取Tool标签（可选实现）
     */
    default String[] getTags() {
        return new String[0];
    }
}