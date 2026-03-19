package com.mineclawd.agent.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口注册表
 * 统一管理所有组件（Tool/Workflow/Skill）的注册和获取
 * 
 * <p>这是统一接口层的核心工具类，负责：</p>
 * <ul>
 *   <li>注册组件</li>
 *   <li>获取组件</li>
 *   <li>管理所有组件的生命周期</li>
 * </ul>
 * 
 * <p>使用线程安全的ConcurrentHashMap存储组件，确保并发安全</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public class InterfaceRegistry {
    
    // ==================== 组件存储 ====================
    
    /**
     * 工具注册表
     * 存储所有注册的工具组件
     */
    private static final ConcurrentHashMap<String, ToolInterface> TOOLS = new ConcurrentHashMap<>();
    
    /**
     * 工作流注册表
     * 存储所有注册的工作流组件
     */
    private static final ConcurrentHashMap<String, WorkflowInterface> WORKFLOWS = new ConcurrentHashMap<>();
    
    /**
     * 技能注册表
     * 存储所有注册的技能组件
     */
    private static final ConcurrentHashMap<String, SkillInterface> SKILLS = new ConcurrentHashMap<>();
    
    // ==================== 工具方法 ====================
    
    /**
     * 注册工具组件
     * 
     * <p>将工具组件添加到注册表中，以便后续获取和使用</p>
     * 
     * @param tool 工具组件
     */
    public static void registerTool(ToolInterface tool) {
        if (tool != null && tool.getName() != null && !tool.getName().isBlank()) {
            TOOLS.put(tool.getName(), tool);
        }
    }
    
    /**
     * 注册工作流组件
     * 
     * <p>将工作流组件添加到注册表中，以便后续获取和使用</p>
     * 
     * @param workflow 工作流组件
     */
    public static void registerWorkflow(WorkflowInterface workflow) {
        if (workflow != null && workflow.getName() != null && !workflow.getName().isBlank()) {
            WORKFLOWS.put(workflow.getName(), workflow);
        }
    }
    
    /**
     * 注册技能组件
     * 
     * <p>将技能组件添加到注册表中，以便后续获取和使用</p>
     * 
     * @param skill 技能组件
     */
    public static void registerSkill(SkillInterface skill) {
        if (skill != null && skill.getName() != null && !skill.getName().isBlank()) {
            SKILLS.put(skill.getName(), skill);
        }
    }
    
    /**
     * 获取工具组件
     * 
     * <p>根据工具名称从注册表中获取工具组件</p>
     * 
     * @param name 工具名称
     * @return 工具组件，如果不存在则返回null
     */
    public static ToolInterface getTool(String name) {
        return TOOLS.get(name);
    }
    
    /**
     * 获取工作流组件
     * 
     * <p>根据工作流名称从注册表中获取工作流组件</p>
     * 
     * @param name 工作流名称
     * @return 工作流组件，如果不存在则返回null
     */
    public static WorkflowInterface getWorkflow(String name) {
        return WORKFLOWS.get(name);
    }
    
    /**
     * 获取技能组件
     * 
     * <p>根据技能名称从注册表中获取技能组件</p>
     * 
     * @param name 技能名称
     * @return 技能组件，如果不存在则返回null
     */
    public static SkillInterface getSkill(String name) {
        return SKILLS.get(name);
    }
    
    // ==================== 批量获取方法 ====================
    
    /**
     * 获取所有工具组件
     * 
     * <p>返回注册表中所有的工具组件列表</p>
     * 
     * @return 工具组件列表
     */
    public static List<ToolInterface> getAllTools() {
        return new ArrayList<>(TOOLS.values());
    }
    
    /**
     * 获取所有工作流组件
     * 
     * <p>返回注册表中所有的工具组件列表</p>
     * 
     * @return 工作流组件列表
     */
    public static List<WorkflowInterface> getAllWorkflows() {
        return new ArrayList<>(WORKFLOWS.values());
    }
    
    /**
     * 获取所有技能组件
     * 
     * <p>返回注册表中所有的技能组件列表</p>
     * 
     * @return 技能组件列表
     */
    public static List<SkillInterface> getAllSkills() {
        return new ArrayList<>(SKILLS.values());
    }
}