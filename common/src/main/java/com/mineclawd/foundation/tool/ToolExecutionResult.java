package com.mineclawd.foundation.tool;

import com.google.gson.JsonObject;

/**
 * 工具执行结果
 * 符合OpenAI API工具调用规范
 */
public record ToolExecutionResult(boolean success, String output, JsonObject structuredOutput) {
    
    /**
     * 创建成功的结果（简单字符串）
     */
    public static ToolExecutionResult success(String output) {
        JsonObject structured = new JsonObject();
        structured.addProperty("result", output);
        structured.addProperty("success", true);
        return new ToolExecutionResult(true, output, structured);
    }
    
    /**
     * 创建失败的结果（简单字符串）
     */
    public static ToolExecutionResult failure(String error) {
        JsonObject structured = new JsonObject();
        structured.addProperty("error", error);
        structured.addProperty("success", false);
        return new ToolExecutionResult(false, error, structured);
    }
    
    /**
     * 创建成功的结果（结构化JSON）
     */
    public static ToolExecutionResult success(JsonObject structuredOutput) {
        structuredOutput.addProperty("success", true);
        return new ToolExecutionResult(true, "", structuredOutput);
    }
    
    /**
     * 创建失败的结果（结构化JSON）
     */
    public static ToolExecutionResult failure(JsonObject structuredError) {
        structuredError.addProperty("success", false);
        return new ToolExecutionResult(false, "", structuredError);
    }
    
    /**
     * 向后兼容的构造函数（用于现有代码）
     */
    public ToolExecutionResult(boolean success, String output) {
        this(success, output, null);
    }
    
    /**
     * 获取符合OpenAI API规范的响应内容
     */
    public String getApiCompliantOutput() {
        if (structuredOutput != null) {
            return structuredOutput.toString();
        }
        
        // 如果没有结构化输出，创建默认格式
        JsonObject result = new JsonObject();
        if (success) {
            result.addProperty("result", output);
            result.addProperty("success", true);
        } else {
            result.addProperty("error", output);
            result.addProperty("success", false);
        }
        return result.toString();
    }
    
    /**
     * 获取简单的输出内容（向后兼容）
     */
    public String getSimpleOutput() {
        return output;
    }
    
    /**
     * 向后兼容的方法
     */
    public String output() {
        return output;
    }
    
    /**
     * 向后兼容的方法
     */
    public boolean success() {
        return success;
    }
}