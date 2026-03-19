package com.mineclawd.modules.api;

import com.mineclawd.modules.core.*;
import com.mineclawd.modules.registry.ModuleRegistry;
import com.mineclawd.llm.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * LLM模块调用接口
 * 
 * <p>基于现有LLM客户端的模块化调用接口</p>
 * 
 * @author MineClawd Team
 * @version 1.0.0
 * @since 2026-03-18
 */
public class LLMModuleAPI {
    
    private final ModuleRegistry moduleRegistry;
    private final OpenAIClient openAIClient;
    private final VertexAIClient vertexAIClient;
    
    public LLMModuleAPI(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
        this.openAIClient = new OpenAIClient();
        this.vertexAIClient = new VertexAIClient();
    }
    
    /**
     * 获取所有模块的OpenAI工具定义
     */
    public List<OpenAITool> getAllOpenAITools() {
        List<OpenAITool> tools = new ArrayList<>();
        
        for (BaseModule module : moduleRegistry.getAllModules()) {
            if (module instanceof ExecutableModule) {
                LLMDescription llmDesc = module.getLLMDescription();
                
                JsonObject parameters = JsonParser.parseString(llmDesc.getParameters())
                    .getAsJsonObject();
                
                OpenAITool tool = new OpenAITool(
                    llmDesc.getFunctionName(),
                    llmDesc.getDescription(),
                    parameters
                );
                tools.add(tool);
            }
        }
        
        return tools;
    }
    
    /**
     * 获取所有模块的Vertex AI函数定义
     */
    public List<VertexAIFunction> getAllVertexAIFunctions() {
        List<VertexAIFunction> functions = new ArrayList<>();
        
        for (BaseModule module : moduleRegistry.getAllModules()) {
            if (module instanceof ExecutableModule) {
                LLMDescription llmDesc = module.getLLMDescription();
                
                JsonObject parameters = JsonParser.parseString(llmDesc.getParameters())
                    .getAsJsonObject();
                
                VertexAIFunction function = new VertexAIFunction(
                    llmDesc.getFunctionName(),
                    llmDesc.getDescription(),
                    parameters
                );
                functions.add(function);
            }
        }
        
        return functions;
    }
    
    /**
     * 执行OpenAI函数调用
     */
    public ExecutionResult executeOpenAIFunctionCall(OpenAIToolCall toolCall) {
        try {
            // 查找对应的模块
            BaseModule module = findModuleByFunctionName(toolCall.name());
            if (module == null || !(module instanceof ExecutableModule)) {
                return ExecutionResult.failure("unknown_function", 
                    "未知函数: " + toolCall.name());
            }
            
            ExecutableModule executableModule = (ExecutableModule) module;
            
            // 解析参数
            Map<String, Object> parameters = parseArguments(toolCall.arguments());
            
            // 创建执行请求
            ExecutionRequest request = ExecutionRequest.builder()
                .moduleId(module.getModuleId())
                .parameters(parameters)
                .build();
            
            // 执行模块
            return executableModule.execute(request);
            
        } catch (Exception e) {
            return ExecutionResult.failure("execution_error", 
                "函数执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行Vertex AI函数调用
     */
    public ExecutionResult executeVertexAIFunctionCall(VertexAIToolCall toolCall) {
        try {
            // 查找对应的模块
            BaseModule module = findModuleByFunctionName(toolCall.name());
            if (module == null || !(module instanceof ExecutableModule)) {
                return ExecutionResult.failure("unknown_function", 
                    "未知函数: " + toolCall.name());
            }
            
            ExecutableModule executableModule = (ExecutableModule) module;
            
            // 解析参数（VertexAI使用JsonObject）
            Map<String, Object> parameters = parseJsonObject(toolCall.args());
            
            // 创建执行请求
            ExecutionRequest request = ExecutionRequest.builder()
                .moduleId(module.getModuleId())
                .parameters(parameters)
                .build();
            
            // 执行模块
            return executableModule.execute(request);
            
        } catch (Exception e) {
            return ExecutionResult.failure("execution_error", 
                "函数执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 异步执行OpenAI函数调用
     */
    public CompletableFuture<ExecutionResult> executeOpenAIFunctionCallAsync(OpenAIToolCall toolCall) {
        return CompletableFuture.supplyAsync(() -> executeOpenAIFunctionCall(toolCall));
    }
    
    /**
     * 异步执行Vertex AI函数调用
     */
    public CompletableFuture<ExecutionResult> executeVertexAIFunctionCallAsync(VertexAIToolCall toolCall) {
        return CompletableFuture.supplyAsync(() -> executeVertexAIFunctionCall(toolCall));
    }
    
    /**
     * 根据函数名查找模块
     */
    private BaseModule findModuleByFunctionName(String functionName) {
        for (BaseModule module : moduleRegistry.getAllModules()) {
            if (module instanceof ExecutableModule) {
                LLMDescription llmDesc = module.getLLMDescription();
                if (functionName.equals(llmDesc.getFunctionName())) {
                    return module;
                }
            }
        }
        return null;
    }
    
    /**
     * 解析函数参数
     */
    private Map<String, Object> parseArguments(String arguments) {
        try {
            JsonObject jsonArgs = JsonParser.parseString(arguments).getAsJsonObject();
            Map<String, Object> parameters = new HashMap<>();
            
            for (String key : jsonArgs.keySet()) {
                parameters.put(key, parseJsonValue(jsonArgs.get(key)));
            }
            
            return parameters;
        } catch (Exception e) {
            throw new IllegalArgumentException("参数解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析JSON值
     */
    private Object parseJsonValue(com.google.gson.JsonElement element) {
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            } else if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
        } else if (element.isJsonObject()) {
            Map<String, Object> map = new HashMap<>();
            for (var entry : element.getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), parseJsonValue(entry.getValue()));
            }
            return map;
        } else if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (var item : element.getAsJsonArray()) {
                list.add(parseJsonValue(item));
            }
            return list;
        }
        
        return null;
    }
    
    /**
     * 解析JsonObject为Map
     */
    private Map<String, Object> parseJsonObject(com.google.gson.JsonObject jsonObject) {
        Map<String, Object> parameters = new HashMap<>();
        
        for (String key : jsonObject.keySet()) {
            parameters.put(key, parseJsonValue(jsonObject.get(key)));
        }
        
        return parameters;
    }
    
    /**
     * 获取模块统计信息
     */
    public ModuleStats getModuleStats() {
        int totalModules = moduleRegistry.getAllModules().size();
        int executableModules = 0;
        
        for (BaseModule module : moduleRegistry.getAllModules()) {
            if (module instanceof ExecutableModule) {
                executableModules++;
            }
        }
        
        return new ModuleStats(totalModules, executableModules);
    }
    
    /**
     * 模块统计信息
     */
    public static class ModuleStats {
        private final int totalModules;
        private final int executableModules;
        
        public ModuleStats(int totalModules, int executableModules) {
            this.totalModules = totalModules;
            this.executableModules = executableModules;
        }
        
        public int getTotalModules() {
            return totalModules;
        }
        
        public int getExecutableModules() {
            return executableModules;
        }
    }
}