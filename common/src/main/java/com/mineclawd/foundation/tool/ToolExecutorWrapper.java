package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;
import com.mineclawd.MineClawd;
import com.mineclawd.foundation.llm.OpenAITool;
import com.mineclawd.foundation.llm.VertexAIFunction;
import net.minecraft.server.command.ServerCommandSource;

/**
 * 工具执行器包装类
 * 将 MineClawdTool 转换为 LLM 客户端可用的格式
 */
public class ToolExecutorWrapper {
    private final MineClawdTool tool;
    
    public ToolExecutorWrapper(MineClawdTool tool) {
        this.tool = tool;
    }
    
    /**
     * 执行工具
     * @param source 命令源
     * @param args 工具参数
     * @return 执行结果字符串
     */
    public String execute(ServerCommandSource source, JsonObject args) {
        if (tool == null) {
            return "ERROR: Tool is null";
        }
        
        if (!tool.isEnabled()) {
            return "ERROR: Tool is disabled";
        }
        
        try {
            var result = tool.execute(source, args);
            return result.success() ? result.output() : "ERROR: " + result.output();
        } catch (Exception e) {
            MineClawd.LOGGER.error("Tool execution failed: {}", tool.getName(), e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * 转换为 OpenAI Tool
     */
    public OpenAITool toOpenAITool() {
        if (!tool.isEnabled()) {
            return null;
        }
        
        return new OpenAITool(
            tool.getName(),
            tool.getDescription(),
            tool.getParameters(),
            tool.getPromptAppendix()
        );
    }
    
    /**
     * 转换为 Vertex AI Function
     */
    public VertexAIFunction toVertexAIFunction() {
        if (!tool.isEnabled()) {
            return null;
        }
        
        return new VertexAIFunction(
            tool.getName(),
            tool.getDescription(),
            tool.getParameters(),
            tool.getPromptAppendix()
        );
    }
}
