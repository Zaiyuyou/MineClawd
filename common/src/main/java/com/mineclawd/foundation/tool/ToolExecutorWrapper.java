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
     * 转换为 OpenAI Tool，根据RevealPolicy层级优化信息暴露
     */
    public OpenAITool toOpenAITool() {
        if (!tool.isEnabled()) {
            return null;
        }
        
        MineClawdTool.RevealPolicy policy = tool.getRevealPolicy();
        MineClawdTool.RevealLayer layer = policy != null ? policy.getLayer() : MineClawdTool.RevealLayer.LAYER_2;
        
        // 根据层级优化工具信息
        String optimizedDescription = optimizeDescriptionForLayer(tool.getDescription(), layer);
        JsonObject optimizedParameters = optimizeParametersForLayer(tool.getParameters(), layer);
        String optimizedAppendix = optimizeAppendixForLayer(tool.getPromptAppendix(), layer);
        
        return new OpenAITool(
            tool.getName(),
            optimizedDescription,
            optimizedParameters,
            optimizedAppendix
        );
    }
    
    /**
     * 转换为 Vertex AI Function，根据RevealPolicy层级优化信息暴露
     */
    public VertexAIFunction toVertexAIFunction() {
        if (!tool.isEnabled()) {
            return null;
        }
        
        MineClawdTool.RevealPolicy policy = tool.getRevealPolicy();
        MineClawdTool.RevealLayer layer = policy != null ? policy.getLayer() : MineClawdTool.RevealLayer.LAYER_2;
        
        // 根据层级优化工具信息
        String optimizedDescription = optimizeDescriptionForLayer(tool.getDescription(), layer);
        JsonObject optimizedParameters = optimizeParametersForLayer(tool.getParameters(), layer);
        String optimizedAppendix = optimizeAppendixForLayer(tool.getPromptAppendix(), layer);
        
        return new VertexAIFunction(
            tool.getName(),
            optimizedDescription,
            optimizedParameters,
            optimizedAppendix
        );
    }
    
    /**
     * 根据层级优化描述信息
     */
    private String optimizeDescriptionForLayer(String description, MineClawdTool.RevealLayer layer) {
        if (description == null || description.isBlank()) {
            return "No description available";
        }
        
        switch (layer) {
            case LAYER_1:
                // 层级1：简短描述（最多50字符）
                if (description.length() > 50) {
                    return description.substring(0, 47) + "...";
                }
                return description;
                
            case LAYER_2:
                // 层级2：完整描述
                return description;
                
            case LAYER_3:
                // 层级3：完整描述 + 额外信息
                return description + " (Detailed guide available via tool-info-request)";
                
            default:
                return description;
        }
    }
    
    /**
     * 根据层级优化参数信息
     */
    private JsonObject optimizeParametersForLayer(JsonObject parameters, MineClawdTool.RevealLayer layer) {
        if (parameters == null) {
            return new JsonObject();
        }
        
        switch (layer) {
            case LAYER_1:
                // 层级1：只包含必需参数
                return simplifyParameters(parameters, true);
                
            case LAYER_2:
                // 层级2：包含所有参数
                return parameters;
                
            case LAYER_3:
                // 层级3：完整参数信息
                return parameters;
                
            default:
                return parameters;
        }
    }
    
    /**
     * 根据层级优化附录信息
     */
    private String optimizeAppendixForLayer(String appendix, MineClawdTool.RevealLayer layer) {
        if (appendix == null || appendix.isBlank()) {
            return null;
        }
        
        switch (layer) {
            case LAYER_1:
                // 层级1：不包含附录（节约token）
                return null;
                
            case LAYER_2:
                // 层级2：简短附录
                if (appendix.length() > 100) {
                    return appendix.substring(0, 97) + "...";
                }
                return appendix;
                
            case LAYER_3:
                // 层级3：完整附录
                return appendix;
                
            default:
                return appendix;
        }
    }
    
    /**
     * 简化参数，只保留必需参数
     */
    private JsonObject simplifyParameters(JsonObject parameters, boolean essentialOnly) {
        if (!parameters.has("properties") || !parameters.get("properties").isJsonObject()) {
            return parameters;
        }
        
        JsonObject simplified = parameters.deepCopy();
        JsonObject properties = simplified.getAsJsonObject("properties");
        JsonObject simplifiedProperties = new JsonObject();
        
        // 只保留必需的参数
        properties.entrySet().forEach(entry -> {
            JsonObject prop = entry.getValue().getAsJsonObject();
            boolean isRequired = prop.has("required") && prop.get("required").getAsBoolean();
            
            if (!essentialOnly || isRequired) {
                simplifiedProperties.add(entry.getKey(), prop);
            }
        });
        
        simplified.add("properties", simplifiedProperties);
        return simplified;
    }
}
