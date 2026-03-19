package com.mineclawd.modules.api;

import com.mineclawd.modules.core.*;

/**
 * UI模块配置
 * 
 * <p>模块的可视化配置信息</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class UIModuleConfiguration {
    
    private final String moduleId;
    private final String moduleName;
    private final ModuleType moduleType;
    private final String description;
    private final LLMDescription llmDescription;
    private final MCPDescription mcpDescription;
    
    public UIModuleConfiguration(String moduleId, String moduleName, ModuleType moduleType,
                                String description, LLMDescription llmDescription, 
                                MCPDescription mcpDescription) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.moduleType = moduleType;
        this.description = description;
        this.llmDescription = llmDescription;
        this.mcpDescription = mcpDescription;
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
    
    public LLMDescription getLLMDescription() {
        return llmDescription;
    }
    
    public MCPDescription getMCPDescription() {
        return mcpDescription;
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
        private LLMDescription llmDescription;
        private MCPDescription mcpDescription;
        
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
        
        public Builder llmDescription(LLMDescription llmDescription) {
            this.llmDescription = llmDescription;
            return this;
        }
        
        public Builder mcpDescription(MCPDescription mcpDescription) {
            this.mcpDescription = mcpDescription;
            return this;
        }
        
        public UIModuleConfiguration build() {
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
            if (llmDescription == null) {
                llmDescription = LLMDescription.builder().build();
            }
            if (mcpDescription == null) {
                mcpDescription = MCPDescription.builder().build();
            }
            
            return new UIModuleConfiguration(moduleId, moduleName, moduleType, 
                description, llmDescription, mcpDescription);
        }
    }
}