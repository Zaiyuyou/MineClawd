package com.mineclawd.tool_sys;

import com.google.gson.JsonObject;

/**
 * 工具定义类，类似于LangChain的工具定义
 * 包含工具的基本信息：名称、描述、参数定义
 */
public record ToolDefinition(
        String name,
        String description,
        JsonObject parameters
) {
    
    /**
     * 创建基础工具定义
     */
    public static ToolDefinition of(String name, String description, JsonObject parameters) {
        return new ToolDefinition(name, description, parameters);
    }
    
    /**
     * 创建无参数工具定义
     */
    public static ToolDefinition noArg(String name, String description) {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new JsonObject());
        parameters.add("required", new com.google.gson.JsonArray());
        return new ToolDefinition(name, description, parameters);
    }
}