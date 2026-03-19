package com.mineclawd.api;

import com.mineclawd.agent.Agent;
import com.mineclawd.agent.interfaces.SkillInterface;
import com.mineclawd.agent.interfaces.ToolInterface;
import com.mineclawd.agent.interfaces.WorkflowInterface;

/**
 * MineClawd 插件基类
 * 所有外挂插件都需要继承此类
 * 
 * <p>插件基类提供了：</p>
 * <ul>
 *   <li>setContext(): 设置插件上下文</li>
 *   <li>onEnable(): 插件启用时调用</li>
 *   <li>onDisable(): 插件禁用时调用</li>
 *   <li>便捷的注册方法</li>
 * </ul>
 * 
 * <p>外挂插件需要实现 onEnable() 和 onDisable() 方法</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public abstract class MineClawdPlugin {
    
    // ==================== 字段 ====================
    
    /**
     * 插件上下文
     */
    protected PluginContext context;
    
    // ==================== 上下文管理 ====================
    
    /**
     * 设置插件上下文
     * 
     * <p>由 MineClawd 框架调用，设置插件的运行时上下文</p>
     * 
     * @param context 插件上下文
     */
    public void setContext(PluginContext context) {
        this.context = context;
    }
    
    // ==================== 插件生命周期 ====================
    
    /**
     * 插件启用
     * 
     * <p>当插件被启用时调用，用于初始化插件</p>
     * 
     * <p>外挂插件需要在此方法中注册自己的组件</p>
     */
    public abstract void onEnable();
    
    /**
     * 插件禁用
     * 
     * <p>当插件被禁用时调用，用于清理资源</p>
     */
    public abstract void onDisable();
    
    // ==================== 便捷注册方法 ====================
    
    /**
     * 注册工具
     * 
     * <p>外挂插件可以通过此方法注册自定义工具</p>
     * 
     * @param tool 工具接口
     */
    protected void registerTool(ToolInterface tool) {
        context.getApi().registerTool(tool);
    }
    
    /**
     * 注册工作流
     * 
     * <p>外挂插件可以通过此方法注册自定义工作流</p>
     * 
     * @param workflow 工作流接口
     */
    protected void registerWorkflow(WorkflowInterface workflow) {
        context.getApi().registerWorkflow(workflow);
    }
    
    /**
     * 注册技能
     * 
     * <p>外挂插件可以通过此方法注册自定义技能</p>
     * 
     * @param skill 技能接口
     */
    protected void registerSkill(SkillInterface skill) {
        context.getApi().registerSkill(skill);
    }
}
