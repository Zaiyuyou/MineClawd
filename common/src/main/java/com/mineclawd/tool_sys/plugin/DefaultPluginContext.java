package com.mineclawd.tool_sys.plugin;

import net.minecraft.server.command.ServerCommandSource;

/**
 * 默认插件上下文实现
 */
public class DefaultPluginContext implements PluginContext {
    
    private final ServerCommandSource source;
    private final PluginManager pluginManager;
    private final PluginConfig config;
    
    public DefaultPluginContext(ServerCommandSource source, PluginManager pluginManager, PluginConfig config) {
        this.source = source;
        this.pluginManager = pluginManager;
        this.config = config;
    }
    
    @Override
    public ServerCommandSource getSource() {
        return source;
    }
    
    @Override
    public PluginConfig getConfig() {
        return config;
    }
    
    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
    @Override
    public void logInfo(String message) {
        System.out.println("[Plugin] " + message);
    }
    
    @Override
    public void logWarning(String message) {
        System.out.println("[Plugin] WARN: " + message);
    }
    
    @Override
    public void logError(String message) {
        System.err.println("[Plugin] ERROR: " + message);
    }
}