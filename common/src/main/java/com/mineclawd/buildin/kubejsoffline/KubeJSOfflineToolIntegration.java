package com.mineclawd.buildin.kubejsoffline;

import com.mineclawd.MineClawd;
import dev.architectury.platform.Platform;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * KubeJSOffline工具集成类 - 提供KubeJSOffline知识库查询功能
 * 集成到MineClawd工具系统中，支持KubeJSOffline知识库的生成、查询和验证
 */
public final class KubeJSOfflineToolIntegration {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private KubeJSOfflineToolIntegration() {
    }
    
    /**
     * KubeJSOffline查询类信息工具
     */
    public static KubeJSOfflineToolExecutor.ToolExecutionResult kubejsofflineQueryClass(ServerCommandSource source, String className) {
        LOGGER.info("[KubeJSOffline] Querying class: {}", className);
        
        // 检查KubeJSOffline是否可用
        if (!KubeJSOfflineToolExecutor.isKubeJSOfflineAvailable()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed. Please install KubeJSOffline first.");
        }
        
        // 检查文档是否已生成
        if (!KubeJSOfflineToolExecutor.isDocumentationGenerated()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline documentation not generated. Please run: /kubejs offline_docs generate");
        }
        
        // 查询类信息
        KubeJSOfflineToolExecutor.ToolExecutionResult result = KubeJSOfflineToolExecutor.queryClassInfo(className);
        
        // 记录查询结果
        if (result.success()) {
            LOGGER.info("[KubeJSOffline] Class query successful: {}", className);
        } else {
            LOGGER.warn("[KubeJSOffline] Class query failed: {}", className);
        }
        
        return result;
    }
    
    /**
     * KubeJSOffline查询事件信息工具
     */
    public static KubeJSOfflineToolExecutor.ToolExecutionResult kubejsofflineQueryEvent(ServerCommandSource source, String eventName) {
        LOGGER.info("[KubeJSOffline] Querying event: {}", eventName);
        
        // 检查KubeJSOffline是否可用
        if (!KubeJSOfflineToolExecutor.isKubeJSOfflineAvailable()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed. Please install KubeJSOffline first.");
        }
        
        // 检查文档是否已生成
        if (!KubeJSOfflineToolExecutor.isDocumentationGenerated()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline documentation not generated. Please run: /kubejs offline_docs generate");
        }
        
        // 查询事件信息
        KubeJSOfflineToolExecutor.ToolExecutionResult result = KubeJSOfflineToolExecutor.queryEventInfo(eventName);
        
        // 记录查询结果
        if (result.success()) {
            LOGGER.info("[KubeJSOffline] Event query successful: {}", eventName);
        } else {
            LOGGER.warn("[KubeJSOffline] Event query failed: {}", eventName);
        }
        
        return result;
    }
    
    /**
     * KubeJSOffline生成知识库工具
     */
    public static KubeJSOfflineToolExecutor.ToolExecutionResult kubejsofflineGenerateKb(ServerCommandSource source) {
        LOGGER.info("[KubeJSOffline] Generating knowledge base");
        
        // 检查KubeJSOffline是否可用
        if (!KubeJSOfflineToolExecutor.isKubeJSOfflineAvailable()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed. Please install KubeJSOffline first.");
        }
        
        // 生成知识库
        KubeJSOfflineToolExecutor.ToolExecutionResult result = KubeJSOfflineToolExecutor.generateKnowledgeBase();
        
        // 记录生成结果
        if (result.success()) {
            LOGGER.info("[KubeJSOffline] Knowledge base generation successful");
        } else {
            LOGGER.warn("[KubeJSOffline] Knowledge base generation failed");
        }
        
        return result;
    }
    
    /**
     * KubeJSOffline调试信息工具
     */
    public static KubeJSOfflineToolExecutor.ToolExecutionResult kubejsofflineDebugInfo(ServerCommandSource source) {
        LOGGER.info("[KubeJSOffline] Getting debug information");
        
        // 获取调试信息
        KubeJSOfflineToolExecutor.ToolExecutionResult result = KubeJSOfflineToolExecutor.getDebugInfo();
        
        // 记录调试信息
        LOGGER.info("[KubeJSOffline] Debug info retrieved");
        
        return result;
    }
    
