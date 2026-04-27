# MineClawd UI开发总结

## 背景与需求

### 初始需求
- 创建一个基于ModernUI-MC的可拖动、缩放的悬浮窗口
- 窗口打开时不暂停游戏
- 实现真正的HUD/覆盖层效果，不影响游戏控制

### 技术演进
1. 初始尝试：使用ModernUI-MC的Fragment和Screen系统
2. 发现问题：传统Screen会暂停游戏并阻止玩家操作
3. 解决方案：实现Screen监听 + 事件广播机制
4. 核心设计：注意力管理系统，选择性屏蔽UI窗口上的玩家操作

## 技术实现

### 关键文件

#### 1. `CorrectHudScreen.java`
- **位置**：`neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/CorrectHudScreen.java`
- **功能**：实现基于Screen的HUD效果
- **核心特性**：
  - 实现`ScreenCallback`接口
  - 重写`isPauseScreen()`返回`false`，确保不暂停游戏
  - 自定义渲染方法，不调用`super.render()`以避免默认背景
  - 实现事件处理，通过注意力管理系统选择性屏蔽事件

#### 2. `AttentionManager.java`
- **位置**：`neoforge/src/main/java/com/mineclawd/foundation/client/ui/framework/AttentionManager.java`
- **功能**：管理用户注意力，选择性屏蔽游戏输入
- **核心特性**：
  - 检测鼠标是否在UI区域内
  - 跟踪用户与UI的交互状态
  - 根据交互状态决定是否屏蔽游戏事件

#### 3. `ModernUINeoForgeWindowManager.java`
- **位置**：`neoforge/src/main/java/com/mineclawd/foundation/client/ModernUINeoForgeWindowManager.java`
- **功能**：管理ModernUI窗口的显示和隐藏
- **核心特性**：
  - 处理快捷键触发
  - 管理CorrectHudScreen的实例

### 依赖配置

#### `build.gradle`修改
- 添加ModernUI-MC jar文件作为依赖
- 确保正确的依赖关系配置
- 处理编译和运行时依赖

## 技术挑战与解决方案

### 1. 依赖配置问题
- **问题**：编译时找不到Minecraft和ModernUI类
- **解决方案**：正确配置build.gradle，添加必要的依赖项

### 2. 屏幕暂停游戏问题
- **问题**：传统Screen实现会暂停游戏
- **解决方案**：实现`ScreenCallback`接口，重写`isPauseScreen()`方法

### 3. 事件处理冲突
- **问题**：UI窗口会阻止玩家与游戏的交互
- **解决方案**：实现注意力管理系统，选择性屏蔽事件

### 4. 透明背景实现
- **问题**：默认Screen会渲染背景
- **解决方案**：重写渲染方法，不调用父类的渲染方法

## 架构设计

### 模块分离
- **common模块**：平台无关的核心代码
- **neoforge模块**：NeoForge平台特定实现
- **client代码**：移至neoforge模块，确保正确的依赖关系

### 接口设计
- 解耦的API设计，便于复用
- 清晰的责任分离：窗口管理、事件处理、渲染逻辑

## 当前状态

### 已完成
- 创建了`CorrectHudScreen`类，实现不暂停游戏的屏幕
- 实现了`AttentionManager`，用于选择性事件屏蔽
- 配置了ModernUI-MC依赖
- 解决了编译错误

### 待完成
- 验证透明背景和不暂停游戏的实现
- 完善窗口的拖动和缩放功能
- 实现现代化风格的UI元素
- 集成MineClawd的具体功能

## 技术栈

- **框架**：ModernUI-MC
- **渲染**：基于Arc3D引擎的GPU加速渲染
- **平台**：NeoForge
- **架构**：Architectury框架

## 开发流程

1. **需求分析**：明确HUD效果的具体要求
2. **技术研究**：深入学习ModernUI-MC的特性和最佳实践
3. **架构设计**：设计解耦的API和模块结构
4. **实现核心功能**：创建CorrectHudScreen和AttentionManager
5. **测试验证**：确保功能正常且不影响游戏体验
6. **优化完善**：改进UI风格和用户体验

## 关键技术点

- **ScreenCallback接口**：控制屏幕行为（暂停游戏、透明度等）
- **注意力管理系统**：选择性屏蔽游戏输入
- **事件处理**：监听玩家操作并广播给游戏线程
- **透明背景**：实现HUD效果
- **GPU加速渲染**：利用ModernUI-MC的渲染特性

## 未来规划

- 实现多窗口支持
- 添加窗口拖动和缩放功能
- 集成MineClawd的具体功能模块
- 优化UI风格，使其与ModernUI-MC的设置界面风格一致
- 实现三栏式布局，支持灵活调整大小
- 添加子窗口的折叠和拖放功能