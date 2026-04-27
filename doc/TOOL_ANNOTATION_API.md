# MineClawd 注解扩展 API 文档

## 一、注解扩展原理

### 核心思想

使用 Java 注解机制，让 Mod 开发者通过声明式编程来定义工具：

```java
public class MyModTools {
    @MineClawdTool(
        name = "my_mod:craft_item",
        description = "Craft an item using a recipe"
    )
    public static ToolExecutionResult craftItem(
        @ToolParam(name = "recipe_id", required = true) String recipeId,
        @ToolParam(name = "count", required = false) Integer count
    ) {
        // 执行逻辑
    }
}
```

### 工作流程

```
┌─────────────────────────────────────────────────────────────┐
│ 阶段 1: 扫描 (Mod 初始化时)                                   │
│ - 扫描所有 Mod 的类                                          │
│ - 查找带有 @MineClawdTool 注解的方法                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 2: 解析 (反射分析)                                       │
│ - 读取注解的 name, description                               │
│ - 解析方法参数上的 @ToolParam                                │
│ - 生成 JSON Schema 参数定义                                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 3: 生成 (动态包装)                                       │
│ - 为每个注解方法生成 Tool 实现                               │
│ - 创建参数解析器                                             │
│ - 创建执行包装器                                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 阶段 4: 注册 (自动注册)                                       │
│ - 调用 ToolRegistry.register()                               │
│ - 存储到全局注册表                                           │
└─────────────────────────────────────────────────────────────┘
```

## 二、核心注解定义

### 2.1 @MineClawdTool

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MineClawdTool {
    String name();                           // 必需: 工具唯一标识
    String description() default "";         // 可选: 给 LLM 的描述
    ToolCategory category() default CUSTOM;  // 可选: 工具分类
    int requiredPermission() default 0;      // 可选: 所需权限
    String[] requires() default {};          // 可选: 依赖的 Mod
}
```

### 2.2 @ToolParam

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParam {
    String name();              // 必需: 参数名称
    boolean required() default true;  // 可选: 是否必需
    ParamType type() default AUTO;    // 可选: 参数类型
    String description() default "";  // 可选: 参数描述
    String defaultValue() default ""; // 可选: 默认值
    ParamRange range() default @ParamRange(); // 可选: 范围限制
}
```

### 2.3 辅助枚举和注解

```java
enum ToolCategory {
    INFORMATION,        // 信息查询
    EXECUTION,          // 执行操作
    FILE_MANAGEMENT,    // 文件管理
    GAME_CONTROL,       // 游戏控制
    CUSTOM              // 自定义
}

enum ParamType {
    AUTO,       // 自动推断
    STRING,     // 字符串
    INTEGER,    // 整数
    DOUBLE,     // 浮点数
    BOOLEAN,    // 布尔值
    JSON        // JSON 对象
}

@interface ParamRange {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
    double minDouble() default Double.MIN_VALUE;
    double maxDouble() default Double.MAX_VALUE;
}
```

## 三、注解处理器实现

### 3.1 核心处理器

