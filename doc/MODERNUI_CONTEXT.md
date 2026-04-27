# MineClawd ModernUI GUI — 项目上下文文档

> 生成日期: 2026-04-26
> 目的: 为新对话提供完整的项目背景、架构、已完成工作和待办事项

---

## 一、项目背景

### 1.1 项目概况

MineClawd 是一个 Minecraft AI 助手模组（NeoForge 1.21.1），使用 Architectury 多平台架构。
用户可以用自然语言与 AI 交互，AI 通过工具系统修改游戏世界、管理资产、执行命令等。

**原 GUI**：基于 Minecraft 原生 `Screen` 类的手绘 UI（`AgentResponseOverlay`），覆盖在 HUD 上。
**重构目标**：使用 **ModernUI-MC** 框架构建全新 GUI，替换旧的 HUD 覆盖式 UI。

### 1.2 技术栈

| 层 | 技术 |
|---|------|
| 模组框架 | NeoForge 1.21.1 + Architectury Loom |
| 映射 | Yarn (v2) + Yarn Mappings Patch |
| UI 框架 | ModernUI-MC 3.x (icyllis.modernui) |
| 文本渲染 | Markflow (ModernUI 内置 Markdown 引擎) |
| 构建 | Gradle 8.8 + ShadowJar |
| 本地化 | Minecraft I18n + Fallback Map |
| 网络 | Architectury NetworkManager |
| LLM | OpenAI API 兼容 / Google Vertex AI |

### 1.3 模块结构

```
MineClawD_RE/
├── common/                          # 跨平台公共代码
│   ├── src/main/java/com/mineclawd/
│   │   ├── MineClawd.java                # 主类 (服务端+客户端)
│   │   ├── MineClawdClientNetworking.java # 网络处理 (包含事件路由)
│   │   ├── AgentStreamEventType.java      # 流事件类型枚举
│   │   ├── foundation/
│   │   │   ├── agent/                     # Agent 管理系统
│   │   │   ├── assets/                    # 资产管理
│   │   │   ├── client/
│   │   │   │   ├── AgentResponseOverlay.java # 旧的 HUD 覆盖式 UI
│   │   │   │   ├── ChatStreamBridge.java     # 流事件 → Fragment 桥梁
│   │   │   │   ├── SessionPayloadBridge.java # 会话数据 → Fragment 桥梁
│   │   │   │   └── ui/
│   │   │   │       ├── i18n/MineClawdI18n.java  # 国际化工具类
│   │   │   │       └── log/Log*.java            # 日志系统
│   │   │   ├── config/                   # 配置系统
│   │   │   ├── llm/                      # LLM 客户端
│   │   │   ├── session/                  # 会话管理
│   │   │   │   ├── SessionManager.java       # 服务端会话管理器
│   │   │   │   └── SessionOverlayPayload.java # 会话数据 DTO
│   │   │   └── tool/                     # 工具系统
│   │   └── buildin/                      # 内置工具实现
│   └── src/main/resources/
│       ├── assets/mineclawd/
│       │   ├── font/NotoEmoji.ttf           # 内置 emoji 字体
│       │   ├── lang/                        # 本地化文件
│       │   └── textures/gui/icon-simplified.png # Orb 图标
│       └── mineclawd/                       # Agent/Prompt 定义
│
└── neoforge/                          # NeoForge 平台代码
    └── src/main/java/com/mineclawd/
        ├── MineClawdNeoForge1211.java      # NeoForge 入口
        ├── foundation/client/
        │   ├── CharInputBridge.java         # 字符输入桥接 (Screen→EditText)
        │   ├── HudOrbHandler.java           # Orb 渲染和交互 (事件订阅)
        │   ├── ModernUINeoForgeWindowManager.java # 窗口管理 (F7 开关)
        │   └── ui/
        │       ├── framework/
        │       │   ├── ModernHudFragment.java  # 主 GUI Fragment (核心文件)
        │       │   ├── EmojiEnabler.java       # Emoji 字体注册
        │       │   ├── HudOrbState.java        # Orb 状态管理
        │       │   ├── IMBlockerCompat.java    # 输入法兼容层
        │       │   └── AttachmentHelper.java   # 附件管理器
        │       └── log/                        # 日志 UI (LogView, LogEntry)
        └── mixin/                          # Mixin 注入

```

---

## 二、完成的功能

### 2.1 ModernUI GUI 框架

