package com.mineclawd.agent.skill;

import com.google.gson.JsonObject;
import com.mineclawd.agent.interfaces.SkillInterface;
import com.mineclawd.agent.interfaces.WorkflowInterface;
import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;
import java.util.Map;

/**
 * 技能定义类
 * 实现了SkillInterface接口，是技能组件的具体实现
 * 
 * <p>技能是动态的工作流生成器，包含：</p>
 * <ul>
 *   <li>name: 技能名称</li>
 *   <li>description: 技能描述</li>
 *   <li>toolList: 技能包含的工具列表</li>
 *   <li>configuration: 技能的生成规则配置</li>
 * </ul>
 * 
 * <p>技能的主要功能：</p>
 * <ul>
 *   <li>执行技能逻辑</li>
 *   <li>生成工作流</li>
 *   <li>提供技能信息</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public class SkillDefinition implements SkillInterface {
    
    // ==================== 字段 ====================
    
    /**
     * 技能名称
     */
    private final String name;
    
    /**
     * 技能描述
     */
    private final String description;
    
    /**
     * 技能包含的工具列表
     */
    private final List<ToolDefinition> toolList;
    
    /**
     * 技能的生成规则配置
     */
    private final JsonObject configuration;
    
    // ==================== 构造函数 ====================
    
    /**
     * 创建技能定义
     * 
     * @param name 技能名称
     * @param description 技能描述
     * @param toolList 技能包含的工具列表
     * @param configuration 技能的生成规则配置
     */
    public SkillDefinition(String name, String description, List<ToolDefinition> toolList, JsonObject configuration) {
        this.name = name;
        this.description = description;
        this.toolList = toolList;
        this.configuration = configuration;
    }
    
    // ==================== SkillInterface 接口实现 ====================
    
    @Override
    public Object execute(Map<String, Object> context) {
        // TODO: 实现技能执行逻辑
        // 执行技能时，可以调用多个工具
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
        // TODO: 实现获取技能具体信息逻辑
        // 返回技能的详细信息，包括名称、描述、工具列表等
        // 具体实现后续填充
        return "";
    }
    
    @Override
    public String getContext(String promptType) {
        // TODO: 实现获取技能上下文逻辑
        // 根据不同的提示类型返回不同的上下文信息
        // 具体实现后续填充
        return "";
    }
    
    @Override
    public String getPrompt(String promptType) {
        // TODO: 实现获取技能提示逻辑
        // 根据不同的提示类型返回不同的提示信息
        // 具体实现后续填充
        return "";
    }
    
    @Override
    public List<ToolDefinition> getToolList() {
        return toolList;
    }
    
    @Override
    public WorkflowInterface generateWorkflow() {
        // TODO: 实现生成工作流逻辑
        // 使用LLM根据技能的工具列表和配置生成工作流
        // 具体实现后续填充
        return null;
    }
}
