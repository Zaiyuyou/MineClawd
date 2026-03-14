package com.mineclawd.tool_sys.plugin.annotation;

import java.lang.annotation.*;

/**
 * 工具执行器注解
 * 标记一个方法为工具执行器
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolExecutor {
    
    /**
     * 工具名称，必须唯一
     */
    String name();
    
    /**
     * 工具描述，用于LLM理解工具功能
     */
    String description();
    
    /**
     * 工具分类
     */
    String category() default "general";
    
    /**
     * 参数模式
     */
    ParameterMode parameterMode() default ParameterMode.JSON_OBJECT;
    
    /**
     * 是否启用
     */
    boolean enabled() default true;
    
    /**
     * 参数模式枚举
     */
    enum ParameterMode {
        JSON_OBJECT,    // JSON对象参数
        STRING_ARRAY,   // 字符串数组参数
        KEY_VALUE       // 键值对参数
    }
}