| 文件 | 状态 | 功能 |
|------|------|------|
| ModernHudFragment.java | ✅ 已重写 | 主 Fragment，包含窗口系统、导航、设置、聊天 |
| HudOrbState.java | ✅ 已重写 | Orb 位置/尺寸持久化，DP 自适应 |
| HudOrbHandler.java | ✅ 已重写 | 平滑圆形渲染 + 纹理图标 |
| EmojiEnabler.java | ✅ 已重写 | NotoEmoji.ttf 注册到 Arc3D 字体管线 |
| IMBlockerCompat.java | ✅ 已完成 | 反射式 IMBlocker 白名单注册 |
| CharInputBridge.java | ✅ 已完成 | Screen.charTyped 到 EditText 的输入桥接 |
| AttachmentHelper.java | ✅ 新建 | 文件选择器 + 附件队列 |
| SessionPayloadBridge.java | ✅ 新建 | 服务器会话数据到 Fragment 的桥梁 |

### 2.2 窗口系统

- 可拖动、可调整大小的浮动窗口（9点拖拽手柄）
- 最大化/最小化（到 Orb）
- 双击外部区域最小化
- 窗口位置/大小持久化（`HudOrbState.saveWindowPos`）
- 入场动画（ease-emphasized 缩放 + 淡入）
- ModernUI 主题色自适应（`colorSurfaceContainerLow` 背景、圆角）

### 2.3 导航系统

- 6 个标签页：Chat / Sessions / Files / Tools / Assets / Settings
- 左侧图标导航栏 + 右侧内容区
- 侧栏（会话列表/状态面板）+ 可拖拽分隔条

### 2.4 聊天页面

- **Markdown 渲染**: 使用 ModernUI Markflow（代码块使用 JetBrains Mono）
- **流式消息**: START 指示器 → DELTA 增量追加 → DONE 完成
- **工具调用卡片**: 实时显示工具名称、状态（⚙️进行中/✅完成），带主题色边框
- **命令行回显**: `$ result` 样式终端回显
- **错误处理**: 错误气泡 + 日志面板错误条目
- **三态发送按钮**:
  - Send（`colorPrimary` 蓝）
  - ■ Stop（`colorError` 红）
  - ↺ Retry（`#FF8C00` 橙）
- **附件按钮**: AWT FileDialog 多选文件，显示 `+N` 徽章
- **输入法兼容**: IMBlocker 白名单 + CharInputBridge
- **停止生成**: 发送 `mineclawd stop`
- **重试机制**: 解析 `Retry Token` 发送 `mineclawd retry <token>`

### 2.5 设置页面

- LLM 供应商: Spinner 下拉选择
- 活动供应商: 描边边框（primary 色 2px active / outlineVariant 1px inactive）
- OpenAI / Vertex AI 参数配置
- 界面选项: 启用 GUI / 调试模式 / 限制工具调用
- 保存/取消按钮

### 2.6 会话管理

- 实时从服务器同步会话列表 (`mineclawd sessions`)
- `SessionPayloadBridge` 将 `SessionOverlayPayload` 路由到 Fragment
- 点击会话切换 (`mineclawd session <token>`)
- 新建会话 (`mineclawd sessions new`)
- 会话标题 + 更新时间显示

### 2.7 Orb (悬浮球)

- 圆形渲染（Android `drawCircle` 风格像素级圆形）
- 抗锯齿边缘（coverage-based alpha blending）
- primary 色描边边框 (`0xCC4A6DC9`)
- 内发光效果
- `icon-simplified.png` 居中纹理
- DP 自适应尺寸 (`max(32, min(sw, sh) / 20)`)
- 圆形碰撞检测
- 拖拽吸附到屏幕边缘

### 2.8 Emoji 显示

- 内置 `NotoEmoji.ttf` 注册到 Arc3D 字体管线
- 字节缓存避免跨 reload 丢失
- `onResourcesReady()` 重新 apply

### 2.9 主题适配

所有控件使用 ModernUI 主题属性而非硬编码颜色:

| 主题属性 | 用途 |
|---------|------|
| `colorPrimary` | 活动元素、发送按钮、选中状态 |
| `colorOnSurface` | 主要文字 |
| `colorOnSurfaceVariant` | 次要文字、图标 |
| `colorSurface` | 面板背景 |
| `colorSurfaceContainerLow` | 窗口背景、卡片填充 |
| `colorSurfaceContainerHigh` | 消息气泡（助手）、输入框背景 |
| `colorSurfaceContainerHighest` | 按钮背景 |
| `colorOutline` | 边框 |
| `colorOutlineVariant` | 非活动边框 |
| `colorError` | 停止按钮 |
| `colorControlHighlight` | Ripple 动效 |

---

## 三、剩余的阶段 2 工作

### 3.1 高优先级

- [ ] **文件/工具/资产标签页**：内容区域目前显示 "Phase 2" 占位符，需要实现
  - 文件浏览器（工作区文件列表）
  - 工具状态面板
  - 资产管理视图（复用 `AssetsOverlayPayload`）
