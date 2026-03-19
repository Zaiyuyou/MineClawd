package com.mineclawd.agent.interfaces;

import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;

/**
 * 工作流接口
 * 定义了工作流组件的统一接口
 * 
 * <p>工作流是预定义的流程，包含多个工具步骤</p>
 * 
 * <p>工作流接口继承了三个基础接口：</p>
 * <ul>
 *   <li>Executable: 可执行</li>
 *   <li>WithContext: 有上下文</li>
 *   <li>WithSpecificInfo: 有具体信息</li>
 * </ul>
 * 
 * <p>工作流还包含以下特有属性：</p>
 * <ul>
 *   <li>steps: 工作流包含的工具步骤列表</li>
 *   <li>userConfirmed: 用户是否已确认该工作流</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface WorkflowInterface extends Executable, WithContext, WithSpecificInfo {
    
    /**
     * 获取工作流的工具步骤列表
     * 
     * <p>工作流由多个工具步骤组成，按顺序执行</p>
     * 
     * @return 工具步骤列表
     */
    List<ToolDefinition> getSteps();
    
    /**
     * 检查用户是否已确认该工作流
     * 
     * <p>对于动态生成的工作流，需要用户确认后才能执行</p>
     * 
     * @return true表示用户已确认，false表示未确认
     */
    boolean isUserConfirmed();
    
    /**
     * 设置用户确认状态
     * 
     * <p>当用户确认工作流后，设置此状态为true</p>
     * 
     * @param confirmed 用户确认状态
     */
    void setUserConfirmed(boolean confirmed);
}
