package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;


public interface MineClawdTool {
    
    /**
     * 揭露策略枚举
     */
    enum RevealStrategy {
        DYNAMIC,    // 动态揭露：按需提供信息，节省token
        ALWAYS_FULL // 始终全量：总是提供完整信息
    }
    
    String getName();
    
    String getDescription();
    
    JsonObject getParameters();
    
    ToolExecutionResult execute(ServerCommandSource source, JsonObject args);
    
    default String getPromptAppendix() {
        return null;
    }
    
    default String getPromptCategory() {
        return null;
    }
    
    default boolean isEnabled() {
        return true;
    }
    
    default boolean supportsAsync() {
        return false;
    }
    
    default ToolStatusDescriptor getToolStatusDescriptor(String toolName, JsonObject args) {
        return new ToolStatusDescriptor("Completed task step", "Tool: " + (toolName == null ? "" : toolName));
    }
    
    /**
     * 揭露策略配置
     * @return 工具的揭露策略
     */
    default RevealStrategy getRevealStrategy() {
        return RevealStrategy.DYNAMIC; // 默认使用动态揭露
    }
    
    /**
     * Layer 3完整指南内容（可选实现）
     * 提供工具的完整使用指南，包括示例、最佳实践等
     * @return 完整指南内容，返回null表示使用默认生成的内容
     */
    default String getFullGuide() {
        return null; // 默认使用自动生成的内容
    }
    
    /**
     * Layer 3参考资源（可选实现）
     * 提供工具相关的参考链接、文件路径等
     * @return 参考资源列表，返回null表示无额外资源
     */
    default List<String> getReferenceResources() {
        return null; // 默认无额外资源
    }
}
