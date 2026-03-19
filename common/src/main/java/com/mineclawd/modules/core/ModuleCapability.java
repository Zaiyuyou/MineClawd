package com.mineclawd.modules.core;

import java.util.List;

/**
 * 模块能力描述
 * 
 * <p>描述模块的功能和能力，用于LLM理解和UI展示</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ModuleCapability {
    
    private final String summary;
    private final String detailedDescription;
    private final List<String> capabilities;
    private final List<String> useCases;
    private final List<String> limitations;
    
    public ModuleCapability(String summary, String detailedDescription,
                          List<String> capabilities, List<String> useCases,
                          List<String> limitations) {
        this.summary = summary;
        this.detailedDescription = detailedDescription;
        this.capabilities = capabilities;
        this.useCases = useCases;
        this.limitations = limitations;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public String getDetailedDescription() {
        return detailedDescription;
    }
    
    public List<String> getCapabilities() {
        return capabilities;
    }
    
    public List<String> getUseCases() {
        return useCases;
    }
    
    public List<String> getLimitations() {
        return limitations;
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
        private String summary;
        private String detailedDescription;
        private List<String> capabilities = List.of();
        private List<String> useCases = List.of();
        private List<String> limitations = List.of();
        
        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }
        
        public Builder detailedDescription(String detailedDescription) {
            this.detailedDescription = detailedDescription;
            return this;
        }
        
        public Builder capabilities(List<String> capabilities) {
            this.capabilities = capabilities;
            return this;
        }
        
        public Builder useCases(List<String> useCases) {
            this.useCases = useCases;
            return this;
        }
        
        public Builder limitations(List<String> limitations) {
            this.limitations = limitations;
            return this;
        }
        
        public ModuleCapability build() {
            if (summary == null) {
                summary = "No summary provided";
            }
            if (detailedDescription == null) {
                detailedDescription = "No detailed description provided";
            }
            
            return new ModuleCapability(summary, detailedDescription, capabilities, useCases, limitations);
        }
    }
}