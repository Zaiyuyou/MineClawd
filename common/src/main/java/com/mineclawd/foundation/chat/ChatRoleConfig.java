package com.mineclawd.foundation.chat;

/**
 * 可配置的角色类
 * 主要用于前端显示，支持基本的角色配置
 */
public class ChatRoleConfig {
    private final String roleId;
    private final ChatRole baseRole;
    private final String avatar;
    private final String bubbleColor;
    private final ChatRole.Alignment alignment;
    private final String displayName;
    private final String indentPattern; // 缩进模式（支持制表符和换行符）
    
    /**
     * 创建角色配置
     */
    public ChatRoleConfig(String roleId, ChatRole baseRole, String avatar, String bubbleColor, 
                         ChatRole.Alignment alignment, String displayName, String indentPattern) {
        this.roleId = roleId;
        this.baseRole = baseRole;
        this.avatar = avatar;
        this.bubbleColor = bubbleColor;
        this.alignment = alignment;
        this.displayName = displayName;
        this.indentPattern = indentPattern;
    }
    
    /**
     * 基于预定义角色创建配置
     */
    public ChatRoleConfig(String roleId, ChatRole baseRole) {
        this(roleId, baseRole, baseRole.getAvatar(), baseRole.getBubbleColor(), 
             baseRole.getAlignment(), baseRole.getDisplayName(), "");
    }
    
    // Getter方法
    public String getRoleId() { return roleId; }
    public ChatRole getBaseRole() { return baseRole; }
    public String getAvatar() { return avatar; }
    public String getBubbleColor() { return bubbleColor; }
    public ChatRole.Alignment getAlignment() { return alignment; }
    public String getDisplayName() { return displayName; }
    public String getIndentPattern() { return indentPattern; }
    
    /**
     * 应用缩进模式到文本
     */
    public String applyIndent(String text) {
        if (indentPattern == null || indentPattern.isEmpty()) {
            return text;
        }
        
        // 处理制表符和换行符缩进
        String[] lines = text.split("\n", -1);
        StringBuilder indented = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                indented.append("\n");
            }
            indented.append(indentPattern).append(lines[i]);
        }
        
        return indented.toString();
    }
    
    /**
     * 检查是否为工具角色
     */
    public boolean isTool() {
        return baseRole == ChatRole.TOOL;
    }
    
    @Override
    public String toString() {
        return "ChatRoleConfig{" +
                "roleId='" + roleId + '\'' +
                ", baseRole=" + baseRole +
                ", avatar='" + avatar + '\'' +
                ", bubbleColor='" + bubbleColor + '\'' +
                ", alignment=" + alignment +
                ", displayName='" + displayName + '\'' +
                ", indentPattern='" + indentPattern + '\'' +
                '}';
    }
}