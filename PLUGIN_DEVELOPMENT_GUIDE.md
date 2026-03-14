# MineClawd 插件开发指南

## 概述

MineClawd 插件系统为第三方mod提供了标准化的集成接口，允许开发者创建自定义工具插件，扩展MineClawd的功能。

## 插件系统架构

```
第三方Mod
    ↓
插件系统 (tool_sys/plugin)
    ↓
MineClawd核心调用
```

## 快速开始

### 1. 创建插件类

```java
package com.yourmod.plugin;

import com.mineclawd.tool_sys.plugin.AbstractPlugin;
import com.mineclawd.tool_sys.plugin.annotation.ToolPlugin;

@ToolPlugin(
    id = "your-plugin-id",
    name = "你的插件名称",
    version = "1.0.0",
    description = "插件描述",
    author = "你的名字"
)
public class YourToolPlugin extends AbstractPlugin {
    
    @Override
    public void onEnable(com.mineclawd.tool_sys.plugin.PluginContext context) {
        super.onEnable(context);
        // 插件初始化逻辑
        info("你的插件已启用");
    }
}
```

### 2. 添加工具方法

```java
@ToolExecutor(
    name = "your-tool-name",
    description = "工具功能描述",
    category = "工具分类"
)
public String yourToolMethod(
    @ToolParameter(name = "param1", description = "参数1描述", required = true) String param1,
    @ToolParameter(name = "param2", description = "参数2描述", defaultValue = "默认值") String param2
) {
    // 工具实现逻辑
    return "执行结果";
}
```

### 3. 注册插件

在你的mod初始化代码中：

```java
// 获取MineClawd插件集成接口
MineClawdPluginIntegration pluginIntegration = ...;

// 创建并注册插件
YourToolPlugin plugin = new YourToolPlugin();
pluginIntegration.registerPlugin(plugin);
```

## 注解详解

### @ToolPlugin

标记一个类为工具插件：

- `id` (必需): 插件唯一标识符
- `name`: 插件显示名称
- `version`: 插件版本
- `description`: 插件描述
- `dependencies`: 依赖的插件ID列表
- `author`: 插件作者

### @ToolExecutor

标记一个方法为工具执行器：

- `name` (必需): 工具唯一名称
- `description` (必需): 工具功能描述（供LLM理解）
- `category`: 工具分类
- `parameterMode`: 参数模式（JSON_OBJECT, STRING_ARRAY, KEY_VALUE）
- `enabled`: 是否启用

### @ToolParameter

描述工具方法的参数：

- `name` (必需): 参数名称
- `description` (必需): 参数描述
- `type`: 参数类型（STRING, INTEGER, BOOLEAN等）
- `required`: 是否必需
- `defaultValue`: 默认值

## 插件生命周期

### 1. 初始化 (`onEnable`)

插件被加载时调用，用于：
- 读取配置
- 初始化资源
- 注册事件监听器

### 2. 运行期

- 工具方法被调用时执行
- 可以访问插件上下文和配置

### 3. 禁用 (`onDisable`)

插件被卸载时调用，用于：
- 清理资源
- 保存状态

## 配置管理

插件可以通过 `PluginConfig` 接口管理配置：

```java
// 读取配置
boolean enabled = config.getBoolean("enabled", true);
String apiKey = config.getString("api_key", "");

// 设置配置
config.setString("last_used", "2024-01-01");
config.save();
```

## 日志记录

使用插件基类提供的便捷方法：

```java
info("信息日志");
warn("警告日志");
error("错误日志");
```

## 参数模式

### JSON_OBJECT (默认)

工具方法接收 `Map<String, Object>` 参数：

```java
@ToolExecutor(parameterMode = ToolExecutor.ParameterMode.JSON_OBJECT)
public String processData(Map<String, Object> data) {
    // 处理JSON数据
}
```

### STRING_ARRAY

工具方法接收字符串数组参数：

```java
@ToolExecutor(parameterMode = ToolExecutor.ParameterMode.STRING_ARRAY)
public String processArray(String[] args) {
    // 处理数组参数
}
```

### KEY_VALUE

工具方法接收键值对参数：

```java
@ToolExecutor(parameterMode = ToolExecutor.ParameterMode.KEY_VALUE)
public String processKeyValue(String key, String value) {
    // 处理键值对
}
```

## 最佳实践

### 1. 命名规范

- 插件ID: 使用小写字母和连字符（如 `my-mod-plugin`）
- 工具名称: 使用小写字母和连字符（如 `process-data`）
- 参数名称: 使用小写字母和下划线（如 `player_name`）

### 2. 错误处理

- 返回清晰的错误信息
- 使用 `ERROR:` 前缀标识错误
- 记录详细的调试信息

### 3. 性能考虑

- 避免在工具方法中执行耗时操作
- 使用异步处理长时间任务
- 合理使用缓存

### 4. 安全性

- 验证输入参数
- 限制资源访问
- 遵循最小权限原则

## 示例插件

参考 `ExampleToolPlugin.java` 查看完整的示例实现。

## 调试和测试

### 日志查看

插件日志会输出到控制台，格式为：
```
[Plugin] 消息内容
[Plugin] WARN: 警告内容
[Plugin] ERROR: 错误内容
```

### 工具测试

可以使用MineClawd的命令行界面测试工具：
```
/mineclawd tool your-tool-name param1=value1 param2=value2
```

## 常见问题

### Q: 插件无法加载？
A: 检查：
- 插件类是否有 `@ToolPlugin` 注解
- 插件ID是否唯一
- 依赖的插件是否已加载

### Q: 工具方法无法调用？
A: 检查：
- 方法是否有 `@ToolExecutor` 注解
- 参数注解是否正确
- 方法签名是否符合要求

### Q: 配置无法保存？
A: 检查：
- 配置键名是否合法
- 是否有写入权限
- 配置值类型是否正确

## 扩展功能

插件系统支持以下扩展：

- **事件系统**: 监听MineClawd事件
- **配置界面**: 提供图形化配置
- **多语言支持**: 国际化插件界面
- **热重载**: 运行时更新插件

---

如有问题，请参考示例代码或联系MineClawd开发团队。