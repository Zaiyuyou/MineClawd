package com.mineclawd.tool;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件系统工具类
 * 管理文件操作相关的工具定义
 */
public class FileSystemTools implements ToolProvider {
    
    @Override
    public List<ToolDefinition> getTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        // 文件系统工具
        tools.add(ToolDefinition.of(
            "list-files",
            "List files/directories under the server root. Supports optional path, recursive, and limit.",
            createListFilesParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "read-files",
            "Read a UTF-8 text file under the server root.",
            createPathParameters("Relative file path under the server root, for example `logs/latest.log` or `world/serverconfig/mod.json`.")
        ));
        
        tools.add(ToolDefinition.of(
            "write-files",
            "Write or overwrite a UTF-8 file under the server root.",
            createWriteParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "copy-files",
            "Copy a file or directory under the server root.",
            createCopyMoveParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "move-files",
            "Move or rename a file or directory under the server root.",
            createCopyMoveParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "grep",
            "Search file contents using regex under the server root.",
            createGrepParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "read-image",
            "Read and describe an image file under the server root using the configured vision model.",
            createReadImageParameters()
        ));
        
        return tools;
    }
    
    // 参数创建方法
    private JsonObject createListFilesParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject path = new JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "Optional directory path. Defaults to server root.");
        properties.add("path", path);
        
        JsonObject recursive = new JsonObject();
        recursive.addProperty("type", "boolean");
        recursive.addProperty("description", "Whether to list recursively. Defaults to false.");
        properties.add("recursive", recursive);
        
        JsonObject limit = new JsonObject();
        limit.addProperty("type", "integer");
        limit.addProperty("description", "Maximum number of files to list. Defaults to 100.");
        limit.addProperty("minimum", 1);
        limit.addProperty("maximum", 1000);
        properties.add("limit", limit);
        
        parameters.add("properties", properties);
        parameters.add("required", new com.google.gson.JsonArray());
        
        return parameters;
    }
    
    private JsonObject createPathParameters(String description) {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject path = new JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", description);
        properties.add("path", path);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("path");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createWriteParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject path = new JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "File path to write to.");
        properties.add("path", path);
        
        JsonObject content = new JsonObject();
        content.addProperty("type", "string");
        content.addProperty("description", "File content to write.");
        properties.add("content", content);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("path");
        required.add("content");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createCopyMoveParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject source = new JsonObject();
        source.addProperty("type", "string");
        source.addProperty("description", "Source file or directory path.");
        properties.add("source", source);
        
        JsonObject destination = new JsonObject();
        destination.addProperty("type", "string");
        destination.addProperty("description", "Destination file or directory path.");
        properties.add("destination", destination);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("source");
        required.add("destination");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createGrepParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject pattern = new JsonObject();
        pattern.addProperty("type", "string");
        pattern.addProperty("description", "Regex pattern to search for.");
        properties.add("pattern", pattern);
        
        JsonObject path = new JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "File or directory path to search in.");
        properties.add("path", path);
        
        JsonObject recursive = new JsonObject();
        recursive.addProperty("type", "boolean");
        recursive.addProperty("description", "Whether to search recursively. Defaults to false.");
        properties.add("recursive", recursive);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("pattern");
        required.add("path");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createReadImageParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject path = new JsonObject();
        path.addProperty("type", "string");
        path.addProperty("description", "Image file path to read and describe.");
        properties.add("path", path);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("path");
        parameters.add("required", required);
        
        return parameters;
    }
}