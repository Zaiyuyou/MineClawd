package com.mineclawd.tool;

import com.google.gson.JsonObject;
import com.mineclawd.tool_sys.ToolDefinition;
import com.mineclawd.tool_sys.ToolProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * 资产管理工具类
 * 管理资产跟踪记录相关的工具定义
 */
public class AssetManagementTools implements ToolProvider {
    
    @Override
    public List<ToolDefinition> getTools() {
        List<ToolDefinition> tools = new ArrayList<>();
        
        // 资产管理工具
        tools.add(ToolDefinition.of(
            "list-assets",
            "List currently tracked persistent asset records for this player/session owner.",
            createNoArgParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "upsert-asset-record",
            "Create or update an asset tracking record for entities, items/blocks/fluids, special items, commands, or game mechanics.",
            createAssetUpsertParameters()
        ));
        
        tools.add(ToolDefinition.of(
            "remove-asset-record",
            "Remove an obsolete asset tracking record by id/reference.",
            createAssetRemoveParameters()
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
    
    private JsonObject createAssetUpsertParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject id = new JsonObject();
        id.addProperty("type", "string");
        id.addProperty("description", "Optional asset id. Omit to auto-generate from category/name.");
        properties.add("id", id);
        
        JsonObject category = new JsonObject();
        category.addProperty("type", "string");
        category.addProperty("description", "Asset category.");
        properties.add("category", category);
        
        JsonObject name = new JsonObject();
        name.addProperty("type", "string");
        name.addProperty("description", "Display name for this asset.");
        properties.add("name", name);
        
        JsonObject description = new JsonObject();
        description.addProperty("type", "string");
        description.addProperty("description", "Full description of this asset.");
        properties.add("description", description);
        
        JsonObject reference = new JsonObject();
        reference.addProperty("type", "string");
        reference.addProperty("description", "Vanilla reference (entity id, item id, block id, etc.).");
        properties.add("reference", reference);
        
        JsonObject tags = new JsonObject();
        tags.addProperty("type", "array");
        tags.addProperty("description", "Optional tags for categorization.");
        JsonObject tagItem = new JsonObject();
        tagItem.addProperty("type", "string");
        tags.add("items", tagItem);
        properties.add("tags", tags);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("category");
        required.add("name");
        required.add("description");
        parameters.add("required", required);
        
        return parameters;
    }
    
    private JsonObject createAssetRemoveParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        
        JsonObject id = new JsonObject();
        id.addProperty("type", "string");
        id.addProperty("description", "Asset id or reference to remove.");
        properties.add("id", id);
        
        JsonObject reference = new JsonObject();
        reference.addProperty("type", "string");
        reference.addProperty("description", "Alias for id.");
        properties.add("reference", reference);
        
        parameters.add("properties", properties);
        
        com.google.gson.JsonArray required = new com.google.gson.JsonArray();
        required.add("id");
        parameters.add("required", required);
        
        return parameters;
    }
}