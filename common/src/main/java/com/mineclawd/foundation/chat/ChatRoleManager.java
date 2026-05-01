package com.mineclawd.foundation.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 角色管理器
 * 管理前端显示所需的角色配置
 */
public class ChatRoleManager {
    private static final ChatRoleManager INSTANCE = new ChatRoleManager();
    
    private final Map<String, ChatRoleConfig> roleConfigs;
    
    private ChatRoleManager() {
        this.roleConfigs = new ConcurrentHashMap<>();
        initializeDefaultRoles();
    }
    
    public static ChatRoleManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化默认角色配置
     */
    private void initializeDefaultRoles() {
        // 仅保留必要的角色类型
        registerRole(new ChatRoleConfig("user", ChatRole.USER));
        registerRole(new ChatRoleConfig("assistant", ChatRole.ASSISTANT));
        registerRole(new ChatRoleConfig("tool", ChatRole.TOOL));
    }
    
    /**
     * 注册角色配置
     */
    public void registerRole(ChatRoleConfig roleConfig) {
        if (roleConfig != null && roleConfig.getRoleId() != null) {
            roleConfigs.put(roleConfig.getRoleId(), roleConfig);
        }
    }
    
    /**
     * 获取角色配置
     */
    public ChatRoleConfig getRoleConfig(String roleId) {
        if (roleId == null) {
            return roleConfigs.get("user"); // 默认用户角色
        }
        
        ChatRoleConfig config = roleConfigs.get(roleId);
        if (config == null) {
            // 如果找不到指定角色，尝试根据角色类型获取
            try {
                ChatRole role = ChatRole.fromString(roleId);
                config = roleConfigs.get(role.name().toLowerCase());
            } catch (Exception e) {
                // 使用默认用户角色
                config = roleConfigs.get("user");
            }
        }
        
        return config;
    }
    
    /**
     * 获取所有已注册的角色
     */
    public Map<String, ChatRoleConfig> getAllRoles() {
        return new HashMap<>(roleConfigs);
    }
    
    /**
     * 检查角色是否存在
     */
    public boolean hasRole(String roleId) {
        return roleConfigs.containsKey(roleId);
    }
    
    /**
     * 移除角色配置
     */
    public void removeRole(String roleId) {
        roleConfigs.remove(roleId);
    }
    
    /**
     * 清空所有角色（保留预定义角色）
     */
    public void clearCustomRoles() {
        // 保留基本的预定义角色
        roleConfigs.entrySet().removeIf(entry -> 
            !entry.getKey().equals("user") && 
            !entry.getKey().equals("assistant") && 
            !entry.getKey().equals("tool")
        );
    }
}