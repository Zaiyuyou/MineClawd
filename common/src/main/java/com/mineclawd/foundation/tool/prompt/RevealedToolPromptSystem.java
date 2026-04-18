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
     * 只包含使用ACTIVE策略的工具
     */
    public static String buildBasicToolListPrompt() {
        try {
            Map<String, MineClawdTool> tools = ToolRegistry.getAllEnabled();
            
            // 过滤出使用ACTIVE策略的工具
            List<MineClawdTool> activeTools = new ArrayList<>();
            tools.forEach((name, tool) -> {
                try {
                    MineClawdTool.RevealPolicy policy = tool.getRevealPolicy();
                    if (policy != null && policy.getTiming() == MineClawdTool.RevealTiming.ACTIVE) {
                        activeTools.add(tool);
                    }
                } catch (Exception e) {
                    MineClawd.LOGGER.warn("Error processing reveal policy for {}: {}", name, e.getMessage());
                }
            });
            
            if (activeTools.isEmpty()) {
                return "";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("--- AVAILABLE TOOLS (ACTIVE Strategy) ---\n");
            sb.append("These tools are available in system context. Use them when appropriate.\n");
            sb.append("For tools with AGGRESSIVE strategy, full details are always available in the tools role.\n");
            sb.append("If you need detailed information about any tool, use the 'tool-info-request' tool.\n\n");
            
            // 按分类分组显示
            Map<String, List<MineClawdTool>> toolsByCategory = new LinkedHashMap<>();
            
            activeTools.forEach(tool -> {
                try {
                    String category = tool.getPromptCategory();
                    if (category == null || category.isBlank()) {
                        category = "uncategorized";
                    }
                    toolsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(tool);
                } catch (Exception e) {
                    MineClawd.LOGGER.warn("Error processing tool category for {}: {}", tool.getName(), e.getMessage());
                }
            });
        
            toolsByCategory.forEach((category, categoryTools) -> {
                sb.append("[").append(category).append("]\n");
                categoryTools.forEach(tool -> {
                    try {
                        String basicDesc = extractBasicDescription(tool);
                        sb.append("  - ").append(tool.getName()).append(": ").append(basicDesc);
                        sb.append(" [Available in system context]\n");
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
                // 使用工具的默认RevealLayer
                try {
                    MineClawdTool.RevealPolicy policy = tool.getRevealPolicy();
                    promptLayer = PromptLayer.valueOf(policy.getLayer().name());
                } catch (Exception ex) {
                    promptLayer = PromptLayer.LAYER_2; // 出错时使用默认值
                }
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
     * 构建智能工具调用指南
     */
    public static String buildToolCallGuidelines() {
        return """
            ## 智能工具使用指南
            
            你是一个可以访问各种工具的AI助手。工具是模块化的，随时可能增减，请遵循以下智能发现机制：
            
            ### 工具发现流程
            1. **理解用户需求**：先分析用户请求的具体任务
            2. **探索可用工具**：使用基础工具集来发现功能工具
            3. **获取详细信息**：需要时使用工具信息请求功能
            4. **执行任务**：选择合适的工具组合完成任务
            
            ### 基础工具集（总是可用）
            - `tool-info-request`：获取特定工具的完整信息
            - `list-available-tools`：查看所有可用工具名称
            - `search-tools`：根据关键词搜索工具
            - `ask-user-question`：向用户提问获取更多信息
            
            ### 智能使用策略
            - **新任务探索**：不确定时先使用工具发现功能
            - **复杂任务**：可以组合使用多个相关工具
            - **高风险操作**：确认用户意图后再执行
            - **错误处理**：工具失败时尝试替代方案
            
            ### 最佳实践
            - 向用户解释将要执行的操作
            - 提供操作进度和结果反馈
            - 处理错误并提供恢复建议
            - 保存常用工具组合模式
            
            记住：工具是你的助手，合理使用它们来获得最佳结果。
            """;
    }
    
    /**
     * 构建工具发现机制说明
     */
    public static String buildToolDiscoveryGuidance() {
        return """
            ## 工具发现机制
            
            由于工具是模块化的，你可能不知道所有可用工具。使用以下机制来发现工具：
            
            ### 发现流程示例
            用户："帮我修改config文件"
            助手："我需要文件编辑工具来完成这个任务。让我先查看可用的文件操作工具..."
            [使用list-tools工具]
            [发现file-read和file-write工具]
            [使用tool-info-request获取file-write工具的详细说明]
            [执行文件修改任务]
            
            ### 何时使用工具发现
            - 遇到新类型的任务时
            - 不确定某个功能是否有对应工具时
            - 需要了解工具详细用法时
            - 工具执行失败需要替代方案时
            
            ### 工具分类参考
            - **文件操作**：file-read, file-write, file-copy, file-move, grep
            - **命令执行**：execute-command, apply-script, reload-game
            - **网络搜索**：search, fetch-url, fetch-modrinth
            - **资源管理**：list-assets, upsert-asset, remove-asset
            - **动态内容**：register-item, update-block, unregister-content
            """;
    }
    
    /**
     * 构建系统提示词（智能上下文管理）
     * 不再依赖会话初始化状态，简化实现
     */
    public static String buildSystemPrompt(String sessionId, boolean isSessionInitialized) {
        StringBuilder sb = new StringBuilder();
        
        // 智能工具使用指南（总是包含）
        sb.append(buildToolCallGuidelines()).append("\n\n");
        
        // 工具发现机制说明（总是包含）
        sb.append(buildToolDiscoveryGuidance()).append("\n\n");
        
        // 智能判断是否需要包含基础工具列表（基于RevealPolicy）
        if (shouldIncludeToolListInSystemPrompt()) {
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
        return buildSystemPrompt((String) null, false);
    }
    
    /**
     * 构建系统提示词（向后兼容）
     */
    public static String buildSystemPrompt(String sessionId) {
        // 默认包含工具列表（向后兼容）
        return buildSystemPrompt(sessionId, false);
    }
    
    /**
     * 智能判断是否需要包含工具列表（基于RevealPolicy）
     * 简化实现，不再依赖会话状态
     */
    private static boolean shouldIncludeToolListInSystemPrompt() {
        // 检查是否有任何工具使用ACTIVE策略
        // 如果有，则需要包含工具列表（ACTIVE工具保存在系统提示词中）
        Map<String, MineClawdTool> tools = ToolRegistry.getAllEnabled();
        for (MineClawdTool tool : tools.values()) {
            try {
                MineClawdTool.RevealPolicy policy = tool.getRevealPolicy();
                if (policy != null) {
                    MineClawdTool.RevealTiming timing = policy.getTiming();
                    if (timing == MineClawdTool.RevealTiming.ACTIVE) {
                        // 如果有ACTIVE策略的工具，需要包含在系统提示词中
                        return true;
                    }
                }
            } catch (Exception e) {
                MineClawd.LOGGER.warn("Error checking reveal policy for tool: {}", e.getMessage());
            }
        }
        
        // 默认：如果没有ACTIVE策略的工具，不包含工具列表
        // AGGRESSIVE工具在tools角色中提供，不需要在系统提示词中列出
        return false;
    }
    
    
    // /**
    //  * 简单的会话处理状态缓存
    //  */
    // private static final Set<String> processedSessions = new HashSet<>();
    
    // /**
    //  * 检查会话是否已经处理过
    //  */
    // private static boolean isSessionProcessed(String sessionId) {
    //     if (sessionId == null || sessionId.isBlank()) {
    //         return false;
    //     }
        
    //     // 如果会话已经处理过，返回true
    //     if (processedSessions.contains(sessionId)) {
    //         return true;
    //     }
        
    //     // 标记会话为已处理
    //     processedSessions.add(sessionId);
    //     return false;
    // }
    
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