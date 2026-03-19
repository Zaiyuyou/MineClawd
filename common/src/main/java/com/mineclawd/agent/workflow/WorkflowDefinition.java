package com.mineclawd.agent.workflow;

import com.mineclawd.agent.interfaces.WorkflowInterface;
import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;
import java.util.Map;

/**
 * 工作流定义类
 * 实现了WorkflowInterface接口，是工作流组件的具体实现
 * 
 * <p>工作流是预定义的流程，包含：</p>
 * <ul>
 *   <li>name: 工作流名称</li>
 *   <li>description: 工作流描述</li>
 *   <li>steps: 工作流包含的工具步骤列表</li>
 *   <li>userConfirmed: 用户是否已确认该工作流</li>
 * </ul>
 * 
 * <p>工作流的主要功能：</p>
 * <ul>
 *   <li>执行工作流</li>
 *   <li>提供工作流信息</li>
 *   <li>管理用户确认状态</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public class WorkflowDefinition implements WorkflowInterface {
    
    // ==================== 字段 ====================
    
    /**
     * 工作流名称
     */
    private final String name;
    
    /**
     * 工作流描述
     */
    private final String description;
    
    /**
     * 工作流包含的工具步骤列表
     */
    private final List<ToolDefinition> steps;
    
    /**
     * 用户是否已确认该工作流
     */
    private boolean userConfirmed;
    
    // ==================== 构造函数 ====================
    
    /**
     * 创建工作流定义
     * 
     * @param name 工作流名称
     * @param description 工作流描述
     * @param steps 工作流包含的工具步骤列表
     * @param userConfirmed 用户是否已确认该工作流
     */
    public WorkflowDefinition(String name, String description, List<ToolDefinition> steps, boolean userConfirmed) {
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.userConfirmed = userConfirmed;
    }
    
    // ==================== WorkflowInterface 接口实现 ====================
    
    @Override
    public Object execute(Map<String, Object> context) {
        // TODO: 实现工作流执行逻辑
        // 按顺序执行工作流中的每个工具步骤
        // 具体实现后续填充
        return null;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getSpecificInfo() {
        // TODO: 实现获取工作流具体信息逻辑
        // 返回工作流的详细信息，包括名称、描述、步骤等
        // 具体实现后续填充
        return "";
    }
    
    @Override
    public String getContext(String promptType) {
        // TODO: 实现获取工作流上下文逻辑
        // 根据不同的提示类型返回不同的上下文信息
        // 具体实现后续填充
        return "";
    }
    
    @Override
    public String getPrompt(String promptType) {
        // TODO: 实现获取工作流提示逻辑
        // 根据不同的提示类型返回不同的提示信息
        // 具体实现后续填充
        return "";
    }
    
    @Override
    public List<ToolDefinition> getSteps() {
        return steps;
    }
    
    @Override
    public boolean isUserConfirmed() {
        return userConfirmed;
    }
    
    @Override
    public void setUserConfirmed(boolean confirmed) {
        this.userConfirmed = confirmed;
    }
}
