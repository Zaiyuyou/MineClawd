/*
 * MineClawd - 基于ModernUI-MC的正确HUD悬浮窗口渲染器
 * Copyright (C) 2024 MineClawd Team. All rights reserved.
 * 
 * 基于ModernUI-MC的正确HUD系统实现真正的悬浮窗口
 */

package com.mineclawd.foundation.client.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.mc.UIManager;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;


/**
 * 基于ModernUI-MC的正确HUD悬浮窗口渲染器
 * 实现真正的悬浮窗口，不影响游戏操作
 * 继承自View，通过onDraw()方法自动获得Canvas
 */
public class TrueHudRenderer extends View {
    
    private static TrueHudRenderer INSTANCE = null;
    
    // 窗口状态
    private boolean mIsVisible = false;
    private boolean mIsDragging = false;
    
    // 窗口位置和尺寸
    private float mWindowX = 100f;
    private float mWindowY = 100f;
    private float mWindowWidth = 300f;
    private float mWindowHeight = 200f;
    
    // 标题栏高度
    private static final float TITLE_BAR_HEIGHT = 30f;
    
    // 关闭按钮尺寸
    private static final float CLOSE_BUTTON_SIZE = 20f;
    private static final float CLOSE_BUTTON_MARGIN = 5f;
    
    // 拖动相关
    private float mDragStartX;
    private float mDragStartY;
    private float mDragOffsetX;
    private float mDragOffsetY;
    
    public TrueHudRenderer(Context context) {
        super(context);
        // 设置窗口初始位置和尺寸
        setLayoutParams(new ViewGroup.LayoutParams((int)mWindowWidth, (int)mWindowHeight));
        
        // 设置View的初始位置和可见性
        setX(mWindowX);
        setY(mWindowY);
        setVisibility(View.VISIBLE);
    }
    
    // 移除无参构造函数，因为View类需要Context参数
    // private TrueHudRenderer() {
    //     // 单例模式 - 保留无参构造函数用于单例
    // }
    
