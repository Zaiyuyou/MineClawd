# MineClawd 工具扩展使用示例

## 一、基础工具示例

### 1.1 简单工具

```java
package com.mymod.tools;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import net.minecraft.server.command.ServerCommandSource;

public class SimpleCraftTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:craft_item";
    }
    
    @Override
    public String getDescription() {
        return "Craft an item using a recipe. " +
               "Use this when the player wants to craft something. " +
               "Requires: recipe_id (string), count (optional integer)";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject recipeId = new JsonObject();
        recipeId.addProperty("type", "string");
        recipeId.addProperty("description", "The recipe identifier (e.g., 'minecraft:diamond_sword')");
        properties.add("recipe_id", recipeId);
        
        JsonObject count = new JsonObject();
        count.addProperty("type", "integer");
        count.addProperty("description", "Number of items to craft (optional, default: 1)");
        count.addProperty("minimum", 1);
        count.addProperty("maximum", 64);
        properties.add("count", count);
        
        params.add("properties", properties);
        
        JsonArray required = new JsonArray();
        required.add("recipe_id");
        params.add("required", required);
        
        return params;
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String recipeId = args.has("recipe_id") ? args.get("recipe_id").getAsString() : null;
        int count = args.has("count") ? args.get("count").getAsInt() : 1;
        
        if (recipeId == null || recipeId.isBlank()) {
            return ToolExecutionResult.failure("recipe_id is required");
        }
        
        try {
            // 执行合成逻辑
            String result = "Crafted " + count + " of " + recipeId;
            return ToolExecutionResult.success(result);
        } catch (Exception e) {
            return ToolExecutionResult.failure("Failed to craft item: " + e.getMessage());
        }
    }
}
```

### 1.2 带 Prompt Appendix 的工具

```java
package com.mymod.tools;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.prompt.ToolPromptCategory;
import net.minecraft.server.command.ServerCommandSource;

public class AdvancedCraftTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:advanced_craft";
    }
    
    @Override
    public String getDescription() {
        return "Advanced crafting with custom recipes";
    }
    
    @Override
    public JsonObject getParameters() {
        // 参数定义...
        return new JsonObject();
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        // 执行逻辑...
        return ToolExecutionResult.success("Advanced craft executed");
    }
    
    @Override
    public String getPromptAppendix() {
        return "*** ADVANCED CRAFTING TOOLS ***\n\n" +
               "Use these tools for advanced crafting operations.\n" +
               "Supports custom recipes and multi-step crafting.";
    }
    
    @Override
    public String getPromptCategory() {
        return ToolPromptCategory.CUSTOM.getDisplayName();
    }
}
```

## 二、注册工具

### 2.1 Mod 初始化时注册

```java
package com.mymod;

import com.mineclawd.foundation.tool.ToolRegistry;
import com.mymod.tools.SimpleCraftTool;
import com.mymod.tools.AdvancedCraftTool;
import net.fabricmc.api.ModInitializer;

public class MyMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        // 注册简单工具
        ToolRegistry.register(new SimpleCraftTool());
        
        // 注册高级工具
        ToolRegistry.register(new AdvancedCraftTool());
        
        MineClawd.LOGGER.info("MyMod tools registered");
    }
}
```

### 2.2 动态注册工具

```java
package com.mymod;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.ToolRegistry;
import net.minecraft.server.command.ServerCommandSource;

public class DynamicToolRegistry {
    
    public static void registerDynamicTools() {
        // 动态注册工具
        ToolRegistry.register(new MineClawdTool() {
            @Override
            public String getName() {
                return "my_mod:dynamic_tool";
            }
            
            @Override
            public String getDescription() {
                return "Dynamically registered tool";
            }
            
            @Override
            public JsonObject getParameters() {
                JsonObject params = new JsonObject();
                params.addProperty("type", "object");
                
                JsonObject properties = new JsonObject();
                properties.add("config", createStringParameter("Configuration"));
                params.add("properties", properties);
                
                JsonArray required = new JsonArray();
                required.add("config");
                params.add("required", required);
                
                return params;
            }
            
            @Override
            public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
                String config = args.has("config") ? args.get("config").getAsString() : null;
                return ToolExecutionResult.success("Dynamic tool executed with config: " + config);
            }
        });
    }
}
```

