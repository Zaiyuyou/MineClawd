package com.mineclawd.tool;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态内容工具类
 * 管理动态占位符内容相关的工具定义
 */
public class DynamicContentTools implements ToolProvider {
    
    @Override
    public List<ToolDefinition> getTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        // 动态内容工具
        tools.add(ToolDefinition.noArg(
            "list-dynamic-content",
            "List currently active dynamic placeholder entries and free slots."
        ));
        
        tools.add(ToolDefinition.of(
            "register-dynamic-item",
            "Claim a free dynamic item placeholder slot.",
            createDynamicItemParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "register-dynamic-block",
            "Claim a free dynamic block placeholder slot.",
            createDynamicBlockParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "register-dynamic-fluid",
            "Claim a free dynamic fluid placeholder slot.",
            createDynamicFluidParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "update-dynamic-item",
            "Update properties for an existing dynamic item slot.",
            createDynamicItemUpdateParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "update-dynamic-block",
            "Update properties for an existing dynamic block slot.",
            createDynamicBlockUpdateParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "update-dynamic-fluid",
            "Update properties for an existing dynamic fluid slot.",
            createDynamicFluidUpdateParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "unregister-dynamic-content",
            "Release a dynamic placeholder entry by type and slot.",
            createDynamicUnregisterParameters()
        ));
        
        return tools;
    }
    
    // 参数创建方法
    private JsonObject createDynamicItemParameters() {
        return createDynamicRegistrationParameters("item");
    }
    
    private JsonObject createDynamicBlockParameters() {
        return createDynamicRegistrationParameters("block");
    }
    
    private JsonObject createDynamicFluidParameters() {
        return createDynamicRegistrationParameters("fluid");
    }
    
    private JsonObject createDynamicItemUpdateParameters() {
        return createDynamicUpdateParameters("item");
    }
    
    private JsonObject createDynamicBlockUpdateParameters() {
        return createDynamicUpdateParameters("block");
    }
    
    private JsonObject createDynamicFluidUpdateParameters() {
        return createDynamicUpdateParameters("fluid");
    }
    
    private JsonObject createDynamicUnregisterParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject type = new JsonObject();
        type.addProperty("type", "string");
        type.addProperty("description", "Dynamic content type (item, block, fluid).");
        properties.add("type", type);
        
        JsonObject slot = new JsonObject();
        slot.addProperty("type", "integer");
        slot.addProperty("description", "Slot index to unregister (1-30).");
        slot.addProperty("minimum", 1);
        slot.addProperty("maximum", 30);
        properties.add("slot", slot);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("type");
        required.add("slot");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createDynamicRegistrationParameters(String type) {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject name = new JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "Display name for the dynamic " + type + ".");
        properties.add("name", name);
        
        JsonObject description = new JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "Description of the dynamic " + type + ".");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("name");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createDynamicUpdateParameters(String type) {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject slot = new JsonObject();
        slot.addProperty("type", "integer");
        slot.addProperty("description", "Existing slot index (1-30).");
        slot.addProperty("minimum", 1);
        slot.addProperty("maximum", 30);
        properties.add("slot", slot);
        
        JsonObject name = new JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "Optional updated display name.");
        properties.add("name", name);
        
        JsonObject description = new JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "Optional updated description.");
        properties.add("description", description);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("slot");
        parameters.add("required", required);
        
        return parameters;
    }
}