    /**
     * 使用Context初始化单例实例
     */
    public static void initialize(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TrueHudRenderer(context);
        }
    }
    
    /**
     * 获取单例实例，需要先调用initialize()
     */
    public static TrueHudRenderer getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("TrueHudRenderer未初始化，请先调用initialize()方法");
        }
        return INSTANCE;
    }
    
    /**
     * 重写onMeasure方法，设置View的测量尺寸
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置View的测量尺寸
        setMeasuredDimension((int)mWindowWidth, (int)mWindowHeight);
    }
    
    /**
     * 重写onLayout方法，设置View的实际位置
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 设置View的实际位置和尺寸
        layout((int)mWindowX, (int)mWindowY, 
               (int)(mWindowX + mWindowWidth), (int)(mWindowY + mWindowHeight));
    }
    
    /**
     * 重写onDraw方法，Canvas由ModernUI-MC的View系统自动提供
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        if (!mIsVisible) {
            return;
        }
        
        // 绘制HUD窗口
        drawHudWindow(canvas);
    }
    
    /**
     * 设置窗口可见性
     */
    public void setVisible(boolean visible) {
        mIsVisible = visible;
        if (visible) {
            // 强制重新布局和重绘
            requestLayout();
            invalidate();
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }
    
    /**
     * 获取窗口可见性
     */
    public boolean isVisible() {
        return mIsVisible;
    }
    
    /**
     * 切换窗口可见性
     */
    public void toggleVisibility() {
        mIsVisible = !mIsVisible;
    }
    
    /**
     * 渲染HUD窗口 - 基于ModernUI-MC的正确渲染方法
     */
    public void renderHud(@NonNull Canvas canvas) {
        if (!mIsVisible) {
            return;
        }
        
        // 使用传入的Canvas进行绘制
        drawHudWindow(canvas);
    }
    
    /**
     * 绘制HUD窗口
     */
    private void drawHudWindow(@NonNull Canvas canvas) {
        // 绘制窗口背景 - 圆角半透明背景
        Paint backgroundPaint = Paint.obtain();
        backgroundPaint.setColor(0xE0292A2C);  // 深色半透明背景
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // 绘制圆角矩形背景
        canvas.drawRoundRect(
            mWindowX, mWindowY, 
            mWindowX + mWindowWidth, mWindowY + mWindowHeight, 
            12, 12,  // 12像素圆角
            backgroundPaint
        );
        
        // 绘制边框
        Paint borderPaint = Paint.obtain();
        borderPaint.setColor(0x80343A40);  // 边框颜色
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);  // 2像素边框
        
        canvas.drawRoundRect(
            mWindowX + 1, mWindowY + 1, 
            mWindowX + mWindowWidth - 1, mWindowY + mWindowHeight - 1, 
            12, 12, 
            borderPaint
        );
        
        // 绘制标题栏
        drawTitleBar(canvas);
        
        // 绘制内容区域
        drawContentArea(canvas);
        
        // 回收Paint对象
        backgroundPaint.recycle();
        borderPaint.recycle();
    }
    
    /**
     * 绘制标题栏
     */
    private void drawTitleBar(@NonNull Canvas canvas) {
        // 标题栏背景
        Paint titleBarPaint = Paint.obtain();
        titleBarPaint.setColor(0xFF1E1E1E);
        titleBarPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawRoundRect(
            mWindowX, mWindowY, 
            mWindowX + mWindowWidth, mWindowY + TITLE_BAR_HEIGHT, 
            12, 12, 
            titleBarPaint
        );
        
        // 标题栏分隔线
        Paint separatorPaint = Paint.obtain();
        separatorPaint.setColor(0x80343A40);
        separatorPaint.setStyle(Paint.Style.STROKE);
        separatorPaint.setStrokeWidth(1);
        
        canvas.drawLine(
            mWindowX, mWindowY + TITLE_BAR_HEIGHT, 
            mWindowX + mWindowWidth, mWindowY + TITLE_BAR_HEIGHT, 
            separatorPaint
        );
        
        // 绘制关闭按钮
        drawCloseButton(canvas);
        
        // 回收Paint对象
        titleBarPaint.recycle();
        separatorPaint.recycle();
    }
    
    /**
     * 绘制关闭按钮
     */
    private void drawCloseButton(@NonNull Canvas canvas) {
        float closeButtonX = mWindowX + mWindowWidth - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN;
        float closeButtonY = mWindowY + CLOSE_BUTTON_MARGIN;
        
        // 关闭按钮背景
        Paint closeBgPaint = Paint.obtain();
        closeBgPaint.setColor(0x80FF6B6B);  // 半透明红色
        closeBgPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawRoundRect(
            closeButtonX, closeButtonY, 
            closeButtonX + CLOSE_BUTTON_SIZE, closeButtonY + CLOSE_BUTTON_SIZE, 
            4, 4, 
            closeBgPaint
        );
        
        // 关闭按钮叉号
        Paint closeIconPaint = Paint.obtain();
        closeIconPaint.setColor(0xFFFFFFFF);
        closeIconPaint.setStyle(Paint.Style.STROKE);
        closeIconPaint.setStrokeWidth(2);
        closeIconPaint.setStrokeCap(Paint.Cap.ROUND);
        
        float iconMargin = 4;
        canvas.drawLine(
            closeButtonX + iconMargin, closeButtonY + iconMargin, 
            closeButtonX + CLOSE_BUTTON_SIZE - iconMargin, closeButtonY + CLOSE_BUTTON_SIZE - iconMargin, 
            closeIconPaint
        );
        
        canvas.drawLine(
            closeButtonX + CLOSE_BUTTON_SIZE - iconMargin, closeButtonY + iconMargin, 
            closeButtonX + iconMargin, closeButtonY + CLOSE_BUTTON_SIZE - iconMargin, 
            closeIconPaint
        );
        
        // 回收Paint对象
        closeBgPaint.recycle();
        closeIconPaint.recycle();
    }
    
    /**
     * 绘制内容区域
     */
    private void drawContentArea(@NonNull Canvas canvas) {
        // 内容区域背景
        Paint contentBgPaint = Paint.obtain();
        contentBgPaint.setColor(0xE0292A2C);
        contentBgPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawRect(
            mWindowX, mWindowY + TITLE_BAR_HEIGHT, 
            mWindowX + mWindowWidth, mWindowY + mWindowHeight, 
            contentBgPaint
        );
        
        // 绘制装饰元素代替文本（暂时解决文本绘制问题）
        Paint decorPaint = Paint.obtain();
        decorPaint.setColor(0x80FFFFFF);
        decorPaint.setStyle(Paint.Style.FILL);
        
        // 绘制装饰线
        canvas.drawRect(
            mWindowX + 10, mWindowY + 10, 
            mWindowX + 150, mWindowY + 15, 
            decorPaint
        );
        
        canvas.drawRect(
            mWindowX + 15, mWindowY + TITLE_BAR_HEIGHT + 30, 
            mWindowX + 250, mWindowY + TITLE_BAR_HEIGHT + 35, 
            decorPaint
        );
        
        // 回收Paint对象
        contentBgPaint.recycle();
        decorPaint.recycle();
    }
    
    /**
     * 处理鼠标事件
     */
    public boolean handleMouseEvent(float mouseX, float mouseY, int button, int action) {
        if (!mIsVisible) {
            return false;
        }
        
        // 检查是否在窗口区域内
        boolean inWindow = mouseX >= mWindowX && mouseX <= mWindowX + mWindowWidth &&
                          mouseY >= mWindowY && mouseY <= mWindowY + mWindowHeight;
        
        if (!inWindow) {
            mIsDragging = false;
            return false;
        }
        
        // 检查是否在标题栏区域
        boolean inTitleBar = mouseX >= mWindowX && mouseX <= mWindowX + mWindowWidth &&
                           mouseY >= mWindowY && mouseY <= mWindowY + TITLE_BAR_HEIGHT;
        
        // 检查是否在关闭按钮区域
        float closeButtonX = mWindowX + mWindowWidth - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN;
        float closeButtonY = mWindowY + CLOSE_BUTTON_MARGIN;
        boolean inCloseButton = mouseX >= closeButtonX && mouseX <= closeButtonX + CLOSE_BUTTON_SIZE &&
                              mouseY >= closeButtonY && mouseY <= closeButtonY + CLOSE_BUTTON_SIZE;
        
        if (action == 1) { // 鼠标按下
            if (inCloseButton) {
                // 点击关闭按钮
                mIsVisible = false;
                return true;
            } else if (inTitleBar) {
                // 开始拖动
                mIsDragging = true;
                mDragStartX = mouseX;
                mDragStartY = mouseY;
                mDragOffsetX = mouseX - mWindowX;
                mDragOffsetY = mouseY - mWindowY;
                return true;
            }
        } else if (action == 0) { // 鼠标释放
            mIsDragging = false;
            return inWindow;
        } else if (action == 2 && mIsDragging) { // 鼠标拖动
            // 更新窗口位置
            mWindowX = mouseX - mDragOffsetX;
            mWindowY = mouseY - mDragOffsetY;
            return true;
        }
        
        return inWindow;
    }
    
    /**
     * 获取窗口边界（用于事件处理）
     */
    public Rect getWindowBounds() {
        return new Rect(
            (int) mWindowX, (int) mWindowY, 
            (int) (mWindowX + mWindowWidth), (int) (mWindowY + mWindowHeight)
        );
    }
}