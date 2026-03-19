package com.mineclawd.modules.api;

import com.mineclawd.modules.core.ModuleType;

/**
 * UI模块信息
 * 
 * <p>用于UI展示的模块基本信息</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class UIModuleInfo {
    
    private final String moduleId;
    private final String moduleName;
    private final ModuleType moduleType;
    private final String description;
    private final boolean isExecutable;
    
    public UIModuleInfo(String moduleId, String moduleName, ModuleType moduleType, 
                       String description, boolean isExecutable) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.moduleType = moduleType;
        this.description = description;
        this.isExecutable = isExecutable;
    }
    
    public String getModuleId() {
        return moduleId;
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public ModuleType getModuleType() {
        return moduleType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isExecutable() {
        return isExecutable;
    }
    
    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private String moduleId;
        private String moduleName;
        private ModuleType moduleType;
        private String description;
        private boolean isExecutable = false;
        
        public Builder moduleId(String moduleId) {
            this.moduleId = moduleId;
            return this;
        }
        
        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }
        
        public Builder moduleType(ModuleType moduleType) {
            this.moduleType = moduleType;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder isExecutable(boolean isExecutable) {
            this.isExecutable = isExecutable;
            return this;
        }
        
        public UIModuleInfo build() {
            if (moduleId == null) {
                throw new IllegalArgumentException("moduleId不能为空");
            }
            if (moduleName == null) {
                moduleName = "未命名模块";
            }
            if (moduleType == null) {
                moduleType = ModuleType.UNKNOWN;
            }
            if (description == null) {
                description = "无描述";
            }
            
            return new UIModuleInfo(moduleId, moduleName, moduleType, description, isExecutable);
        }
    }
}