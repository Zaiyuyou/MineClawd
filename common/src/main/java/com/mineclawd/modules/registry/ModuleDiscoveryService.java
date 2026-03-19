package com.mineclawd.modules.registry;

import com.mineclawd.modules.core.*;
import com.mineclawd.modules.tool.FileReadToolModule;
import com.mineclawd.modules.kubejs.KubeJSOfflineModule;
import java.util.*;

/**
 * 模块发现服务
 * 
 * <p>负责自动扫描和加载模块</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ModuleDiscoveryService {
    
    private final ModuleRegistry registry;
    private final List<String> scanPackages;
    
    public ModuleDiscoveryService(ModuleRegistry registry, List<String> scanPackages) {
        this.registry = registry;
        this.scanPackages = scanPackages;
    }
    
    /**
     * 扫描并注册模块
     */
    public void scanAndRegisterModules() {
        // 在实际实现中，这里会使用反射机制扫描指定包下的模块类
        // 为了演示，我们手动注册一些示例模块
        
        // 注册文件读取工具模块
        FileReadToolModule fileReadModule = new FileReadToolModule();
        registry.registerModule(fileReadModule);
        
        // 注册KubeJSOffline模块
        try {
            KubeJSOfflineModule kubejsModule = new KubeJSOfflineModule();
            registry.registerModule(kubejsModule);
            System.out.println("KubeJSOffline模块注册成功");
        } catch (Exception e) {
            System.err.println("KubeJSOffline模块注册失败: " + e.getMessage());
        }
        
        System.out.println("模块扫描完成，已注册模块: " + registry.getStatistics().getTotalModules());
    }
    
    /**
     * 热加载模块
     */
    public void hotLoadModule(BaseModule module) {
        try {
            registry.registerModule(module);
            System.out.println("热加载模块成功: " + module.getModuleId());
        } catch (Exception e) {
            System.err.println("热加载模块失败: " + e.getMessage());
        }
    }
    
    /**
     * 热卸载模块
     */
    public void hotUnloadModule(String moduleId) {
        try {
            registry.unregisterModule(moduleId);
            System.out.println("热卸载模块成功: " + moduleId);
        } catch (Exception e) {
            System.err.println("热卸载模块失败: " + e.getMessage());
        }
    }
}