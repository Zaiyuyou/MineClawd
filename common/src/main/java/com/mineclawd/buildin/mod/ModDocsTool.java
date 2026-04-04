package com.mineclawd.buildin.mod;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.buildin.kubejs.KubeJsToolExecutor;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Mod文档工具的新架构包装器
 * 将Mod文档相关的工具包装成 MineClawdTool 接口
 */
public class ModDocsTool {
    
    /**
     * URL获取工具
     */
    public static class FetchUrlTool implements MineClawdTool {
        @Override
        public String getName() {
            return "fetch_url";
        }
        
        @Override
        public String getDescription() {
            return "Fetch any HTTP(S) page; HTML is returned as Markdown. " +
                   "Use for command usage, config keys, API details, or mod documentation. " +
                   "Required: url (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject url = new JsonObject();
            url.addProperty("type", "string");
            url.addProperty("description", "HTTP(S) URL to fetch. HTML will be converted to Markdown.");
            properties.add("url", url);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("url");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String url = args.has("url") ? args.get("url").getAsString() : null;
            
            if (url == null || url.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: url");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = ModDocsToolExecutor.fetchUrl(url);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Web";
        }
    }
    
    /**
     * Modrinth项目获取工具
     */
    public static class FetchModrinthTool implements MineClawdTool {
        @Override
        public String getName() {
            return "fetch_modrinth";
        }
        
        @Override
        public String getDescription() {
            return "Fetch the Modrinth project page for an installed mod id. " +
                   "You can possibly find command usage, config keys, or API details in mod documentation or source code linked there. " +
                   "Required: mod_id (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject modId = new JsonObject();
            modId.addProperty("type", "string");
            modId.addProperty("description", "Mod ID to fetch Modrinth project page for");
            properties.add("mod_id", modId);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("mod_id");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String modId = args.has("mod_id") ? args.get("mod_id").getAsString() : null;
            
            if (modId == null || modId.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: mod_id");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = ModDocsToolExecutor.fetchModrinth(modId);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Mods";
        }
    }
    
    /**
     * 命令列表工具
     */
    public static class ListCommandsTool implements MineClawdTool {
        @Override
        public String getName() {
            return "list_commands";
        }
        
        @Override
        public String getDescription() {
            return "List available root commands, optionally filtered by mod_id. " +
                   "Filtered matching is best-effort based on command names and prefixes. " +
                   "Optional: mod_id (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject modId = new JsonObject();
            modId.addProperty("type", "string");
            modId.addProperty("description", "Optional mod ID to filter commands by");
            properties.add("mod_id", modId);
            
            params.add("properties", properties);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String modId = args.has("mod_id") ? args.get("mod_id").getAsString() : null;
            
            KubeJsToolExecutor.ToolExecutionResult result = ModDocsToolExecutor.listCommands(source, modId);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Commands";
        }
    }
}