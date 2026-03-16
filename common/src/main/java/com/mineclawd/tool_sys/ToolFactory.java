package com.mineclawd.tool_sys;

import com.mineclawd.llm.OpenAITool;
import com.mineclawd.llm.VertexAIFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具工厂类，根据LLM提供商创建对应的工具对象
 */
public final class ToolFactory {
    
    /**
     * 注册工具定义到ToolRegistry
     */
    public static void registerTool(ToolDefinition tool) {
        ToolRegistry.register(tool);
        System.out.println("[ToolFactory] 已注册工具: " + tool.name());
    }
    
    /**
     * 注销工具定义
     */
    public static void unregisterTool(String toolName) {
        ToolRegistry.unregister(toolName);
        System.out.println("[ToolFactory] 已注销工具: " + toolName);
    }
    
    /**
     * 创建OpenAI工具列表
     */
    public static List<OpenAITool> createOpenAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        System.out.println("[ToolFactory] createOpenAiTools() 被调用: dynamicRegistryEnabled=" + dynamicRegistryEnabled + ", searchEnabled=" + searchEnabled);
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        System.out.println("[ToolFactory] ToolRegistry.getFiltered() 返回 " + definitions.size() + " 个工具定义");
        List<OpenAITool> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            System.out.println("[ToolFactory] 添加工具: " + definition.name());
            tools.add(new OpenAITool(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
        System.out.println("[ToolFactory] createOpenAiTools() 返回 " + tools.size() + " 个工具");
        return tools;
    }
    
    /**
     * 创建VertexAI工具列表
     */
    public static List<VertexAIFunction> createVertexTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        List<VertexAIFunction> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            tools.add(new VertexAIFunction(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
        return tools;
    }
}