```java
public class AnnotationToolProcessor {
    
    /**
     * 扫描并注册所有注解工具
     */
    public static void scanAndRegister(Class<?> modClass) {
        Method[] methods = modClass.getDeclaredMethods();
        for (Method method : methods) {
            MineClawdTool annotation = method.getAnnotation(MineClawdTool.class);
            if (annotation != null) {
                MineClawdTool tool = processMethod(modClass, method, annotation);
                if (tool != null) {
                    ToolRegistry.register(tool);
                }
            }
        }
    }
    
    /**
     * 处理单个方法
     */
    private static MineClawdTool processMethod(
        Class<?> modClass, 
        Method method, 
        MineClawdTool annotation
    ) {
        // 1. 检查依赖
        if (!checkDependencies(annotation.requires())) {
            return null;
        }
        
        // 2. 解析参数
        List<ToolParameter> parameters = parseParameters(method);
        
        // 3. 生成 JSON Schema
        JsonObject parametersSchema = generateJsonSchema(parameters);
        
        // 4. 创建描述
        String description = annotation.description();
        if (description.isEmpty()) {
            description = generateDescriptionFromMethod(method, parameters);
        }
        
        // 5. 创建工具实例
        return new AnnotationBasedTool(
            annotation.name(),
            description,
            parametersSchema,
            modClass,
            method,
            parameters
        );
    }
    
    /**
     * 生成 JSON Schema
     */
    private static JsonObject generateJsonSchema(List<ToolParameter> parameters) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonArray required = new JsonArray();
        for (ToolParameter param : parameters) {
            if (param.isRequired()) {
                required.add(param.getName());
            }
        }
        if (!required.isEmpty()) {
            schema.add("required", required);
        }
        
        JsonObject properties = new JsonObject();
        for (ToolParameter param : parameters) {
            properties.add(param.getName(), generateParamSchema(param));
        }
        schema.add("properties", properties);
        
        return schema;
    }
    
    /**
     * 生成单个参数的 Schema
     */
    private static JsonObject generateParamSchema(ToolParameter param) {
        JsonObject paramSchema = new JsonObject();
        
        ParamType type = param.getType();
        if (type == ParamType.AUTO) {
            type = inferTypeFromName(param.getName());
        }
        
        switch (type) {
            case STRING:
                paramSchema.addProperty("type", "string");
                break;
            case INTEGER:
                paramSchema.addProperty("type", "integer");
                break;
            case DOUBLE:
                paramSchema.addProperty("type", "number");
                break;
            case BOOLEAN:
                paramSchema.addProperty("type", "boolean");
                break;
        }
        
        if (param.getDescription() != null) {
            paramSchema.addProperty("description", param.getDescription());
        }
        
        if (param.getDefaultValue() != null) {
            paramSchema.addProperty("default", param.getDefaultValue());
        }
        
        // 范围限制
        ParamRange range = param.getRange();
        if (range != null) {
            if (type == ParamType.INTEGER) {
                if (range.min() != Integer.MIN_VALUE) {
                    paramSchema.addProperty("minimum", range.min());
                }
                if (range.max() != Integer.MAX_VALUE) {
                    paramSchema.addProperty("maximum", range.max());
                }
            }
        }
        
        return paramSchema;
    }
    
    /**
     * 从参数名推断类型
     */
    private static ParamType inferTypeFromName(String paramName) {
        String lower = paramName.toLowerCase();
        if (lower.contains("id") || lower.contains("name") || 
            lower.contains("path") || lower.contains("file")) {
            return ParamType.STRING;
        } else if (lower.contains("count") || lower.contains("number")) {
            return ParamType.INTEGER;
        } else if (lower.contains("enable") || lower.contains("flag")) {
            return ParamType.BOOLEAN;
        }
        return ParamType.STRING;
    }
}
```

### 3.2 工具执行包装

```java
private static class AnnotationBasedTool implements MineClawdTool {
    private final String name;
    private final String description;
    private final JsonObject parameters;
    private final Class<?> modClass;
    private final Method method;
    private final List<ToolParameter> parametersDef;
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        try {
            // 1. 准备方法参数
            Object[] methodArgs = prepareMethodArguments(args);
            
            // 2. 调用方法
            method.setAccessible(true);
            Object result = method.invoke(modClass, methodArgs);
            
            // 3. 处理返回值
            if (result instanceof ToolExecutionResult) {
                return (ToolExecutionResult) result;
            } else if (result instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) result;
                boolean success = Boolean.TRUE.equals(map.get("success"));
                String output = String.valueOf(map.get("output"));
                return new ToolExecutionResult(success, output);
            } else if (result instanceof String) {
                return ToolExecutionResult.success((String) result);
            } else {
                return ToolExecutionResult.success(String.valueOf(result));
            }
            
        } catch (Exception e) {
            return ToolExecutionResult.failure("Tool execution failed: " + e.getMessage());
        }
    }
    
    /**
     * 准备方法参数
     */
    private Object[] prepareMethodArguments(JsonObject args) {
        Object[] methodArgs = new Object[parametersDef.size()];
        
        for (int i = 0; i < parametersDef.size(); i++) {
            ToolParameter param = parametersDef.get(i);
            
            if (args.has(param.getName())) {
                JsonElement element = args.get(param.getName());
                methodArgs[i] = convertValue(element, param.getType());
            } else if (!param.isRequired()) {
                methodArgs[i] = convertValue(
                    new JsonPrimitive(param.getDefaultValue()), 
                    param.getType()
                );
            } else {
                throw new IllegalArgumentException(
                    "Missing required parameter: " + param.getName()
                );
            }
        }
        
        return methodArgs;
    }
    
    /**
     * 转换值类型
     */
    private Object convertValue(JsonElement element, ParamType type) {
        switch (type) {
            case STRING:
                return element.getAsString();
            case INTEGER:
                return element.getAsInt();
            case DOUBLE:
                return element.getAsDouble();
            case BOOLEAN:
                return element.getAsBoolean();
            default:
                return element;
        }
    }
}
```

## 四、使用示例

### 4.1 基础工具

