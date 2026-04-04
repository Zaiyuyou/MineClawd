# MineClawd 工具系统新架构文档

## 📋 概述

本文档记录了 MineClawd 工具系统的新架构设计、实现成果、技术细节以及后续开发计划。新架构实现了模块化、可配置、热重载的工具管理系统。

## 🏗️ 架构设计

### 模块化架构

新架构将项目结构分为两大模块：

- **`foundation/`** - 基础架构模块
  - 工具接口定义 (`MineClawdTool`)
  - 工具注册表 (`ToolRegistry`)
  - 配置管理系统 (`ToolConfig`)
  - 提示词系统 (`RevealedToolPromptSystem`)

- **`buildin/`** - 具体功能实现模块
  - KubeJS工具 (`kubejs/`)
  - 文件操作工具 (`files/`)
  - 网络工具 (`web/`)
  - Mod文档工具 (`mod/`)

### 核心组件

#### 1. MineClawdTool 接口
```java
public interface MineClawdTool {
    String getName();                    // 工具名称
    String getDescription();            // 工具描述
    JsonObject getParameters();         // 参数定义
    ToolExecutionResult execute(ServerCommandSource source, JsonObject args);
    boolean supportsAsync();            // 是否支持异步
    String getPromptCategory();         // 提示词分类
    String getPromptAppendix();         // 提示词附录
    boolean isEnabled();                // 是否启用
    RevealStrategy getRevealStrategy(); // 揭露策略
}
```

#### 2. ToolRegistry 工具注册表
- 动态工具注册/注销
- 工具状态管理
- 支持热重载 (`reloadBuiltInTools()`)
- 与 Minecraft reload 指令联动

#### 3. ToolConfig 配置管理系统
- 配置文件自动管理 (`mineclawd-tools.json`)
- 工具状态持久化
- 动态配置更新
- 错误处理和默认配置回退

#### 4. RevealedToolPromptSystem 揭露式提示词系统
- 三层信息揭露机制
- 动态提示词生成
- OpenAI API 兼容格式

## ✅ 已完成的工作

### 1. 工具迁移完成度

#### ✅ 已迁移的工具 (29个)

**KubeJS工具** (`buildin/kubejs/KubeJsTools.java`):
- `execute-command` - 执行命令工具
- `apply-instant-server-script` - 应用即时服务器脚本
- `list-server-scripts` - 列出服务器脚本
- `ask-user-question` - 询问用户问题（异步工具）
- `reload-game` - 重新加载游戏
- `sync-command-tree` - 同步命令树

**文件操作工具** (`buildin/files/WorkspaceFileTool.java`):
- `list-files` - 列出文件/目录
- `read-files` - 读取文本文件
- `write-files` - 写入文本文件
- `copy-files` - 复制文件/目录
- `move-files` - 移动/重命名文件/目录
- `grep` - 文件内容搜索
- `curl` - HTTP请求工具

**网络工具** (`buildin/web/WebTools.java`):
- `search` - 网络搜索工具（Tavily API）

**Mod文档工具** (`buildin/mod/ModDocsTool.java`):
- `fetch_url` - 获取HTTP(S)页面
- `fetch_modrinth` - 获取Modrinth项目页面
- `list_commands` - 列出可用根命令

**动态内容工具** (`buildin/dynamic/DynamicContentTool.java`):
- `list-dynamic-content` - 列出动态内容
- `register-dynamic-item` - 注册动态物品
- `register-dynamic-block` - 注册动态方块
- `register-dynamic-fluid` - 注册动态流体
- `update-dynamic-item` - 更新动态物品
- `update-dynamic-block` - 更新动态方块
- `update-dynamic-fluid` - 更新动态流体
- `unregister-dynamic-content` - 注销动态内容

**系统工具**:
- `tool-info-request` - 工具信息请求（揭露式提示词系统）

### 2. 配置管理系统实现

#### 配置文件结构
```json
{
  "execute-command": {
    "enabled": true,
    "category": "KubeJS"
  },
  "search": {
    "enabled": true,
    "category": "Web"
  }
}
```

#### 命令支持
```bash
/mineclawd tools                    # 列出工具状态
/mineclawd tools enable <工具名>    # 启用工具
/mineclawd tools disable <工具名>   # 禁用工具
/mineclawd tools reload            # 重载工具配置
```

### 3. 揭露式提示词系统

#### 三层揭露机制
- **LAYER_1**: 工具名称 + 简短描述
- **LAYER_2**: 完整工具描述 + 参数定义
- **LAYER_3**: 详细文档 + 使用示例

#### 揭露策略
- `DYNAMIC`: 动态揭露（默认）
- `ALWAYS_FULL`: 总是显示完整信息

### 4. 热重载支持

- 服务器启动时自动重载工具
- 支持运行时配置更新
- 与 Minecraft 生命周期事件联动

## 🔧 技术实现细节

