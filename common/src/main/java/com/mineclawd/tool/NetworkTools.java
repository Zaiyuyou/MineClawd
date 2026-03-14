package com.mineclawd.tool;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络工具类
 * 管理网络请求相关的工具定义
 */
public class NetworkTools implements ToolProvider {
    
    @Override
    public List<ToolDefinition> getTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        // 网络工具
        tools.add(ToolDefinition.of(
            "fetch_url",
            "Fetch any HTTP(S) URL and return readable content. HTML is converted to Markdown.",
            createFetchUrlParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "fetch_modrinth",
            "Fetch the Modrinth project page content for an installed mod id.",
            createFetchModrinthParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "curl",
            "Perform an HTTP request and return status/body.",
            createCurlParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "search",
            "Search the web via Tavily and return concise snippets with URLs. Use this when external references are needed.",
            createSearchParameters()
        ));
        
        return tools;
    }
    
    // 参数创建方法
    private JsonObject createFetchUrlParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject url = new JsonObject();
        url.addProperty("type", "string");
        url.addProperty("description", "HTTP(S) URL to fetch.");
        properties.add("url", url);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("url");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createFetchModrinthParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject modId = new JsonObject();
        modId.addProperty("type", "string");
        modId.addProperty("description", "Installed mod id to resolve a Modrinth page for.");
        properties.add("mod_id", modId);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("mod_id");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createCurlParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject url = new JsonObject();
        url.addProperty("type", "string");
        url.addProperty("description", "URL to request.");
        properties.add("url", url);
        
        JsonObject method = new JsonObject();
        method.addProperty("type", "string");
        method.addProperty("description", "HTTP method (GET, POST, PUT, DELETE). Defaults to GET.");
        properties.add("method", method);
        
        JsonObject headers = new JsonObject();
        headers.addProperty("type", "object");
        headers.addProperty("description", "Optional HTTP headers.");
        properties.add("headers", headers);
        
        JsonObject body = new JsonObject();
        body.addProperty("type", "string");
        body.addProperty("description", "Optional request body.");
        properties.add("body", body);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("url");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createSearchParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject query = new JsonObject();
        query.addProperty("type", "string");
        query.addProperty("description", "Web search query.");
        properties.add("query", query);
        
        JsonObject maxResults = new JsonObject();
        maxResults.addProperty("type", "integer");
        maxResults.addProperty("description", "Maximum number of results. Defaults to 5.");
        maxResults.addProperty("minimum", 1);
        maxResults.addProperty("maximum", 10);
        properties.add("max_results", maxResults);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("query");
        parameters.add("required", required);
        
        return parameters;
    }
}