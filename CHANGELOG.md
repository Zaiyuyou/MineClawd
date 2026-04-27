# MineClawd 变更日志

## v2.0.0 (2026-04-05) - 完全重构版本

### 🎉 重大更新：工具系统完全重构

此版本对 MineClawd 的工具系统进行了彻底重构，从硬编码的 switch-case 架构转变为模块化、配置驱动的现代化架构。

#### 🏗️ 架构重构

**从旧架构到新架构的转变：**
- ❌ **旧架构**：单一文件硬编码，工具逻辑分散在 `MineClawd.java` 中
- ✅ **新架构**：模块化分层设计，工具按功能分类管理

**新的包结构：**
```
com.mineclawd/
├── foundation/          # 基础架构模块
│   ├── tool/           # 工具接口和注册系统
│   └── kubejs/         # KubeJS 基础支持
└── buildin/            # 具体功能实现模块
    ├── kubejs/         # KubeJS 相关工具
    ├── files/          # 文件操作工具
    ├── web/            # 网络工具
    ├── mod/            # Mod 文档工具
    └── dynamic/        # 动态内容工具
```

#### 🔧 工具迁移完成度

**已成功迁移 29/30 个内置工具：**

- ✅ **KubeJS工具** (`buildin/kubejs/KubeJsTools.java`)
  - `execute-command` - 执行命令工具
  - `apply-instant-server-script` - 应用即时服务器脚本
  - `list-server-scripts` - 列出服务器脚本
  - `ask-user-question` - 询问用户问题（异步工具）
  - `reload-game` - 重新加载游戏
  - `sync-command-tree` - 同步命令树

- ✅ **文件操作工具** (`buildin/files/WorkspaceFileTool.java`)
  - `list-files` - 列出文件/目录
  - `read-files` - 读取文本文件
  - `write-files` - 写入文本文件
  - `copy-files` - 复制文件/目录
  - `move-files` - 移动/重命名文件/目录
  - `grep` - 文件内容搜索
  - `curl` - HTTP请求工具

- ✅ **网络工具** (`buildin/web/WebTools.java`)
  - `search` - 网络搜索工具（Tavily API）

- ✅ **Mod文档工具** (`buildin/mod/ModDocsTool.java`)
  - `fetch_url` - 获取HTTP(S)页面
  - `fetch_modrinth` - 获取Modrinth项目页面
  - `list_commands` - 列出可用根命令

- ✅ **动态内容工具** (`buildin/dynamic/DynamicContentTool.java`)
  - `list-dynamic-content` - 列出动态内容
  - `register-dynamic-item` - 注册动态物品
  - `register-dynamic-block` - 注册动态方块
  - `register-dynamic-fluid` - 注册动态流体
  - `update-dynamic-item` - 更新动态物品
  - `update-dynamic-block` - 更新动态方块
  - `update-dynamic-fluid` - 更新动态流体
  - `unregister-dynamic-content` - 注销动态内容

#### ⚙️ 新增功能

**1. 配置管理系统 (`ToolConfig`)**
- 自动配置文件管理 (`config/mineclawd-tools.json`)
- 运行时工具启用/禁用
- 工具分类和自定义配置支持

**2. 工具注册表 (`ToolRegistry`)**
- 动态工具注册/注销
- 热重载支持 (`reloadBuiltInTools()`)
- 与 Minecraft reload 指令联动

**3. 揭露式提示词系统 (`RevealedToolPromptSystem`)**
- 三层信息揭露机制 (LAYER_1/LAYER_2/LAYER_3)
- 动态提示词生成
- OpenAI API 兼容格式

**4. 工具管理命令**
```bash
/mineclawd tools                    # 列出工具状态
/mineclawd tools enable <工具名>    # 启用工具
/mineclawd tools disable <工具名>   # 禁用工具
/mineclawd tools reload            # 重载工具配置
```

#### 🔄 向后兼容性

**保持兼容的方面：**
- ✅ Mod ID 保持不变 (`mineclawd`)
- ✅ 核心功能 API 保持兼容
- ✅ 现有配置和会话数据兼容

**需要用户注意的变更：**
- ❌ 工具内部实现完全重构
- ❌ 部分工具的执行逻辑可能有所调整
- ❌ 配置文件格式更新（自动迁移）

#### 📊 技术改进

**代码质量提升：**
- 模块化设计，职责分离清晰
- 统一的错误处理机制
- 完整的类型安全
- 易于测试和维护

**性能优化：**
- 减少内存占用
- 优化工具加载速度
- 改进提示词生成效率

#### 🚀 开发者体验

**新的工具开发模式：**
```java
public class MyCustomTool implements MineClawdTool {
    @Override
    public String getName() { return "my-tool"; }
    
    @Override
    public String getDescription() { return "我的自定义工具"; }
    
    @Override
    public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
        // 工具实现逻辑
        return ToolExecutionResult.success("操作成功");
    }
}
```

#### 📝 文件变更摘要

**删除的文件：**
- 所有旧的工具 executor 类
- 硬编码的工具实现
- 过时的配置和UI类

**新增的文件：**
- `foundation/tool/` - 基础工具架构
- `buildin/` - 模块化工具实现
- 架构文档和开发指南

#### 🔮 未来展望

此重构为后续功能奠定了基础：
- 插件化工具系统
- 图形化配置界面
- 工具市场支持
- AI驱动的工具推荐

---

## v1.3.0 (之前版本)

### 功能特性
- 基础 AI 助手功能
- KubeJS 集成支持
- 会话管理和角色系统
- 基础工具支持

---

*此变更日志最后更新: 2026-04-05*