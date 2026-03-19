package com.mineclawd.modules.core;

/**
 * MCP描述信息
 * 
 * <p>用于Model Context Protocol的协议描述</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class MCPDescription {
    
    private final String toolName;
    private final String description;
    private final String inputSchema;
    private final String outputSchema;
    
    public MCPDescription(String toolName, String description, String inputSchema, String outputSchema) {
        this.toolName = toolName;
        this.description = description;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getInputSchema() {
        return inputSchema;
    }
    
    public String getOutputSchema() {
        return outputSchema;
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
        private String toolName;
        private String description;
        private String inputSchema = "{}";
        private String outputSchema = "{}";
        
        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder inputSchema(String inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }
        
        public Builder outputSchema(String outputSchema) {
            this.outputSchema = outputSchema;
            return this;
        }
        
        public MCPDescription build() {
            if (toolName == null) {
                toolName = "unnamed_tool";
            }
            if (description == null) {
                description = "No description provided";
            }
            
            return new MCPDescription(toolName, description, inputSchema, outputSchema);
        }
    }
}