- [ ] **会话历史重建**：重新打开 Fragment 时，应能从服务器重新拉取历史
  - 当前 `onCreate` 中 `requestSessionsFromServer()` 在 `onResume` 调用
  - 但 `buildChatPage` 只在 `switchTab(0)` 时重建，切换标签再回来需要完整重建
- [ ] **附件上传进度**：目前只显示计数，没有上传进度条
- [ ] **复制消息文本**：长按/右键消息气泡可复制内容

### 3.2 中等优先级

- [ ] **Persona/Agent 切换 UI**：侧栏应显示 Persona 和 Agent 选择器
  - 数据已通过 `SessionOverlayPayload.personas()/agents()` 传递
  - 当前 Fragment 未使用
- [ ] **Prompt/System Prompt 显示**：可查看当前使用的 System Prompt
- [ ] **消息搜索**：在聊天历史中搜索关键词
- [ ] **消息操作**：编辑已发送消息、删除消息

### 3.3 低优先级

- [ ] **代码块复制按钮**：Markdown 代码块右上角添加复制图标
- [ ] **图片预览**：附件图片在聊天中内联预览
- [ ] **@提及**：@提及其他 Agent 切换
- [ ] **打字机音效**：AI 回复时的打字机音效

---

## 四、架构设想与技术细节

### 4.1 事件流架构

```
服务器端 (MineClawd.java)
  → 处理 LLM 请求 → 流式输出 agent events
  → sendAgentStreamPacket(player, requestId, type, payload)
  → NetworkManager s2c → MineClawdNetworking.AGENT_STREAM_EVENT
  
客户端 (MineClawdClientNetworking.java)
  → 接收 AgentStreamEventType + payload
  → AgentResponseOverlay.handleStreamEvent()  // 更新旧 UI
  → ChatStreamBridge.forward()                // 转发到 Fragment
  
Fragment (ModernHudFragment.java)
  → onStreamEvent() → 更新聊天容器 UI
```

### 4.2 会话数据流

```
服务器端
  → /mineclawd sessions 命令
  → sendSessionsOverlayToPlayer()
  → 构建 SessionOverlayPayload JSON
  → NetworkManager s2c → OPEN_SESSIONS

客户端
  → MineClawdClientNetworking 接收
  → AgentResponseOverlay.handleSessionsPayload()  // 更新旧 UI
  → SessionPayloadBridge.forward()                // 转发到 Fragment

Fragment
  → onSessionPayload() → 更新 mSessions / mMessages / 侧栏
```

### 4.3 关键设计决策

**为什么 Fragment 不使用 requireActivity()**
ModernUI 的 Fragment 不继承 Android Fragment，没有 `requireActivity()` 方法。
关闭 Fragment 的正确方式是 `MinecraftClient.getInstance().setScreen(null)`。

**为什么 Emoji 用字体注册而非 ModernUI 的彩色纹理系统**
ModernUI 的 `sUseColorEmoji=true` 加载的是 PNG 纹理序列，与我们的 TTF 字体路线不同。
两者冲突时都不显示。解决方案：关闭 `sUseColorEmoji`，将 TTF 注册为 fallback 字体。

**为什么 SessionPayloadBridge 放在 common 模块**
`MineClawdClientNetworking.java` 在 common 模块，而 `ModernHudFragment` 在 neoforge 模块。
桥梁接口在 common 定义，Fragment 在 neoforge 订阅，避免跨模块引用。

**为什么 Orb 使用像素循环而非 ModernUI Canvas 绘制**
`HudOrbHandler` 在 `RenderGuiEvent.Post` 中绘制，这是 Minecraft 原版渲染管线。
不能使用 ModernUI 的 Canvas（属于 ModernUI 内部渲染线程）。只能使用原生的 `DrawContext` API。

### 4.4 代码模式参考

**窗口创建模式**:
```java
// 所有 UI 组件在 onCreateView 中创建
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
    var ctx = requireContext();
    resolveThemeColors(ctx);
    // ... 创建 View 树
    mRoot.post(() -> {
        // 入场动画
        ObjectAnimator anim = ObjectAnimator.ofFloat(mWindow, View.SCALE_X, 0.92f, 1.0f);
        anim.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        anim.setDuration(400);
        anim.start();
    });
    return mRoot;
}
```

**事件桥梁模式**:
```java
// 在 Fragment 中订阅
ChatStreamBridge.setListener(this::onStreamEvent);

// 在 onDestroy 中取消
ChatStreamBridge.clearListener();
```

**主题色解析模式**:
```java
private void resolveThemeColors(Context ctx) {
    TypedValue tv = new TypedValue();
    ctx.getTheme().resolveAttribute(R.ns, R.attr.colorPrimary, tv, true);
    mColorPrimary = tv.data;
}
```

