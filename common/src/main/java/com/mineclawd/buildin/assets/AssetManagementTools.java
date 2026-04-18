package com.mineclawd.buildin.assets;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.foundation.tool.ToolStatusDescriptor;
import com.mineclawd.foundation.assets.AssetsManager;
import com.mineclawd.foundation.assets.AssetsManager.AssetDraft;
import com.mineclawd.foundation.assets.AssetsManager.AssetRecord;
import com.mineclawd.foundation.assets.AssetsManager.UpsertResult;
import net.minecraft.server.command.ServerCommandSource;

/**
 * 资产管理工具的新架构包装器
 * 将资产管理相关的工具包装成 MineClawdTool 接口
 */
public class AssetManagementTools {
    
    /**
     * 列出资产工具
     */
    public static class ListAssetsTool implements MineClawdTool {
        @Override
        public String getName() {
            return "list-assets";
        }
        
        @Override
        public String getDescription() {
            return "List currently tracked persistent asset records for this player/session owner. " +
                   "Use this to inspect all currently tracked assets. " +
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
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String ownerKey = getOwnerKey(source);
            AssetsManager assetsManager = new AssetsManager();
            
            java.util.List<AssetRecord> assets = assetsManager.list(ownerKey);
            if (assets.isEmpty()) {
                return ToolExecutionResult.success("No assets tracked yet.");
            }
            
            StringBuilder output = new StringBuilder("Assets (`" + assets.size() + "` total):\\n");
            for (AssetRecord asset : assets) {
                if (asset == null) continue;
                
                output.append("- `").append(asset.id()).append("` ").append(asset.name());
                
                if (!asset.summary().isBlank()) {
                    output.append(" — ").append(asset.summary());
                }
                
                output.append(" (category: ").append(asset.category().name().toLowerCase()).append(")");
                
                if (!asset.scriptPath().isBlank()) {
                    output.append(" [script: ").append(asset.scriptPath()).append("]");
                }
                
                output.append("\\n");
            }
            
            return ToolExecutionResult.success(output.toString());
        }
        
        @Override
        public String getPromptAppendix() {
            return "*** ASSET LISTING TOOL ***\\n\\n" +
                   "Use this tool to view all currently tracked assets for the current player/session.\\n" +
                   "This helps you understand what content has been created and can be referenced.\\n\\n" +
                   "Output includes: asset ID, category, name, summary, and category-specific details.";
        }
        
        @Override
        public String getPromptCategory() {
            return "Assets";
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public ToolStatusDescriptor getToolStatusDescriptor(String toolName, JsonObject args) {
            return new ToolStatusDescriptor("Listing assets", "View tracked asset records");
        }
        
        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return MineClawdTool.RevealPolicy.passive(MineClawdTool.RevealLayer.LAYER_3); // 专业功能工具，层级3
        }
    }
    
    /**
     * 创建或更新资产记录工具
     */
    public static class UpsertAssetRecordTool implements MineClawdTool {
        @Override
        public String getName() {
            return "upsert-asset-record";
        }
        
        @Override
        public String getDescription() {
            return "Create or update an asset tracking record for entities, items/blocks/fluids, special items, commands, or game mechanics. " +
                   "Use this to track persistent assets so future sessions can continue previous work. " +
                   "Required: category (string), name (string); Optional: id, summary, script_path, details, content_id, special_item_id, special_item_nbt, command, entity_uuid, entity_dimension, entity_x, entity_y, entity_z, session_id";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject id = new JsonObject();
            id.addProperty("type", "string");
            id.addProperty("description", "Optional asset id. Omit to auto-generate from category/name");
            properties.add("id", id);
            
            JsonObject category = new JsonObject();
            category.addProperty("type", "string");
            category.addProperty("description", "Asset category");
            JsonArray categoryEnum = new JsonArray();
            categoryEnum.add("entities");
            categoryEnum.add("items_blocks_fluids");
            categoryEnum.add("special_items");
            categoryEnum.add("commands");
            categoryEnum.add("game_mechanics");
            category.add("enum", categoryEnum);
            properties.add("category", category);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "Display name for this asset");
            properties.add("name", name);
            
            JsonObject summary = new JsonObject();
            summary.addProperty("type", "string");
            summary.addProperty("description", "Optional one-sentence summary");
            properties.add("summary", summary);
            
            JsonObject scriptPath = new JsonObject();
            scriptPath.addProperty("type", "string");
            scriptPath.addProperty("description", "Optional script path, for example `commands/fly.js`");
            properties.add("script_path", scriptPath);
            
