package com.mineclawd.modules.router;

import com.mineclawd.modules.core.*;
import java.util.Map;

/**
 * 模块Schema执行器
 * 
 * <p>将schema执行委托给具体的模块</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class ModuleSchemaExecutor implements SchemaExecutor {
    
    private final ExecutableModule module;
    
    public ModuleSchemaExecutor(ExecutableModule module) {
        this.module = module;
    }
    
    @Override
    public ExecutionResult executeSchema(SchemaData schema, ExecutionContext context) {
        try {
            // 创建执行请求
            ExecutionRequest request = ExecutionRequest.builder()
                .moduleId(module.getModuleId())
                .parameters(schema.getParameters())
                .context(context.getContext())
                .build();
            
            // 执行模块
            return module.execute(request);
            
        } catch (Exception e) {
            return ExecutionResult.failure("schema_execution_error", 
                "Schema执行失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getSupportedSchemaType() {
        return module.getModuleId();
    }
}