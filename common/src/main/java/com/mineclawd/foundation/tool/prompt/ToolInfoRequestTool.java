package com.mineclawd.foundation.tool.prompt;

import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.ToolStatusDescriptor;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.List;

/**
 * 工具信息请求工具
 * 允许LLM请求特定工具的详细信息
 */
public class ToolInfoRequestTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "tool-info-request";
    }
    
    @Override
    public String getDescription() {
        return "Request detailed information about a specific tool. Use this when you need to understand how to use a tool properly.";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        // 工具名称参数
        JsonObject toolName = new JsonObject();
        toolName.addProperty("type", "string");
        toolName.addProperty("description", "The name of the tool to get information about");
        properties.add("tool_name", toolName);
        
        // 信息层级参数
        JsonObject layer = new JsonObject();
        layer.addProperty("type", "string");
        layer.addProperty("description", "The level of detail needed: LAYER_1 (basic), LAYER_2 (detailed), LAYER_3 (full guide)");
        layer.addProperty("enum", "LAYER_1,LAYER_2,LAYER_3");
        properties.add("layer", layer);
        
        params.add("properties", properties);
        
        JsonArray required = new JsonArray();
        required.add("tool_name");
        params.add("required", required);
        
        return params;
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String toolName = args.has("tool_name") ? args.get("tool_name").getAsString() : null;
        String layer = args.has("layer") ? args.get("layer").getAsString() : "LAYER_2";
        
        if (toolName == null || toolName.isBlank()) {
            return ToolExecutionResult.failure("tool_name is required");
        }
        
        // 使用新的 RevealedToolPromptSystem 构建工具信息响应
        String response = RevealedToolPromptSystem.buildToolInfoResponse(toolName, layer);
        
        return ToolExecutionResult.success(response);
    }
    
    @Override
    public String getPromptAppendix() {
        return """
            *** TOOL INFO REQUEST TOOL ***
            
            Use this tool to get detailed information about other tools when you're unsure how to use them.
            
            Parameters:
            - tool_name (string, required): The name of the tool you want information about
            - layer (string, optional): The level of detail (LAYER_1=basic, LAYER_2=detailed, LAYER_3=full guide)
            
            Usage guidelines:
            1. Use this before calling a tool you're unfamiliar with
            2. Choose the appropriate detail level based on your needs
            3. Pay attention to required parameters and usage examples
            4. Use the information to make better tool selection decisions
            
            Example usage:
            - Get basic info: {"tool_name": "execute-command", "layer": "LAYER_1"}
            - Get detailed info: {"tool_name": "apply-instant-server-script", "layer": "LAYER_2"}
            - Get full guide: {"tool_name": "list-server-scripts", "layer": "LAYER_3"}
            """;
    }
    
    @Override
    public String getPromptCategory() {
        return "system";
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public boolean supportsAsync() {
        return false;
    }
    
    @Override
    public ToolStatusDescriptor getToolStatusDescriptor(String toolName, JsonObject args) {
        String requestedTool = args.has("tool_name") ? args.get("tool_name").getAsString() : null;
        String shortText = requestedTool == null || requestedTool.isBlank() ? 
            "Requested tool information" : "Requested info for: " + requestedTool;
        String hoverText = requestedTool == null || requestedTool.isBlank() ? 
            "" : "Tool: " + requestedTool;
        return new ToolStatusDescriptor(shortText, hoverText);
    }
    
    @Override
    public MineClawdTool.RevealStrategy getRevealStrategy() {
        return MineClawdTool.RevealStrategy.ALWAYS_FULL; // 工具信息请求工具总是提供完整信息
    }
    
    @Override
    public String getFullGuide() {
        return """
            # Tool Information Request Tool - Complete Guide
            
            ## Overview
            The Tool Information Request tool allows you to get detailed information about any available tool in the system. This is particularly useful when you're unsure how to use a specific tool or need more detailed instructions.
            
            ## When to Use This Tool
            - Before calling a tool you're unfamiliar with
            - When you need detailed parameter information
            - When you want usage examples and best practices
            - When you encounter errors with a tool and need troubleshooting guidance
            
            ## Available Information Levels
            
            ### LAYER_1 (Basic)
            - Tool name and brief description
            - Basic usage information
            
            ### LAYER_2 (Detailed)  
            - Complete parameter descriptions
            - Required vs optional parameters
            - Detailed usage guidelines
            
            ### LAYER_3 (Full Guide)
            - Complete documentation with examples
            - Best practices and common pitfalls
            - Reference resources
            - Troubleshooting guide
            
            ## Example Usage Patterns
            
            **Basic Information Request:**
            ```json
            {"tool_name": "execute-command", "layer": "LAYER_1"}
            ```
            
            **Detailed Information Request:**
            ```json
            {"tool_name": "apply-instant-server-script", "layer": "LAYER_2"}
            ```
            
            **Complete Guide Request:**
            ```json
            {"tool_name": "list-server-scripts", "layer": "LAYER_3"}
            ```
            
            ## Best Practices
            
            1. **Start with LAYER_1** for unfamiliar tools to get basic understanding
            2. **Use LAYER_2** when you need parameter details for implementation
            3. **Request LAYER_3** for complex tools or when troubleshooting
            4. **Cache the information** - you don't need to request the same tool multiple times
            5. **Combine with tool calls** - use the information immediately in your workflow
            
            ## Common Scenarios
            
            **Scenario 1: Learning a New Tool**
            1. Request LAYER_1 information
            2. Read the basic description
            3. If needed, request LAYER_2 for parameter details
            4. Implement the tool call
            
            **Scenario 2: Troubleshooting**
            1. Request LAYER_3 information for the problematic tool
            2. Check the best practices section
            3. Review parameter requirements
            4. Adjust your implementation accordingly
            
            ## Integration with Other Tools
            
            This tool works seamlessly with all other tools in the system. After getting information about a tool, you can immediately use that knowledge to make better tool selection and implementation decisions.
            
            Remember: This tool is your gateway to understanding the entire tool ecosystem. Use it wisely to become more effective in your tasks.
            """;
    }
    
    @Override
    public List<String> getReferenceResources() {
        return Arrays.asList(
            "System Tool Documentation",
            "Tool Usage Best Practices Guide", 
            "Parameter Specification Standards",
            "Error Handling Guidelines"
        );
    }
}