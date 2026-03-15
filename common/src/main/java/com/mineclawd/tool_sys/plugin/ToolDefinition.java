package com.mineclawd.tool_sys.plugin;

import com.mineclawd.tool_sys.plugin.annotation.ToolExecutor;
import com.mineclawd.tool_sys.plugin.annotation.ToolParameter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具定义类
 * 描述一个工具执行器的完整信息
 */
public class ToolDefinition {
    
    private final String name;
    private final String description;
    private final String category;
    private final Method method;
    private final Object pluginInstance;
    private final List<ParameterDefinition> parameters;
    private final ToolExecutor.ParameterMode parameterMode;
    private boolean enabled;
    private final String source;
    
    public ToolDefinition(String name, String description, String category, 
                         Method method, Object pluginInstance, 
                         ToolExecutor.ParameterMode parameterMode) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.method = method;
        this.pluginInstance = pluginInstance;
        this.parameterMode = parameterMode;
        this.enabled = true;
        this.source = extractSource(pluginInstance);
        this.parameters = extractParameters(method);
    }
    
    private String extractSource(Object pluginInstance) {
        if (pluginInstance instanceof AbstractPlugin plugin) {
            String pluginId = plugin.getPluginId();
            if ("mineclawd".equals(pluginId)) {
                return "mineclawd";
            }
            return "mod:" + pluginId;
        }
        return "unknown";
    }
    
    private List<ParameterDefinition> extractParameters(Method method) {
        List<ParameterDefinition> params = new ArrayList<>();
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        
        for (int i = 0; i < methodParams.length; i++) {
            java.lang.reflect.Parameter param = methodParams[i];
            ToolParameter annotation = param.getAnnotation(ToolParameter.class);
            
            if (annotation != null) {
                params.add(new ParameterDefinition(
                    annotation.name(),
                    annotation.description(),
                    annotation.type(),
                    annotation.required(),
                    annotation.defaultValue()
                ));
            } else {
                // 如果没有注解，使用默认参数定义
                params.add(new ParameterDefinition(
                    "arg" + i,
                    "Parameter " + i,
                    ToolParameter.ParameterType.STRING,
                    true,
                    ""
                ));
            }
        }
        
        return params;
    }
    
    // Getter方法
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Method getMethod() { return method; }
    public Object getPluginInstance() { return pluginInstance; }
    public List<ParameterDefinition> getParameters() { return parameters; }
    public ToolExecutor.ParameterMode getParameterMode() { return parameterMode; }
    public boolean isEnabled() { return enabled; }
    public String getSource() { return source; }
    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    /**
     * 参数定义内部类
     */
    public static class ParameterDefinition {
        private final String name;
        private final String description;
        private final ToolParameter.ParameterType type;
        private final boolean required;
        private final String defaultValue;
        
        public ParameterDefinition(String name, String description, 
                                  ToolParameter.ParameterType type, 
                                  boolean required, String defaultValue) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
        }
        
        // Getter方法
        public String getName() { return name; }
        public String getDescription() { return description; }
        public ToolParameter.ParameterType getType() { return type; }
        public boolean isRequired() { return required; }
        public String getDefaultValue() { return defaultValue; }
    }
}