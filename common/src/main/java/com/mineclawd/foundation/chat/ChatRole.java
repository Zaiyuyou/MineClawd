package com.mineclawd.foundation.chat;

/**
 * 聊天角色类型枚举
 * 主要用于前端显示，支持基本的对话角色
 */
public enum ChatRole {
    /**
     * 用户角色 - 玩家输入
     */
    USER(
        "👤",           // 头像
        "#E3F2FD",      // 气泡颜色（浅蓝色）
        Alignment.RIGHT, // 显示位置（右对齐）
        "用户"           // 角色名称
    ),
    
    /**
     * AI助手角色 - 主要AI响应
     */
    ASSISTANT(
        "🤖",           // 头像
        "#F3E5F5",      // 气泡颜色（浅紫色）
        Alignment.LEFT,  // 显示位置（左对齐）
        "AI助手"         // 角色名称
    ),
    
    /**
     * 工具调用角色 - 工具执行结果，用扳手表示
     */
    TOOL(
        "🔧",           // 头像（扳手）
        "#FFF3E0",      // 气泡颜色（浅橙色）
        Alignment.LEFT,  // 显示位置（左对齐）
        "工具"           // 角色名称
    );
    
    private final String avatar;
    private final String bubbleColor;
    private final Alignment alignment;
    private final String displayName;
    
    ChatRole(String avatar, String bubbleColor, Alignment alignment, String displayName) {
        this.avatar = avatar;
        this.bubbleColor = bubbleColor;
        this.alignment = alignment;
        this.displayName = displayName;
    }
    
    /**
     * 获取角色头像（emoji或文字，长度在5字符以内）
     */
    public String getAvatar() {
        return avatar;
    }
    
    /**
     * 获取气泡颜色（十六进制颜色代码）
     */
    public String getBubbleColor() {
        return bubbleColor;
    }
    
    /**
     * 获取显示位置
     */
    public Alignment getAlignment() {
        return alignment;
    }
    
    /**
     * 获取角色显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 根据字符串名称获取角色类型
     */
    public static ChatRole fromString(String name) {
        if (name == null) return USER;
        
        try {
            return ChatRole.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER; // 默认返回用户角色
        }
    }
    
    /**
     * 显示位置枚举
     */
    public enum Alignment {
        LEFT,   // 左对齐
        RIGHT,  // 右对齐
        CENTER  // 居中
    }
}