package com.mineclawd.agent.interfaces;

import com.google.gson.JsonObject;

/**
 * 工具接口
 * 定义了工具组件的统一接口
 * 
 * <p>工具是可执行的操作，包含名称、描述、参数定义等信息</p>
 * 
 * <p>工具接口继承了三个基础接口：</p>
 * <ul>
 *   <li>Executable: 可执行</li>
 *   <li>WithContext: 有上下文</li>
 *   <li>WithSpecificInfo: 有具体信息</li>
 * </ul>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public interface ToolInterface extends Executable, WithContext, WithSpecificInfo {
    
    /**
     * 获取工具的参数定义
     * 
     * <p>参数定义是一个JSON对象，描述了工具接受的参数格式</p>
     * 
     * @return 工具的参数定义（JSON对象）
     */
    JsonObject getParameters();
}