    /**
     * 验证KubeJS代码是否使用正确的API（后生成验证）
     */
    public static KubeJSOfflineToolExecutor.ToolExecutionResult verifyKubeJSCode(ServerCommandSource source, String code) {
        LOGGER.info("[KubeJSOffline] Verifying KubeJS code");
        
        // 检查KubeJSOffline是否可用
        if (!KubeJSOfflineToolExecutor.isKubeJSOfflineAvailable()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed. Cannot verify code accuracy.");
        }
        
        // 检查文档是否已生成
        if (!KubeJSOfflineToolExecutor.isDocumentationGenerated()) {
            return new KubeJSOfflineToolExecutor.ToolExecutionResult(false, 
                "KubeJSOffline documentation not generated. Cannot verify code accuracy.");
        }
        
        // 提取代码中的类和事件引用
        List<String> classes = extractClassReferences(code);
        List<String> events = extractEventReferences(code);
        
        StringBuilder verificationReport = new StringBuilder();
        verificationReport.append("# KubeJS Code Verification Report\n\n");
        
        // 验证类引用
        if (!classes.isEmpty()) {
            verificationReport.append("## Class References Verification\n");
            for (String className : classes) {
                KubeJSOfflineToolExecutor.ToolExecutionResult classResult = kubejsofflineQueryClass(source, className);
                if (classResult.success()) {
                    verificationReport.append("- ✅ ").append(className).append(": Verified\n");
                } else {
                    verificationReport.append("- ❌ ").append(className).append(": Not found in KubeJSOffline knowledge base\n");
                }
            }
            verificationReport.append("\n");
        }
        
        // 验证事件引用
        if (!events.isEmpty()) {
            verificationReport.append("## Event References Verification\n");
            for (String eventName : events) {
                KubeJSOfflineToolExecutor.ToolExecutionResult eventResult = kubejsofflineQueryEvent(source, eventName);
                if (eventResult.success()) {
                    verificationReport.append("- ✅ ").append(eventName).append(": Verified\n");
                } else {
                    verificationReport.append("- ❌ ").append(eventName).append(": Not found in KubeJSOffline knowledge base\n");
                }
            }
            verificationReport.append("\n");
        }
        
        // 检查代码质量
        verificationReport.append("## Code Quality Assessment\n");
        verificationReport.append(assessCodeQuality(code));
        
        LOGGER.info("[KubeJSOffline] Code verification completed");
        return new KubeJSOfflineToolExecutor.ToolExecutionResult(true, verificationReport.toString());
    }
    
    /**
     * 从代码中提取类引用
     */
    private static List<String> extractClassReferences(String code) {
        List<String> classes = new ArrayList<>();
        // 简单的类引用提取逻辑
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("new ") || line.contains(".") || line.contains("extends")) {
                // 这里可以添加更复杂的类名提取逻辑
                if (line.contains("Item") || line.contains("Block") || line.contains("Entity")) {
                    classes.add("KubeJS相关类");
                }
            }
        }
        return classes;
    }
    
    /**
     * 从代码中提取事件引用
     */
    private static List<String> extractEventReferences(String code) {
        List<String> events = new ArrayList<>();
        // 简单的事件引用提取逻辑
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("Event") && (line.contains("on") || line.contains("listen"))) {
                // 这里可以添加更复杂的事件名提取逻辑
                if (line.contains("Player") || line.contains("Block") || line.contains("Entity")) {
                    events.add("KubeJS事件");
                }
            }
        }
        return events;
    }
    
    /**
     * 评估代码质量
     */
    private static String assessCodeQuality(String code) {
        StringBuilder assessment = new StringBuilder();
        
        // 检查代码长度
        if (code.length() > 1000) {
            assessment.append("- ⚠️ Code is quite long, consider breaking into smaller functions\n");
        }
        
        // 检查错误处理
        if (!code.contains("try") && !code.contains("catch")) {
            assessment.append("- ⚠️ Consider adding error handling with try/catch blocks\n");
        }
        
        // 检查注释
        if (!code.contains("//") && !code.contains("/*")) {
            assessment.append("- ℹ️ Consider adding comments for better maintainability\n");
        }
        
        // 检查现代KubeJS语法
        if (code.contains("events.") || code.contains("PlayerEvents")) {
            assessment.append("- ✅ Uses modern KubeJS event syntax\n");
        }
        
        if (assessment.length() == 0) {
            assessment.append("- ✅ Code appears to follow good practices\n");
        }
        
        return assessment.toString();
    }
}