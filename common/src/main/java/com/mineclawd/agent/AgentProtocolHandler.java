package com.mineclawd.agent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineclawd.llm.OpenAIMessage;
import com.mineclawd.llm.OpenAITool;
import com.mineclawd.llm.OpenAIToolCall;
import com.mineclawd.llm.VertexAIMessage;
import com.mineclawd.llm.VertexAIFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 协议处理器
 * 负责构建和解析 Agent 与 LLM 通信的 JSON 协议
 * 不包含具体的网络通信逻辑，只处理 JSON 数据结构
 */
public final class AgentProtocolHandler {
    
    private AgentProtocolHandler() {
        // 私有构造函数
    }
    
    // ==================== OpenAI JSON 构建 ====================
    
    /**
     * 构建 OpenAI 请求体
     * @param model 模型名称
     * @param history 对话历史
     * @param tools 工具列表
     * @param stream 是否启用流式响应
     * @return 请求体 JSON 对象
     */
    public static JsonObject buildOpenAiRequest(
            String model,
            List<OpenAIMessage> history,
            @Nullable List<OpenAITool> tools,
            boolean stream
    ) {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        if (stream) {
            body.addProperty("stream", true);
        }
        
        JsonArray messages = buildMessagesArray(history);
        body.add("messages", messages);
        
        if (tools != null && !tools.isEmpty()) {
            JsonArray toolsArray = buildToolsArray(tools);
            body.add("tools", toolsArray);
        }
        
        return body;
    }
    
    /**
     * 构建 messages 数组
     */
    private static JsonArray buildMessagesArray(List<OpenAIMessage> history) {
        JsonArray messages = new JsonArray();
        if (history == null) {
            return messages;
        }
        
        for (OpenAIMessage message : history) {
            if (message == null) {
                continue;
            }
            
            JsonObject msg = new JsonObject();
            msg.addProperty("role", message.role());
            
            // 构建 content
            if (message.contentParts() != null && !message.contentParts().isEmpty()) {
                JsonArray parts = new JsonArray();
                for (JsonObject part : message.contentParts()) {
                    if (part != null) {
                        parts.add(part.deepCopy());
                    }
                }
                msg.add("content", parts);
            } else if (message.content() != null) {
                msg.addProperty("content", message.content());
            } else {
                msg.add("content", new JsonObject()); // null content
            }
            
            // 构建 tool_calls
            if (message.toolCalls() != null && !message.toolCalls().isEmpty()) {
                JsonArray toolCalls = new JsonArray();
                for (OpenAIToolCall call : message.toolCalls()) {
                    if (call == null) {
                        continue;
                    }
                    JsonObject callObj = new JsonObject();
                    if (call.id() != null && !call.id().isBlank()) {
                        callObj.addProperty("id", call.id());
                    }
                    callObj.addProperty("type", "function");
                    
                    JsonObject function = new JsonObject();
                    function.addProperty("name", call.name());
                    function.addProperty("arguments", call.arguments() == null ? "" : call.arguments());
                    callObj.add("function", function);
                    
                    toolCalls.add(callObj);
                }
                msg.add("tool_calls", toolCalls);
            }
            
            // 构建 tool_call_id
            if (message.toolCallId() != null && !message.toolCallId().isBlank()) {
                msg.addProperty("tool_call_id", message.toolCallId());
            }
            
            messages.add(msg);
        }
        
        return messages;
    }
    
    /**
     * 构建 tools 数组
     */
    private static JsonArray buildToolsArray(List<OpenAITool> tools) {
        JsonArray toolsArray = new JsonArray();
        
        for (OpenAITool tool : tools) {
            if (tool == null) {
                continue;
            }
            
            JsonObject toolObj = new JsonObject();
            toolObj.addProperty("type", "function");
            
            JsonObject function = new JsonObject();
            function.addProperty("name", tool.name());
            
            if (tool.description() != null && !tool.description().isBlank()) {
                function.addProperty("description", tool.description());
            }
            
            if (tool.parameters() != null) {
                function.add("parameters", tool.parameters());
            }
            
            toolObj.add("function", function);
            toolsArray.add(toolObj);
        }
        
        return toolsArray;
    }
    
    // ==================== Vertex AI JSON 构建 ====================
    
    /**
     * 构建 Vertex AI 请求体
     * @param model 模型路径
     * @param history 对话历史
     * @param tools 工具列表
     * @param stream 是否启用流式响应
     * @return 请求体 JSON 对象
     */
    public static JsonObject buildVertexAiRequest(
            String model,
            List<VertexAIMessage> history,
            List<VertexAIFunction> tools,
            boolean stream
    ) {
        JsonObject body = new JsonObject();
        
        JsonArray contents = buildContentsArray(history);
        body.add("contents", contents);
        
        if (tools != null && !tools.isEmpty()) {
            JsonObject tool = buildToolObject(tools);
            JsonArray toolsArray = new JsonArray();
            toolsArray.add(tool);
            body.add("tools", toolsArray);
            
            JsonObject toolConfig = buildToolConfig();
            body.add("toolConfig", toolConfig);
        }
        
        return body;
    }
    
