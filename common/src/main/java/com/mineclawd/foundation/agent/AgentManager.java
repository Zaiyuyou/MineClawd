package com.mineclawd.foundation.agent;

import com.mineclawd.MineClawd;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AgentManager {
    public static final String DEFAULT_AGENT = "default";
    private static final String FILE_EXTENSION = ".md";
    private static final Pattern OWNER_SANITIZE = Pattern.compile("[^a-zA-Z0-9._-]");

    public static final String PROMPT_TYPE_BASE = "base";
    public static final String PROMPT_TYPE_DYNAMIC_REGISTRY = "dynamic_registry";
    public static final String PROMPT_TYPE_ASSET_TRACKING = "asset_tracking";
    
    private final Path agentsRoot;
    private final Path activeRoot;
    private static final String ACTIVE_FILE_EXTENSION = ".txt";

    public AgentManager() {
        Path mineclawdRoot = Platform.getGameFolder().resolve("mineclawd");
        this.agentsRoot = mineclawdRoot.resolve("agents");
        this.activeRoot = agentsRoot.resolve(".active");
        ensureDirectory(agentsRoot);
        ensureDirectory(activeRoot);
        ensureBundledAgents();
    }

    public synchronized List<String> listAgentNames() {
        ensureBundledAgents();
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(agentsRoot)) {
            stream.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> !name.startsWith("."))
                    .filter(name -> !name.isBlank())
                    .forEach(names::add);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to list agents in {}: {}", agentsRoot, exception.getMessage());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public synchronized String resolveAgentName(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        String wanted = reference.trim().toLowerCase(Locale.ROOT);
        for (String name : listAgentNames()) {
            if (name.toLowerCase(Locale.ROOT).equals(wanted)) {
                return name;
            }
        }
        return null;
    }

    public synchronized Agent loadActiveAgent(String ownerKey) {
        ensureBundledAgents();
        String agentName = getActiveAgentName(ownerKey);
        String basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
        String dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);
        String assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);
        
        if (basePrompt == null) {
            agentName = DEFAULT_AGENT;
            basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
            dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);
            assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);
        }
        
        if (basePrompt == null) {
            basePrompt = "";
            dynamicRegistryPrompt = "";
            assetTrackingPrompt = "";
        }
        
        return new Agent(agentName, basePrompt, dynamicRegistryPrompt, assetTrackingPrompt);
    }

    public synchronized String getActiveAgentName(String ownerKey) {
        ensureBundledAgents();
        String selected = readSelectedAgent(ownerKey);
        String resolved = resolveAgentName(selected);
        if (resolved != null) {
            return resolved;
        }
        return DEFAULT_AGENT;
    }

    public synchronized boolean setActiveAgent(String ownerKey, String agentReference) {
        String resolved = resolveAgentName(agentReference);
        if (resolved == null) {
            return false;
        }
        writeSelectedAgent(ownerKey, resolved);
        return true;
    }

    public synchronized String readAgentPromptContent(String agentName, String promptType) {
        String resolved = resolveAgentName(agentName);
        if (resolved == null) {
            return null;
        }
        Path path = agentsRoot.resolve(resolved).resolve(promptType + FILE_EXTENSION);
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to read agent prompt {}: {}", path, exception.getMessage());
            return null;
        }
    }

    private void ensureBundledAgents() {
        ensureDefaultAgent();
        ensureDumAgent();
    }

    private void ensureDefaultAgent() {
        Path defaultAgentDir = agentsRoot.resolve(DEFAULT_AGENT);
        ensureDirectory(defaultAgentDir);
        
        ensureFileFromResourceIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_BASE + FILE_EXTENSION), "/mineclawd/agents/default/base.md");
        ensureFileFromResourceIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_DYNAMIC_REGISTRY + FILE_EXTENSION), "/mineclawd/agents/default/dynamic_registry.md");
        ensureFileFromResourceIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_ASSET_TRACKING + FILE_EXTENSION), "/mineclawd/agents/default/asset_tracking.md");
    }
    
    private void ensureDumAgent() {
        Path dumAgentDir = agentsRoot.resolve("dum");
        ensureDirectory(dumAgentDir);
        
        ensureFileFromResourceIfMissing(dumAgentDir.resolve(PROMPT_TYPE_BASE + FILE_EXTENSION), "/mineclawd/agents/dum/base.md");
        ensureFileFromResourceIfMissing(dumAgentDir.resolve(PROMPT_TYPE_DYNAMIC_REGISTRY + FILE_EXTENSION), "/mineclawd/agents/dum/dynamic_registry.md");
        ensureFileFromResourceIfMissing(dumAgentDir.resolve(PROMPT_TYPE_ASSET_TRACKING + FILE_EXTENSION), "/mineclawd/agents/dum/asset_tracking.md");
    }
    
    private void ensureDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create agent directory: " + path, exception);
        }
    }
    
    private void ensureFileFromResourceIfMissing(Path target, String resourcePath) {
        if (Files.isRegularFile(target)) {
            return;
        }
        try (InputStream stream = AgentManager.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                MineClawd.LOGGER.warn("Bundled agent resource not found: {}", resourcePath);
                return;
            }
            Files.createDirectories(target.getParent());
            Files.write(target, stream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to write bundled agent {}: {}", target, exception.getMessage());
        }
    }
    
    private String readSelectedAgent(String ownerKey) {
        Path path = activeRoot.resolve(safeOwner(ownerKey) + ACTIVE_FILE_EXTENSION);
        if (!Files.isRegularFile(path)) {
            return DEFAULT_AGENT;
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to read active agent {}: {}", path, exception.getMessage());
            return DEFAULT_AGENT;
        }
    }
    
    private void writeSelectedAgent(String ownerKey, String agentName) {
        Path path = activeRoot.resolve(safeOwner(ownerKey) + ACTIVE_FILE_EXTENSION);
        try {
            Files.writeString(path, agentName + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to persist active agent: " + path, exception);
        }
    }
    
    private String safeOwner(String ownerKey) {
        String value = ownerKey == null ? "" : ownerKey.trim();
        value = OWNER_SANITIZE.matcher(value).replaceAll("_");
        if (value.isBlank()) {
            return "unknown";
        }
        return value;
    }

    public record Agent(String name, String basePrompt, String dynamicRegistryPrompt, String assetTrackingPrompt) {
        public boolean hasBasePrompt() {
            return basePrompt != null && !basePrompt.isBlank();
        }
        
        public boolean hasDynamicRegistryPrompt() {
            return dynamicRegistryPrompt != null && !dynamicRegistryPrompt.isBlank();
        }
        
        public boolean hasAssetTrackingPrompt() {
            return assetTrackingPrompt != null && !assetTrackingPrompt.isBlank();
        }
    }
}