```java
public class MyModTools {
    
    @MineClawdTool(
        name = "my_mod:craft_item",
        description = "Craft an item using a recipe",
        category = ToolCategory.EXECUTION
    )
    public static ToolExecutionResult craftItem(
        @ToolParam(
            name = "recipe_id",
            required = true,
            description = "The recipe identifier"
        ) String recipeId,
        
        @ToolParam(
            name = "count",
            required = false,
            description = "Number of items to craft",
            defaultValue = "1"
        ) int count
    ) {
        try {
            if (count < 1 || count > 64) {
                return ToolExecutionResult.failure("count must be 1-64");
            }
            
            String result = "Crafted " + count + " of " + recipeId;
            return ToolExecutionResult.success(result);
            
        } catch (Exception e) {
            return ToolExecutionResult.failure("Failed: " + e.getMessage());
        }
    }
}
```

### 4.2 带依赖的工具

```java
@MineClawdTool(
    name = "my_mod:advanced_tool",
    requires = {"some_mod", "another_mod"}
)
public static ToolExecutionResult advancedTool(
    @ToolParam(name = "config") String config
) {
    // 只有当依赖的 Mod 都加载时才会注册
    return ToolExecutionResult.success("Executed: " + config);
}
```

### 4.3 自动类型推断

```java
@MineClawdTool(
    name = "my_mod:check_status"
)
public static ToolExecutionResult checkStatus(
    @ToolParam(name = "level") String level,      // 自动推断为 STRING
    @ToolParam(name = "verbose") boolean verbose, // 自动推断为 BOOLEAN
    @ToolParam(name = "count") int count          // 自动推断为 INTEGER
) {
    return ToolExecutionResult.success("Status: " + level);
}
```

### 4.4 范围限制

```java
@MineClawdTool(
    name = "my_mod:set_value"
)
public static ToolExecutionResult setValue(
    @ToolParam(
        name = "value",
        range = @ParamRange(min = 1, max = 100)
    ) int value
) {
    return ToolExecutionResult.success("Value: " + value);
}
```

## 五、注册工具

### 5.1 手动注册

```java
public class MyMod implements ModInitializer {
    @Override
    public void onInitialize() {
        AnnotationToolProcessor.scanAndRegister(MyModTools.class);
    }
}
```

### 5.2 自动扫描

```java
public class ToolAnnotationScanner {
    public static void scanAllMods(String... basePackages) {
        for (String basePackage : basePackages) {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(
                MineClawdTool.class
            );
            
            for (Class<?> clazz : classes) {
                AnnotationToolProcessor.scanAndRegister(clazz);
            }
        }
    }
}

// 使用
ToolAnnotationScanner.scanAllMods("com.mymod", "com.othermod");
```

## 六、完整示例

### 6.1 工具类

```java
package com.mymod.tools;

import com.mineclawd.foundation.tool.annotation.*;
import com.mineclawd.foundation.tool.ToolExecutionResult;

public class MyModTools {
    
    /**
     * 合成物品
     */
    @MineClawdTool(
        name = "my_mod:craft_item",
        description = "Craft an item using a recipe. " +
                     "Use this when the player wants to craft something.",
        category = ToolCategory.EXECUTION
    )
    public static ToolExecutionResult craftItem(
        @ToolParam(
            name = "recipe_id",
            required = true,
            description = "The recipe identifier (e.g., 'minecraft:diamond_sword')"
        ) String recipeId,
        
        @ToolParam(
            name = "count",
            required = false,
            description = "Number of items to craft (1-64)",
            defaultValue = "1",
            range = @ParamRange(min = 1, max = 64)
        ) int count
    ) {
        try {
            if (recipeId == null || recipeId.isBlank()) {
                return ToolExecutionResult.failure("recipe_id is required");
            }
            
            String result = "Successfully crafted " + count + " of " + recipeId;
            return ToolExecutionResult.success(result);
            
        } catch (Exception e) {
            return ToolExecutionResult.failure("Failed: " + e.getMessage());
        }
    }
    
    /**
     * 检查状态
     */
    @MineClawdTool(
        name = "my_mod:check_status",
        description = "Check the current system status",
        category = ToolCategory.INFORMATION
    )
    public static ToolExecutionResult checkStatus(
        @ToolParam(
            name = "level",
            required = false,
            defaultValue = "normal",
            description = "Status level: 'normal' or 'detailed'"
        ) String level
    ) {
        String status;
        if ("detailed".equals(level)) {
            status = "System status: Detailed info...\n" +
                    "- CPU: 45%\n" +
                    "- Memory: 60%\n" +
                    "- Disk: 30%";
        } else {
            status = "System status: OK";
        }
        return ToolExecutionResult.success(status);
    }
    
    /**
     * 带依赖的工具
     */
    @MineClawdTool(
        name = "my_mod:advanced_feature",
        requires = {"some_mod"}
    )
    public static ToolExecutionResult advancedFeature(
        @ToolParam(name = "config") String config
    ) {
        return ToolExecutionResult.success("Advanced: " + config);
    }
}
```

