package com.mineclawd.tool_sys;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 参数工厂类
 * 用于生成符合OpenAI API规范的函数调用参数schema
 * 支持JSON驱动生成和schema验证
 */
public final class ParameterFactory {
    
    /**
     * 创建无参数的工具参数schema
     */
    public static JsonObject createNoArgParameters() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new JsonObject()); // ✅ 正确：空对象
        parameters.add("required", new JsonArray());
        return parameters;
    }
    
    /**
     * 创建字符串参数schema
     */
    public static JsonObject createStringParameter(String name, String description, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "string");
        param.addProperty("description", description);
        
        return createSingleParameterSchema(name, param, required);
    }
    
    /**
     * 创建整数参数schema
     */
    public static JsonObject createIntegerParameter(String name, String description, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "integer");
        param.addProperty("description", description);
        
        return createSingleParameterSchema(name, param, required);
    }
    
    /**
     * 创建布尔参数schema
     */
    public static JsonObject createBooleanParameter(String name, String description, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "boolean");
        param.addProperty("description", description);
        
        return createSingleParameterSchema(name, param, required);
    }
    
    /**
     * 创建数组参数schema
     */
    public static JsonObject createArrayParameter(String name, String description, String itemType, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "array");
        param.addProperty("description", description);
        
        JsonObject items = new JsonObject();
        items.addProperty("type", itemType);
        param.add("items", items);
        
        return createSingleParameterSchema(name, param, required);
    }
    
    /**
     * 创建复杂参数schema（支持嵌套对象）
     */
    public static JsonObject createObjectParameter(String name, String description, JsonObject properties, boolean required) {
        JsonObject param = new JsonObject();
        param.addProperty("type", "object");
        param.addProperty("description", description);
        param.add("properties", properties);
        
        return createSingleParameterSchema(name, param, required);
    }
    
    /**
     * 创建完整的参数schema
     */
    public static JsonObject createParameterSchema(JsonObject properties, JsonArray required) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", properties);
        schema.add("required", required);
        
        return validateSchema(schema) ? schema : null;
    }
    
    /**
     * 从JSON配置创建参数schema
     */
    public static JsonObject createFromJson(String jsonConfig) {
        try {
            JsonObject config = com.google.gson.JsonParser.parseString(jsonConfig).getAsJsonObject();
            return createFromJson(config);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从JSON对象创建参数schema
     */
    public static JsonObject createFromJson(JsonObject config) {
        JsonObject properties = config.getAsJsonObject("properties");
        JsonArray required = config.getAsJsonArray("required");
        
        if (properties == null) {
            properties = new JsonObject();
        }
        if (required == null) {
            required = new JsonArray();
        }
        
        return createParameterSchema(properties, required);
    }
    
    /**
     * 验证schema是否符合OpenAI API规范
     */
    public static boolean validateSchema(JsonObject schema) {
        if (schema == null) return false;
        
        // 检查type字段
        if (!schema.has("type") || !schema.get("type").getAsString().equals("object")) {
            return false;
        }
        
        // 检查properties字段（必须是对象）
        if (!schema.has("properties") || !schema.get("properties").isJsonObject()) {
            return false;
        }
        
        // 检查required字段（必须是数组）
        if (!schema.has("required") || !schema.get("required").isJsonArray()) {
            return false;
        }
        
        // 验证properties中的每个参数
        JsonObject properties = schema.getAsJsonObject("properties");
        for (String key : properties.keySet()) {
            JsonObject param = properties.getAsJsonObject(key);
            if (!validateParameter(param)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 验证单个参数定义
     */
    private static boolean validateParameter(JsonObject param) {
        if (!param.has("type")) return false;
        
        String type = param.get("type").getAsString();
        switch (type) {
            case "string":
            case "number":
            case "integer":
            case "boolean":
                return true;
                
            case "array":
                return param.has("items") && param.get("items").isJsonObject();
                
            case "object":
                return param.has("properties") && param.get("properties").isJsonObject();
                
            default:
                return false;
        }
    }
    
    /**
     * 创建单个参数的schema
     */
    private static JsonObject createSingleParameterSchema(String name, JsonObject param, boolean required) {
        JsonObject properties = new JsonObject();
        properties.add(name, param);
        
        JsonArray requiredArray = new JsonArray();
        if (required) {
            requiredArray.add(name);
        }
        
        return createParameterSchema(properties, requiredArray);
    }
}