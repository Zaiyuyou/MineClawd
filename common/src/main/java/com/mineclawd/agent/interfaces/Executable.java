package com.mineclawd.agent.interfaces;

import java.util.Map;

/**
 * 可执行接口
 * 定义了所有可执行组件（Tool/Workflow/Skill）的基本执行方法
 * 
 * <p>这是统一接口层的核心接口之一，所有可执行组件都必须实现此接口</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface Executable {
    
    /**
     * 执行组件
     * 
     * <p>这是组件的核心执行方法，所有具体的执行逻辑都在此方法中实现</p>
     * 
     * @param context 执行上下文，包含执行时需要的各种参数和数据
     *                例如：玩家信息、会话数据、环境变量等
     * @return 执行结果，返回类型根据具体组件而定
     *         - Tool: 返回工具执行结果
     *         - Workflow: 返回工作流执行结果
     *         - Skill: 返回技能执行结果
     */
    Object execute(Map<String, Object> context);
    
    /**
     * 获取组件名称
     * 
     * <p>每个组件都有唯一的名称，用于识别和调用</p>
     * 
     * @return 组件名称
     */
    String getName();
    
    /**
     * 获取组件描述
     * 
     * <p>组件的详细描述，用于向用户或LLM展示组件的功能</p>
     * 
     * @return 组件描述
     */
    String getDescription();
}