## 三、工具分类示例

### 3.1 信息查询工具

```java
package com.mymod.tools.information;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.prompt.ToolPromptCategory;
import net.minecraft.server.command.ServerCommandSource;

public class StatusCheckTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:check_status";
    }
    
    @Override
    public String getDescription() {
        return "Check the current system status";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        properties.add("level", createStringParameter("Status level", false));
        params.add("properties", properties);
        
        return params;
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String level = args.has("level") ? args.get("level").getAsString() : "normal";
        
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
    
    @Override
    public String getPromptCategory() {
        return ToolPromptCategory.INFORMATION.getDisplayName();
    }
    
    private static JsonObject createStringParameter(String description) {
        return createStringParameter(description, true);
    }
    
    private static JsonObject createStringParameter(String description, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "string");
        param.addProperty("description", description);
        
        if (!required) {
            param.addProperty("default", "normal");
        }
        
        return param;
    }
}
```

### 3.2 文件管理工具

```java
package com.mymod.tools.files;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.prompt.ToolPromptCategory;
import net.minecraft.server.command.ServerCommandSource;

public class CustomFileTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:read_custom_file";
    }
    
    @Override
    public String getDescription() {
        return "Read a custom format file";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        properties.add("path", createStringParameter("File path"));
        properties.add("format", createStringParameter("File format", false));
        params.add("properties", properties);
        
        JsonArray required = new JsonArray();
        required.add("path");
        params.add("required", required);
        
        return params;
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String path = args.get("path").getAsString();
        String format = args.has("format") ? args.get("format").getAsString() : "default";
        
        // 读取自定义格式文件
        String content = readFile(path, format);
        
        return ToolExecutionResult.success(content);
    }
    
    @Override
    public String getPromptCategory() {
        return ToolPromptCategory.FILE_MANAGEMENT.getDisplayName();
    }
    
    private static String readFile(String path, String format) {
        // 实际的文件读取逻辑
        return "File content: " + path;
    }
    
    private static JsonObject createStringParameter(String description) {
        return createStringParameter(description, true);
    }
    
    private static JsonObject createStringParameter(String description, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "string");
        param.addProperty("description", description);
        
        if (!required) {
            param.addProperty("default", "default");
        }
        
        return param;
    }
}
```

### 3.3 游戏控制工具

```java
package com.mymod.tools.game;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.prompt.ToolPromptCategory;
import net.minecraft.server.command.ServerCommandSource;

public class GameControlTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:toggle_feature";
    }
    
    @Override
    public String getDescription() {
        return "Toggle a game feature on or off";
    }
    
    @Override
    public JsonObject getParameters() {
        JsonObject params = new JsonObject();
        params.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        properties.add("feature", createStringParameter("Feature name"));
        properties.add("enabled", createBooleanParameter("Enable or disable"));
        params.add("properties", properties);
        
        JsonArray required = new JsonArray();
        required.add("feature");
        required.add("enabled");
        params.add("required", required);
        
        return params;
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        String feature = args.get("feature").getAsString();
        boolean enabled = args.get("enabled").getAsBoolean();
        
        // 切换功能
        toggleFeature(feature, enabled);
        
        return ToolExecutionResult.success("Feature '" + feature + "' is now " + (enabled ? "enabled" : "disabled"));
    }
    
    @Override
    public String getPromptCategory() {
        return ToolPromptCategory.GAME_CONTROL.getDisplayName();
    }
    
    private static void toggleFeature(String feature, boolean enabled) {
        // 实际的功能切换逻辑
    }
    
    private static JsonObject createStringParameter(String description) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "string");
        param.addProperty("description", description);
        return param;
    }
    
    private static JsonObject createBooleanParameter(String description) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "boolean");
        param.addProperty("description", description);
        return param;
    }
}
```

