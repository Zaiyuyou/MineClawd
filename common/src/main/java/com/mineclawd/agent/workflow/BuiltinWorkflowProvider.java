package com.mineclawd.agent.workflow;

import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;

/**
 * 内置工作流提供者
 * 提供内置的工作流定义
 * 
 * <p>此提供者包含系统内置的工作流，例如：</p>
 * <ul>
 *   <li>部署工作流</li>
 *   <li>文件备份工作流</li>
 *   <li>其他系统内置工作流</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public class BuiltinWorkflowProvider implements WorkflowProvider {
    
    @Override
    public List<WorkflowDefinition> getWorkflows() {
        // TODO: 实现获取内置工作流逻辑
        // 返回系统内置的工作流列表
        // 具体实现后续填充
        return null;
    }
    
    @Override
    public String getName() {
        return "builtin";
    }
}
