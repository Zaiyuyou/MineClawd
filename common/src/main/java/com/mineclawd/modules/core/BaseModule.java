package com.mineclawd.modules.core;

/**
 * 基础模块接口
 * 所有模块都必须实现此接口
 * 
 * <p>简化版模块接口，只包含核心必要功能</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public interface BaseModule {
    
    /**
     * 获取模块唯一标识
     * 
     * @return 模块ID，格式建议："模块类型-模块名称"，如："tool-file-read"
     */
    String getModuleId();
    
    /**
     * 获取模块名称
     * 
     * @return 模块的人类可读名称
     */
    String getModuleName();
    
    /**
     * 获取模块类型
     * 
     * @return 模块类型枚举
     */
    ModuleType getModuleType();
    
    /**
     * 获取模块简介
     * 
     * @return 模块的简要描述
     */
    String getModuleDescription();
    
    /**
     * 获取LLM描述（用于Function Call）
     * 
     * @return LLM可理解的函数调用描述
     */
    LLMDescription getLLMDescription();
    
    /**
     * 获取MCP描述（用于Model Context Protocol）
     * 
     * @return MCP协议描述
     */
    MCPDescription getMCPDescription();
}