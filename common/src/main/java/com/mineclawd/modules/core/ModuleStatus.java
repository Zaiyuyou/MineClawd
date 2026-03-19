package com.mineclawd.modules.core;

/**
 * 模块状态枚举
 * 
 * <p>定义模块的生命周期状态</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public enum ModuleStatus {
    
    /**
     * 未初始化 - 模块已创建但未初始化
     */
    UNINITIALIZED("uninitialized", "未初始化"),
    
    /**
     * 初始化中 - 模块正在初始化
     */
    INITIALIZING("initializing", "初始化中"),
    
    /**
     * 就绪 - 模块已初始化并准备就绪
     */
    READY("ready", "就绪"),
    
    /**
     * 运行中 - 模块正在执行操作
     */
    RUNNING("running", "运行中"),
    
    /**
     * 暂停 - 模块已暂停
     */
    PAUSED("paused", "暂停"),
    
    /**
     * 错误 - 模块发生错误
     */
    ERROR("error", "错误"),
    
    /**
     * 停止 - 模块已停止
     */
    STOPPED("stopped", "停止"),
    
    /**
     * 卸载中 - 模块正在卸载
     */
    UNLOADING("unloading", "卸载中"),
    
    /**
     * 已卸载 - 模块已卸载
     */
    UNLOADED("unloaded", "已卸载");
    
    private final String code;
    private final String description;
    
    ModuleStatus(String code, String description) {
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
     * 检查模块是否可执行
     */
    public boolean isExecutable() {
        return this == READY || this == RUNNING || this == PAUSED;
    }
    
    /**
     * 检查模块是否处于活动状态
     */
    public boolean isActive() {
        return this == READY || this == RUNNING || this == PAUSED;
    }
    
    /**
     * 根据代码获取模块状态
     */
    public static ModuleStatus fromCode(String code) {
        for (ModuleStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的模块状态: " + code);
    }
}