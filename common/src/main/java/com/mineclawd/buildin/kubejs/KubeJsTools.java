package com.mineclawd.buildin.kubejs;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.buildin.kubejs.KubeJsToolExecutor;
import net.minecraft.server.command.ServerCommandSource;

/**
 * KubeJS工具的新架构包装器
 * 将KubeJS相关的工具包装成 MineClawdTool 接口
 */
public class KubeJsTools {
    
    /**
     * 执行命令工具
     */
    public static class ExecuteCommandTool implements MineClawdTool {
        @Override
        public String getName() {
            return "execute-command";
        }
        
        @Override
        public String getDescription() {
            return "Execute a Minecraft command. " +
                   "Use this to perform in-game actions. " +
                   "Requires: command (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject command = new JsonObject();
            command.addProperty("type", "string");
            command.addProperty("description", "The Minecraft command to execute");
            properties.add("command", command);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("command");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public com.mineclawd.foundation.tool.ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String command = args.has("command") ? args.get("command").getAsString() : null;
            if (command == null || command.isBlank()) {
                return com.mineclawd.foundation.tool.ToolExecutionResult.failure("Missing required parameter: command");
            }
            KubeJsToolExecutor.ToolExecutionResult result = KubeJsToolExecutor.executeCommand(source, command);
            return com.mineclawd.foundation.tool.ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "KubeJS";
        }
    }
    
    /**
     * 执行即时服务器脚本工具
     */
    public static class ApplyInstantServerScriptTool implements MineClawdTool {
        @Override
        public String getName() {
            return "apply-instant-server-script";
        }
        
        @Override
        public String getDescription() {
            return "Apply a KubeJS server script immediately. " +
                   "Use this to run KubeJS code on the server. " +
                   "Requires: code (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject code = new JsonObject();
            code.addProperty("type", "string");
            code.addProperty("description", "The KubeJS server script code to execute");
            properties.add("code", code);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("code");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public com.mineclawd.foundation.tool.ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String code = args.has("code") ? args.get("code").getAsString() : null;
            if (code == null || code.isBlank()) {
                return com.mineclawd.foundation.tool.ToolExecutionResult.failure("Missing required parameter: code");
            }
            // 使用KubeJsToolExecutor的即时脚本执行方法
            KubeJsToolExecutor.ToolExecutionResult result = KubeJsToolExecutor.executeInstant(source, code);
            return com.mineclawd.foundation.tool.ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "KubeJS";
        }
    }
    
    /**
     * 列出服务器脚本工具
     */
    public static class ListServerScriptsTool implements MineClawdTool {
        @Override
        public String getName() {
            return "list-server-scripts";
        }
        
        @Override
        public String getDescription() {
            return "List available KubeJS server scripts. " +
                   "Use this to see what scripts are available. " +
                   "No parameters required";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public com.mineclawd.foundation.tool.ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            KubeJsToolExecutor.ToolExecutionResult result = KubeJsToolExecutor.listServerScripts(source);
            return com.mineclawd.foundation.tool.ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "KubeJS";
        }
    }
    
    /**
     * 询问用户问题工具（异步工具）
     */
    public static class AskUserQuestionTool implements MineClawdTool {
        @Override
        public String getName() {
            return "ask-user-question";
        }
        
        @Override
        public String getDescription() {
            return "Ask the player a targeted question when details are ambiguous. " +
                   "Provide a concise question and up to 5 preset options. " +
                   "Required: question (string); Optional: options (array of strings)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject question = new JsonObject();
            question.addProperty("type", "string");
            question.addProperty("description", "Question to ask the player");
            properties.add("question", question);
            
            JsonObject options = new JsonObject();
            options.addProperty("type", "array");
            options.addProperty("description", "Optional preset options (up to 5)");
            JsonObject items = new JsonObject();
            items.addProperty("type", "string");
            options.add("items", items);
            properties.add("options", options);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("question");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public com.mineclawd.foundation.tool.ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String question = args.has("question") ? args.get("question").getAsString() : null;
            
            if (question == null || question.isBlank()) {
                return com.mineclawd.foundation.tool.ToolExecutionResult.failure("Missing required parameter: question");
            }
            
            JsonArray optionsArray = args.has("options") ? args.get("options").getAsJsonArray() : null;
            java.util.List<String> options = null;
            if (optionsArray != null) {
                options = new java.util.ArrayList<>();
                for (com.google.gson.JsonElement element : optionsArray) {
                    options.add(element.getAsString());
                }
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = KubeJsToolExecutor.askUserQuestion(source, question, options);
            return com.mineclawd.foundation.tool.ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return true;
        }
        
        @Override
        public String getPromptCategory() {
            return "Interaction";
        }
    }
    
    /**
     * 游戏重载工具
     */
    public static class ReloadGameTool implements MineClawdTool {
        @Override
        public String getName() {
            return "reload-game";
        }
        
        @Override
        public String getDescription() {
            return "Run /reload command and return KubeJS loading errors. " +
                   "Use this to reload game configurations, scripts, and data packs. " +
                   "No parameters required";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            params.add("properties", new JsonObject());
            return params;
        }
        
        @Override
        public com.mineclawd.foundation.tool.ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            KubeJsToolExecutor.ToolExecutionResult result = KubeJsToolExecutor.reloadGame(source);
            return com.mineclawd.foundation.tool.ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Game Management";
        }
    }
    
    /**
     * 命令树同步工具
     */
    public static class SyncCommandTreeTool implements MineClawdTool {
        @Override
        public String getName() {
            return "sync-command-tree";
        }
        
        @Override
        public String getDescription() {
            return "Push refreshed command suggestions to online players. " +
                   "Call ONLY when command registrations changed AND reload already succeeded. " +
                   "No parameters required";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            params.add("properties", new JsonObject());
            return params;
        }
        
        @Override
        public com.mineclawd.foundation.tool.ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            KubeJsToolExecutor.ToolExecutionResult result = KubeJsToolExecutor.syncCommandTree(source);
            return com.mineclawd.foundation.tool.ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Game Management";
        }
    }
}