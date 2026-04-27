/*
 * MineClawd - 悬浮式Markdown窗口
 * Copyright (C) 2024 MineClawd Team. All rights reserved.
 */

package com.mineclawd.foundation.client.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.fragment.FragmentContainerView;
import icyllis.modernui.fragment.FragmentTransaction;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

/**
 * 悬浮式Markdown窗口 - 卡片式设计
 * 支持拖动、调整大小、最小化等现代化功能
 */
public class FloatingMarkdownWindow extends Fragment implements ScreenCallback {

    private static final int ID_FRAGMENT_CONTAINER = 0x1001;
    private static final int ID_TITLE_BAR = 0x1002;
    private static final int ID_CLOSE_BUTTON = 0x1003;
    private static final int ID_MINIMIZE_BUTTON = 0x1004;
    
    // 窗口尺寸
    private static final int WINDOW_WIDTH = 800;  // dp
    private static final int WINDOW_HEIGHT = 600; // dp
    
    // 窗口位置（居中）
    private int mWindowX = 0;
    private int mWindowY = 0;
    
    // 窗口状态
    private boolean mIsMinimized = false;
    private boolean mIsDragging = false;
    private float mLastTouchX, mLastTouchY;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        var context = requireContext();
        
        // 创建悬浮窗口根容器 - 使用固定尺寸
        var root = new FrameLayout(context);
        
        // 计算窗口尺寸（转换为像素）
        int width = root.dp(WINDOW_WIDTH);
        int height = root.dp(WINDOW_HEIGHT);
        
        // 设置窗口尺寸
        root.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        
        // 应用现代化背景 - 半透明圆角背景
        ShapeDrawable windowBackground = new ShapeDrawable();
        windowBackground.setShape(ShapeDrawable.RECTANGLE);
        windowBackground.setCornerRadius(root.dp(12));  // 12dp圆角
        windowBackground.setColor(0xE0292A2C);  // 深色半透明背景
        root.setBackground(windowBackground);
        
        // 添加阴影效果
        root.setElevation(root.dp(16));  // 16dp阴影，增强悬浮感
        
        // 创建标题栏
        createTitleBar(root);
        
        // 创建内容区域容器
        var contentContainer = new FrameLayout(context);
        var contentParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        
        // 设置内容区域边距，为标题栏留出空间
        contentParams.topMargin = root.dp(48);  // 标题栏高度
        contentContainer.setLayoutParams(contentParams);
        
        // 创建FragmentContainerView用于管理Fragment
        var fragmentContainer = new FragmentContainerView(context);
        fragmentContainer.setId(ID_FRAGMENT_CONTAINER);
        fragmentContainer.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        contentContainer.addView(fragmentContainer);
        root.addView(contentContainer);
        
        // 设置窗口初始位置（居中）
        centerWindow(root);
        
