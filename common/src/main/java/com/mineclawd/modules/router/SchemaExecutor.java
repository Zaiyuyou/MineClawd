package com.mineclawd.modules.router;

import com.mineclawd.modules.core.ExecutionResult;

/**
 * Schema执行器接口
 * 
 * <p>负责执行特定类型的schema</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public interface SchemaExecutor {
    
    /**
     * 执行schema
     * 
     * @param schema schema数据
     * @param context 执行上下文
     * @return 执行结果
     */
    ExecutionResult executeSchema(SchemaData schema, ExecutionContext context);
    
    /**
     * 获取支持的schema类型
     * 
     * @return schema类型
     */
    String getSupportedSchemaType();
    
    /**
     * 验证schema是否可执行
     * 
     * @param schema schema数据
     * @return 验证结果
     */
    default boolean canExecute(SchemaData schema) {
        return getSupportedSchemaType().equals(schema.getSchemaType());
    }
}