    /**
     * 构建 contents 数组
     */
    private static JsonArray buildContentsArray(List<VertexAIMessage> history) {
        JsonArray contents = new JsonArray();
        if (history == null) {
            return contents;
        }
        
        for (VertexAIMessage message : history) {
            if (message == null) {
                continue;
            }
            
            JsonObject content = new JsonObject();
            if (message.role() != null && !message.role().isBlank()) {
                content.addProperty("role", message.role());
            }
            content.add("parts", message.toJsonParts());
            contents.add(content);
        }
        
        return contents;
    }
    
    /**
     * 构建 tool 对象
     */
    private static JsonObject buildToolObject(List<VertexAIFunction> tools) {
        JsonObject tool = new JsonObject();
        JsonArray declarations = new JsonArray();
        
        for (VertexAIFunction function : tools) {
            if (function == null) {
                continue;
            }
            
            JsonObject functionJson = new JsonObject();
            functionJson.addProperty("name", function.name());
            
            if (function.description() != null && !function.description().isBlank()) {
                functionJson.addProperty("description", function.description());
            }
            
            if (function.parameters() != null) {
                functionJson.add("parameters", function.parameters());
            }
            
            declarations.add(functionJson);
        }
        
        tool.add("functionDeclarations", declarations);
        return tool;
    }
    
    /**
     * 构建 toolConfig 对象
     */
    private static JsonObject buildToolConfig() {
        JsonObject toolConfig = new JsonObject();
        JsonObject functionCallingConfig = new JsonObject();
        functionCallingConfig.addProperty("mode", "AUTO");
        toolConfig.add("functionCallingConfig", functionCallingConfig);
        return toolConfig;
    }
    
    // ==================== OpenAI 响应解析 ====================
    
    /**
     * 解析 OpenAI 响应
     * @param responseBody 响应体 JSON 字符串
     * @return 解析后的响应对象
     */
    public static OpenAiResponse parseOpenAiResponse(String responseBody) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        List<OpenAIToolCall> toolCalls = new ArrayList<>();
        
