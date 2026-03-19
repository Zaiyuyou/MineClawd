package com.mineclawd.modules.core;

/**
 * 模块类型枚举
 * 
 * <p>定义系统中支持的模块类型</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public enum ModuleType {
    
    /**
     * 工具模块 - 提供原子操作
     */
    TOOL("tool", "工具模块"),
    
    /**
     * 技能模块 - 动态生成工作流
     */
    SKILL("skill", "技能模块"),
    
    /**
     * 工作流模块 - 执行预定义流程
     */
    WORKFLOW("workflow", "工作流模块"),
    
    /**
     * 资产模块 - 管理游戏资产
     */
    ASSET("asset", "资产模块"),
    
    /**
     * 数据模块 - 数据处理操作
     */
    DATA("data", "数据模块"),
    
    /**
     * 系统模块 - 系统级功能
     */
    SYSTEM("system", "系统模块"),
    
    /**
     * 第三方模块 - 外部扩展模块
     */
    THIRD_PARTY("third_party", "第三方模块"),
    
    /**
     * 未知类型 - 默认失败返回值
     */
    UNKNOWN("unknown", "未知类型");
    
    private final String code;
    private final String description;
    
    ModuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取模块类型
     */
    public static ModuleType fromCode(String code) {
        for (ModuleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的模块类型: " + code);
    }
}