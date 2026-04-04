package com.mineclawd.buildin.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.buildin.kubejs.KubeJsToolExecutor;
import net.minecraft.server.command.ServerCommandSource;

/**
 * 网络工具的新架构包装器
 * 将网络相关的工具包装成 MineClawdTool 接口
 */
public class WebTools {
    
    /**
     * 网络搜索工具
     */
    public static class SearchTool implements MineClawdTool {
        @Override
        public String getName() {
            return "search";
        }
        
        @Override
        public String getDescription() {
            return "Search the web via Tavily and return concise snippets with URLs. " +
                   "Use this when external references are needed beyond installed-mod docs. " +
                   "Required: query (string); Optional: max_results (integer)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject query = new JsonObject();
            query.addProperty("type", "string");
            query.addProperty("description", "Web search query");
            properties.add("query", query);
            
            JsonObject maxResults = new JsonObject();
            maxResults.addProperty("type", "integer");
            maxResults.addProperty("description", "Maximum number of results (optional, defaults to 5)");
            properties.add("max_results", maxResults);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("query");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String query = args.has("query") ? args.get("query").getAsString() : null;
            Integer maxResults = args.has("max_results") ? args.get("max_results").getAsInt() : null;
            
            if (query == null || query.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: query");
            }
            
            // 需要从配置中获取Tavily API Key
            // 这里暂时使用null，实际执行时会检查配置
            KubeJsToolExecutor.ToolExecutionResult result = SearchToolExecutor.searchWeb(null, query, maxResults);
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
}