**ShapeDrawable 圆角背景**:
```java
var bg = new ShapeDrawable();
bg.setShape(ShapeDrawable.RECTANGLE);
bg.setCornerRadius(mWindow.dp(8));
bg.setColor(mColorSurfaceContainerHigh);
bg.setStroke(mWindow.dp(1), mColorOutlineVariant);
view.setBackground(bg);
```

**RippleDrawable 按钮反馈**:
```java
var ripple = new RippleDrawable(
    ColorStateList.valueOf(themeColor),
    shapeDrawable,
    null  // mask
);
button.setBackground(ripple);
```

**Markdown 渲染**:
```java
var builder = Markflow.builder(requireContext());
builder.usePlugin(new MarkflowPlugin() {
    @Override
    public void configureTheme(MarkflowTheme.Builder tb) {
        tb.codeTypeface(Typeface.getSystemFont("JetBrains Mono Medium"));
    }
});
mMarkflow = builder.build();
// 使用
mMarkflow.setMarkdown(textView, markdownString);
```

---

## 五、文件索引

### neoforge 模块 GUI 文件

| 路径 | 行数 | 说明 |
|------|------|------|
| [ModernHudFragment.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/ModernHudFragment.java) | ~1345 | 主 GUI Fragment，核心文件 |
| [HudOrbHandler.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/HudOrbHandler.java) | ~234 | Orb 渲染和交互 |
| [HudOrbState.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/HudOrbState.java) | ~41 | Orb 状态管理 |
| [EmojiEnabler.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/EmojiEnabler.java) | ~94 | Emoji 字体注册 |
| [IMBlockerCompat.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/IMBlockerCompat.java) | ~85 | 输入法兼容 |
| [AttachmentHelper.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/AttachmentHelper.java) | ~90 | 附件文件选择器 |
| [CharInputBridge.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/CharInputBridge.java) | ~46 | 输入桥接 |
| [ModernUINeoForgeWindowManager.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/neoforge/src/main/java/com/mineclawd/foundation/client/ModernUINeoForgeWindowManager.java) | - | 窗口开关 (F7) |

### common 模块桥梁文件

| 路径 | 说明 |
|------|------|
| [ChatStreamBridge.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/client/ChatStreamBridge.java) | 流事件桥梁 |
| [SessionPayloadBridge.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/client/SessionPayloadBridge.java) | 会话数据桥梁 |
| [AgentResponseOverlay.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/client/AgentResponseOverlay.java) | 旧的覆盖 UI (~4500行，参考实现) |
| [MineClawdI18n.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/client/ui/i18n/MineClawdI18n.java) | 国际化工具 |

### 数据模型文件

| 路径 | 说明 |
|------|------|
| [SessionOverlayPayload.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/session/SessionOverlayPayload.java) | 会话数据 DTO |
| [AgentStreamEventType.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/AgentStreamEventType.java) | 流事件类型 |
| [MineClawdConfig.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/config/MineClawdConfig.java) | 配置模型 |

---

## 六、常见问题

### Q: 添加新标签页需要改哪里？
1. `switchTab()` 中的 `icons` 和 `labels` 数组
2. `updateMainContent()` 中的 `case` 分支
3. `updateSidebarContent()` 中的内容逻辑
4. 对应的 `build*Page()` 方法

### Q: 如何添加新的 i18n 键？
需要同时修改 3 个地方:
1. [en_us.json](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/resources/assets/mineclawd/lang/en_us.json)
2. [zh_cn.json](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/resources/assets/mineclawd/lang/zh_cn.json)
3. [MineClawdI18n.java](file:///d:/MC/MechanoMunch/versions/XechanoMunch/mods/projects/MineClawd_RE/common/src/main/java/com/mineclawd/foundation/client/ui/i18n/MineClawdI18n.java) — FALLBACK map

### Q: ModernUI Canvas 能在 HUD 渲染中使用吗？
不能。ModernUI 的 `Canvas`/`Paint` 只在其内部渲染线程有效。
`HudOrbHandler.onRenderGui` 在 Minecraft 主渲染线程的 `RenderGuiEvent.Post` 中调用，
只能使用 Minecraft 原生的 `DrawContext.fill()` / `DrawContext.drawTexture()` 等 API。

### Q: 构建命令
```bash
.\gradlew.bat :neoforge:compileJava          # 仅编译
.\gradlew.bat :common:compileJava             # 仅编译 common
.\gradlew.bat build                           # 完整构建 + remap + shadowJar
```

---

*此文档由 AI 辅助生成，用于保持跨对话的上下文一致性。*
