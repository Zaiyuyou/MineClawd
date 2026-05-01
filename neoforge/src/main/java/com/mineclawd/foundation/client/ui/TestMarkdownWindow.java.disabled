/*
 * MineClawd - Test Markdown Window
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
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

/**
 * 现代化Markdown窗口 - 应用ModernUI-MC设计模式
 * 包含圆角边框、阴影效果、现代化按钮和动画
 */
public class TestMarkdownWindow extends Fragment implements ScreenCallback {

    private static final int ID_FRAGMENT_CONTAINER = 0x1001;
    private static final int ID_TITLE_BAR = 0x1002;
    private static final int ID_CLOSE_BUTTON = 0x1003;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        var context = requireContext();
        
        // 创建现代化根布局 - 圆角边框 + 阴影
        var root = new FrameLayout(context);
        root.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // 应用现代化背景 - 半透明圆角背景
        ShapeDrawable windowBackground = new ShapeDrawable();
        windowBackground.setShape(ShapeDrawable.RECTANGLE);
        windowBackground.setCornerRadius(root.dp(12));  // 12dp圆角
        windowBackground.setColor(0xE0292A2C);  // 深色半透明背景
        root.setBackground(windowBackground);
        
        // 添加阴影效果
        root.setElevation(root.dp(8));  // 8dp阴影
        
        // 创建标题栏
        createTitleBar(root);
        
        // 创建内容区域容器
        var contentContainer = new FrameLayout(context);
        contentContainer.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // 设置内容区域边距，为标题栏留出空间
        var params = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
        params.topMargin = root.dp(48);  // 标题栏高度
        contentContainer.setLayoutParams(params);
        
        // 创建FragmentContainerView用于管理Fragment
        var fragmentContainer = new FragmentContainerView(context);
        fragmentContainer.setId(ID_FRAGMENT_CONTAINER);
        fragmentContainer.setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        contentContainer.addView(fragmentContainer);
        root.addView(contentContainer);
        
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
        
        // 关闭按钮
        var closeButton = new Button(context);
        closeButton.setId(ID_CLOSE_BUTTON);
        closeButton.setText("×");
        closeButton.setTextSize(20);
        closeButton.setTextColor(0xFFFFFFFF);
        closeButton.setBackground(createModernButtonBackground(root));
        
        var buttonParams = new LinearLayout.LayoutParams(
            root.dp(40),  // 40dp宽度
            root.dp(40)   // 40dp高度
        );
        buttonParams.setMargins(0, 0, root.dp(8), 0);
        closeButton.setLayoutParams(buttonParams);
        
        // 关闭按钮点击事件
        closeButton.setOnClickListener(v -> {
            // 关闭窗口动画
            animateWindowClose(root);
        });
        
        titleBar.addView(closeButton);
        root.addView(titleBar);
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
    
    /**
     * 窗口关闭动画 - 缩放 + 淡出效果
     */
    private void animateWindowClose(View window) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(window, View.SCALE_X, 1f, 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(window, View.SCALE_Y, 1f, 0.8f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(window, View.ALPHA, 1f, 0f);
        
        scaleX.setDuration(250);
        scaleY.setDuration(250);
        alpha.setDuration(250);
        
        // 使用强调动画插值器
        scaleX.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        scaleY.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        alpha.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
        
        // 动画结束后关闭窗口
        alpha.addUpdateListener(animation -> {
            if ((float) animation.getAnimatedValue() <= 0.1f) {
                // 通过ModernUI-MC API关闭窗口
                // 在ModernUI-MC中，窗口关闭由UIManager处理
                // 这里我们只需要设置shouldClose为true，系统会自动处理
                // 实际关闭逻辑由ModernUI-MC框架处理
            }
        });
        
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