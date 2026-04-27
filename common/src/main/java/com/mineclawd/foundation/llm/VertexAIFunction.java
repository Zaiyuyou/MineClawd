package com.mineclawd.foundation.llm;

import com.google.gson.JsonObject;

public record VertexAIFunction(String name, String description, JsonObject parameters, String appendix) {
}
