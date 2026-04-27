/*
 * MineClawd - 正确的HUD悬浮窗口渲染器
 * Copyright (C) 2024 MineClawd Team. All rights reserved.
 * 
 * 正确的悬浮窗口实现，基于ModernUI-MC的HUD渲染系统
 */

package com.mineclawd.foundation.client.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.Rect;
import icyllis.modernui.mc.MuiModApi;

/**
 * 正确的HUD悬浮窗口渲染器 - 不影响游戏操作
 * 基于ModernUI-MC的正确HUD渲染机制实现
 */
public class CorrectHudRenderer {
    
    private static CorrectHudRenderer instance;
    
    // 窗口状态
    private boolean mIsVisible = false;
    private int mWindowX = 100;
    private int mWindowY = 100;
    private int mWindowWidth = 400;
    private int mWindowHeight = 300;
    
    // 窗口交互状态
    private boolean mIsDragging = false;
    private int mDragStartX, mDragStartY;
    
    private CorrectHudRenderer() {
        // 私有构造函数
    }
    
    /**
     * 获取HUD渲染器实例
     */
    public static CorrectHudRenderer getInstance() {
        if (instance == null) {
            instance = new CorrectHudRenderer();
        }
        return instance;
    }
    
    /**
     * 渲染HUD窗口
     */
    public void renderHud(@NonNull Canvas canvas) {
        if (!mIsVisible) {
            return;
        }
        
        // 绘制窗口背景 - 圆角半透明背景
        Paint backgroundPaint = Paint.obtain();
        backgroundPaint.setColor(0xE0292A2C);  // 深色半透明背景
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // 绘制圆角矩形背景
        canvas.drawRoundRect(
            mWindowX, mWindowY, 
            mWindowX + mWindowWidth, mWindowY + mWindowHeight, 
            12, backgroundPaint
        );
        
        // 绘制边框
        Paint borderPaint = Paint.obtain();
        borderPaint.setColor(0x80343A40);  // 边框颜色
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);  // 2像素边框
        
        canvas.drawRoundRect(
            mWindowX + 1, mWindowY + 1, 
            mWindowX + mWindowWidth - 1, mWindowY + mWindowHeight - 1, 
            12, borderPaint
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
        Paint titleBgPaint = Paint.obtain();
        titleBgPaint.setColor(0x80343A40);  // 深色半透明背景
        titleBgPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawRect(
            mWindowX, mWindowY, 
            mWindowX + mWindowWidth, mWindowY + 40,  // 40像素高度
            titleBgPaint
        );
        
        // 标题文本（使用简单的矩形表示）
        Paint titleTextPaint = Paint.obtain();
        titleTextPaint.setColor(0xFFFFFFFF);  // 白色
        titleTextPaint.setStyle(Paint.Style.FILL);
        
        // 绘制标题文本区域（矩形表示）
        canvas.drawRect(
            mWindowX + 16, mWindowY + 15, 
            mWindowX + 200, mWindowY + 25, 
            titleTextPaint
        );
        
        // 回收Paint对象
        titleBgPaint.recycle();
        titleTextPaint.recycle();
    }
    
    /**
     * 绘制内容区域
     */
    private void drawContentArea(@NonNull Canvas canvas) {
        // 内容区域背景
        Paint contentBgPaint = Paint.obtain();
        contentBgPaint.setColor(0x40292A2C);  // 更浅的半透明背景
        contentBgPaint.setStyle(Paint.Style.FILL);
        
        canvas.drawRect(
            mWindowX, mWindowY + 40,  // 从标题栏下方开始
            mWindowX + mWindowWidth, mWindowY + mWindowHeight,
            contentBgPaint
        );
        
        // 绘制内容文本区域（矩形表示）
        Paint contentPaint = Paint.obtain();
        contentPaint.setColor(0xFFE0E0E0);  // 浅灰色
        contentPaint.setStyle(Paint.Style.FILL);
        
        // 绘制多行文本区域
        canvas.drawRect(mWindowX + 16, mWindowY + 60, mWindowX + mWindowWidth - 16, mWindowY + 75, contentPaint);
        canvas.drawRect(mWindowX + 16, mWindowY + 85, mWindowX + mWindowWidth - 16, mWindowY + 100, contentPaint);
        canvas.drawRect(mWindowX + 16, mWindowY + 110, mWindowX + mWindowWidth - 16, mWindowY + 125, contentPaint);
        
        // 回收Paint对象
        contentBgPaint.recycle();
        contentPaint.recycle();
    }
    
    /**
     * 处理鼠标事件
     */
    public boolean handleMouseEvent(int mouseX, int mouseY, int button, int action) {
        if (!mIsVisible) {
            return false; // 事件未处理，继续传递给游戏
        }
        
        // 检查是否在窗口范围内
        boolean inWindow = mouseX >= mWindowX && mouseX <= mWindowX + mWindowWidth &&
                          mouseY >= mWindowY && mouseY <= mWindowY + mWindowHeight;
        
        if (!inWindow) {
            return false; // 事件未处理，继续传递给游戏
        }
        
        // 检查是否在标题栏范围内（可拖动区域）
        boolean inTitleBar = mouseY <= mWindowY + 40;
        
        if (action == 1) { // 鼠标按下
            if (inTitleBar) {
                mIsDragging = true;
                mDragStartX = mouseX - mWindowX;
                mDragStartY = mouseY - mWindowY;
                return true; // 事件已处理
            }
        } else if (action == 0) { // 鼠标释放
            mIsDragging = false;
            return true; // 事件已处理
        } else if (action == 2 && mIsDragging) { // 鼠标拖动
            mWindowX = mouseX - mDragStartX;
            mWindowY = mouseY - mDragStartY;
            return true; // 事件已处理
        }
        
        return true; // 在窗口内的事件都处理，不传递给游戏
    }
    
    /**
     * 显示悬浮窗口
     */
    public void show() {
        mIsVisible = true;
    }
    
    /**
     * 隐藏悬浮窗口
     */
    public void hide() {
        mIsVisible = false;
        mIsDragging = false; // 重置拖动状态
    }
    
    /**
     * 切换悬浮窗口显示状态
     */
    public void toggle() {
        mIsVisible = !mIsVisible;
        if (!mIsVisible) {
            mIsDragging = false; // 隐藏时重置拖动状态
        }
    }
    
    /**
     * 获取窗口是否可见
     */
    public boolean isVisible() {
        return mIsVisible;
    }
    
    /**
     * 获取窗口是否正在拖动
     */
    public boolean isDragging() {
        return mIsDragging;
    }
}