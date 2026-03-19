package com.mineclawd.modules.api;

import com.mineclawd.modules.core.*;
import com.mineclawd.modules.registry.ModuleRegistry;
import java.util.*;

/**
 * UI模块调用接口
 * 
 * <p>为UI提供友好的模块调用接口</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class UIModuleAPI {
    
    private final ModuleRegistry moduleRegistry;
    
    public UIModuleAPI(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }
    
    /**
     * 获取所有模块的UI配置信息
     */
    public List<UIModuleInfo> getAllUIModuleInfo() {
        List<UIModuleInfo> uiInfos = new ArrayList<>();
        
        for (BaseModule module : moduleRegistry.getAllModules()) {
            uiInfos.add(createUIModuleInfo(module));
        }
        
        return uiInfos;
    }
    
    /**
     * 按类型获取模块的UI配置信息
     */
    public List<UIModuleInfo> getUIModuleInfoByType(ModuleType type) {
        List<UIModuleInfo> uiInfos = new ArrayList<>();
        
        for (BaseModule module : moduleRegistry.getModulesByType(type)) {
            uiInfos.add(createUIModuleInfo(module));
        }
        
        return uiInfos;
    }
    
    /**
     * 搜索模块的UI配置信息
     */
    public List<UIModuleInfo> searchUIModuleInfo(String keyword) {
        List<UIModuleInfo> uiInfos = new ArrayList<>();
        
        for (BaseModule module : moduleRegistry.searchModules(keyword)) {
            uiInfos.add(createUIModuleInfo(module));
        }
        
        return uiInfos;
    }
    
    /**
     * 获取单个模块的UI配置信息
     */
    public UIModuleInfo getUIModuleInfo(String moduleId) {
        BaseModule module = moduleRegistry.getModule(moduleId);
        if (module == null) {
            return null;
        }
        
        return createUIModuleInfo(module);
    }
    
    /**
     * 执行模块操作（UI友好格式）
     */
    public UIExecutionResult executeModuleOperation(UIExecutionRequest request) {
        try {
            BaseModule module = moduleRegistry.getModule(request.getModuleId());
            if (module == null) {
                return UIExecutionResult.failure("模块不存在: " + request.getModuleId());
            }
            
            if (!(module instanceof ExecutableModule)) {
                return UIExecutionResult.failure("模块不支持执行操作: " + request.getModuleId());
            }
            
            ExecutableModule executableModule = (ExecutableModule) module;
            
            // 转换UI请求为执行请求
            ExecutionRequest executionRequest = ExecutionRequest.builder()
                .moduleId(request.getModuleId())
                .operation(request.getOperation())
                .parameters(request.getParameters())
                .context(request.getContext())
                .build();
            
            // 执行模块
            ExecutionResult executionResult = executableModule.execute(executionRequest);
            
            // 转换执行结果为UI结果
            return UIExecutionResult.fromExecutionResult(executionResult);
            
        } catch (Exception e) {
            return UIExecutionResult.failure("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模块的可视化配置
     */
    public UIModuleConfiguration getUIModuleConfiguration(String moduleId) {
        BaseModule module = moduleRegistry.getModule(moduleId);
        if (module == null) {
            return null;
        }
        
        return UIModuleConfiguration.builder()
            .moduleId(module.getModuleId())
            .moduleName(module.getModuleName())
            .moduleType(module.getModuleType())
            .description(module.getModuleDescription())
            .llmDescription(module.getLLMDescription())
            .mcpDescription(module.getMCPDescription())
            .build();
    }
    
    /**
     * 更新模块配置
     */
    public UpdateResult updateModuleConfiguration(String moduleId, Map<String, Object> config) {
        // 在实际实现中，这里会更新模块的配置
        // 目前返回成功结果
        return UpdateResult.success("配置更新成功");
    }
    
    /**
     * 创建UI模块信息
     */
    private UIModuleInfo createUIModuleInfo(BaseModule module) {
        return UIModuleInfo.builder()
            .moduleId(module.getModuleId())
            .moduleName(module.getModuleName())
            .moduleType(module.getModuleType())
            .description(module.getModuleDescription())
            .isExecutable(module instanceof ExecutableModule)
            .build();
    }
}