package com.mineclawd.modules.router;

import java.util.Map;

/**
 * Schema数据
 * 
 * <p>LLM返回的schema数据结构</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class SchemaData {
    
    private final String schemaType;
    private final String schemaContent;
    private final Map<String, Object> parameters;
    private final Map<String, Object> metadata;
    
    public SchemaData(String schemaType, String schemaContent, 
                     Map<String, Object> parameters, Map<String, Object> metadata) {
        this.schemaType = schemaType;
        this.schemaContent = schemaContent;
        this.parameters = parameters;
        this.metadata = metadata;
    }
    
    public String getSchemaType() {
        return schemaType;
    }
    
    public String getSchemaContent() {
        return schemaContent;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
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
        private String schemaType;
        private String schemaContent;
        private Map<String, Object> parameters = Map.of();
        private Map<String, Object> metadata = Map.of();
        
        public Builder schemaType(String schemaType) {
            this.schemaType = schemaType;
            return this;
        }
        
        public Builder schemaContent(String schemaContent) {
            this.schemaContent = schemaContent;
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public SchemaData build() {
            if (schemaType == null) {
                throw new IllegalArgumentException("schemaType不能为空");
            }
            if (schemaContent == null) {
                schemaContent = "{}";
            }
            
            return new SchemaData(schemaType, schemaContent, parameters, metadata);
        }
    }
}