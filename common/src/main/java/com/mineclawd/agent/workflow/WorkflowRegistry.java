package com.mineclawd.agent.workflow;

import com.mineclawd.agent.interfaces.InterfaceRegistry;
import com.mineclawd.agent.interfaces.WorkflowInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流注册表
 * 统一管理所有工作流组件的注册和获取
 * 
 * <p>这是工作流系统的核心类，负责：</p>
 * <ul>
 *   <li>注册工作流</li>
 *   <li>获取工作流</li>
 *   <li>管理工作流提供者</li>
 *   <li>加载工作流</li>
 * </ul>
 * 
 * <p>使用单例模式，确保全局只有一个实例</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public final class WorkflowRegistry {
    
    // ==================== 组件存储 ====================
    
    /**
     * 工作流存储表
     * 存储所有注册的工作流组件
     */
    private static final ConcurrentHashMap<String, WorkflowInterface> WORKFLOWS = new ConcurrentHashMap<>();
    
    /**
     * 工作流提供者列表
     * 存储所有注册的工作流提供者
     */
    private static final List<WorkflowProvider> PROVIDERS = new ArrayList<>();
    
    /**
     * 单例实例
     */
    private static final WorkflowRegistry INSTANCE = new WorkflowRegistry();
    
    // ==================== 构造函数 ====================
    
    /**
     * 私有构造函数
     * 
     * <p>防止外部实例化，确保单例</p>
     */
    private WorkflowRegistry() {
        registerBuiltinProviders();
    }
    
    // ==================== 单例方法 ====================
    
    /**
     * 获取单例实例
     * 
     * <p>全局唯一访问点</p>
     * 
     * @return WorkflowRegistry实例
     */
    public static WorkflowRegistry getInstance() {
        return INSTANCE;
    }
    
    // ==================== 内置提供者 ====================
    
    /**
     * 注册内置工作流提供者
     * 
     * <p>在初始化时注册所有内置的工作流提供者</p>
     */
    private void registerBuiltinProviders() {
        // TODO: 注册内置工作流提供者
        // 例如：PROVIDERS.add(new BuiltinWorkflowProvider());
        // 具体实现后续填充
    }
    
    /**
     * 从提供者加载工作流
     * 
     * <p>遍历所有工作流提供者，加载它们提供的工作流</p>
     */
    private void loadWorkflowsFromProviders() {
        // TODO: 实现从提供者加载工作流逻辑
        // 遍历所有工作流提供者，调用它们的getWorkflows()方法
        // 然后将工作流注册到WORKFLOWS表中
        // 具体实现后续填充
    }
    
    // ==================== 注册方法 ====================
    
    /**
     * 注册工作流
     * 
     * <p>将工作流注册到注册表中</p>
     * 
     * @param workflow 工作流定义
     */
    public static void register(WorkflowDefinition workflow) {
        // TODO: 实现注册工作流逻辑
        // 1. 将工作流添加到WORKFLOWS表
        // 2. 调用InterfaceRegistry.registerWorkflow(workflow)
        // 具体实现后续填充
    }
    
    // ==================== 获取方法 ====================
    
    /**
     * 获取工作流
     * 
     * <p>根据工作流名称从注册表中获取工作流</p>
     * 
     * @param name 工作流名称
     * @return 工作流接口，如果不存在则返回null
     */
    public static WorkflowInterface getWorkflow(String name) {
        // TODO: 实现获取工作流逻辑
        // 从WORKFLOWS表中获取工作流
        // 具体实现后续填充
        return null;
    }
    
    /**
     * 获取所有工作流
     * 
     * <p>返回注册表中所有的工具组件列表</p>
     * 
     * @return 工作流组件列表
     */
    public static List<WorkflowInterface> getAllWorkflows() {
        return new ArrayList<>(WORKFLOWS.values());
    }
}