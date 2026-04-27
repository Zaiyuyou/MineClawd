package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;


public interface MineClawdTool {
    
    /**
     * 揭露时机枚举
     */
    enum RevealTiming {
        PASSIVE,    // 被动：只在LLM通过ToolInfoRequestTool请求时揭露
        ACTIVE,     // 主动：在会话开始时揭露一次
        AGGRESSIVE  // 积极：每次对话都揭露
    }
    
    /**
     * 揭露层级枚举
     */
    enum RevealLayer {
        LAYER_1,    // 基础信息：名称和简要描述
        LAYER_2,    // 详细信息：完整描述和参数
        LAYER_3     // 完整信息：包含示例和最佳实践
    }
    
    /**
     * 揭露策略类
     */
    class RevealPolicy {
        private final RevealTiming timing;
        private final RevealLayer layer;
        
        public RevealPolicy(RevealTiming timing, RevealLayer layer) {
            this.timing = timing;
            this.layer = layer;
        }
        
        public RevealTiming getTiming() {
            return timing;
        }
        
        public RevealLayer getLayer() {
            return layer;
        }
        
        public static RevealPolicy passive(RevealLayer layer) {
            return new RevealPolicy(RevealTiming.PASSIVE, layer);
        }
        
        public static RevealPolicy active(RevealLayer layer) {
            return new RevealPolicy(RevealTiming.ACTIVE, layer);
        }
        
        public static RevealPolicy aggressive(RevealLayer layer) {
            return new RevealPolicy(RevealTiming.AGGRESSIVE, layer);
        }
        
        // 默认策略：被动揭露，层级2
        public static RevealPolicy defaultPolicy() {
            return passive(RevealLayer.LAYER_2);
        }
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
    default RevealPolicy getRevealPolicy() {
        return RevealPolicy.defaultPolicy(); // 默认使用被动揭露，层级2
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