            JsonObject details = new JsonObject();
            details.addProperty("type", "string");
            details.addProperty("description", "Optional free-form details");
            properties.add("details", details);
            
            JsonObject contentId = new JsonObject();
            contentId.addProperty("type", "string");
            contentId.addProperty("description", "Content ID for items/blocks/fluids category");
            properties.add("content_id", contentId);
            
            JsonObject specialItemId = new JsonObject();
            specialItemId.addProperty("type", "string");
            specialItemId.addProperty("description", "Special item ID for special_items category");
            properties.add("special_item_id", specialItemId);
            
            JsonObject specialItemNbt = new JsonObject();
            specialItemNbt.addProperty("type", "string");
            specialItemNbt.addProperty("description", "Special item NBT for special_items category");
            properties.add("special_item_nbt", specialItemNbt);
            
            JsonObject command = new JsonObject();
            command.addProperty("type", "string");
            command.addProperty("description", "Command for commands category");
            properties.add("command", command);
            
            JsonObject entityUuid = new JsonObject();
            entityUuid.addProperty("type", "string");
            entityUuid.addProperty("description", "Entity UUID for entities category");
            properties.add("entity_uuid", entityUuid);
            
            JsonObject entityDimension = new JsonObject();
            entityDimension.addProperty("type", "string");
            entityDimension.addProperty("description", "Entity dimension for entities category");
            properties.add("entity_dimension", entityDimension);
            
            JsonObject entityX = new JsonObject();
            entityX.addProperty("type", "number");
            entityX.addProperty("description", "Entity X coordinate for entities category");
            properties.add("entity_x", entityX);
            
            JsonObject entityY = new JsonObject();
            entityY.addProperty("type", "number");
            entityY.addProperty("description", "Entity Y coordinate for entities category");
            properties.add("entity_y", entityY);
            
            JsonObject entityZ = new JsonObject();
            entityZ.addProperty("type", "number");
            entityZ.addProperty("description", "Entity Z coordinate for entities category");
            properties.add("entity_z", entityZ);
            
            JsonObject sessionId = new JsonObject();
            sessionId.addProperty("type", "string");
            sessionId.addProperty("description", "Session ID for tracking");
            properties.add("session_id", sessionId);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("category");
            required.add("name");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String ownerKey = getOwnerKey(source);
            AssetsManager assetsManager = new AssetsManager();
            
            String scriptPath = args.has("script_path") ? args.get("script_path").getAsString() : "";
            if (scriptPath.isBlank() && args.has("scriptPath")) {
                scriptPath = args.get("scriptPath").getAsString();
            }
            
            AssetDraft draft = new AssetDraft(
                args.has("id") ? args.get("id").getAsString() : "",
                args.has("category") ? args.get("category").getAsString() : "",
                args.has("name") ? args.get("name").getAsString() : "",
                args.has("summary") ? args.get("summary").getAsString() : "",
                scriptPath,
                args.has("details") ? args.get("details").getAsString() : "",
                args.has("content_id") ? args.get("content_id").getAsString() : "",
                args.has("special_item_id") ? args.get("special_item_id").getAsString() : "",
                args.has("special_item_nbt") ? args.get("special_item_nbt").getAsString() : "",
                args.has("command") ? args.get("command").getAsString() : "",
                args.has("entity_uuid") ? args.get("entity_uuid").getAsString() : "",
                args.has("entity_dimension") ? args.get("entity_dimension").getAsString() : "",
                args.has("entity_x") ? args.get("entity_x").getAsDouble() : 0.0,
                args.has("entity_y") ? args.get("entity_y").getAsDouble() : 0.0,
                args.has("entity_z") ? args.get("entity_z").getAsDouble() : 0.0,
                args.has("session_id") ? args.get("session_id").getAsString() : ""
            );
            
            UpsertResult result = assetsManager.upsert(ownerKey, draft);
            if (!result.success()) {
                return ToolExecutionResult.failure(result.message());
            }
            
            AssetRecord record = result.record();
            if (record == null) {
                return ToolExecutionResult.success(result.message());
            }
            
            StringBuilder output = new StringBuilder(result.message());
            output.append("\\n\\nAsset record details:\\n");
            output.append("- ID: `").append(record.id()).append("`\\n");
            output.append("- Category: `").append(record.category().name().toLowerCase()).append("`\\n");
            output.append("- Name: `").append(record.name()).append("`\\n");
            
            if (!record.summary().isBlank()) {
                output.append("- Summary: `").append(record.summary()).append("`\\n");
            }
            
