import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolRegistry;
import com.mineclawd.foundation.tool.prompt.RevealedToolPromptSystem;

import java.util.Map;

/**
 * 测试工具暴露策略
 */
public class TestToolExposure {
    public static void main(String[] args) {
        System.out.println("=== 测试工具暴露策略 ===");
        
        // 获取所有启用的工具
        Map<String, MineClawdTool> tools = ToolRegistry.getAllEnabled();
        
        System.out.println("总工具数量: " + tools.size());
        
        // 统计不同策略的工具数量
        int aggressiveCount = 0;
        int activeCount = 0;
        int passiveCount = 0;
        int noPolicyCount = 0;
        
        for (MineClawdTool tool : tools.values()) {
            try {
                MineClawdTool.RevealPolicy policy = tool.getRevealPolicy();
                if (policy == null) {
                    noPolicyCount++;
                } else {
                    switch (policy.getTiming()) {
                        case AGGRESSIVE:
                            aggressiveCount++;
                            break;
                        case ACTIVE:
                            activeCount++;
                            break;
                        case PASSIVE:
                            passiveCount++;
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting policy for tool: " + tool.getName());
            }
        }
        
        System.out.println("AGGRESSIVE 策略工具数量: " + aggressiveCount);
        System.out.println("ACTIVE 策略工具数量: " + activeCount);
        System.out.println("PASSIVE 策略工具数量: " + passiveCount);
        System.out.println("无策略工具数量: " + noPolicyCount);
        
        // 测试系统提示词构建
        System.out.println("\n=== 测试系统提示词构建 ===");
        String systemPrompt = RevealedToolPromptSystem.buildSystemPrompt();
        System.out.println("系统提示词长度: " + systemPrompt.length());
        
        // 检查是否包含ACTIVE工具列表
        if (systemPrompt.contains("AVAILABLE TOOLS (ACTIVE Strategy)")) {
            System.out.println("✓ 系统提示词包含ACTIVE工具列表");
        } else {
            System.out.println("✗ 系统提示词不包含ACTIVE工具列表");
        }
        
        // 测试基础工具列表提示词
        System.out.println("\n=== 测试基础工具列表提示词 ===");
        String basicToolList = RevealedToolPromptSystem.buildBasicToolListPrompt();
        System.out.println("基础工具列表长度: " + basicToolList.length());
        
        if (basicToolList.contains("ACTIVE Strategy")) {
            System.out.println("✓ 基础工具列表只包含ACTIVE策略工具");
        } else if (basicToolList.isEmpty()) {
            System.out.println("ℹ 基础工具列表为空（没有ACTIVE策略工具）");
        } else {
            System.out.println("✗ 基础工具列表可能包含其他策略的工具");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}