package com.mineclawd.agent.skill;

import java.util.List;

/**
 * 技能提供者接口
 * 定义了技能提供者需要实现的方法
 * 
 * <p>技能提供者用于提供一组技能定义，可以是内置的，也可以是外挂的</p>
 * 
 * <p>实现此接口的类需要提供：</p>
 * <ul>
 *   <li>getSkills(): 获取技能列表</li>
 *   <li>getName(): 获取提供者名称</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface SkillProvider {
    
    /**
     * 获取技能列表
     * 
     * <p>返回该提供者管理的所有技能定义</p>
     * 
     * @return 技能定义列表
     */
    List<SkillDefinition> getSkills();
    
    /**
     * 获取提供者名称
     * 
     * <p>用于日志和调试，标识技能的来源</p>
     * 
     * @return 提供者名称
     */
    String getName();
}
