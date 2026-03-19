package com.mineclawd.agent.workflow;

import java.util.List;

/**
 * 工作流提供者接口
 * 定义了工作流提供者需要实现的方法
 * 
 * <p>工作流提供者用于提供一组工作流定义，可以是内置的，也可以是外挂的</p>
 * 
 * <p>实现此接口的类需要提供：</p>
 * <ul>
 *   <li>getWorkflows(): 获取工作流列表</li>
 *   <li>getName(): 获取提供者名称</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface WorkflowProvider {
    
    /**
     * 获取工作流列表
     * 
     * <p>返回该提供者管理的所有工作流定义</p>
     * 
     * @return 工作流定义列表
     */
    List<WorkflowDefinition> getWorkflows();
    
    /**
     * 获取提供者名称
     * 
     * <p>用于日志和调试，标识工作流的来源</p>
     * 
     * @return 提供者名称
     */
    String getName();
}
