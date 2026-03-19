package com.mineclawd.api;

import com.mineclawd.agent.Agent;
import com.mineclawd.agent.interfaces.SkillInterface;
import com.mineclawd.agent.interfaces.ToolInterface;
import com.mineclawd.agent.interfaces.WorkflowInterface;

/**
 * MineClawd API 接口
 * 定义了外挂 Mod 可以使用的 API 方法
 * 
 * <p>这是外挂插件系统的核心接口，外挂 Mod 通过此接口与 MineClawd 交互</p>
 * 
 * <p>API 方法包括：</p>
 * <ul>
 *   <li>registerTool(): 注册工具</li>
 *   <li>registerWorkflow(): 注册工作流</li>
 *   <li>registerSkill(): 注册技能</li>
 *   <li>getAgent(): 获取 Agent</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface MineClawdAPI {
    
    /**
     * 注册工具
     * 
     * <p>外挂 Mod 可以通过此方法注册自定义工具</p>
     * 
     * @param tool 工具接口
     */
    void registerTool(ToolInterface tool);
    
    /**
     * 注册工作流
     * 
     * <p>外挂 Mod 可以通过此方法注册自定义工作流</p>
     * 
     * @param workflow 工作流接口
     */
    void registerWorkflow(WorkflowInterface workflow);
    
    /**
     * 注册技能
     * 
     * <p>外挂 Mod 可以通过此方法注册自定义技能</p>
     * 
     * @param skill 技能接口
     */
    void registerSkill(SkillInterface skill);
    
    /**
     * 获取 Agent
     * 
     * <p>外挂 Mod 可以通过此方法获取指定所有者的 Agent</p>
     * 
     * @param ownerKey 所有者键
     * @return Agent 实例
     */
    Agent getAgent(String ownerKey);
}
