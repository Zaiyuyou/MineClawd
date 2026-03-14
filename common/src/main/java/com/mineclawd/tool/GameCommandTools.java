package com.mineclawd.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏命令工具类
 * 管理Minecraft命令相关的工具定义
 */
public class GameCommandTools implements ToolProvider {
    
    @Override
    public List<ToolDefinition> getTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        // 游戏命令工具
        tools.add(ToolDefinition.of(
            "execute-command",
            "Execute a Minecraft command and return command output/result.",
            createCommandParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "list_commands",
            "List available root commands. Optionally filter with mod_id (best-effort by command name/prefix).",
            createListCommandsParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "reload-game",
            "Run /reload and return command output plus detected KubeJS load errors.",
            createNoArgParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "sync-command-tree",
            "Refresh command tree/tab-completion for all online players. Use only after command registration changes.",
            createNoArgParameters()
        ));
        
        return tools;
    }
    
    // 参数创建方法
    private JsonObject createNoArgParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new JsonObject());
        parameters.add("required", new com.google.gson.JsonArray());
        return parameters;
    }
    
    private JsonObject createCommandParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject command = new JsonObject();
        command.addProperty("type", "string");
        command.addProperty("description", "Minecraft command to run, with or without leading slash.");
        properties.add("command", command);
        
        parameters.add("properties", properties);
        
        // 正确的required字段格式应该是JSON数组
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("command");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createListCommandsParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject modId = new JsonObject();
        modId.addProperty("type", "string");
        modId.addProperty("description", "Optional installed mod id filter, for example `kubejs`.");
        properties.add("mod_id", modId);
        
        parameters.add("properties", properties);
        parameters.add("required", new com.google.gson.JsonArray());
        
        return parameters;
    }
}