        if (responseJson.has("choices") && responseJson.get("choices").isJsonArray()) {
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                
                // 解析 content
                if (firstChoice.has("message") && firstChoice.get("message").isJsonObject()) {
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    
                    if (message.has("content") && !message.get("content").isJsonNull()) {
                        text = message.get("content").getAsString();
                    }
                    
                    // 解析 tool_calls
                    if (message.has("tool_calls") && message.get("tool_calls").isJsonArray()) {
                        JsonArray messageToolCalls = message.getAsJsonArray("tool_calls");
                        for (JsonElement toolCallElement : messageToolCalls) {
                            if (toolCallElement.isJsonObject()) {
                                JsonObject toolCallObj = toolCallElement.getAsJsonObject();
                                OpenAIToolCall toolCall = parseOpenAiToolCall(toolCallObj);
                                if (toolCall != null) {
                                    toolCalls.add(toolCall);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new OpenAiResponse(text, toolCalls);
    }
    
    /**
     * 解析单个 OpenAI tool_call
     */
    private static OpenAIToolCall parseOpenAiToolCall(JsonObject toolCallObj) {
        if (!toolCallObj.has("type") || !"function".equals(toolCallObj.get("type").getAsString())) {
            return null;
        }
        
        String id = null;
        if (toolCallObj.has("id") && !toolCallObj.get("id").isJsonNull()) {
            id = toolCallObj.get("id").getAsString();
        }
        
        if (!toolCallObj.has("function") || !toolCallObj.get("function").isJsonObject()) {
            return null;
        }
        
        JsonObject function = toolCallObj.getAsJsonObject("function");
        String name = function.has("name") ? function.get("name").getAsString() : null;
        String arguments = function.has("arguments") ? function.get("arguments").getAsString() : "";
        
        return new OpenAIToolCall(id, name, arguments);
    }
    
    /**
     * 解析流式 OpenAI 响应
     * @param responseBody 响应体 JSON 字符串
     * @return 流式响应数据
     */
    public static OpenAiStreamData parseOpenAiStream(String responseBody) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        OpenAIToolCall toolCall = null;
        
        if (responseJson.has("choices") && responseJson.get("choices").isJsonArray()) {
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (!choices.isEmpty()) {
                JsonObject delta = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                
                // 解析 content
                if (delta.has("content") && !delta.get("content").isJsonNull()) {
                    text = delta.get("content").getAsString();
                }
                
                // 解析 tool_calls
                if (delta.has("tool_calls") && delta.get("tool_calls").isJsonArray()) {
                    JsonArray toolCalls = delta.getAsJsonArray("tool_calls");
                    if (!toolCalls.isEmpty()) {
                        JsonObject toolCallObj = toolCalls.get(0).getAsJsonObject();
                        toolCall = parseOpenAiToolCall(toolCallObj);
                    }
                }
            }
        }
        
        return new OpenAiStreamData(text, toolCall);
    }
    
    // ==================== Vertex AI 响应解析 ====================
    
    /**
     * 解析 Vertex AI 响应
     * @param responseBody 响应体 JSON 字符串
     * @return 解析后的响应对象
     */
    public static VertexAiResponse parseVertexAiResponse(String responseBody) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        List<VertexAiToolCall> toolCalls = new ArrayList<>();
        
        if (responseJson.has("candidates") && responseJson.get("candidates").isJsonArray()) {
            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (!candidates.isEmpty()) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                
                if (firstCandidate.has("content") && firstCandidate.get("content").isJsonObject()) {
                    JsonObject content = firstCandidate.getAsJsonObject("content");
                    
                    if (content.has("parts") && content.get("parts").isJsonArray()) {
                        JsonArray parts = content.getAsJsonArray("parts");
                        for (JsonElement partElement : parts) {
                            if (partElement.isJsonObject()) {
                                JsonObject part = partElement.getAsJsonObject();
                                
                                // 解析 text
                                if (part.has("text") && !part.get("text").isJsonNull()) {
                                    text = part.get("text").getAsString();
                                }
                                
                                // 解析 functionCall
                                if (part.has("functionCall") && part.get("functionCall").isJsonObject()) {
                                    JsonObject functionCall = part.getAsJsonObject("functionCall");
                                    String name = functionCall.has("name") ? functionCall.get("name").getAsString() : null;
                                    JsonObject args = functionCall.has("args") ? functionCall.getAsJsonObject("args") : new JsonObject();
                                    
                                    VertexAiToolCall toolCall = new VertexAiToolCall(name, args);
                                    toolCalls.add(toolCall);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new VertexAiResponse(text, toolCalls);
    }
    
    /**
     * 解析流式 Vertex AI 响应
     * @param responseBody 响应体 JSON 字符串
     * @return 流式响应数据
     */
    public static VertexAiStreamData parseVertexAiStream(String responseBody) {
        JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
        
        String text = "";
        VertexAiToolCall toolCall = null;
        
        if (responseJson.has("candidates") && responseJson.get("candidates").isJsonArray()) {
            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (!candidates.isEmpty()) {
                JsonObject delta = candidates.get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonObject("parts")
                        .get(0).getAsJsonObject();
                
                // 解析 text
                if (delta.has("text") && !delta.get("text").isJsonNull()) {
                    text = delta.get("text").getAsString();
                }
                
                // 解析 functionCall
                if (delta.has("functionCall") && delta.get("functionCall").isJsonObject()) {
                    JsonObject functionCall = delta.getAsJsonObject("functionCall");
                    String name = functionCall.has("name") ? functionCall.get("name").getAsString() : null;
                    JsonObject args = functionCall.has("args") ? functionCall.getAsJsonObject("args") : new JsonObject();
                    
                    toolCall = new VertexAiToolCall(name, args);
                }
            }
        }
        
        return new VertexAiStreamData(text, toolCall);
    }
    
    // ==================== 响应数据类 ====================
    
    /**
     * OpenAI 响应数据
     */
    public static class OpenAiResponse {
        private final String text;
        private final List<OpenAIToolCall> toolCalls;
        
        public OpenAiResponse(String text, List<OpenAIToolCall> toolCalls) {
            this.text = text;
            this.toolCalls = toolCalls;
        }
        
        public String text() { return text; }
        public List<OpenAIToolCall> toolCalls() { return toolCalls; }
        public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
    }
    
    /**
     * OpenAI 流式响应数据
     */
    public static class OpenAiStreamData {
        private final String text;
        private final OpenAIToolCall toolCall;
        
        public OpenAiStreamData(String text, OpenAIToolCall toolCall) {
            this.text = text;
            this.toolCall = toolCall;
        }
        
        public String text() { return text; }
        public OpenAIToolCall toolCall() { return toolCall; }
        public boolean hasToolCall() { return toolCall != null; }
    }
    
    /**
     * Vertex AI 响应数据
     */
    public static class VertexAiResponse {
        private final String text;
        private final List<VertexAiToolCall> toolCalls;
        
        public VertexAiResponse(String text, List<VertexAiToolCall> toolCalls) {
            this.text = text;
            this.toolCalls = toolCalls;
        }
        
        public String text() { return text; }
        public List<VertexAiToolCall> toolCalls() { return toolCalls; }
        public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
    }
    
    /**
     * Vertex AI 流式响应数据
     */
    public static class VertexAiStreamData {
        private final String text;
        private final VertexAiToolCall toolCall;
        
        public VertexAiStreamData(String text, VertexAiToolCall toolCall) {
            this.text = text;
            this.toolCall = toolCall;
        }
        
        public String text() { return text; }
        public VertexAiToolCall toolCall() { return toolCall; }
        public boolean hasToolCall() { return toolCall != null; }
    }
    
    /**
     * Vertex AI Tool Call 数据
     */
    public static class VertexAiToolCall {
        private final String name;
        private final JsonObject arguments;
        
        public VertexAiToolCall(String name, JsonObject arguments) {
            this.name = name;
            this.arguments = arguments;
        }
        
        public String name() { return name; }
        public JsonObject arguments() { return arguments; }
    }
}
