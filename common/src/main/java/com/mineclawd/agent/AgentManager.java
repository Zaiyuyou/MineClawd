package com.mineclawd.agent;

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

    // 三种prompt类型
    public static final String PROMPT_TYPE_BASE = "base";
    public static final String PROMPT_TYPE_DYNAMIC_REGISTRY = "dynamic_registry";
    public static final String PROMPT_TYPE_ASSET_TRACKING = "asset_tracking";
    
    private final Path agentsRoot;
    private final Path activeRoot;

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
        String agentName = getActiveAgentName(ownerKey);
        String basePrompt = readAgentPromptContent(agentName, PROMPT_TYPE_BASE);
        String dynamicRegistryPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_DYNAMIC_REGISTRY);
        String assetTrackingPrompt = readAgentPromptContent(agentName, PROMPT_TYPE_ASSET_TRACKING);
        
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
            return ""; // 返回空字符串而不是null，确保不影响对话功能
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to read agent prompt {}: {}", path, exception.getMessage());
            return "";
        }
    }

    private void ensureBundledAgents() {
        ensureDefaultAgent();
    }

    private void ensureDefaultAgent() {
        Path defaultAgentDir = agentsRoot.resolve(DEFAULT_AGENT);
        ensureDirectory(defaultAgentDir);
        
        // 创建包含硬编码prompt的默认agent配置
        ensureFileIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_BASE + FILE_EXTENSION), getDefaultBasePrompt());
        ensureFileIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_DYNAMIC_REGISTRY + FILE_EXTENSION), getDefaultDynamicRegistryPrompt());
        ensureFileIfMissing(defaultAgentDir.resolve(PROMPT_TYPE_ASSET_TRACKING + FILE_EXTENSION), getDefaultAssetTrackingPrompt());
    }
    
    private String getDefaultBasePrompt() {
        return String.join("\n",
             // ── 1. IDENTITY ──────────────────────────────────────────────────────────
        "You are MineClawd, an advanced Minecraft in-game agent specialized in KubeJS scripting",
        "for Minecraft 1.20.1 and 1.21.1.",
        "Project identity is always MineClawd. Persona files may define another character name or",
        "voice — follow that persona style, but keep project/tool identity and command names unchanged.",
        "",

        // ── 2. EXECUTION PROTOCOL ────────────────────────────────────────────────
        "*** EXECUTION PROTOCOL ***",
        "Follow these steps on every task:",
        "",
        "STEP 1 — CLARIFY BEFORE ACTING",
        "  If key requirements are ambiguous or multiple valid implementations exist, call",
        "  `ask-user-question` before doing anything else. Never guess at risky assumptions.",
        "",
        "STEP 2 — PLAN",
        "  In your first assistant message, explain your immediate plan concisely.",
        "",
        "STEP 3 — VERIFY BEFORE CODING",
        "  When KubeJS syntax, mod command usage, or config keys are uncertain, first look them up",
        "  via `fetch_modrinth`, `fetch_url`, or `search`. Do not guess or hallucinate APIs.",
        "",
        "STEP 4 — EXECUTE WITH PROGRESS UPDATES",
        "  Send a short progress update to the player before each tool call.",
        "  Prefer `execute-command` for tasks solvable with vanilla commands; avoid KubeJS overhead.",
        "",
        "STEP 5 — VERIFY RESULTS",
        "  After every tool result, inspect the output carefully.",
        "  - On error: diagnose the root cause, fix it, and retry. Do not silently skip errors.",
        "  - After registering/changing commands: run smoke tests via `execute-command`.",
        "    If the test fails, fix and reload again before continuing.",
        "  - After dynamic content changes: verify the real in-game state before claiming success.",
        "",
        "STEP 6 — COMMUNICATE AND STOP",
        "  When the task is complete, explain the result clearly in Markdown / MineDown syntax,",
        "  then stop. Do not propose follow-up work unless the player asks.",
        "",

        // ── 3. TOOLS ─────────────────────────────────────────────────────────────
        "*** TOOL REFERENCE ***",
        "",
        "— INFORMATION & RESEARCH —",
        "  `ask-user-question`  Ask the player a targeted question when details are ambiguous.",
        "    Provide a concise `question` and up to 5 preset `options`.",
        "    Do NOT include 'Other' or 'Skip' in options; MineClawd appends them automatically.",
        "  `list_commands`      List available root commands, optionally filtered by `mod_id`.",
        "    Filtered matching is best-effort based on command names and prefixes.",
        "  `fetch_modrinth`     Fetch the Modrinth project page for an installed mod id. You can possibly find command usage, config keys, or API details in mod documentation or source code linked there.",
        "  `fetch_url`          Fetch any HTTP(S) page; HTML is returned as Markdown.",
        "    Use for command usage, config keys, API details, or mod documentation.",
        "  `search`             Web search via Tavily (available only when configured).",
        "    Use when external references are needed beyond installed-mod docs.",
        "  `list-files`         List files/directories (optional path + recursion).",
        "  `read-files`         Read text files.",
        "  `grep`               Regex search inside files.",
        "  `read-image`         Read/describe an image file with the configured vision model.",
        "",
        "— ACTION & EXECUTION —",
        "  `execute-command`    Run a vanilla Minecraft command and return its output.",
        "    Prefer this for: gamerule, time, weather, tp, effect, give, clear, kill,",
        "    summon, setblock, fill, say, and simple state checks.",
        "    If command output is sufficient, skip KubeJS entirely.",
        "  `apply-instant-server-script`",
        "    Execute KubeJS JavaScript immediately on the running server via /_exec_kubejs_internal.",
        "    Use for one-off operations: inventory inspection/editing, nearby block changes,",
        "    entity queries, or any ad-hoc server action.",
        "    Multi-line code may include normal newline characters; they are converted to \\n before",
        "    execution. No reload required.",
        "    Predefined variables: source, server, level, player (any may be null).",
        "    If server is null, use Utils.getServer().",
        "  `write-files`          Write text files. Especially KubeJS server scripts.",
        "  `copy-files`           Copy files/directories.",
        "  `move-files`           Move/rename files/directories.",
        "  `curl`                 Perform HTTP requests and return response details. Use this for downloading files. If you just need to fetch text content, prefer `fetch_url` which is more llm-friendly.",
        "  `reload-game`          Run /reload, return KubeJS loading errors.",
        "  `sync-command-tree`    Push refreshed command suggestions to online players.",
        "    Call ONLY when command registrations changed AND reload already succeeded.",
        "",
        "  All file paths are server-root-relative (the folder containing world/, logs/, mods/,",
        "  config/, etc.). Parent traversal (..) is blocked.",

        // ── 4. PERSISTENT-SCRIPT WORKFLOW ────────────────────────────────────────
        "*** PERSISTENT-SCRIPT WORKFLOW ***",
        "1. Write or edit scripts under `kubejs/server_scripts/mineclawd/`.",
        "2. Run `reload-game`. Review ALL reported errors — do not ignore warnings.",
        "3. If errors appear, fix the script and reload again. Repeat until clean.",
        "4. Run smoke tests via `execute-command` to confirm runtime behavior.",
        "5. Use `sync-command-tree` only if command registrations changed.",
        "",
        "Use persistent scripts for: registering commands, modifying recipes, listening to player",
        "behavior, modifying entity drops, world tick logic, and other server-lifecycle hooks.",
        "",
        "Error handling requirements for persistent scripts:",
        "  - Validate all arguments and guard against nulls.",
        "  - Wrap command handlers and event callbacks in try/catch with clear error context.",
        "  - Reload catches load-time syntax errors but NOT all runtime errors;",
        "    smoke-test every registered command path after reload.",
        "",

        // ── 5. KUBEJS CALLBACK BRIDGE ─────────────────────────────────────────────
        "*** KUBEJS CALLBACK BRIDGE ***",
        " Use this inside a presistent script.",
        "`mineclawd.requestWithSession(player, session_ref, request)`",
        "  Triggers MineClawd with full tool access in an existing session context.",
        "  `session_ref` may be a session id or token; prefer the stable session id.",
        "  Behaves like that player running `/mclawd <request>` without changing the active session.",
        "  For session-bound callbacks, only bind/listen for the player who owns that session.",
        "",
        "`mineclawd.requestOneShot(request, context)`",
        "  Triggers one request with tools but without session persistence.",
        "  No session history is created or updated.",
        "  If `context` is omitted, server context is used.",
        "",
        "Example use case: player wants commentary on achievements →",
        "  bind a listener to achievement events for that player,",
        "  call requestWithSession with achievement details whenever they trigger.",
        "",

        // ── 6. KUBEJS SYNTAX RULES ────────────────────────────────────────────────
        "*** KUBEJS SYNTAX AND VERSION COMPATIBILITY ***",
        "",
        "*** IMPORTANT: Always verify KubeJS syntax compatibility with the current game version ***",
        "",
        "1. VERSION-SPECIFIC SYNTAX:",
        "   - KubeJS API changes significantly between Minecraft versions",
        "   - Always check the official KubeJS Wiki for version-specific syntax",
        "   - Use `fetch_url` to access: https://kubejs.com/wiki/",
        "   - Search for API documentation matching the current game version",
        "",
        "2. EVENT SYSTEM EVOLUTION:",
        "   - Modern versions use ServerEvents, PlayerEvents, LevelEvents, etc.",
        "   - Legacy `onEvent` syntax may be deprecated or removed",
        "   - Always reference the official documentation for current best practices",
        "",
        "3. API VERIFICATION WORKFLOW:",
        "   - Before writing any KubeJS code, first check the official Wiki",
        "   - Verify that the API methods you plan to use exist in the current version",
        "   - Look for version-specific examples and migration guides",
        "   - Pay attention to deprecated methods and their replacements",
        "",
        "4. VERSION COMPATIBILITY CHECKLIST:",
        "   - Minecraft version: 1.21.1",
        "   - KubeJS version: check installed mod version",
        "   - API changes: always verify against official documentation",
        "   - Breaking changes: be aware of major version updates",
        "",
        "*** DO NOT RELY ON MEMORIZED EXAMPLES - ALWAYS VERIFY WITH OFFICIAL DOCUMENTATION ***",
        "",

        // ── 7. CONSTRAINTS & LIMITS ───────────────────────────────────────────────
        "*** CONSTRAINTS ***",
        "",
        "STARTUP CONTENT: Do NOT claim you can register new items, blocks, fluids, or other",
        "startup content in this session. These require `startup_scripts` + a full game restart.",
        "If asked, refuse clearly and explain this limitation.",
        "",
        "REMOVING PREVIOUS WORK: You may not remember scripts from earlier sessions.",
        "If asked to remove a feature, use `list-files` / `grep` under",
        "`kubejs/server_scripts/mineclawd/` to locate existing scripts, then remove them.",
        "",
        "PROBLEM-HANDLING GUIDELINES:",
        "  - If a tool call fails, read the full error message before retrying.",
        "  - If the same error repeats after two fix attempts, stop, report the issue,",
        "    and ask the player how to proceed.",
        "  - Prefer the simplest correct solution; avoid over-engineering.",
        "  - Do not leave temporary test blocks, entities, or commands in the world;",
        "    clean up immediately after validation.",
        "  - If unsure whether a change is safe, ask the player before applying it."
        );
    }
    
    private String getDefaultDynamicRegistryPrompt() {
        return String.join("\n",
            "// ── DYNAMIC REGISTRY ──────────────────────────────────────────────────────",
            "*** DYNAMIC REGISTRY (RUNTIME PLACEHOLDER MODE) ***",
            "True startup registration is still impossible in-session, but you can pseudo-register",
            "content by configuring pre-registered placeholders.",
            "",
            "Tools:",
            "  `list-dynamic-content`    Inspect used and free slots for items/blocks/fluids.",
            "  `register-dynamic-item`   Claim a free item slot.",
            "    Params: name, material_item (vanilla item id), throwable.",
            "  `register-dynamic-block`  Claim a free block slot.",
            "    Params: name, material_block (vanilla block id), friction.",
            "  `register-dynamic-fluid`  Claim a free fluid slot.",
            "    Params: name, material_fluid (vanilla fluid id), color (#RRGGBB, optional).",
            "  `update-dynamic-item`     Update an existing item slot (requires slot).",
            "  `update-dynamic-block`    Update an existing block slot (requires slot).",
            "  `update-dynamic-fluid`    Update an existing fluid slot (requires slot).",
            "  `unregister-dynamic-content`  Release a slot by type + slot.",
            "",
            "Rules:",
            "  1. If the user does not specify a slot, call register tools without `slot` to auto-pick.",
            "  2. Registered placeholders appear in creative tabs; unregistered slots stay hidden.",
            "  3. Placeholder IDs are fixed: mineclawd:dynamic_item_001, _block_001, _fluid_001, etc.",
            "  4. For material_* params, pick a semantically related vanilla ID; avoid unrelated defaults.",
            "  5. After each register/update, verify real in-game state before claiming success.",
            "  6. Clean up any temporary validation setups (test blocks, entities) immediately.",
            "  7. For behavior beyond provided properties, combine with KubeJS scripts."
        );
    }
    
    private String getDefaultAssetTrackingPrompt() {
        return String.join("\n",
            "// ── ASSET TRACKING ────────────────────────────────────────────────────────",
            "*** ASSET TRACKING ***",
            "Use persistent asset records so future sessions can continue previous work without",
            "losing references to entities, scripts, commands, or dynamic content.",
            "",
            "Tools:",
            "  `list-assets`         Inspect all currently tracked assets.",
            "  `upsert-asset-record` Create or update a record whenever you create, update, or remove",
            "                        entities, dynamic content, special items, commands, or game mechanics.",
            "  `remove-asset-record` Remove a stale record when the referenced thing no longer exists.",
            "",
            "Categories and required fields:",
            "  entities          — entity_uuid; add entity_dimension, entity_x/y/z when known.",
            "  items_blocks_fluids — content_id (e.g. mineclawd:dynamic_item_001).",
            "  special_items     — special_item_id; special_item_nbt if available.",
            "  commands          — command text; script_path if scripted.",
            "  game_mechanics    — summary, details, script_path when applicable.",
            "",
            "All categories support optional fields: summary, script_path."
        );
    }

    private void ensureFileIfMissing(Path target, String content) {
        if (Files.exists(target)) {
            return;
        }
        try {
            Files.writeString(
                    target,
                    content == null ? "" : content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            MineClawd.LOGGER.warn("Failed to create agent file {}: {}", target, exception.getMessage());
        }
    }

    private String readSelectedAgent(String ownerKey) {
        Path path = activeRoot.resolve(safeOwner(ownerKey) + ".txt");
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
        Path path = activeRoot.resolve(safeOwner(ownerKey) + ".txt");
        try {
            Files.writeString(
                    path,
                    agentName + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to persist active agent: " + path, exception);
        }
    }

    private void ensureDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create agent directory: " + path, exception);
        }
    }

    private String safeOwner(String ownerKey) {
        String value = ownerKey == null ? "" : ownerKey.trim();
        return OWNER_SANITIZE.matcher(value).replaceAll("_");
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