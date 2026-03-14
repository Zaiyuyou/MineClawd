package com.mineclawd.tool;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * 交互工具类
 * 管理用户交互和脚本执行相关的工具定义
 */
public class InteractionTools implements ToolProvider {
    
    @Override
    public List<ToolDefinition> getTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        // 交互工具
        tools.add(ToolDefinition.of(
            "ask-user-question",
            "Ask the player a clarification question with up to five preset options. Do not include Other/Skip in options; MineClawd injects Other and handles skip.",
            createQuestionParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "apply-instant-server-script",
            "Apply immediate runtime changes by executing KubeJS JavaScript via /_exec_kubejs_internal.",
            createCodeParameters("JavaScript code to execute immediately. Supports multi-line strings.")
        ));
        
        return tools;
    }
    
    // 参数创建方法
    private JsonObject createQuestionParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject question = new JsonObject();
        question.addProperty("type", "string");
        question.addProperty("description", "Question for the player. Keep concise.");
        properties.add("question", question);
        
        JsonObject options = new JsonObject();
        options.addProperty("type", "array");
        options.addProperty("description", "Preset options (1-5 items). Do not include Other/Skip.");
        JsonObject item = new JsonObject();
        item.addProperty("type", "string");
        options.add("items", item);
        options.addProperty("minItems", 1);
        options.addProperty("maxItems", 5);
        properties.add("options", options);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("question");
        required.add("options");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createCodeParameters(String description) {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject code = new JsonObject();
        code.addProperty("type", "string");
        code.addProperty("description", description);
        properties.add("code", code);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("code");
        parameters.add("required", required);
        
        return parameters;
    }
}