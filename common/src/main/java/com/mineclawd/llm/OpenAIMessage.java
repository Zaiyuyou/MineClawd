package com.mineclawd.llm;

import com.google.gson.JsonObject;

import java.util.List;

public record OpenAIMessage(
        String role,
        String content,
        List<JsonObject> contentParts,
        List<OpenAIToolCall> toolCalls,
        String toolCallId
) {
    public OpenAIMessage(String role, String content, List<OpenAIToolCall> toolCalls, String toolCallId) {
        this(role, content, null, toolCalls, toolCallId);
    }

    public static OpenAIMessage system(String text) {
        return new OpenAIMessage("system", text, null, null, null);
    }

    public static OpenAIMessage user(String text) {
        return new OpenAIMessage("user", text, null, null, null);
    }

    public static OpenAIMessage userWithParts(String text, List<JsonObject> parts) {
        return new OpenAIMessage("user", text, parts, null, null);
    }

    public static OpenAIMessage assistant(String text, List<OpenAIToolCall> toolCalls) {
        return new OpenAIMessage("assistant", text, null, toolCalls, null);
    }

    public static OpenAIMessage tool(String toolCallId, String text) {
        return new OpenAIMessage("tool", text, null, null, toolCallId);
    }
}
