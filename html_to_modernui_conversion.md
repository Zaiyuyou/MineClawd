# HTML到ModernUI-MC代码转换指南

## 🎯 转换原则

### 1. 设计系统映射
```
HTML/CSS          → ModernUI-MC
-----------        -------------
<div>             → View/FrameLayout
<span>            → TextView
<button>          → Button
<input>           → EditText
<ul>/<li>         → ListView/Adapter
flexbox           → LinearLayout/RelativeLayout
CSS Grid          → GridLayout
```

### 2. 样式属性转换
```
CSS属性           → ModernUI属性
--------           -------------
background        → setBackground()
border-radius     → drawRoundRect()
box-shadow        → setElevation()
color             → setTextColor()
font-size         → setTextSize()
margin/padding    → LayoutParams
```

## 🔄 具体转换示例

### 示例1：窗口容器转换
```html
<!-- HTML -->
<div class="mineclawd-window">
    <div class="sidebar">...</div>
    <div class="main-content">...</div>
</div>
```

```java
// ModernUI-MC
public class ModernUIMineClawdWindow extends Fragment {
    private FrameLayout mRootLayout;
    private LinearLayout mSidebarLayout;
    private LinearLayout mMainContentLayout;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        // 创建根布局
        mRootLayout = new FrameLayout(getContext());
        
        // 设置窗口样式
        setupWindowStyle(mRootLayout);
        
        // 创建侧边栏
        createSidebar(mRootLayout);
        
        // 创建主内容区
        createMainContent(mRootLayout);
        
        return mRootLayout;
    }
    
    private void setupWindowStyle(FrameLayout root) {
        // 设置尺寸
        var params = new FrameLayout.LayoutParams(900, 650);
        root.setLayoutParams(params);
        
        // 设置背景（使用ModernUI的GPU加速渲染）
        var background = new ColorDrawable(Color.argb(255, 30, 30, 30));
        root.setBackground(background);
        
        // 设置圆角（通过Canvas绘制）
        root.setClipToOutline(false); // ModernUI使用自定义渲染
    }
}
```

### 示例2：导航项转换
```html
<!-- HTML -->
<div class="nav-item active" onclick="switchTab('chat')">
    <span class="nav-icon">💬</span>
    <span>智能对话</span>
</div>
```

```java
// ModernUI-MC
private Button createNavButton(Context context, String icon, String text, int tabIndex) {
    var button = new Button(context);
    
    // 设置按钮样式
    button.setText(icon + " " + text);
    button.setTextSize(16);
    button.setTextColor(Color.WHITE);
    
    // 设置布局参数
    var params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    );
    params.setMargins(0, 0, 0, 8);
    button.setLayoutParams(params);
    
    // 设置背景（使用ModernUI的StateListDrawable）
    var background = createNavButtonBackground();
    button.setBackground(background);
    
    // 设置点击事件
    button.setOnClickListener(v -> switchTab(tabIndex));
    
    return button;
}

private Drawable createNavButtonBackground() {
    // 创建状态选择器
    var states = new StateListDrawable();
    
    // 正常状态
    var normal = new ColorDrawable(Color.TRANSPARENT);
    states.addState(new int[]{}, normal);
    
    // 按下状态
    var pressed = new ColorDrawable(Color.argb(50, 255, 255, 255));
    states.addState(new int[]{android.R.attr.state_pressed}, pressed);
    
    // 选中状态
    var selected = new ColorDrawable(Color.argb(30, 102, 126, 234));
    states.addState(new int[]{android.R.attr.state_selected}, selected);
    
    return states;
}
```

### 示例3：卡片布局转换
```html
<!-- HTML -->
<div class="card">
    <div class="card-title">欢迎使用 MineClawd AI 助手</div>
    <div class="card-content">...</div>
    <button class="btn">开始对话</button>
</div>
```

