# AgentProtocolHandler 使用指南

## 概述

`AgentProtocolHandler` 是一个专门处理 Agent 与 LLM 通信 JSON 协议的类。它负责：

- **构建 JSON 请求**：构造发送给 LLM 的请求体
- **解析 JSON 响应**：解析 LLM 返回的响应
- **不包含网络通信**：只处理 JSON 数据结构，不涉及 HTTP 请求

## 设计原则

1. **单一职责**：只负责 JSON 构建和解析
2. **无状态**：所有方法都是静态的
3. **与 LLM 解耦**：不依赖具体的 LLM 客户端实现
4. **支持多种协议**：支持 OpenAI 和 Vertex AI

## 使用示例

### 1. 构建 OpenAI 请求

```java
import com.mineclawd.agent.AgentProtocolHandler;
import com.mineclawd.llm.OpenAIMessage;
import com.mineclawd.llm.OpenAITool;

// 构建对话历史
List<OpenAIMessage> history = new ArrayList<>();
history.add(OpenAIMessage.system("You are a helpful assistant."));
history.add(OpenAIMessage.user("帮我读取文件"));

// 创建工具列表
List<OpenAITool> tools = ToolFactory.createOpenAiTools(true, true);

// 构建请求体
JsonObject request = AgentProtocolHandler.buildOpenAiRequest(
    "gpt-4o",           // 模型名称
    history,            // 对话历史
    tools,              // 工具列表
    false               // 是否流式
);

// 发送请求（使用 OpenAIClient.sendMessage）
String jsonRequest = GSON.toJson(request);
// ... 发送 HTTP 请求 ...
```

### 2. 构建 Vertex AI 请求

```java
import com.mineclawd.agent.AgentProtocolHandler;
import com.mineclawd.llm.VertexAIMessage;
import com.mineclawd.llm.VertexAIFunction;

// 构建对话历史
List<VertexAIMessage> history = new ArrayList<>();
history.add(new VertexAIMessage("user", List.of(new JsonPrimitive("帮我读取文件"))));

// 创建工具列表
List<VertexAIFunction> tools = ToolFactory.createVertexTools(true, true);

// 构建请求体
JsonObject request = AgentProtocolHandler.buildVertexAiRequest(
    "models/gemini-pro",  // 模型路径
    history,              // 对话历史
    tools,                // 工具列表
    false                 // 是否流式
);

// 发送请求（使用 VertexAIClient.sendMessage）
String jsonRequest = GSON.toJson(request);
// ... 发送 HTTP 请求 ...
```

### 3. 解析 OpenAI 响应

```java
import com.mineclawd.agent.AgentProtocolHandler;

// LLM 返回的响应体
String responseBody = """
{
  "choices": [{
    "message": {
      "content": "文件内容：...",
      "tool_calls": [{
        "id": "call_abc123",
        "type": "function",
        "function": {
          "name": "read-files",
          "arguments": "{\"path\":\"C:/...\"}"
        }
      }]
    }
  }]
}
""";

// 解析响应
AgentProtocolHandler.OpenAiResponse response = 
    AgentProtocolHandler.parseOpenAiResponse(responseBody);

// 获取文本内容
String text = response.text();

// 获取工具调用
List<OpenAIToolCall> toolCalls = response.toolCalls();
for (OpenAIToolCall call : toolCalls) {
    System.out.println("Tool: " + call.name());
    System.out.println("Arguments: " + call.arguments());
}
```

### 4. 解析 Vertex AI 响应

```java
import com.mineclawd.agent.AgentProtocolHandler;

// LLM 返回的响应体
String responseBody = """
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "文件内容：..."
      }, {
        "functionCall": {
          "name": "read-files",
          "args": {"path": "C:/..."}
        }
      }]
    }
  }]
}
""";

// 解析响应
AgentProtocolHandler.VertexAiResponse response = 
    AgentProtocolHandler.parseVertexAiResponse(responseBody);

// 获取文本内容
String text = response.text();

// 获取工具调用
List<VertexAiToolCall> toolCalls = response.toolCalls();
for (AgentProtocolHandler.VertexAiToolCall call : toolCalls) {
    System.out.println("Tool: " + call.name());
    System.out.println("Arguments: " + call.arguments());
}
```

### 5. 流式响应处理

```java
import com.mineclawd.agent.AgentProtocolHandler;

// 流式响应数据
String chunk = """
{
  "choices": [{
    "delta": {
      "content": "文件"
    }
  }]
}
""";

// 解析流式数据
AgentProtocolHandler.OpenAiStreamData data = 
    AgentProtocolHandler.parseOpenAiStream(chunk);

// 获取增量内容
String text = data.text();  // "文件"
AgentProtocolHandler.OpenAIToolCall toolCall = data.toolCall();

if (toolCall != null) {
    System.out.println("Tool Call: " + toolCall.name());
}
```

