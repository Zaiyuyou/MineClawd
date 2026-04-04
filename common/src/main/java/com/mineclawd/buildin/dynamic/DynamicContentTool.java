package com.mineclawd.buildin.dynamic;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mineclawd.foundation.tool.MineClawdTool;
import com.mineclawd.foundation.tool.ToolExecutionResult;
import com.mineclawd.buildin.kubejs.KubeJsToolExecutor;
import net.minecraft.server.command.ServerCommandSource;

/**
 * 动态内容工具的新架构包装器
 * 将动态内容相关的工具包装成 MineClawdTool 接口
 */
public class DynamicContentTool {
    
    /**
     * 列出动态内容工具
     */
    public static class ListDynamicContentTool implements MineClawdTool {
        @Override
        public String getName() {
            return "list-dynamic-content";
        }
        
        @Override
        public String getDescription() {
            return "List all registered dynamic content (items, blocks, fluids). " +
                   "Use this to see what dynamic content is currently available. " +
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
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.list();
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 注册动态物品工具
     */
    public static class RegisterDynamicItemTool implements MineClawdTool {
        @Override
        public String getName() {
            return "register-dynamic-item";
        }
        
        @Override
        public String getDescription() {
            return "Register a new dynamic item with specified properties. " +
                   "Required: slot (integer), name (string), material_item (string); Optional: throwable (boolean)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Slot number for the item");
            properties.add("slot", slot);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "Name of the dynamic item");
            properties.add("name", name);
            
            JsonObject materialItem = new JsonObject();
            materialItem.addProperty("type", "string");
            materialItem.addProperty("description", "Material item ID for the dynamic item");
            properties.add("material_item", materialItem);
            
            JsonObject throwable = new JsonObject();
            throwable.addProperty("type", "boolean");
            throwable.addProperty("description", "Whether the item is throwable (optional)");
            properties.add("throwable", throwable);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("slot");
            required.add("name");
            required.add("material_item");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String materialItem = args.has("material_item") ? args.get("material_item").getAsString() : null;
            Boolean throwable = args.has("throwable") ? args.get("throwable").getAsBoolean() : null;
            
            if (slot == null || name == null || name.isBlank() || materialItem == null || materialItem.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameters: slot, name, material_item");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.registerItem(source, slot, name, materialItem, throwable);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 注册动态方块工具
     */
    public static class RegisterDynamicBlockTool implements MineClawdTool {
        @Override
        public String getName() {
            return "register-dynamic-block";
        }
        
        @Override
        public String getDescription() {
            return "Register a new dynamic block with specified properties. " +
                   "Required: slot (integer), name (string), material_block (string); Optional: friction (number)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Slot number for the block");
            properties.add("slot", slot);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "Name of the dynamic block");
            properties.add("name", name);
            
            JsonObject materialBlock = new JsonObject();
            materialBlock.addProperty("type", "string");
            materialBlock.addProperty("description", "Material block ID for the dynamic block");
            properties.add("material_block", materialBlock);
            
            JsonObject friction = new JsonObject();
            friction.addProperty("type", "number");
            friction.addProperty("description", "Friction value for the block (optional)");
            properties.add("friction", friction);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("slot");
            required.add("name");
            required.add("material_block");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String materialBlock = args.has("material_block") ? args.get("material_block").getAsString() : null;
            Double friction = args.has("friction") ? args.get("friction").getAsDouble() : null;
            
            if (slot == null || name == null || name.isBlank() || materialBlock == null || materialBlock.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameters: slot, name, material_block");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.registerBlock(source, slot, name, materialBlock, friction);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 注册动态流体工具
     */
    public static class RegisterDynamicFluidTool implements MineClawdTool {
        @Override
        public String getName() {
            return "register-dynamic-fluid";
        }
        
        @Override
        public String getDescription() {
            return "Register a new dynamic fluid with specified properties. " +
                   "Required: slot (integer), name (string), material_fluid (string), color (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Slot number for the fluid");
            properties.add("slot", slot);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "Name of the dynamic fluid");
            properties.add("name", name);
            
            JsonObject materialFluid = new JsonObject();
            materialFluid.addProperty("type", "string");
            materialFluid.addProperty("description", "Material fluid ID for the dynamic fluid");
            properties.add("material_fluid", materialFluid);
            
            JsonObject color = new JsonObject();
            color.addProperty("type", "string");
            color.addProperty("description", "Color code for the fluid");
            properties.add("color", color);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("slot");
            required.add("name");
            required.add("material_fluid");
            required.add("color");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String materialFluid = args.has("material_fluid") ? args.get("material_fluid").getAsString() : null;
            String color = args.has("color") ? args.get("color").getAsString() : null;
            
            if (slot == null || name == null || name.isBlank() || materialFluid == null || materialFluid.isBlank() || color == null || color.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameters: slot, name, material_fluid, color");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.registerFluid(source, slot, name, materialFluid, color);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 更新动态物品工具
     */
    public static class UpdateDynamicItemTool implements MineClawdTool {
        @Override
        public String getName() {
            return "update-dynamic-item";
        }
        
        @Override
        public String getDescription() {
            return "Update an existing dynamic item with new properties. " +
                   "Required: slot (integer), name (string), material_item (string); Optional: throwable (boolean)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Slot number of the item to update");
            properties.add("slot", slot);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "New name for the dynamic item");
            properties.add("name", name);
            
            JsonObject materialItem = new JsonObject();
            materialItem.addProperty("type", "string");
            materialItem.addProperty("description", "New material item ID for the dynamic item");
            properties.add("material_item", materialItem);
            
            JsonObject throwable = new JsonObject();
            throwable.addProperty("type", "boolean");
            throwable.addProperty("description", "Whether the item is throwable (optional)");
            properties.add("throwable", throwable);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("slot");
            required.add("name");
            required.add("material_item");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String materialItem = args.has("material_item") ? args.get("material_item").getAsString() : null;
            Boolean throwable = args.has("throwable") ? args.get("throwable").getAsBoolean() : null;
            
            if (slot == null || name == null || name.isBlank() || materialItem == null || materialItem.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameters: slot, name, material_item");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.updateItem(source, slot, name, materialItem, throwable);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 更新动态方块工具
     */
    public static class UpdateDynamicBlockTool implements MineClawdTool {
        @Override
        public String getName() {
            return "update-dynamic-block";
        }
        
        @Override
        public String getDescription() {
            return "Update an existing dynamic block with new properties. " +
                   "Required: slot (integer), name (string), material_block (string); Optional: friction (number)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Slot number of the block to update");
            properties.add("slot", slot);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "New name for the dynamic block");
            properties.add("name", name);
            
            JsonObject materialBlock = new JsonObject();
            materialBlock.addProperty("type", "string");
            materialBlock.addProperty("description", "New material block ID for the dynamic block");
            properties.add("material_block", materialBlock);
            
            JsonObject friction = new JsonObject();
            friction.addProperty("type", "number");
            friction.addProperty("description", "New friction value for the block (optional)");
            properties.add("friction", friction);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("slot");
            required.add("name");
            required.add("material_block");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String materialBlock = args.has("material_block") ? args.get("material_block").getAsString() : null;
            Double friction = args.has("friction") ? args.get("friction").getAsDouble() : null;
            
            if (slot == null || name == null || name.isBlank() || materialBlock == null || materialBlock.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameters: slot, name, material_block");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.updateBlock(source, slot, name, materialBlock, friction);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 更新动态流体工具
     */
    public static class UpdateDynamicFluidTool implements MineClawdTool {
        @Override
        public String getName() {
            return "update-dynamic-fluid";
        }
        
        @Override
        public String getDescription() {
            return "Update an existing dynamic fluid with new properties. " +
                   "Required: slot (integer), name (string), material_fluid (string), color (string)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Slot number of the fluid to update");
            properties.add("slot", slot);
            
            JsonObject name = new JsonObject();
            name.addProperty("type", "string");
            name.addProperty("description", "New name for the dynamic fluid");
            properties.add("name", name);
            
            JsonObject materialFluid = new JsonObject();
            materialFluid.addProperty("type", "string");
            materialFluid.addProperty("description", "New material fluid ID for the dynamic fluid");
            properties.add("material_fluid", materialFluid);
            
            JsonObject color = new JsonObject();
            color.addProperty("type", "string");
            color.addProperty("description", "New color code for the fluid");
            properties.add("color", color);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("slot");
            required.add("name");
            required.add("material_fluid");
            required.add("color");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            String name = args.has("name") ? args.get("name").getAsString() : null;
            String materialFluid = args.has("material_fluid") ? args.get("material_fluid").getAsString() : null;
            String color = args.has("color") ? args.get("color").getAsString() : null;
            
            if (slot == null || name == null || name.isBlank() || materialFluid == null || materialFluid.isBlank() || color == null || color.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameters: slot, name, material_fluid, color");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.updateFluid(source, slot, name, materialFluid, color);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
    
    /**
     * 注销动态内容工具
     */
    public static class UnregisterDynamicContentTool implements MineClawdTool {
        @Override
        public String getName() {
            return "unregister-dynamic-content";
        }
        
        @Override
        public String getDescription() {
            return "Unregister dynamic content by type and optional slot. " +
                   "Required: type (string: item, block, or fluid); Optional: slot (integer)";
        }
        
        @Override
        public JsonObject getParameters() {
            JsonObject params = new JsonObject();
            params.addProperty("type", "object");
            
            JsonObject properties = new JsonObject();
            
            JsonObject type = new JsonObject();
            type.addProperty("type", "string");
            type.addProperty("description", "Type of content to unregister (item, block, or fluid)");
            properties.add("type", type);
            
            JsonObject slot = new JsonObject();
            slot.addProperty("type", "integer");
            slot.addProperty("description", "Optional slot number to unregister specific content");
            properties.add("slot", slot);
            
            params.add("properties", properties);
            
            JsonArray required = new JsonArray();
            required.add("type");
            params.add("required", required);
            
            return params;
        }
        
        @Override
        public ToolExecutionResult execute(ServerCommandSource source, JsonObject args) {
            String type = args.has("type") ? args.get("type").getAsString() : null;
            Integer slot = args.has("slot") ? args.get("slot").getAsInt() : null;
            
            if (type == null || type.isBlank()) {
                return ToolExecutionResult.failure("Missing required parameter: type");
            }
            
            KubeJsToolExecutor.ToolExecutionResult result = DynamicContentToolExecutor.unregister(source, type, slot);
            return ToolExecutionResult.success(result.output());
        }
        
        @Override
        public boolean supportsAsync() {
            return false;
        }
        
        @Override
        public String getPromptCategory() {
            return "Dynamic Content";
        }
    }
}