### 1. 工具注册流程
```java
// 1. 工具注册时自动创建配置
ToolConfig.registerTool(toolName, tool);

// 2. 从配置读取启用状态  
boolean enabled = ToolConfig.isToolEnabled(toolName);

// 3. 状态变更时保存配置
ToolConfig.setToolEnabled(toolName, enabled);
```

### 2. 事件联动机制
```java
// 服务器启动时重载工具
LifecycleEvent.SERVER_STARTED.register(server -> {
    ToolRegistry.reloadBuiltInTools();
});
```

### 3. 异步工具支持
```java
@Override
public boolean supportsAsync() {
    return true; // 如 ask-user-question 工具
}
```

## ❌ 尚未迁移的工具

### 遗留工具列表

**图像处理工具**:
- `read-image` - 读取/描述图像文件

### 迁移优先级
1. **低优先级**: `read-image`

## 🚀 待办事项

### 1. 外挂工具自定义系统
**目标**: 允许用户自定义编辑工具的 description、appendix 等，并支持热更新

**实现方案**:
```java
// 在 ToolConfig 中扩展自定义字段
public class ToolConfigEntry {
    public boolean enabled = true;
    public String category = "General";
    public String customDescription = null;    // 自定义描述
    public String customAppendix = null;       // 自定义附录
    public JsonObject customParameters = null; // 自定义参数
    public JsonObject customConfig = null;
}
```

**功能需求**:
- [ ] 支持工具描述的动态编辑
- [ ] 支持提示词附录的自定义
- [ ] 支持参数定义的动态调整
- [ ] 配置文件变更监听和热重载
- [ ] 用户界面支持（GUI/命令）

### 2. 插件化工具系统
**目标**: 支持第三方插件注册自定义工具

**实现方案**:
```java
// 插件工具注册接口
public interface ToolPlugin {
    void registerTools(ToolRegistry registry);
    void onToolConfigChanged(String toolName, ToolConfigEntry config);
}
```

### 3. 高级配置功能
- [ ] 工具权限管理系统
- [ ] 工具使用统计和限制
- [ ] 配置版本管理和迁移
- [ ] 配置模板和批量操作

### 4. 遗留工具迁移
- [x] 创建 `CommandManagementTool.java` (已整合到 `ModDocsTool.java`)
- [x] 创建 `GameManagementTool.java` (已整合到 `KubeJsTools.java`)
- [x] 创建 `DynamicContentTool.java` (已完成)
- [ ] 创建 `ImageProcessingTool.java`

## 📊 架构优势

### 1. 模块化设计
- 清晰的职责分离
- 易于扩展和维护
- 支持插件化开发

### 2. 配置驱动
- 动态配置管理
- 持久化状态保存
- 热重载支持

### 3. 用户体验
- 直观的命令接口
- 实时状态反馈
- 错误处理和恢复

### 4. 技术先进性
- 符合 OpenAI API 规范
- 支持异步操作
- 完整的错误处理机制

## 🔍 技术挑战与解决方案

### 1. 热重载实现
**挑战**: 工具状态与游戏状态同步
**解决方案**: 事件驱动架构 + 配置持久化

### 2. 配置管理
**挑战**: 动态配置更新与状态一致性
**解决方案**: 原子操作 + 事务性更新

### 3. 异步工具支持
**挑战**: 异步操作与同步API的兼容
**解决方案**: 回调机制 + 状态管理

## 📝 使用示例

### 1. 工具状态管理
```bash
# 查看所有工具状态
/mineclawd tools

# 启用特定工具
/mineclawd tools enable search

# 禁用特定工具
/mineclawd tools disable curl

# 重载工具配置
/mineclawd tools reload
```

### 2. 配置文件示例
```json
{
  "execute-command": {
    "enabled": true,
    "category": "KubeJS",
    "customDescription": "执行 Minecraft 命令的强大工具"
  },
  "search": {
    "enabled": false,
    "category": "Web",
    "customAppendix": "需要配置 Tavily API Key 才能使用"
  }
}
```

## 🔮 未来展望

### 短期目标 (已完成)
1. ✅ 完成遗留工具迁移 (29/30个工具已迁移)
2. 实现基础的自定义编辑功能
3. 完善错误处理和用户反馈

### 中期目标 (1-2月)
1. 实现完整的插件化系统
2. 开发图形化配置界面
3. 添加工具使用统计功能

### 长期目标 (3-6月)
1. 支持工具市场/插件商店
2. 实现AI驱动的工具推荐
3. 跨服务器配置同步

---

## 📞 技术支持

- **文档维护**: 本文档应随代码更新而更新
- **问题反馈**: 通过项目Issue系统报告问题
- **开发讨论**: 使用项目Discussion功能进行技术讨论

## 📄 版本历史

- **v1.0** (2026-04-05): 初始版本，记录新架构实现成果
- **后续更新**: 记录架构演进和功能增强

---

*本文档最后更新: 2026-04-05*