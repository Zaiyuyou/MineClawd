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
     * 创建OpenAI工具列表
     */
    public static List<OpenAITool> createOpenAiTools(boolean dynamicRegistryEnabled, boolean searchEnabled) {
        List<ToolDefinition> definitions = ToolRegistry.getFiltered(dynamicRegistryEnabled, searchEnabled);
        List<OpenAITool> tools = new ArrayList<>();
        
        for (ToolDefinition definition : definitions) {
            tools.add(new OpenAITool(
                definition.name(),
                definition.description(),
                definition.parameters()
            ));
        }
        
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