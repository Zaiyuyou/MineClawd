package com.mineclawd.buildin.files;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.buildin.kubejs.KubeJsToolExecutor;
import net.minecraft.server.command.ServerCommandSource;

/**
 * 文件操作工具的新架构包装器
 * 将文件操作相关的工具包装成 MineClawdTool 接口
 */
public class WorkspaceFileTool {
    
    /**
     * 列出文件工具
     */
    public static class ListFilesTool implements MineClawdTool {
        @Override
        public String getName() {
            return "list-files";
        }
        
        @Override
        public String getDescription() {
            return "List files and directories in the workspace. " +
                   "Use this to explore the file system. " +
                   "Optional parameters: path (string), recursive (boolean), limit (integer)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject path = new JsonObject();
            path.addProperty("type", "string");
            path.addProperty("description", "The directory path to list (optional, defaults to workspace root)");
            properties.add("path", path);
            
            JsonObject recursive = new JsonObject();
            recursive.addProperty("type", "boolean");
            recursive.addProperty("description", "Whether to list recursively (optional, defaults to false)");
            properties.add("recursive", recursive);
            
            JsonObject limit = new JsonObject();
            limit.addProperty("type", "integer");
            limit.addProperty("description", "Maximum number of entries to return (optional, defaults to 120)");
            properties.add("limit", limit);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String path = args.has("path") ? args.get("path").getAsString() : null;
            Boolean recursive = args.has("recursive") ? args.get("recursive").getAsBoolean() : null;
            Integer limit = args.has("limit") ? args.get("limit").getAsInt() : null;
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.listFiles(source, path, recursive, limit);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Files";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
    
    /**
     * 读取文件工具
     */
    public static class ReadFilesTool implements MineClawdTool {
        @Override
        public String getName() {
            return "read-files";
        }
        
        @Override
        public String getDescription() {
            return "Read the contents of text files. " +
                   "Use this to read configuration files, scripts, or documentation. " +
                   "Required: path (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject path = new JsonObject();
            path.addProperty("type", "string");
            path.addProperty("description", "The file path to read");
            properties.add("path", path);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("path");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String path = args.has("path") ? args.get("path").getAsString() : null;
            if (path == null || path.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: path");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.readFile(source, path);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Files";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
    
    /**
     * 写入文件工具
     */
    public static class WriteFilesTool implements MineClawdTool {
        @Override
        public String getName() {
            return "write-files";
        }
        
        @Override
        public String getDescription() {
            return "Write content to files. " +
                   "Use this to create or modify files. " +
                   "Required: path (string), content (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject path = new JsonObject();
            path.addProperty("type", "string");
            path.addProperty("description", "The file path to write");
            properties.add("path", path);
            
            JsonObject content = new JsonObject();
            content.addProperty("type", "string");
            content.addProperty("description", "The content to write to the file");
            properties.add("content", content);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("path");
            required.add("content");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String path = args.has("path") ? args.get("path").getAsString() : null;
            String content = args.has("content") ? args.get("content").getAsString() : null;
            
            if (path == null || path.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: path");
            }
            if (content == null || content.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: content");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.writeFile(source, path, content);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Files";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
    
    /**
     * 复制文件工具
     */
    public static class CopyFilesTool implements MineClawdTool {
        @Override
        public String getName() {
            return "copy-files";
        }
        
        @Override
        public String getDescription() {
            return "Copy files or directories. " +
                   "Use this to duplicate files. " +
                   "Required: source (string), destination (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject source = new JsonObject();
            source.addProperty("type", "string");
            source.addProperty("description", "The source file or directory path");
            properties.add("source", source);
            
            JsonObject destination = new JsonObject();
            destination.addProperty("type", "string");
            destination.addProperty("description", "The destination path");
            properties.add("destination", destination);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("source");
            required.add("destination");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String src = args.has("source") ? args.get("source").getAsString() : null;
            String dest = args.has("destination") ? args.get("destination").getAsString() : null;
            
            if (src == null || src.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: source");
            }
            if (dest == null || dest.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: destination");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.moveFiles(source, src, dest);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Files";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
    
    /**
     * 移动文件工具
     */
    public static class MoveFilesTool implements MineClawdTool {
        @Override
        public String getName() {
            return "move-files";
        }
        
        @Override
        public String getDescription() {
            return "Move or rename files or directories. " +
                   "Use this to reorganize files. " +
                   "Required: source (string), destination (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject source = new JsonObject();
            source.addProperty("type", "string");
            source.addProperty("description", "The source file or directory path");
            properties.add("source", source);
            
            JsonObject destination = new JsonObject();
            destination.addProperty("type", "string");
            destination.addProperty("description", "The destination path");
            properties.add("destination", destination);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("source");
            required.add("destination");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String src = args.has("source") ? args.get("source").getAsString() : null;
            String dest = args.has("destination") ? args.get("destination").getAsString() : null;
            
            if (src == null || src.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: source");
            }
            if (dest == null || dest.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: destination");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.moveFiles(source, src, dest);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Files";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
    
    /**
     * 文本搜索工具 (grep)
     */
    public static class GrepTool implements MineClawdTool {
        @Override
        public String getName() {
            return "grep";
        }
        
        @Override
        public String getDescription() {
            return "Search for text patterns in files. " +
                   "Use this to find specific content in files. " +
                   "Required: pattern (string), path (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject pattern = new JsonObject();
            pattern.addProperty("type", "string");
            pattern.addProperty("description", "The regex pattern to search for");
            properties.add("pattern", pattern);
            
            JsonObject path = new JsonObject();
            path.addProperty("type", "string");
            path.addProperty("description", "The file or directory path to search in");
            properties.add("path", path);
            
            JsonObject recursive = new JsonObject();
            recursive.addProperty("type", "boolean");
            recursive.addProperty("description", "Whether to search recursively (optional, defaults to false)");
            properties.add("recursive", recursive);
            
            JsonObject caseSensitive = new JsonObject();
            caseSensitive.addProperty("type", "boolean");
            caseSensitive.addProperty("description", "Whether the search is case sensitive (optional, defaults to false)");
            properties.add("caseSensitive", caseSensitive);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("pattern");
            required.add("path");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String pattern = args.has("pattern") ? args.get("pattern").getAsString() : null;
            String path = args.has("path") ? args.get("path").getAsString() : null;
            Boolean recursive = args.has("recursive") ? args.get("recursive").getAsBoolean() : null;
            Boolean caseSensitive = args.has("caseSensitive") ? args.get("caseSensitive").getAsBoolean() : null;
            
            if (pattern == null || pattern.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: pattern");
            }
            if (path == null || path.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: path");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.grepFiles(source, pattern, path, null, caseSensitive, null);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Files";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
    
    /**
     * HTTP请求工具 (curl)
     */
    public static class CurlTool implements MineClawdTool {
        @Override
        public String getName() {
            return "curl";
        }
        
        @Override
        public String getDescription() {
            return "Perform an HTTP request and return status/body. " +
                   "Use this to make HTTP requests to APIs or web services. " +
                   "Required: url (string); Optional: method (string), body (string), headers (object), timeout_seconds (integer)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject url = new JsonObject();
            url.addProperty("type", "string");
            url.addProperty("description", "HTTP(S) URL to request");
            properties.add("url", url);
            
            JsonObject method = new JsonObject();
            method.addProperty("type", "string");
            method.addProperty("description", "Optional HTTP method: GET, POST, PUT, PATCH, DELETE, HEAD");
            properties.add("method", method);
            
            JsonObject body = new JsonObject();
            body.addProperty("type", "string");
            body.addProperty("description", "Optional request body");
            properties.add("body", body);
            
            JsonObject headers = new JsonObject();
            headers.addProperty("type", "object");
            headers.addProperty("description", "Optional request headers");
            properties.add("headers", headers);
            
            JsonObject timeoutSeconds = new JsonObject();
            timeoutSeconds.addProperty("type", "integer");
            timeoutSeconds.addProperty("description", "Optional timeout in seconds");
            properties.add("timeout_seconds", timeoutSeconds);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("url");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String url = args.has("url") ? args.get("url").getAsString() : null;
            String method = args.has("method") ? args.get("method").getAsString() : null;
            String body = args.has("body") ? args.get("body").getAsString() : null;
            
            // 处理headers参数
            java.util.Map<String, String> headers = null;
            if (args.has("headers")) {
                JsonObject headersObj = args.get("headers").getAsJsonObject();
                headers = new java.util.HashMap<>();
                for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : headersObj.entrySet()) {
                    headers.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            
            Integer timeoutSeconds = args.has("timeout_seconds") ? args.get("timeout_seconds").getAsInt() : null;
            
            if (url == null || url.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: url");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = WorkspaceFileToolExecutor.curl(url, method, body, headers, timeoutSeconds);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Network";
        }

        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return RevealPolicy.active(RevealLayer.LAYER_2); // 主动揭露，层级2
        }
    }
}