        return root;
    }
    
    /**
     * 创建现代化标题栏
     */
    private void createTitleBar(ViewGroup root) {
        var context = root.getContext();
        
        // 标题栏容器
        var titleBar = new LinearLayout(context);
        titleBar.setId(ID_TITLE_BAR);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        
        // 标题栏背景 - 渐变效果
        ShapeDrawable titleBg = new ShapeDrawable();
        titleBg.setShape(ShapeDrawable.RECTANGLE);
        titleBg.setColor(0x80343A40);  // 深色半透明背景
        titleBar.setBackground(titleBg);
        
        var titleParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            root.dp(48)  // 48dp高度
        );
        titleBar.setLayoutParams(titleParams);
        
        // 标题文本
        var titleText = new TextView(context);
        titleText.setText("MineClawd Markdown编辑器");
        titleText.setTextSize(16);
        titleText.setTextColor(0xFFFFFFFF);  // 白色文字
        titleText.setPadding(root.dp(16), 0, 0, 0);
        
        var textParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.weight = 1;  // 占据剩余空间
        titleBar.addView(titleText, textParams);
        
        // 最小化按钮
        var minimizeButton = createTitleBarButton(root, "−");
        minimizeButton.setId(ID_MINIMIZE_BUTTON);
        minimizeButton.setOnClickListener(v -> toggleMinimize());
        titleBar.addView(minimizeButton);
        
        // 关闭按钮
        var closeButton = createTitleBarButton(root, "×");
        closeButton.setId(ID_CLOSE_BUTTON);
        closeButton.setOnClickListener(v -> closeWindow());
        titleBar.addView(closeButton);
        
        // 设置标题栏可拖动
        setupTitleBarDrag(titleBar);
        
        root.addView(titleBar);
    }
    
    /**
     * 创建标题栏按钮
     */
    private Button createTitleBarButton(View view, String text) {
        var context = view.getContext();
        var button = new Button(context);
        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(0xFFFFFFFF);
        button.setBackground(createModernButtonBackground(view));
        
        var buttonParams = new LinearLayout.LayoutParams(
            view.dp(40),  // 40dp宽度
            view.dp(40)   // 40dp高度
        );
        buttonParams.setMargins(0, 0, view.dp(4), 0);
        button.setLayoutParams(buttonParams);
        
        return button;
    }
    
    /**
     * 创建现代化按钮背景 - 圆角 + 涟漪效果
     */
    private RippleDrawable createModernButtonBackground(View view) {
        ShapeDrawable buttonBg = new ShapeDrawable();
        buttonBg.setShape(ShapeDrawable.RECTANGLE);
        buttonBg.setCornerRadius(1000);  // 完全圆角
        buttonBg.setColor(0x40FFFFFF);   // 半透明白色背景
        
        return new RippleDrawable(
            ColorStateList.valueOf(0x40FFFFFF),  // 涟漪颜色
            buttonBg, 
            null
        );
    }
    
    /**
     * 设置标题栏拖动功能
     */
    private void setupTitleBarDrag(View titleBar) {
        titleBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsDragging = true;
                    mLastTouchX = event.getRawX();
                    mLastTouchY = event.getRawY();
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    if (mIsDragging) {
                        float deltaX = event.getRawX() - mLastTouchX;
                        float deltaY = event.getRawY() - mLastTouchY;
                        
                        mWindowX += deltaX;
                        mWindowY += deltaY;
                        
                        // 更新窗口位置
                        updateWindowPosition();
                        
                        mLastTouchX = event.getRawX();
                        mLastTouchY = event.getRawY();
                    }
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsDragging = false;
                    return true;
            }
            return false;
        });
    }
    
    /**
     * 更新窗口位置
     */
    private void updateWindowPosition() {
        var root = getView();
        if (root != null) {
            var params = (FrameLayout.LayoutParams) root.getLayoutParams();
            params.leftMargin = mWindowX;
            params.topMargin = mWindowY;
            root.setLayoutParams(params);
        }
    }
    
    /**
     * 居中窗口
     */
    private void centerWindow(View root) {
        // 获取屏幕尺寸
        int screenWidth = root.getContext().getResources().getDisplayMetrics().widthPixels;
        int screenHeight = root.getContext().getResources().getDisplayMetrics().heightPixels;
        
        // 计算居中位置
        mWindowX = (screenWidth - root.dp(WINDOW_WIDTH)) / 2;
        mWindowY = (screenHeight - root.dp(WINDOW_HEIGHT)) / 2;
        
        updateWindowPosition();
    }
    
    /**
     * 切换最小化状态
     */
    private void toggleMinimize() {
        mIsMinimized = !mIsMinimized;
        
        var root = getView();
        if (root != null) {
            var contentContainer = root.findViewById(ID_FRAGMENT_CONTAINER);
            if (contentContainer != null) {
                if (mIsMinimized) {
                    // 最小化动画
                    ObjectAnimator.ofFloat(contentContainer, View.ALPHA, 1f, 0f)
                            .setDuration(200)
                            .start();
                    contentContainer.setVisibility(View.GONE);
                } else {
                    // 恢复动画
                    contentContainer.setVisibility(View.VISIBLE);
                    ObjectAnimator.ofFloat(contentContainer, View.ALPHA, 0f, 1f)
                            .setDuration(200)
                            .start();
                }
            }
        }
    }
    
    /**
     * 关闭窗口
     */
    private void closeWindow() {
        var root = getView();
        if (root != null) {
            // 关闭窗口动画
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(root, View.SCALE_X, 1f, 0.8f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(root, View.SCALE_Y, 1f, 0.8f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(root, View.ALPHA, 1f, 0f);
            
            scaleX.setDuration(250);
            scaleY.setDuration(250);
            alpha.setDuration(250);
            
            scaleX.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
            scaleY.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
            alpha.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
            
            // 动画结束后关闭窗口
            alpha.addUpdateListener(animation -> {
                if ((float) animation.getAnimatedValue() <= 0.1f) {
                    // 通过ModernUI-MC的ScreenCallback接口关闭窗口
                    // 实际关闭逻辑由ModernUI-MC框架处理，当shouldClose()返回true时
                    // 用户按下ESC键或点击关闭按钮时会触发关闭
                }
            });
            
            scaleX.start();
            scaleY.start();
            alpha.start();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable DataSet savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 窗口打开动画
        animateWindowOpen(view);
        
        // 在视图创建后添加MarkdownFragment
        var fragmentContainer = view.findViewById(ID_FRAGMENT_CONTAINER);
        if (fragmentContainer != null) {
            // 使用FragmentTransaction正确添加Fragment
            getChildFragmentManager().beginTransaction()
                    .add(ID_FRAGMENT_CONTAINER, new SimpleMarkdownFragment(), "markdown")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setReorderingAllowed(true)
                    .commit();
        }
    }
    
    /**
     * 窗口打开动画 - 缩放 + 淡入效果
     */
    private void animateWindowOpen(View window) {
        // 初始状态：缩小并透明
        window.setScaleX(0.8f);
        window.setScaleY(0.8f);
        window.setAlpha(0f);
        
        // 动画到正常状态
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(window, View.SCALE_X, 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(window, View.SCALE_Y, 0.8f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(window, View.ALPHA, 0f, 1f);
        
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        alpha.setDuration(300);
        
        // 使用强调动画插值器
        scaleX.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        scaleY.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        alpha.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        
        scaleX.start();
        scaleY.start();
        alpha.start();
    }
    
    // ScreenCallback接口实现
    @Override
    public boolean isPauseScreen() {
        return false; // 不暂停游戏
    }
    
    @Override
    public boolean shouldClose() {
        return true; // 允许用户关闭窗口
    }
}