## 四、完整示例

### 4.1 Mod 主类

```java
package com.mymod;

import com.mineclawd.foundation.tool.ToolRegistry;
import com.mymod.tools.information.StatusCheckTool;
import com.mymod.tools.files.CustomFileTool;
import com.mymod.tools.game.GameControlTool;
import net.fabricmc.api.ModInitializer;

public class MyMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        // 注册信息查询工具
        ToolRegistry.register(new StatusCheckTool());
        
        // 注册文件管理工具
        ToolRegistry.register(new CustomFileTool());
        
        // 注册游戏控制工具
        ToolRegistry.register(new GameControlTool());
        
        MineClawd.LOGGER.info("MyMod tools registered successfully");
    }
}
```

### 4.2 使用 Prompt Appendix

```java
package com.mymod.tools;

import com.google.gson.JsonObject;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.prompt.ToolPromptCategory;
import net.minecraft.server.command.ServerCommandSource;

public class CraftingTool implements MineClawdTool {
    
    @Override
    public String getName() {
        return "my_mod:craft_item";
    }
    
    @Override
    public String getDescription() {
        return "Craft an item using a recipe";
    }
    
    @Override
    public JsonObject getParameters() {
        // 参数定义...
        return new JsonObject();
    }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        // 执行逻辑...
        return ToolExecutionResult.success("Crafted item");
    }
    
    @Override
    public String getPromptAppendix() {
        return "*** CRAFTING TOOLS ***\n\n" +
               "Use these tools for crafting items.\n" +
               "Supports vanilla recipes and custom recipes.\n" +
               "Requires: recipe_id (string), count (optional integer)";
    }
    
    @Override
    public String getPromptCategory() {
        return ToolPromptCategory.CUSTOM.getDisplayName();
    }
}
```

## 五、最佳实践

### 5.1 命名规范

```java
// ✅ 好的命名
@Override
public String getName() {
    return "my_mod:craft_item";
}

@Override
public String getName() {
    return "my_mod:check_status";
}

// ❌ 不好的命名
@Override
public String getName() {
    return "tool1";
}

@Override
public String getName() {
    return "do_something";
}
```

### 5.2 描述规范

```java
// ✅ 好的描述
@Override
public String getDescription() {
    return "Craft an item using a recipe. " +
           "Use this when the player wants to craft something. " +
           "Requires: recipe_id (string), count (optional integer)";
}

// ❌ 不好的描述
@Override
public String getDescription() {
    return "Crafting tool";
}
```

### 5.3 参数规范

```java
// ✅ 好的参数定义
JsonObject recipeId = new JsonObject();
recipeId.addProperty("type", "string");
recipeId.addProperty("description", "The recipe identifier (e.g., 'minecraft:diamond_sword')");

JsonObject count = new JsonObject();
count.addProperty("type", "integer");
count.addProperty("description", "Number of items to craft (optional, default: 1)");
count.addProperty("minimum", 1);
count.addProperty("maximum", 64);

// ❌ 不好的参数定义
JsonObject recipeId = new JsonObject();
recipeId.addProperty("type", "string");
// 缺少 description
```

### 5.4 错误处理

```java
// ✅ 好的错误处理
@Override
public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
    String recipeId = args.has("recipe_id") ? args.get("recipe_id").getAsString() : null;
    
    if (recipeId == null || recipeId.isBlank()) {
        return ToolExecutionResult.failure("recipe_id is required");
    }
    
    try {
        // 执行逻辑
        return ToolExecutionResult.success("Success");
    } catch (Exception e) {
        return ToolExecutionResult.failure("Failed: " + e.getMessage());
    }
}

// ❌ 不好的错误处理
@Override
public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
    String recipeId = args.get("recipe_id").getAsString(); // 可能抛出异常
    // 没有错误处理
}
```

---

**文档版本**: 1.0  
**最后更新**: 2026-03-29  
**作者**: 在与有
