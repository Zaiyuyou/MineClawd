package com.mineclawd.foundation.llm;

import java.util.List;

public record OpenAIResponse(String text, List<OpenAIToolCall> toolCalls) {
}
