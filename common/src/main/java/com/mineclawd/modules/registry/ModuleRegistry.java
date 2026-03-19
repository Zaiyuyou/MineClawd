package com.mineclawd.modules.registry;

import com.mineclawd.modules.core.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 模块注册中心
 * 
 * <p>负责模块的注册、发现和管理</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ModuleRegistry {
    
    private final Map<String, BaseModule> modules = new ConcurrentHashMap<>();
    private final Map<ModuleType, List<BaseModule>> modulesByType = new ConcurrentHashMap<>();
    private final List<ModuleEventListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * 注册模块
     */
    public void registerModule(BaseModule module) {
        String moduleId = module.getModuleId();
        
        if (modules.containsKey(moduleId)) {
            throw new IllegalArgumentException("模块已存在: " + moduleId);
        }
        
        modules.put(moduleId, module);
        modulesByType.computeIfAbsent(module.getModuleType(), k -> new CopyOnWriteArrayList<>())
                    .add(module);
        
        // 通知监听器
        notifyModuleRegistered(module);
    }
    
    /**
     * 注销模块
     */
    public void unregisterModule(String moduleId) {
        BaseModule module = modules.remove(moduleId);
        if (module != null) {
            List<BaseModule> typeModules = modulesByType.get(module.getModuleType());
            if (typeModules != null) {
                typeModules.remove(module);
            }
            
            // 通知监听器
            notifyModuleUnregistered(module);
        }
    }
    
    /**
     * 获取模块
     */
    public BaseModule getModule(String moduleId) {
        return modules.get(moduleId);
    }
    
    /**
     * 获取所有模块
     */
    public List<BaseModule> getAllModules() {
        return new ArrayList<>(modules.values());
    }
    
    /**
     * 按类型获取模块
     */
    public List<BaseModule> getModulesByType(ModuleType type) {
        List<BaseModule> typeModules = modulesByType.get(type);
        return typeModules != null ? new ArrayList<>(typeModules) : new ArrayList<>();
    }
    
    /**
     * 搜索模块
     */
    public List<BaseModule> searchModules(String keyword) {
        List<BaseModule> results = new ArrayList<>();
        for (BaseModule module : modules.values()) {
            if (module.getModuleName().toLowerCase().contains(keyword.toLowerCase()) ||
                module.getModuleDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                module.getModuleId().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(module);
            }
        }
        return results;
    }
    
    /**
     * 获取模块统计信息
     */
    public ModuleStatistics getStatistics() {
        Map<ModuleType, Integer> typeCounts = new HashMap<>();
        for (ModuleType type : modulesByType.keySet()) {
            typeCounts.put(type, modulesByType.get(type).size());
        }
        
        return new ModuleStatistics(
            modules.size(),
            typeCounts,
            System.currentTimeMillis()
        );
    }
    
    /**
     * 添加模块事件监听器
     */
    public void addListener(ModuleEventListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除模块事件监听器
     */
    public void removeListener(ModuleEventListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyModuleRegistered(BaseModule module) {
        for (ModuleEventListener listener : listeners) {
            listener.onModuleRegistered(module);
        }
    }
    
    private void notifyModuleUnregistered(BaseModule module) {
        for (ModuleEventListener listener : listeners) {
            listener.onModuleUnregistered(module);
        }
    }
    
    /**
     * 模块事件监听器接口
     */
    public interface ModuleEventListener {
        void onModuleRegistered(BaseModule module);
        void onModuleUnregistered(BaseModule module);
    }
    
    /**
     * 模块统计信息
     */
    public static class ModuleStatistics {
        private final int totalModules;
        private final Map<ModuleType, Integer> modulesByType;
        private final long timestamp;
        
        public ModuleStatistics(int totalModules, Map<ModuleType, Integer> modulesByType, long timestamp) {
            this.totalModules = totalModules;
            this.modulesByType = modulesByType;
            this.timestamp = timestamp;
        }
        
        public int getTotalModules() {
            return totalModules;
        }
        
        public Map<ModuleType, Integer> getModulesByType() {
            return modulesByType;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}