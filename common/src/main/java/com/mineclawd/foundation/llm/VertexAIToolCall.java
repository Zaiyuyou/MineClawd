package com.mineclawd.foundation.llm;

import com.google.gson.JsonObject;

public record VertexAIToolCall(String name, JsonObject args) {
}
