package com.mineclawd.modules.tool;

/**
 * Tool分类枚举
 * 
 * <p>定义Tool的功能分类</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public enum ToolCategory {
    
    FILE_SYSTEM("file_system", "文件系统"),
    NETWORK("network", "网络"),
    GAME_COMMAND("game_command", "游戏命令"),
    DYNAMIC_CONTENT("dynamic_content", "动态内容"),
    ASSET_MANAGEMENT("asset_management", "资产管理"),
    INTERACTION("interaction", "交互"),
    DATA_PROCESSING("data_processing", "数据处理"),
    SYSTEM("system", "系统"),
    UTILITY("utility", "工具"),
    OTHER("other", "其他");
    
    private final String code;
    private final String description;
    
    ToolCategory(String code, String description) {
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
     * 根据代码获取分类
     */
    public static ToolCategory fromCode(String code) {
        for (ToolCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return OTHER;
    }
}