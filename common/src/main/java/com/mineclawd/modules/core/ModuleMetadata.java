package com.mineclawd.modules.core;

import java.time.Instant;
import java.util.Map;

/**
 * 模块元数据
 * 
 * <p>包含模块的创建信息、版本信息、标签等元数据</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ModuleMetadata {
    
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String createdBy;
    private final String lastModifiedBy;
    private final Map<String, String> tags;
    private final Map<String, Object> extensions;
    
    public ModuleMetadata(Instant createdAt, Instant updatedAt, 
                         String createdBy, String lastModifiedBy,
                         Map<String, String> tags, Map<String, Object> extensions) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.tags = tags;
        this.extensions = extensions;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public Map<String, Object> getExtensions() {
        return extensions;
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
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String lastModifiedBy;
        private Map<String, String> tags = Map.of();
        private Map<String, Object> extensions = Map.of();
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder lastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }
        
        public Builder tags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }
        
        public Builder extensions(Map<String, Object> extensions) {
            this.extensions = extensions;
            return this;
        }
        
        public ModuleMetadata build() {
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            if (updatedAt == null) {
                updatedAt = Instant.now();
            }
            if (createdBy == null) {
                createdBy = "system";
            }
            if (lastModifiedBy == null) {
                lastModifiedBy = "system";
            }
            
            return new ModuleMetadata(createdAt, updatedAt, createdBy, lastModifiedBy, tags, extensions);
        }
    }
}