## API 参考

### OpenAI 请求构建

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `buildOpenAiRequest` | `model, history, tools, stream` | `JsonObject` | 构建完整的 OpenAI 请求体 |
| `buildMessagesArray` | `history` | `JsonArray` | 构建 messages 数组（内部使用） |
| `buildToolsArray` | `tools` | `JsonArray` | 构建 tools 数组（内部使用） |

### Vertex AI 请求构建

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `buildVertexAiRequest` | `model, history, tools, stream` | `JsonObject` | 构建完整的 Vertex AI 请求体 |
| `buildContentsArray` | `history` | `JsonArray` | 构建 contents 数组（内部使用） |
| `buildToolObject` | `tools` | `JsonObject` | 构建 tool 对象（内部使用） |
| `buildToolConfig` | 无 | `JsonObject` | 构建 toolConfig 对象（内部使用） |

### OpenAI 响应解析

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `parseOpenAiResponse` | `responseBody` | `OpenAiResponse` | 解析完整的 OpenAI 响应 |
| `parseOpenAiStream` | `responseBody` | `OpenAiStreamData` | 解析流式 OpenAI 响应 |

### Vertex AI 响应解析

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `parseVertexAiResponse` | `responseBody` | `VertexAiResponse` | 解析完整的 Vertex AI 响应 |
| `parseVertexAiStream` | `responseBody` | `VertexAiStreamData` | 解析流式 Vertex AI 响应 |

## 响应数据类

### OpenAiResponse

```java
public static class OpenAiResponse {
    public String text()           // 文本内容
    public List<OpenAIToolCall> toolCalls()  // 工具调用列表
    public boolean hasToolCalls()  // 是否有工具调用
}
```

### OpenAiStreamData

```java
public static class OpenAiStreamData {
    public String text()           // 增量文本
    public OpenAIToolCall toolCall()  // 工具调用
    public boolean hasToolCall()   // 是否有工具调用
}
```

### VertexAiResponse

```java
public static class VertexAiResponse {
    public String text()                    // 文本内容
    public List<VertexAiToolCall> toolCalls()  // 工具调用列表
    public boolean hasToolCalls()           // 是否有工具调用
}
```

### VertexAiStreamData

```java
public static class VertexAiStreamData {
    public String text()                    // 增量文本
    public VertexAiToolCall toolCall()      // 工具调用
    public boolean hasToolCall()            // 是否有工具调用
}
```

### VertexAiToolCall

```java
public static class VertexAiToolCall {
    public String name()           // 工具名称
    public JsonObject arguments()  // 参数（JSON 对象）
}
```

## 与现有代码的集成

### 重构 runOpenAiAgent

**之前**：
```java
private void runOpenAiAgent(...) {
    // ... 省略 ...
    
    // 直接构建请求
    JsonObject body = new JsonObject();
    body.addProperty("model", config.model);
    // ... 手动构建 messages ...
    // ... 手动构建 tools ...
    
    CompletableFuture<OpenAIResponse> requestFuture = OPENAI_CLIENT.sendMessage(
            config.endpoint,
            config.apiKey,
            config.model,
            history,
            ToolFactory.createOpenAiTools(...),
            ...
    );
}
```

**之后**：
```java
private void runOpenAiAgent(...) {
    // ... 省略 ...
    
    // 使用 AgentProtocolHandler 构建请求
    JsonObject request = AgentProtocolHandler.buildOpenAiRequest(
            config.model,
            history,
            ToolFactory.createOpenAiTools(...),
            runtime.clientStreamEnabled()
    );
    
    // 发送请求（JSON 已经构建好）
    String jsonRequest = GSON.toJson(request);
    // ... 发送 HTTP 请求 ...
    
    // 解析响应
    AgentProtocolHandler.OpenAiResponse response = 
            AgentProtocolHandler.parseOpenAiResponse(responseBody);
    
    // 处理响应
    String text = response.text();
    List<OpenAIToolCall> toolCalls = response.toolCalls();
}
```

## 优势

1. **清晰的职责分离**：
   - `AgentProtocolHandler`：负责 JSON 构建和解析
   - `OpenAIClient`：负责 HTTP 通信
   - `MineClawd`：负责业务逻辑

2. **易于测试**：
   - 可以独立测试 JSON 构建和解析逻辑
   - 不需要真实的网络请求

3. **易于维护**：
   - JSON 协议变更时只需修改一处
   - 支持多种 LLM 提供商

4. **代码复用**：
   - 所有 JSON 处理逻辑集中在一处
   - 避免重复代码

## 注意事项

1. **线程安全**：所有方法都是静态的，无状态，线程安全
2. **空值处理**：所有方法都处理了 null 值
3. **错误处理**：解析方法返回默认值，不抛出异常
4. **版本兼容**：只处理已知的字段，忽略未知字段