            if (!record.scriptPath().isBlank()) {
                output.append("- Script Path: `").append(record.scriptPath()).append("`\\n");
            }
            
            return ToolExecutionResult.success(output.toString());
        }
        
        @Override
        public String getPromptAppendix() {
            return "*** ASSET UPSERT TOOL ***\\n\\n" +
                   "Use this tool to create or update asset tracking records.\\n" +
                   "This ensures future sessions can continue previous work without losing references.\\n\\n" +
                   "Required fields: category, name\\n" +
                   "Optional fields: summary, script_path, details, and category-specific fields.\\n\\n" +
                   "Categories and required fields:\\n" +
                   "- entities: entity_uuid (add dimension/coordinates when known)\\n" +
                   "- items_blocks_fluids: content_id\\n" +
                   "- special_items: special_item_id (special_item_nbt if available)\\n" +
                   "- commands: command (script_path if scripted)\\n" +
                   "- game_mechanics: summary, details, script_path when applicable";
        }
        
        @Override
        public String getPromptCategory() {
            return "Assets";
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public ToolStatusDescriptor getToolStatusDescriptor(String toolName, JsonObject args) {
            String category = args.has("category") ? args.get("category").getAsString() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String shortText = category != null && name != null ? 
                "Upsert: " + category + "/" + name : "Upsert asset record";
            String hoverText = category != null && name != null ? 
                "Category: " + category + ", Name: " + name : "";
            return new ToolStatusDescriptor(shortText, hoverText);
        }
        
        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return MineClawdTool.RevealPolicy.passive(MineClawdTool.RevealLayer.LAYER_3); // 专业功能工具，层级3
        }
    }
    
    /**
     * 移除资产记录工具
     */
    public static class RemoveAssetRecordTool implements MineClawdTool {
        @Override
        public String getName() {
            return "remove-asset-record";
        }
        
        @Override
        public String getDescription() {
            return "Remove an obsolete asset tracking record by id/reference. " +
                   "Use this to remove stale records when the referenced thing no longer exists. " +
                   "Required: id (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject id = new JsonObject();
            id.addProperty("type", "string");
            id.addProperty("description", "Asset id to remove");
            properties.add("id", id);
            
            JsonObject reference = new JsonObject();
            reference.addProperty("type", "string");
            reference.addProperty("description", "Alias for id");
            properties.add("reference", reference);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("id");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String ownerKey = getOwnerKey(source);
            AssetsManager assetsManager = new AssetsManager();
            
            String id = args.has("id") ? args.get("id").getAsString() : "";
            if (id.isBlank() && args.has("reference")) {
                id = args.get("reference").getAsString();
            }
            
            if (id.isBlank()) {
                return ToolExecutionResult.failure("Tool call is missing required string `id`.");
            }
            
            AssetRecord record = assetsManager.resolve(ownerKey, id);
            if (record == null) {
                return ToolExecutionResult.failure("Asset record was not found.");
            }
            
            boolean removed = assetsManager.remove(ownerKey, record.id());
            if (!removed) {
                return ToolExecutionResult.failure("Failed to remove asset record.");
            }
            
            return ToolExecutionResult.success("Removed asset record `" + record.id() + "`.");
        }
        
        @Override
        public String getPromptAppendix() {
            return "*** ASSET REMOVAL TOOL ***\\n\\n" +
                   "Use this tool to remove obsolete asset tracking records.\\n" +
                   "This helps keep the asset database clean and relevant.\\n\\n" +
                   "Required: id (or reference alias)\\n\\n" +
                   "Usage: Call this when an asset is no longer needed or has been deleted.\\n" +
                   "The tool will first resolve the asset by ID/reference, then remove it.";
        }
        
        @Override
        public String getPromptCategory() {
            return "Assets";
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public ToolStatusDescriptor getToolStatusDescriptor(String toolName, JsonObject args) {
            String assetId = args.has("id") ? args.get("id").getAsString() : null;
            String shortText = assetId != null ? "Remove: " + assetId : "Remove asset record";
            String hoverText = assetId != null ? "Asset ID: " + assetId : "";
            return new ToolStatusDescriptor(shortText, hoverText);
        }
        
        @Override
        public MineClawdTool.RevealPolicy getRevealPolicy() {
            return MineClawdTool.RevealPolicy.passive(MineClawdTool.RevealLayer.LAYER_3); // 专业功能工具，层级3
        }
    }
    
    private static String getOwnerKey(ServerCommandSource source) {
        if (source.getEntity() != null) {
            return source.getEntity().getUuidAsString();
        }
        return "server";
    }
}