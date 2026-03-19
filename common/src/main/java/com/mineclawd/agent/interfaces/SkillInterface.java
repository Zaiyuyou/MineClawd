package com.mineclawd.agent.interfaces;

import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;

/**
 * 技能接口
 * 定义了技能组件的统一接口
 * 
 * <p>技能是动态的工作流生成器，包含工具列表和生成规则</p>
 * 
 * <p>技能接口继承了三个基础接口：</p>
 * <ul>
 *   <li>Executable: 可执行</li>
 *   <li>WithContext: 有上下文</li>
 *   <li>WithSpecificInfo: 有具体信息</li>
 * </ul>
 * 
 * <p>技能还包含以下特有属性：</p>
 * <ul>
 *   <li>toolList: 技能包含的工具列表</li>
 *   <li>generateWorkflow(): 根据技能生成工作流</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface SkillInterface extends Executable, WithContext, WithSpecificInfo {
    
    /**
     * 获取技能的工具列表
     * 
     * <p>技能由多个工具组成，这些工具可以被LLM用来生成工作流</p>
     * 
     * @return 工具列表
     */
    List<ToolDefinition> getToolList();
    
    /**
     * 生成工作流
     * 
     * <p>根据技能的配置和工具列表，让LLM动态生成工作流</p>
     * 
     * <p>生成的工作流需要用户确认后才能执行</p>
     * 
     * @return 生成的工作流对象
     */
    WorkflowInterface generateWorkflow();
}
