package com.mineclawd.modules.router;

import com.mineclawd.modules.core.*;
import com.mineclawd.modules.registry.ModuleRegistry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema路由器
 * 
 * <p>负责将LLM返回的schema路由到对应的执行器</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class SchemaRouter {
    
    private final ModuleRegistry moduleRegistry;
    private final Map<String, SchemaExecutor> executors = new ConcurrentHashMap<>();
    
    public SchemaRouter(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
        // 自动注册所有可执行模块
        autoRegisterExecutors();
    }
    
    /**
     * 路由schema到对应的执行器
     */
    public RouteResult routeSchema(SchemaData schema, ExecutionContext context) {
        try {
            // 1. 验证schema
            ValidationResult validation = validateSchema(schema);
            if (!validation.isValid()) {
                return RouteResult.failure("Schema验证失败: " + validation.getMessage());
            }
            
            // 2. 查找对应的执行器
            SchemaExecutor executor = findExecutor(schema);
            if (executor == null) {
                return RouteResult.failure("未找到对应的执行器: " + schema.getSchemaType());
            }
            
            // 3. 执行schema
            ExecutionResult result = executor.executeSchema(schema, context);
            
            return RouteResult.success(result);
            
        } catch (Exception e) {
            return RouteResult.failure("路由失败: " + e.getMessage());
        }
    }
    
    /**
     * 注册schema执行器
     */
    public void registerExecutor(String schemaType, SchemaExecutor executor) {
        executors.put(schemaType, executor);
    }
    
    /**
     * 注销schema执行器
     */
    public void unregisterExecutor(String schemaType) {
        executors.remove(schemaType);
    }
    
    /**
     * 获取所有注册的schema类型
     */
    public List<String> getRegisteredSchemaTypes() {
        return new ArrayList<>(executors.keySet());
    }
    
    /**
     * 验证schema是否可路由
     */
    public ValidationResult validateSchema(SchemaData schema) {
        if (schema == null) {
            return ValidationResult.failure("Schema不能为空");
        }
        
        if (schema.getSchemaType() == null || schema.getSchemaType().trim().isEmpty()) {
            return ValidationResult.failure("Schema类型不能为空");
        }
        
        if (!executors.containsKey(schema.getSchemaType())) {
            return ValidationResult.failure("未知的Schema类型: " + schema.getSchemaType());
        }
        
        return ValidationResult.success();
    }
    
    /**
     * 查找对应的执行器
     */
    private SchemaExecutor findExecutor(SchemaData schema) {
        return executors.get(schema.getSchemaType());
    }
    
    /**
     * 自动注册所有可执行模块作为schema执行器
     */
    private void autoRegisterExecutors() {
        for (BaseModule module : moduleRegistry.getAllModules()) {
            if (module instanceof ExecutableModule) {
                // 使用模块ID作为schema类型
                String schemaType = module.getModuleId();
                
                // 创建模块执行器
                ModuleSchemaExecutor executor = new ModuleSchemaExecutor((ExecutableModule) module);
                
                // 注册执行器
                registerExecutor(schemaType, executor);
            }
        }
    }
    
    /**
     * 获取路由统计信息
     */
    public RouterStats getRouterStats() {
        return new RouterStats(
            executors.size(),
            moduleRegistry.getAllModules().size()
        );
    }
    
    /**
     * 路由统计信息
     */
    public static class RouterStats {
        private final int registeredExecutors;
        private final int totalModules;
        
        public RouterStats(int registeredExecutors, int totalModules) {
            this.registeredExecutors = registeredExecutors;
            this.totalModules = totalModules;
        }
        
        public int getRegisteredExecutors() {
            return registeredExecutors;
        }
        
        public int getTotalModules() {
            return totalModules;
        }
        
        public int getUnregisteredModules() {
            return totalModules - registeredExecutors;
        }
    }
}