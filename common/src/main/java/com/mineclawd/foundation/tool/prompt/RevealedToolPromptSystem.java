package com.mineclawd.foundation.tool.prompt;

import com.mineclawd.MineClawd;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.*;

/**
 * 揭露式提示词系统
 * 基于现有 MineClawdTool 架构实现按需揭露工具信息的策略
 */
public class RevealedToolPromptSystem {
    
    /**
     * 提示词层级枚举
     */
    public enum PromptLayer {
        LAYER_1, // 基础层：工具名称 + 简短描述(内部文本)
        LAYER_2, // 详细层：详细描述 + 参数说明(内部文本)
        LAYER_3  // 完整层：完整指南 + 示例(外部文本)
    }
    
    /**
     * 从 MineClawdTool 提取基础描述 (Layer 1)
     */
    private static String extractBasicDescription(MineClawdTool tool) {
        String description = tool.getDescription();
        // 如果描述过长，截取前50字符
        if (description != null && description.length() > 50) {
            description = description.substring(0, 47) + "...";
        }
        return description != null ? description : "No description available";
    }
    
    /**
     * 从 MineClawdTool 提取详细描述 (Layer 2)
     */
    private static String extractDetailedDescription(MineClawdTool tool) {
        StringBuilder sb = new StringBuilder();
        
        // 基础描述
        String description = tool.getDescription();
        if (description != null) {
            sb.append("Description: ").append(description).append("\n\n");
        }
        
        // 参数信息
        JsonObject params = tool.getParameters();
        if (params != null) {
            sb.append("Parameters:\n");
            if (params.has("properties") && params.get("properties").isJsonObject()) {
                JsonObject properties = params.getAsJsonObject("properties");
                properties.entrySet().forEach(entry -> {
                    JsonObject prop = entry.getValue().getAsJsonObject();
                    String type = prop.has("type") ? prop.get("type").getAsString() : "unknown";
                    String desc = prop.has("description") ? prop.get("description").getAsString() : "";
                    sb.append("  - ").append(entry.getKey()).append(" (").append(type).append("): ").append(desc).append("\n");
                });
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 从 MineClawdTool 提取完整指南 (Layer 3)
     */
    private static String extractFullGuide(MineClawdTool tool) {
        StringBuilder sb = new StringBuilder();
        
        // 首先检查工具是否提供了自定义完整指南
        String customFullGuide;
        try {
            customFullGuide = tool.getFullGuide();
        } catch (Exception e) {
            MineClawd.LOGGER.warn("Error getting custom full guide for {}: {}", tool.getName(), e.getMessage());
            customFullGuide = null;
        }
        
        if (customFullGuide != null && !customFullGuide.isBlank()) {
            // 使用工具提供的自定义完整指南
            sb.append(customFullGuide);
        } else {
            // 自动生成完整指南
            sb.append("# Complete Guide\n\n");
            
            // 基础描述
            String description = tool.getDescription();
            if (description != null) {
                sb.append("## Description\n").append(description).append("\n\n");
            }
            
            // 参数信息
            JsonObject params = tool.getParameters();
            if (params != null) {
                sb.append("## Parameters\n");
                if (params.has("properties") && params.get("properties").isJsonObject()) {
                    JsonObject properties = params.getAsJsonObject("properties");
                    properties.entrySet().forEach(entry -> {
                        JsonObject prop = entry.getValue().getAsJsonObject();
                        String type = prop.has("type") ? prop.get("type").getAsString() : "unknown";
                        String desc = prop.has("description") ? prop.get("description").getAsString() : "";
                        sb.append("  - **").append(entry.getKey()).append("** (").append(type).append("): ").append(desc).append("\n");
                    });
                }
                
                // 必需参数
                if (params.has("required") && params.get("required").isJsonArray()) {
                    sb.append("\n**Required parameters:** ");
                    params.getAsJsonArray("required").forEach(req -> {
                        sb.append(req.getAsString()).append(" ");
                    });
                    sb.append("\n");
                }
            }
            
            // 使用指南（从 prompt appendix 中提取）
            String appendix = tool.getPromptAppendix();
            if (appendix != null && !appendix.isBlank()) {
                sb.append("\n## Usage Guide\n").append(appendix);
            }
            
            // 参考资源
            List<String> references;
            try {
                references = tool.getReferenceResources();
            } catch (Exception e) {
                MineClawd.LOGGER.warn("Error getting reference resources for {}: {}", tool.getName(), e.getMessage());
                references = null;
            }
            
            if (references != null && !references.isEmpty()) {
                sb.append("\n## Reference Resources\n");
                references.forEach(ref -> sb.append("  - ").append(ref).append("\n"));
            }
            
            // 添加通用最佳实践
            sb.append("\n## Best Practices\n");
            sb.append("1. Always provide required parameters\n");
            sb.append("2. Handle errors gracefully\n");
            sb.append("3. Use appropriate parameter values\n");
            sb.append("4. Test with simple cases first\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 构建基础工具列表提示词 (Layer 1)
     * 直接使用 ToolRegistry 获取所有启用的工具
     */
    public static String buildBasicToolListPrompt() {
        try {
            Map<String, MineClawdTool> tools = ToolRegistry.getAllEnabled();
            
            if (tools.isEmpty()) {
                return "";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("--- AVAILABLE TOOLS (Basic Info) ---\n");
            sb.append("You have access to the following tools. Use them when appropriate.\n");
            sb.append("If you need detailed information about a specific tool, use the 'tool-info-request' tool.\n\n");
            
            // 按分类分组显示
            Map<String, List<MineClawdTool>> toolsByCategory = new LinkedHashMap<>();
            
            tools.forEach((name, tool) -> {
                try {
                    String category = tool.getPromptCategory();
                    if (category == null || category.isBlank()) {
                        category = "uncategorized";
                    }
                    toolsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(tool);
                } catch (Exception e) {
                    MineClawd.LOGGER.warn("Error processing tool category for {}: {}", name, e.getMessage());
                }
            });
        
            toolsByCategory.forEach((category, categoryTools) -> {
                sb.append("[").append(category).append("]\n");
                categoryTools.forEach(tool -> {
                    try {
                        String basicDesc = extractBasicDescription(tool);
                        sb.append("  - ").append(tool.getName()).append(": ").append(basicDesc);
                        
                        // 显示启用状态
                        if (!tool.isEnabled()) {
                            sb.append(" [DISABLED]");
                        }
                        sb.append("\n");
                        
                        // 如果工具使用ALWAYS_FULL策略，添加标记
                        MineClawdTool.RevealStrategy strategy;
                        try {
                            strategy = tool.getRevealStrategy();
                        } catch (Exception e) {
                            MineClawd.LOGGER.warn("Error getting reveal strategy for {}: {}", tool.getName(), e.getMessage());
                            strategy = MineClawdTool.RevealStrategy.DYNAMIC; // 默认使用动态策略
                        }
                        
                        if (strategy == MineClawdTool.RevealStrategy.ALWAYS_FULL) {
                            sb.append("    [Full details always available]\n");
                        }
                    } catch (Exception e) {
                        MineClawd.LOGGER.warn("Error processing tool {}: {}", tool.getName(), e.getMessage());
                        sb.append("  - ").append(tool.getName()).append(": [Error processing tool description]\n");
                    }
                });
                sb.append("\n");
            });
            
            sb.append("-----------------------\n");
            return sb.toString();
        } catch (Exception e) {
            MineClawd.LOGGER.error("Error building basic tool list prompt: {}", e.getMessage());
            return "[Error loading tool information]\n";
        }
    }
    
    /**
     * 构建特定工具的详细提示词 (Layer 2)
     */
    public static String buildToolDetailedPrompt(String toolName) {
        try {
            MineClawdTool tool = ToolRegistry.get(toolName);
            if (tool == null) {
                return "Tool not found: " + toolName;
            }
            
            StringBuilder sb = new StringBuilder();
            
            // Layer 1: 基础信息
            String basicInfo = buildBasicToolInfo(tool);
            sb.append(basicInfo).append("\n\n");
            
            // Layer 2: 详细描述
            sb.append("--- DETAILED INFORMATION ---\n\n");
            String detailedDesc = extractDetailedDescription(tool);
            sb.append(detailedDesc);
            
            sb.append("\n-----------------------\n");
            return sb.toString();
        } catch (Exception e) {
            MineClawd.LOGGER.error("Error building detailed prompt for {}: {}", toolName, e.getMessage());
            return "Error loading tool details for: " + toolName;
        }
    }
    
    /**
     * 构建特定工具的完整指南 (Layer 3)
     */
    public static String buildToolFullGuide(String toolName) {
        try {
            MineClawdTool tool = ToolRegistry.get(toolName);
            if (tool == null) {
                return "Tool not found: " + toolName;
            }
            
            StringBuilder sb = new StringBuilder();
            
            // Layer 1: 基础信息
            String basicInfo = buildBasicToolInfo(tool);
            sb.append(basicInfo).append("\n\n");
            
            // Layer 2: 详细描述
            sb.append("--- DETAILED INFORMATION ---\n\n");
            String detailedDesc = extractDetailedDescription(tool);
            sb.append(detailedDesc);
            
            // Layer 3: 完整指南
            sb.append("\n--- COMPLETE GUIDE ---\n\n");
            String fullGuide = extractFullGuide(tool);
            sb.append(fullGuide);
            
            sb.append("\n-----------------------\n");
            return sb.toString();
        } catch (Exception e) {
            MineClawd.LOGGER.error("Error building full guide for {}: {}", toolName, e.getMessage());
            return "Error loading full guide for: " + toolName;
        }
    }
    
    /**
     * 构建工具信息请求的响应
     */
    public static String buildToolInfoResponse(String toolName, String layer) {
        try {
            MineClawdTool tool = ToolRegistry.get(toolName);
            if (tool == null) {
                return "ERROR: Tool not found: " + toolName;
            }
            
            PromptLayer promptLayer;
            try {
                promptLayer = PromptLayer.valueOf(layer.toUpperCase());
            } catch (IllegalArgumentException e) {
                promptLayer = PromptLayer.LAYER_2; // 默认使用详细层
            }
            
            StringBuilder sb = new StringBuilder();
            
            // Layer 1: 基础信息（所有层级都包含）
            String basicInfo = buildBasicToolInfo(tool);
            sb.append(basicInfo).append("\n\n");
            
            switch (promptLayer) {
                case LAYER_1:
                    // 只包含基础信息
                    break;
                    
                case LAYER_2:
                    // 基础信息 + 详细描述
                    String detailedDesc = extractDetailedDescription(tool);
                    sb.append("--- DETAILED INFORMATION ---\n\n");
                    sb.append(detailedDesc);
                    break;
                    
                case LAYER_3:
                    // 基础信息 + 详细描述 + 完整指南
                    String detailedDescL3 = extractDetailedDescription(tool);
                    sb.append("--- DETAILED INFORMATION ---\n\n");
                    sb.append(detailedDescL3);
                    
                    sb.append("\n--- COMPLETE GUIDE ---\n\n");
                    String fullGuide = extractFullGuide(tool);
                    sb.append(fullGuide);
                    break;
                    
                default:
                    String detailedDescDefault = extractDetailedDescription(tool);
                    sb.append("--- DETAILED INFORMATION ---\n\n");
                    sb.append(detailedDescDefault);
            }
            
            return sb.toString();
        } catch (Exception e) {
            MineClawd.LOGGER.error("Error building tool info response for {}: {}", toolName, e.getMessage());
            return "Error loading tool information for: " + toolName;
        }
    }
    
    /**
     * 构建基础工具信息 (Layer 1)
     */
    private static String buildBasicToolInfo(MineClawdTool tool) {
        String basicDesc = extractBasicDescription(tool);
        return "Tool: " + tool.getName() + "\nDescription: " + basicDesc;
    }
    
    /**
     * 构建工具调用指南
     */
    public static String buildToolCallGuidelines() {
        return """
            ### Tool Call Guidelines
            
            When using tools, follow these principles:
            
            1. **Choose the right tool**: Select the most appropriate tool for the task
            2. **Provide required parameters**: Ensure all required parameters are provided
            3. **Handle errors gracefully**: If a tool fails, try alternative approaches
            4. **Use tool-info-request**: When unsure about a tool's usage, request detailed information
            5. **Be specific**: Provide clear and specific parameter values
            
            Remember: Tools are your assistants. Use them wisely to achieve the best results.
            """;
    }
    
    /**
     * 构建系统提示词（智能上下文管理）
     * @param sessionId 会话ID，用于判断是否需要包含工具列表
     */
    public static String buildSystemPrompt(String sessionId) {
        StringBuilder sb = new StringBuilder();
        
        // 工具调用指南（总是包含）
        sb.append(buildToolCallGuidelines()).append("\n\n");
        
        // 智能判断是否需要包含基础工具列表
        if (shouldIncludeToolList(sessionId)) {
            String toolList = buildBasicToolListPrompt();
            if (!toolList.isBlank()) {
                sb.append(toolList);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 构建系统提示词（向后兼容）
     */
    public static String buildSystemPrompt() {
        // 默认包含工具列表（向后兼容）
        return buildSystemPrompt((String) null);
    }
    
    /**
     * 智能判断是否需要包含工具列表
     */
    private static boolean shouldIncludeToolList(String sessionId) {
        // 如果会话ID为空，说明是新会话，需要包含工具列表
        if (sessionId == null || sessionId.isBlank()) {
            return true;
        }
        
        // 使用简单的会话ID缓存机制
        // 对于同一个会话ID，只在首次请求时包含工具列表
        return !isSessionProcessed(sessionId);
    }
    
    /**
     * 简单的会话处理状态缓存
     */
    private static final Set<String> processedSessions = new HashSet<>();
    
    /**
     * 检查会话是否已经处理过
     */
    private static boolean isSessionProcessed(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        
        // 如果会话已经处理过，返回true
        if (processedSessions.contains(sessionId)) {
            return true;
        }
        
        // 标记会话为已处理
        processedSessions.add(sessionId);
        return false;
    }
    
    /**
     * 检查是否需要工具信息请求
     */
    public static boolean needsToolInfoRequest(String toolName, List<String> conversationHistory) {
        MineClawdTool tool = ToolRegistry.get(toolName);
        if (tool == null) {
            return false;
        }
        
        // 检查对话历史中是否已经请求过该工具的详细信息
        for (String message : conversationHistory) {
            if (message.contains("TOOL DETAILS: " + toolName) || 
                message.contains("TOOL FULL GUIDE: " + toolName)) {
                return false; // 已经请求过，不需要再次请求
            }
        }
        
        // 对于复杂工具，建议请求详细信息
        String toolNameLower = toolName.toLowerCase();
        return toolNameLower.contains("script") || 
               toolNameLower.contains("file") || 
               toolNameLower.contains("search") ||
               toolNameLower.contains("apply");
    }
}