package com.mineclawd.modules.demo;

import com.mineclawd.modules.core.*;
import com.mineclawd.modules.registry.*;
import com.mineclawd.modules.tool.*;
import java.util.Map;

/**
 * 模块化架构演示类
 * 
 * <p>展示如何使用新的模块化接口</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ModuleDemo {
    
    public static void main(String[] args) {
        System.out.println("=== MineClawd 模块化架构演示 ===\n");
        
        // 1. 创建模块注册中心
        ModuleRegistry registry = new ModuleRegistry();
        
        // 2. 创建模块发现服务
        ModuleDiscoveryService discoveryService = new ModuleDiscoveryService(
            registry, 
            java.util.List.of("com.mineclawd.modules")
        );
        
        // 3. 扫描并注册模块
        discoveryService.scanAndRegisterModules();
        
        // 4. 演示模块调用
        demonstrateModuleUsage(registry);
        
        // 5. 演示模块搜索和统计
        demonstrateModuleSearch(registry);
        
        // 6. 演示LLM和MCP描述
        demonstrateLLMAndMCP(registry);
        
        // 7. 演示热加载
        demonstrateHotLoading(registry, discoveryService);
        
        System.out.println("\n=== 演示完成 ===");
    }
    
    private static void demonstrateModuleUsage(ModuleRegistry registry) {
        System.out.println("\n1. 模块调用演示:");
        
        // 演示文件读取模块
        demonstrateFileReadModule(registry);
        
        // 演示KubeJSOffline模块
        demonstrateKubeJSOfflineModule(registry);
    }
    
    private static void demonstrateFileReadModule(ModuleRegistry registry) {
        System.out.println("\n1.1 文件读取模块演示:");
        
        BaseModule fileReadModule = registry.getModule("tool-file-read");
        if (fileReadModule instanceof ExecutableModule) {
            ExecutableModule executableModule = (ExecutableModule) fileReadModule;
            
            System.out.println("模块信息: " + executableModule.getModuleName() + " (" + executableModule.getModuleId() + ")");
            System.out.println("模块描述: " + executableModule.getModuleDescription());
            
            // 执行模块操作
            ExecutionRequest request = ExecutionRequest.builder()
                .moduleId(executableModule.getModuleId())
                .operation("read_file")
                .parameters(Map.of("file_path", "/test/file.txt", "encoding", "UTF-8"))
                .build();
            
            ExecutionResult result = executableModule.execute(request);
            System.out.println("执行结果: " + result.isSuccess());
            System.out.println("执行消息: " + result.getMessage());
            System.out.println("执行数据: " + result.getData());
        }
    }
    
    private static void demonstrateKubeJSOfflineModule(ModuleRegistry registry) {
        System.out.println("\n1.2 KubeJSOffline模块演示:");
        
        BaseModule kubejsModule = registry.getModule("kubejs-offline");
        if (kubejsModule instanceof ExecutableModule) {
            ExecutableModule executableModule = (ExecutableModule) kubejsModule;
            
            System.out.println("模块信息: " + executableModule.getModuleName() + " (" + executableModule.getModuleId() + ")");
            System.out.println("模块描述: " + executableModule.getModuleDescription());
            
            // 演示检查可用性
            ExecutionRequest checkRequest = ExecutionRequest.builder()
                .moduleId(executableModule.getModuleId())
                .operation("check_availability")
                .parameters(Map.of())
                .build();
            
            ExecutionResult checkResult = executableModule.execute(checkRequest);
            System.out.println("检查可用性结果: " + checkResult.isSuccess());
            System.out.println("检查消息: " + checkResult.getMessage());
            System.out.println("检查数据: " + checkResult.getData());
            
            // 演示查询类信息
            ExecutionRequest queryRequest = ExecutionRequest.builder()
                .moduleId(executableModule.getModuleId())
                .operation("query_class")
                .parameters(Map.of("class_name", "ItemEvents"))
                .build();
            
            ExecutionResult queryResult = executableModule.execute(queryRequest);
            System.out.println("查询类结果: " + queryResult.isSuccess());
            System.out.println("查询消息: " + queryResult.getMessage());
            System.out.println("查询数据: " + queryResult.getData());
        }
    }
    
    private static void demonstrateModuleSearch(ModuleRegistry registry) {
        System.out.println("\n2. 模块搜索和统计演示:");
        
        // 搜索模块
        java.util.List<BaseModule> searchResults = registry.searchModules("file");
        System.out.println("搜索'file'的结果: " + searchResults.size() + " 个模块");
        
        for (BaseModule module : searchResults) {
            System.out.println("  - " + module.getModuleName() + " (" + module.getModuleId() + ")");
        }
        
        // 获取统计信息
        ModuleRegistry.ModuleStatistics stats = registry.getStatistics();
        System.out.println("\n模块统计信息:");
        System.out.println("总模块数: " + stats.getTotalModules());
        System.out.println("按类型统计: " + stats.getModulesByType());
    }
    
    private static void demonstrateLLMAndMCP(ModuleRegistry registry) {
        System.out.println("\n3. LLM和MCP描述演示:");
        
        // 获取文件读取模块
        BaseModule fileReadModule = registry.getModule("tool-file-read");
        if (fileReadModule != null) {
            System.out.println("模块: " + fileReadModule.getModuleName());
            System.out.println("LLM描述: " + fileReadModule.getLLMDescription().getDescription());
            System.out.println("LLM函数名: " + fileReadModule.getLLMDescription().getFunctionName());
            System.out.println("LLM参数: " + fileReadModule.getLLMDescription().getParameters());
            
            System.out.println("MCP工具名: " + fileReadModule.getMCPDescription().getToolName());
            System.out.println("MCP描述: " + fileReadModule.getMCPDescription().getDescription());
            System.out.println("MCP输入Schema: " + fileReadModule.getMCPDescription().getInputSchema());
            System.out.println("MCP输出Schema: " + fileReadModule.getMCPDescription().getOutputSchema());
        }
    }
    
    private static void demonstrateHotLoading(ModuleRegistry registry, ModuleDiscoveryService discoveryService) {
        System.out.println("\n4. 热加载演示:");
        
        // 创建一个简单的演示模块
        BaseModule demoModule = new BaseModule() {
            @Override
            public String getModuleId() { return "demo-test-module"; }
            @Override
            public String getModuleName() { return "演示模块"; }
            @Override
            public ModuleType getModuleType() { return ModuleType.UNKNOWN; }
            @Override
            public String getModuleDescription() { return "这是一个演示模块"; }
            @Override
            public LLMDescription getLLMDescription() { 
                return LLMDescription.builder()
                    .functionName("demo_module")
                    .description("演示模块功能")
                    .build();
            }
            @Override
            public MCPDescription getMCPDescription() { 
                return MCPDescription.builder()
                    .toolName("demo_module")
                    .description("演示模块工具")
                    .build();
            }
        };
        
        // 热加载模块
        discoveryService.hotLoadModule(demoModule);
        
        // 验证模块已加载
        BaseModule loadedModule = registry.getModule("demo-test-module");
        if (loadedModule != null) {
            System.out.println("热加载成功: " + loadedModule.getModuleName());
        }
        
        // 热卸载模块
        discoveryService.hotUnloadModule("demo-test-module");
        
        // 验证模块已卸载
        BaseModule unloadedModule = registry.getModule("demo-test-module");
        if (unloadedModule == null) {
            System.out.println("热卸载成功: demo-test-module");
        }
    }
}