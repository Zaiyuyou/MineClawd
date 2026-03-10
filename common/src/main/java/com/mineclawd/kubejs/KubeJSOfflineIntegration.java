package com.mineclawd.kubejs;

import dev.architectury.platform.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * KubeJSOffline集成类，用于动态生成精简版KubeJS知识库
 * 避免使用过时的硬编码语法，实时获取当前游戏版本的API信息
 */
public class KubeJSOfflineIntegration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String KUBEJS_PACKAGE_PREFIX = "dev.latvian.mods.kubejs";
    
    /**
     * 检查KubeJSOffline是否可用
     */
    public static boolean isKubeJSOfflineAvailable() {
        try {
            // 使用反射检查KubeJSOffline类是否存在
            Class<?> kubeJSOfflineClass = Class.forName("pie.ilikepiefoo.kubejsoffline.KubeJSOffline");
            java.lang.reflect.Field helperField = kubeJSOfflineClass.getField("HELPER");
            Object helper = helperField.get(null);
            return helper != null;
        } catch (Exception e) {
            LOGGER.debug("KubeJSOffline not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成基础KubeJS知识库（回退方案）
     */
    private static String generateFallbackKubeJSKnowledge() {
        StringBuilder knowledge = new StringBuilder();
        knowledge.append("# KubeJS基础知识库（回退方案）\n\n");
        knowledge.append("**生成时间**: ").append(java.time.LocalDateTime.now()).append("\n\n");
        knowledge.append("**Minecraft版本**: ").append(getMinecraftVersion()).append("\n\n");
        knowledge.append("**注意**: KubeJSOffline不可用，以下是基础KubeJS信息\n\n");
        
        knowledge.append("## 基础事件系统\n\n");
        knowledge.append("### 服务器事件\n");
        knowledge.append("- `server.load()` - 服务器加载时触发\n");
        knowledge.append("- `server.unload()` - 服务器卸载时触发\n");
        knowledge.append("- `server.tick()` - 服务器每tick触发\n\n");
        
        knowledge.append("### 客户端事件\n");
        knowledge.append("- `client.loggedIn()` - 客户端登录时触发\n");
        knowledge.append("- `client.loggedOut()` - 客户端登出时触发\n");
        knowledge.append("- `client.tick()` - 客户端每tick触发\n\n");
        
        knowledge.append("### 物品事件\n");
        knowledge.append("- `item.rightClicked()` - 物品右键点击时触发\n");
        knowledge.append("- `item.entityInteracted()` - 物品与实体交互时触发\n\n");
        
        knowledge.append("## 基础工具类\n\n");
        knowledge.append("### 物品操作\n");
        knowledge.append("- `Item.of()` - 创建物品实例\n");
        knowledge.append("- `Item.get()` - 获取物品\n");
        knowledge.append("- `Item.modify()` - 修改物品属性\n\n");
        
        knowledge.append("### 方块操作\n");
        knowledge.append("- `Block.of()` - 创建方块实例\n");
        knowledge.append("- `Block.get()` - 获取方块\n");
        knowledge.append("- `Block.modify()` - 修改方块属性\n\n");
        
        knowledge.append("## 安装KubeJSOffline\n\n");
        knowledge.append("要获取完整的动态API信息，请安装KubeJSOffline模组。\n");
        knowledge.append("安装后使用命令: `").append(getDocumentationCommand()).append("` 生成完整文档。\n");
        
        return knowledge.toString();
    }
    
    /**
     * 生成精简版KubeJS知识库
     * 只包含通过KubeJSOffline动态发现的API信息，无硬编码内容
     */
    public static String generateConciseKubeJSKnowledge() {
        if (!isKubeJSOfflineAvailable()) {
            return generateFallbackKubeJSKnowledge();
        }
        
        try {
            StringBuilder knowledge = new StringBuilder();
            knowledge.append("# KubeJS动态知识库\n\n");
            knowledge.append("**生成时间**: ").append(java.time.LocalDateTime.now()).append("\n\n");
            
            // 使用反射获取KubeJS相关类
            Class<?> kubeJSOfflineClass = Class.forName("pie.ilikepiefoo.kubejsoffline.KubeJSOffline");
            java.lang.reflect.Field helperField = kubeJSOfflineClass.getField("HELPER");
            Object helper = helperField.get(null);
            
            // 调用getClasses方法
            java.lang.reflect.Method getClassesMethod = helper.getClass().getMethod("getClasses");
            Class[] classes = (Class[]) getClassesMethod.invoke(helper);
            
            List<Class> kubejsClasses = filterKubeJSClasses(classes);
            
            knowledge.append("## 动态发现的KubeJS API\n\n");
            knowledge.append(generateDynamicAPIInfo(kubejsClasses));
            
            return knowledge.toString();
            
        } catch (Exception e) {
            LOGGER.warn("Failed to generate KubeJS knowledge: {}", e.getMessage());
            return generateFallbackKubeJSKnowledge();
        }
    }
    
    /**
     * 过滤出KubeJS相关的类
     */
    private static List<Class> filterKubeJSClasses(Class[] classes) {
        List<Class> kubejsClasses = new ArrayList<>();
        for (Class clazz : classes) {
            if (clazz != null && clazz.getName().startsWith(KUBEJS_PACKAGE_PREFIX)) {
                kubejsClasses.add(clazz);
            }
        }
        return kubejsClasses;
    }
    
    /**
     * 生成动态API信息，只包含通过反射发现的类和方法
     */
    private static String generateDynamicAPIInfo(List<Class> kubejsClasses) {
        StringBuilder info = new StringBuilder();
        
        // 按类别分组
        List<Class> eventClasses = kubejsClasses.stream()
                .filter(clazz -> clazz.getSimpleName().contains("Event"))
                .collect(Collectors.toList());
        
        List<Class> utilityClasses = kubejsClasses.stream()
                .filter(clazz -> clazz.getSimpleName().contains("Utils") || 
                                clazz.getSimpleName().contains("Item") ||
                                clazz.getSimpleName().contains("Block") ||
                                clazz.getSimpleName().contains("Fluid"))
                .collect(Collectors.toList());
        
        // 事件系统
        if (!eventClasses.isEmpty()) {
            info.append("### 事件系统\n");
            for (Class eventClass : eventClasses) {
                if (eventClass.getSimpleName().endsWith("Events")) {
                    info.append("#### ").append(eventClass.getSimpleName()).append("\n");
                    
                    java.lang.reflect.Method[] methods = eventClass.getMethods();
                    for (java.lang.reflect.Method method : methods) {
                        if (method.getParameterCount() == 1 && 
                            method.getParameterTypes()[0].getSimpleName().contains("Consumer")) {
                            info.append("- `").append(method.getName()).append("()`\n");
                        }
                    }
                    info.append("\n");
                }
            }
        }
        
        // 工具类
        if (!utilityClasses.isEmpty()) {
            info.append("### 工具类\n");
            for (Class utilClass : utilityClasses) {
                info.append("#### ").append(utilClass.getSimpleName()).append("\n");
                
                java.lang.reflect.Method[] methods = utilClass.getMethods();
                for (java.lang.reflect.Method method : methods) {
                    if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                        java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                        info.append("- `").append(method.getName()).append("()`\n");
                    }
                }
                info.append("\n");
            }
        }
        
        // 其他类
        List<Class> otherClasses = kubejsClasses.stream()
                .filter(clazz -> !eventClasses.contains(clazz) && !utilityClasses.contains(clazz))
                .collect(Collectors.toList());
        
        if (!otherClasses.isEmpty()) {
            info.append("### 其他API类\n");
            for (Class otherClass : otherClasses) {
                info.append("- `").append(otherClass.getSimpleName()).append("`\n");
            }
        }
        
        return info.toString();
    }
    
    /**
     * 获取Minecraft版本
     */
    private static String getMinecraftVersion() {
        try {
            return Platform.getMinecraftVersion();
        } catch (Exception e) {
            return "未知版本";
        }
    }
    
    /**
     * 检查文档是否已生成
     */
    public static boolean isDocumentationGenerated() {
        try {
            Path docsPath = Platform.getGameFolder().resolve("kubejs/documentation");
            return Files.exists(docsPath) && Files.isDirectory(docsPath);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取文档生成命令
     */
    public static String getDocumentationCommand() {
        return "/kubejs offline_docs generate";
    }
}