```java
// ModernUI-MC
private View createCard(Context context, String title, String content) {
    var cardLayout = new LinearLayout(context);
    cardLayout.setOrientation(LinearLayout.VERTICAL);
    
    // 设置卡片样式
    var cardParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    );
    cardParams.setMargins(0, 0, 0, 20);
    cardLayout.setLayoutParams(cardParams);
    
    // 设置卡片背景（使用ModernUI的圆角绘制）
    cardLayout.setBackground(createCardBackground());
    
    // 添加标题
    var titleView = new TextView(context);
    titleView.setText(title);
    titleView.setTextSize(18);
    titleView.setTextColor(Color.WHITE);
    titleView.setTypeface(Typeface.DEFAULT_BOLD);
    
    var titleParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    );
    titleParams.setMargins(24, 24, 24, 12);
    titleView.setLayoutParams(titleParams);
    
    cardLayout.addView(titleView);
    
    // 添加内容
    var contentView = new TextView(context);
    contentView.setText(content);
    contentView.setTextSize(14);
    contentView.setTextColor(Color.argb(200, 255, 255, 255));
    contentView.setLineSpacing(4, 1.2f);
    
    var contentParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    );
    contentParams.setMargins(24, 0, 24, 24);
    contentView.setLayoutParams(contentParams);
    
    cardLayout.addView(contentView);
    
    return cardLayout;
}

private Drawable createCardBackground() {
    // ModernUI-MC使用自定义的圆角背景绘制
    var shape = new GradientDrawable();
    shape.setShape(GradientDrawable.RECTANGLE);
    shape.setCornerRadius(12); // 12dp圆角
    shape.setColor(Color.argb(50, 255, 255, 255)); // 半透明白色
    shape.setStroke(1, Color.argb(100, 255, 255, 255)); // 边框
    
    return shape;
}
```

## 🎨 ModernUI-MC特有功能

### 1. GPU加速渲染
```java
// 自定义View实现GPU加速的圆角效果
public class RoundedCardView extends View {
    private Paint mBackgroundPaint;
    private float mCornerRadius;
    
    public RoundedCardView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(Color.argb(50, 255, 255, 255));
        mCornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 使用ModernUI的GPU加速圆角绘制
        canvas.drawRoundRect(
            0, 0, getWidth(), getHeight(), 
            mCornerRadius, mBackgroundPaint
        );
    }
}
```

### 2. 动画效果
```java
// 页面切换动画
private void switchTabWithAnimation(int newTab) {
    // 淡出当前页面
    var fadeOut = ObjectAnimator.ofFloat(mCurrentContent, "alpha", 1f, 0f);
    fadeOut.setDuration(200);
    
    // 显示新页面
    var fadeIn = ObjectAnimator.ofFloat(mNewContent, "alpha", 0f, 1f);
    fadeIn.setDuration(200);
    
    // 组合动画
    var animatorSet = new AnimatorSet();
    animatorSet.playSequentially(fadeOut, fadeIn);
    animatorSet.start();
}
```

## 🔧 开发工作流

### 步骤1：HTML原型设计
1. 使用HTML/CSS创建界面原型
2. 在浏览器中测试交互效果
3. 调整布局和样式

### 步骤2：代码转换
1. 将HTML结构转换为ModernUI视图层次
2. 将CSS样式转换为ModernUI样式属性
3. 将JavaScript交互转换为Java事件处理

### 步骤3：ModernUI优化
1. 使用ModernUI特有的GPU加速功能
2. 优化性能（视图复用、异步加载等）
3. 集成Minecraft游戏数据

### 步骤4：测试验证
1. 在Minecraft中测试界面显示
2. 验证交互功能
3. 性能测试和优化

## 📊 性能优化建议

### 1. 视图层次优化
- 使用`<merge>`标签减少视图层级
- 避免嵌套过深的布局
- 使用`ViewStub`延迟加载复杂视图

### 2. 内存管理
- 及时释放不再使用的资源
- 使用`WeakReference`避免内存泄漏
- 合理使用缓存机制

### 3. 渲染优化
- 使用ModernUI的GPU加速渲染
- 避免在`onDraw`中创建对象
- 使用硬件层加速动画

---

## 🎯 下一步行动

1. **验证HTML原型**：使用Live Server测试设计效果
2. **开始代码转换**：按照本指南逐步转换HTML到ModernUI
3. **功能模块实现**：逐个实现各个功能页面
4. **集成测试**：在Minecraft环境中测试完整功能

这个转换指南将帮助你高效地将HTML设计转换为高质量的ModernUI-MC界面！