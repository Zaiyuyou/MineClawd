package com.mineclawd.tool_sys.plugin.annotation;

import java.lang.annotation.*;

/**
 * 工具参数注解
 * 描述工具方法的参数信息
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParameter {
    
    /**
     * 参数名称
     */
    String name();
    
    /**
     * 参数描述
     */
    String description();
    
    /**
     * 参数类型
     */
    ParameterType type() default ParameterType.STRING;
    
    /**
     * 是否必需
     */
    boolean required() default true;
    
    /**
     * 默认值
     */
    String defaultValue() default "";
    
    /**
     * 参数类型枚举
     */
    enum ParameterType {
        STRING,     // 字符串
        INTEGER,    // 整数
        BOOLEAN,    // 布尔值
        NUMBER,     // 数字
        ARRAY,      // 数组
        OBJECT      // 对象
    }
}