### 6.2 Mod 初始化

```java
package com.mymod;

import com.mineclawd.foundation.tool.annotation.AnnotationToolProcessor;
import net.fabricmc.api.ModInitializer;

public class MyMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        // 扫描并注册所有注解工具
        AnnotationToolProcessor.scanAndRegister(MyModTools.class);
        
        MineClawd.LOGGER.info("MyMod tools registered via annotations");
    }
}
```

## 七、对比：注解 vs 接口

### 7.1 接口方式

```java
public class CraftItemTool implements MineClawdTool {
    @Override
    public String getName() { return "my_mod:craft_item"; }
    
    @Override
    public String getDescription() { return "..."; }
    
    @Override
    public JsonObject getParameters() { return ...; }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        // 实现逻辑
    }
}
```

### 7.2 注解方式

```java
public class MyModTools {
    @MineClawdTool(name = "my_mod:craft_item", description = "...")
    public static ToolExecutionResult craftItem(
        @ToolParam(name = "recipe_id", required = true) String recipeId
    ) {
        // 实现逻辑
    }
}
```

### 7.3 对比总结

| 特性 | 接口方式 | 注解方式 |
|------|---------|---------|
| 代码量 | 多 | 少 |
| 声明性 | 低 | 高 |
| 灵活性 | 高 | 中 |
| 类型安全 | 高 | 高 |
| IDE 支持 | 好 | 好 |
| 学习曲线 | 平缓 | 平缓 |

## 八、最佳实践

### 8.1 命名规范

```java
// ✅ 好的命名
@MineClawdTool(name = "my_mod:craft_item")
@MineClawdTool(name = "my_mod:check_status")
@MineClawdTool(name = "my_mod:get_info")

// ❌ 不好的命名
@MineClawdTool(name = "tool1")
@MineClawdTool(name = "do_something")
```

### 8.2 描述规范

```java
// ✅ 好的描述
@MineClawdTool(
    name = "my_mod:craft_item",
    description = "Craft an item using a recipe. " +
                 "Use this when the player wants to craft something. " +
                 "Requires: recipe_id (string), count (optional integer)"
)

// ❌ 不好的描述
@MineClawdTool(
    name = "my_mod:craft_item",
    description = "Crafting tool"
)
```

### 8.3 参数规范

```java
// ✅ 好的参数定义
@ToolParam(
    name = "count",
    required = false,
    defaultValue = "1",
    description = "Number of items to craft (1-64)",
    range = @ParamRange(min = 1, max = 64)
)

// ❌ 不好的参数定义
@ToolParam(name = "count")
```

## 九、工具 Prompt Appendix 支持

### 9.1 注解支持 Prompt Appendix

```java
@MineClawdTool(
    name = "my_mod:craft_item",
    description = "Craft an item using a recipe",
    promptAppendix = "### Crafting Tools\n\nUse these tools for crafting items."
)
public static ToolExecutionResult craftItem(
    @ToolParam(name = "recipe_id", required = true) String recipeId
) {
    // ...
}
```

### 9.2 Agent 级别 Prompt 配置

```java
// 在 Agent 的 base.md 中配置
// mineclawd/agents/default/base.md

You are MineClawd, an advanced Minecraft in-game agent.

### Enabled Prompt Categories
- dynamic_registry
- asset_tracking
- custom_tools

### Disabled Prompt Categories
- debug_info
```

### 9.3 注解处理器增强

```java
public class AnnotationToolProcessor {
    
    private static MineClawdTool processMethod(
        Class<?> modClass, 
        Method method, 
        MineClawdTool annotation
    ) {
        // ... 现有逻辑 ...
        
        // 处理 Prompt Appendix
        String appendix = annotation.promptAppendix();
        
        // 创建工具实例
        return new AnnotationBasedTool(
            annotation.name(),
            description,
            parametersSchema,
            modClass,
            method,
            parameters,
            appendix  // 传递 Prompt Appendix
        );
    }
}
```

## 十、总结

注解扩展的优势：

1. **简洁性** - 一个注解搞定注册
2. **声明式** - 代码更易读
3. **类型安全** - 编译时检查
4. **灵活性** - 支持默认值、依赖检查
5. **易用性** - 开发者友好
6. **Prompt Appendix** - 支持工具特定的 LLM 上下文

推荐使用场景：

- ✅ 简单的工具函数
- ✅ 参数较少的工具
- ✅ 快速原型开发
- ✅ 代码量敏感的项目
- ✅ 需要自定义 LLM 上下文的工具

不推荐场景：

- ❌ 需要复杂执行逻辑的工具
- ❌ 需要异步执行的工具
- ❌ 需要精细控制的工具
