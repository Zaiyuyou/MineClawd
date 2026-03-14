package com.mineclawd.tool_sys.plugin.annotation;

import java.lang.annotation.*;

/**
 * 工具插件注解
 * 标记一个类为MineClawd工具插件
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolPlugin {
    
    /**
     * 插件ID，必须唯一
     */
    String id();
    
    /**
     * 插件名称
     */
    String name();
    
    /**
     * 插件版本
     */
    String version() default "1.0.0";
    
    /**
     * 插件描述
     */
    String description() default "";
    
    /**
     * 依赖的插件ID列表
     */
    String[] dependencies() default {};
    
    /**
     * 插件作者
     */
    String author() default "";
}