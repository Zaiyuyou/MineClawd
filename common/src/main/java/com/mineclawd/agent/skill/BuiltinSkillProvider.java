package com.mineclawd.agent.skill;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;

import java.util.List;

/**
 * 内置技能提供者
 * 提供内置的技能定义
 * 
 * <p>此提供者包含系统内置的技能，例如：</p>
 * <ul>
 *   <li>代码审查技能</li>
 *   <li>文件分析技能</li>
 *   <li>其他系统内置技能</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public class BuiltinSkillProvider implements SkillProvider {
    
    @Override
    public List<SkillDefinition> getSkills() {
        // TODO: 实现获取内置技能逻辑
        // 返回系统内置的技能列表
        // 具体实现后续填充
        return null;
    }
    
    @Override
    public String getName() {
        return "builtin";
    }
}
