d:\MC\MechanoMunch\versions\XechanoMunch\mods\projects\MineClawd\common\src\main\java\com\mineclawd\tool\ToolRegistry.java
package com.mineclawd.tool;

import com.google.gson.JsonObject;
import net.minecraft.server.command.ServerCommandSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ToolRegistry {
    private static final ToolRegistry INSTANCE = new ToolRegistry();
    
    private final Map<String, ToolExecutor> tools = new ConcurrentHashMap<>();
    private final Map<String, ToolDefinition> definitions = new ConcurrentHashMap<>();
    
    private ToolRegistry() {}
    
    public static ToolRegistry getInstance() {
        return INSTANCE;
    }
    
    public void register(ToolExecutor executor) {
        tools.put(executor.getName(), executor);
        definitions.put(executor.getName(), new ToolDefinition(
            executor.getName(),
            executor.getDescription(),
            executor.getParameters()
        ));
    }
    
    public ToolExecutor getExecutor(String name) {
        return tools.get(name);
    }
    
    public ToolDefinition getDefinition(String name) {
        return definitions.get(name);
    }
    
    public boolean exists(String name) {
        return tools.containsKey(name);
    }
    
    public List<String> getAllToolNames() {
        return new ArrayList<>(tools.keySet());
    }
    
    public List<ToolDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }
}