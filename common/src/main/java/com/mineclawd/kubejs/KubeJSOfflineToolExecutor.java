package com.mineclawd.kubejs;

import com.mineclawd.MineClawd;
import dev.architectury.platform.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * KubeJSOffline工具执行器 - 提供KubeJSOffline功能调用接口
 * 让MineClawd可以自由调用KubeJSOffline的功能进行类检索和调试
 */
public final class KubeJSOfflineToolExecutor {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // KubeJSOffline文档路径
    private static final Path KUBEJS_DOCS_DIR = Platform.getGameFolder().resolve("kubejs/documentation");
    private static final Path MINECLAWD_CACHE_DIR = Platform.getGameFolder().resolve("mineclawd/kubejsoffline_cache");
    
    private KubeJSOfflineToolExecutor() {
    }
    
    /**
     * 检查KubeJSOffline是否可用
     */
    public static boolean isKubeJSOfflineAvailable() {
        try {
            Class.forName("pie.ilikepiefoo.kubejsoffline.KubeJSOffline");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 检查KubeJSOffline文档是否已生成
     */
    public static boolean isDocumentationGenerated() {
        return Files.exists(KUBEJS_DOCS_DIR) && Files.isDirectory(KUBEJS_DOCS_DIR);
    }
    
    /**
     * 生成KubeJSOffline知识库
     */
    public static ToolExecutionResult generateKnowledgeBase() {
        if (!isKubeJSOfflineAvailable()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed. Please install KubeJSOffline first.");
        }
        
        try {
            // 使用反射调用KubeJSOffline的文档生成功能
            Class<?> kubeJSOfflineClass = Class.forName("pie.ilikepiefoo.kubejsoffline.KubeJSOffline");
            
            // 检查是否已经生成文档
            if (isDocumentationGenerated()) {
                return new ToolExecutionResult(true, 
                    "KubeJSOffline documentation already exists. Use query methods to access the knowledge base.");
            }
            
            // 提示用户生成文档
            return new ToolExecutionResult(false, 
                "KubeJSOffline documentation not generated. Please run: /kubejs offline_docs generate");
                
        } catch (Exception e) {
            LOGGER.error("Error generating KubeJSOffline knowledge base: {}", e.getMessage());
            return new ToolExecutionResult(false, 
                "Error accessing KubeJSOffline: " + e.getMessage());
        }
    }
    
    /**
     * 生成结构化JSON知识库（核心功能）
     */
    public static ToolExecutionResult generateStructuredJSON() {
        if (!isKubeJSOfflineAvailable()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed. Please install KubeJSOffline first.");
        }
        
        try {
            // 使用反射获取KubeJSOffline的Helper实例
            Class<?> kubeJSOfflineClass = Class.forName("pie.ilikepiefoo.kubejsoffline.KubeJSOffline");
            java.lang.reflect.Field helperField = kubeJSOfflineClass.getField("HELPER");
            Object helper = helperField.get(null);
            
            if (helper == null) {
                return new ToolExecutionResult(false, 
                    "KubeJSOffline HELPER is not initialized. Please wait for mod initialization.");
            }
            
            // TODO: 实现 generateStructuredDataFromHelper 和 saveStructuredJSON 方法
            return new ToolExecutionResult(false, 
                "generateStructuredJSON not implemented yet. Please use generateKnowledgeBase instead.");
                
        } catch (Exception e) {
            LOGGER.error("Error generating structured JSON: {}", e.getMessage());
            return new ToolExecutionResult(false, 
                "Error generating structured JSON: " + e.getMessage());
        }
    }
    
    /**
     * 查询KubeJS类信息
     */
    public static ToolExecutionResult queryClassInfo(String className) {
        if (!isKubeJSOfflineAvailable()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed.");
        }
        
        if (!isDocumentationGenerated()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline documentation not generated. Please run: /kubejs offline_docs generate");
        }
        
        try {
            // 搜索文档中的类信息
            String classInfo = searchClassInDocumentation(className);
            if (classInfo != null) {
                return new ToolExecutionResult(true, classInfo);
            } else {
                return new ToolExecutionResult(false, 
                    "Class not found in KubeJSOffline documentation: " + className);
            }
        } catch (Exception e) {
            LOGGER.error("Error querying class info: {}", e.getMessage());
            return new ToolExecutionResult(false, 
                "Error querying class information: " + e.getMessage());
        }
    }
    
    /**
     * 查询KubeJS事件信息
     */
    public static ToolExecutionResult queryEventInfo(String eventName) {
        if (!isKubeJSOfflineAvailable()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed.");
        }
        
        if (!isDocumentationGenerated()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline documentation not generated. Please run: /kubejs offline_docs generate");
        }
        
        try {
            // 搜索文档中的事件信息
            String eventInfo = searchEventInDocumentation(eventName);
            if (eventInfo != null) {
                return new ToolExecutionResult(true, eventInfo);
            } else {
                return new ToolExecutionResult(false, 
                    "Event not found in KubeJSOffline documentation: " + eventName);
            }
        } catch (Exception e) {
            LOGGER.error("Error querying event info: {}", e.getMessage());
            return new ToolExecutionResult(false, 
                "Error querying event information: " + e.getMessage());
        }
    }
    
    /**
     * 获取KubeJSOffline调试信息
     */
    public static ToolExecutionResult getDebugInfo() {
        if (!isKubeJSOfflineAvailable()) {
            return new ToolExecutionResult(false, 
                "KubeJSOffline mod is not installed.");
        }
        
        try {
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("# KubeJSOffline Debug Information\n\n");
            
            debugInfo.append("## Mod Status\n");
            debugInfo.append("- Available: " + isKubeJSOfflineAvailable() + "\n");
            debugInfo.append("- Documentation Generated: " + isDocumentationGenerated() + "\n");
            
            if (isDocumentationGenerated()) {
                debugInfo.append("\n## Documentation Statistics\n");
                debugInfo.append("- Documentation Path: " + KUBEJS_DOCS_DIR + "\n");
                
                // 统计文档文件
                try {
                    List<Path> files = Files.walk(KUBEJS_DOCS_DIR)
                        .filter(Files::isRegularFile)
                        .collect(java.util.stream.Collectors.toList());
                    debugInfo.append("- Total Files: " + files.size() + "\n");
                } catch (IOException e) {
                    debugInfo.append("- File Count: Error reading directory\n");
                }
            }
            
            return new ToolExecutionResult(true, debugInfo.toString());
            
        } catch (Exception e) {
            LOGGER.error("Error getting debug info: {}", e.getMessage());
            return new ToolExecutionResult(false, 
                "Error getting debug information: " + e.getMessage());
        }
    }
    
    // 私有方法 - 在文档中搜索类信息
    private static String searchClassInDocumentation(String className) throws IOException {
        // 实现文档搜索逻辑
        List<String> results = new ArrayList<>();
        
        // 搜索HTML文档中的类信息
        Files.walk(KUBEJS_DOCS_DIR)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".html"))
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    if (content.toLowerCase().contains(className.toLowerCase())) {
                        results.add("Found in: " + path.getFileName());
                    }
                } catch (IOException e) {
                    LOGGER.debug("Error reading file: {}", path);
                }
            });
        
        return results.isEmpty() ? null : String.join("\n", results);
    }
    
    // 私有方法 - 在文档中搜索事件信息
    private static String searchEventInDocumentation(String eventName) throws IOException {
        // 实现事件搜索逻辑
        List<String> results = new ArrayList<>();
        
        // 搜索包含"Event"的文件
        Files.walk(KUBEJS_DOCS_DIR)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().contains("Event"))
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    if (content.toLowerCase().contains(eventName.toLowerCase())) {
                        results.add("Found in: " + path.getFileName());
                    }
                } catch (IOException e) {
                    LOGGER.debug("Error reading file: {}", path);
                }
            });
        
        return results.isEmpty() ? null : String.join("\n", results);
    }
    
    /**
     * 工具执行结果类
     */
    public static class ToolExecutionResult {
        private final boolean success;
        private final String output;
        
        public ToolExecutionResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }
        
        public boolean success() {
            return success;
        }
        
        public String output() {
            return output;
        }
    }
}