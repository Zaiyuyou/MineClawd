# Changelog

All notable changes to this project are documented in this file.

## [1.6.3-Zaiyuyou-fork] - 2026-03-14

### Architecture Refactoring (Tool System Decoupling)

- **工具定义解耦重构**：将OpenAITools和vertexTools从MineClawd.java中解耦出来，创建了类似LangChain的工具链架构
- **新增ToolDefinition类**：封装工具信息（名称、描述、参数），提供类型安全的工具定义
- **新增ToolRegistry类**：集中管理所有工具定义，支持条件过滤和工具注册
- **新增ToolFactory类**：根据LLM提供商创建对应的工具对象，支持OpenAI和VertexAI格式
- **工具链管理**：实现了类似LangChain的工具链管理机制，支持工具的条件启用/禁用

### Fixed

- **修复文件结构问题**：在删除工具定义方法时修复了文件结构破坏导致的编译错误
- **清理孤立代码**：删除了所有孤立的参数工具方法，确保代码整洁性
- **编译验证**：确保重构后的代码能够正常编译和运行

## [1.6.2-Zaiyuyou-fork] - 2026-03-14

### Agent System (Major Architecture Change)

- **重构了prompt架构**：从MineClawd的硬编码prompt系统转变为可自定义添加和切换的agent系统
- **新增AgentManager**：支持创建、管理和切换不同的agent配置
- **Agent优先级修复**：修复了agent prompt优先级问题，确保agent的prompt优先于配置中的硬编码值
- **多类型prompt支持**：每个agent支持base、dynamic_registry、asset_tracking三种类型的prompt
- **默认agent**：内置了"default"和"dum"两个示例agent
- **Agent配置目录**：agent配置存储在`mineclawd/agents/`目录下，支持热重载

### Fixed

- **修复agent prompt优先级**：现在agent的prompt会正确提交给LLM，不再被配置中的硬编码值覆盖
- **优化prompt构建逻辑**：确保agent prompt > 配置prompt > 硬编码默认值的正确优先级顺序

## [1.5.1] - 2026-02-26

### Added

- Added an in-overlay `Retry` button for modded clients after LLM request failures, so you can retry directly without manually typing `/mineclawd retry <token>`.

### Fixed

- Fixed client mod detection failing on dedicated servers, causing all GUI features to fall back to chat-text mode for clients that have the mod installed.
- Fixed dedicated-server packet sync crashes (`CustomPacketPayload$Type` null) that broke GUI sync and showed `unexpected error` when opening config.
- Fixed config GUI behavior on dedicated servers so it now loads and edits server config values; client-only settings (such as `Enable GUI`) remain local.
- Fixed resumed-session continuity so requests now use the session history provider when needed, preventing "memory lost" after selecting a session in the overlay.
- Fixed `apply-instant-server-script` payload decoding on dedicated servers, including KubeJS class-filter compatibility during UTF-8 decoding.
- Added always-on server-side logging for agent conversations, tool calls, and tool results (previously all logging was gated behind debug mode).

## [1.5.0] - 2026-02-25

### Added

- Added a docs-first reference toolkit for mod integration work using `list_commands`, `fetch_modrinth`, and `fetch_url`, so MineClawd can verify command usage and mod docs before writing scripts.
- Added web search support via Tavily (`search` tool), with configurable `tavily-api-key`, so MineClawd can verify external information before acting.
- Added optional dependency behavior for YACL and KubeJS integrations so MineClawd can still run when these mods are not installed; config GUI opens when YACL is present, while non-GUI workflows remain available.
- Added inline `Other` answer input in the overlay question block, so players can provide custom clarification answers directly in the overlay.

### Fixed

- Fixed cross-version input passthrough in overlay/question interactions where key, mouse, or scroll input could leak into underlying screens (such as chat or inventory search).

## [1.4.0] - 2026-02-20

### Assistant Overlay GUI

- Added a dedicated top-layer assistant window with draggable position, resizable bounds, minimization to an AssistiveTouch orb, and OP-only default orb visibility.
- Added real-time streaming output in the overlay with a blinking cursor and a temporary `MineClawd: Thinking...` indicator before the first assistant delta.
- Added in-window prompt input with text selection, caret movement, copy/paste shortcuts, scroll handling, and send/stop generation controls.
- Added integrated `Config`, `Sessions`, `Persona`, and `Assets` menu entries in the overlay.
- Added session list UI with `New Session`, in-place session switching, and session history rendering in the overlay.
- Added AskUserQuestion rendering inside the overlay and persisted selected answers into message history.
- Updated role highlighting so `You` and `MineClawd` messages are visually distinct, with one `MineClawd` prefix per assistant turn.
- Added AssistiveTouch orb icon rendering from `icon-simplified.png` and corrected orb icon centering/sizing behavior.
- Added client config option `Enable GUI` (default ON). When OFF, MineClawd GUI features degrade to non-GUI behavior while dynamic runtime content support remains available.

### Assets Tracking

- Added persistent asset records managed by tools for categories: `Entities`, `Items/Blocks/Fluids`, `Special Items`, `Commands`, and `Game Mechanics`.
- Added asset metadata support for summary/script path plus category-specific fields (for example entity UUID/location and content identifiers/NBT).
- Added `/mineclawd assets` operations with GUI integration for quick actions: teleport (entities), give (items), modify shortcut prompt, and delete shortcut prompt.
- Added Assets overlay view with category filters (`All` + per-category tabs) and per-entry action controls.

### Runtime Streaming And Controls

- Enabled streaming response handling for both OpenAI and Vertex AI providers, with server-to-client stream event transport.
- Added `/mineclawd stop` to cancel active generation, including in-flight network cancellation and pending question cleanup.
- Added overlay stop action during generation via the input-bar button.
- Fixed session resume behavior after leaving/rejoining world while a request is running so the active session can still be reopened from GUI.
- Fixed input bleed-through so typing/scrolling in MineClawd overlay no longer leaks into chat, inventory search, or other active screens.
- Restored `MineClawd finished working for ...` chat status line after generation completes.
- Added spacing between assistant text segments split by tool calls to keep streamed output readable.

## [1.3.0] - 2026-02-13

### Added

- Architectury multi-loader project layout with dedicated targets for:
  - Fabric `1.20.1`
  - Forge `1.20.1`
  - NeoForge `1.21.1`
- NeoForge metadata/runtime support using `mods.toml` (1.20.1) and `neoforge.mods.toml` (1.21.1).
- NeoForge `1.21.1` Yarn compatibility mapping patch for stable Loom remapping.
- Player join handshake packet for reliable client-mod detection across loaders.

### Changed

- Migrated shared gameplay logic to Architectury common modules while preserving behavior parity.
- Updated runtime dependencies per platform:
  - Fabric keeps Mod Menu integration.
  - Forge/NeoForge use Better Modlist (Mod Menu alternative).
  - YACL, KubeJS, and Rhino now resolve with loader-specific coordinates.
- Updated project version to `1.3.0`.

### Fixed

- Fixed false `client mod not detected` on Forge/NeoForge by replacing brittle channel checks with a ready handshake.
- Fixed `/mineclawd history` desync on NeoForge sessions by updating packet send gating.
- Fixed rich text book rendering to show formatted history content instead of raw JSON text.
- Fixed dynamic content sync timing on client join so runtime content and related textures load correctly.

## [1.2.0] - 2026-02-10

### Added

- Runtime dynamic placeholder registry with `30` item slots, `30` block slots, and `30` fluid slots.
- Dynamic content tool suite:
- `list-dynamic-content`
- `register-dynamic-item`
- `register-dynamic-block`
- `register-dynamic-fluid`
- `update-dynamic-item`
- `update-dynamic-block`
- `update-dynamic-fluid`
- `unregister-dynamic-content`
- Client-side dynamic model/render pipeline for runtime material substitution and fluid tint rendering.
- KubeJS callback bridge APIs:
- `global.mineclawd.requestWithSession(player, session_ref, request)`
- `global.mineclawd.requestOneShot(request, context)`
- aliases `callWithSession` and `callOneShot`.
- `/mineclawd history` command with a client-opened written book view and rich text rendering.
- `ask-user-question` tool with client question popup UI, option buttons, free-form response, and timeout handling.
- LLM request recovery UX with clickable `[Retry]` and `[Adjust Prompt]` chat actions.
- `/mineclawd retry <token>` command and failed-request token tracking.
- `/mineclawd sessions repair [session]` command to repair malformed session history turns.
- `sync-command-tree` tool to refresh Brigadier command trees for online players.

### Changed

- Dynamic registry runtime mode is now configurable via `dynamic-registry-mode`:
- `AUTO`: enabled in single-player runtime, disabled on dedicated servers by default.
- `ENABLED`: forces runtime placeholders on (dedicated servers warn that clients must install MineClawd).
- `DISABLED`: fully off.
- Dynamic placeholder state is now persisted and restored across relog/restart, and synced to players on join.
- Dynamic fluid defaults now keep water-like movement behavior (flow speed, tick rate, level decrease, collision profile).
- Added water fluid-tag compatibility entries for all dynamic still/flowing fluids.
- System prompt now conditionally appends dynamic-registry guidance only when runtime placeholders are enabled.
- System prompt now documents KubeJS callback usage and session-binding constraints.
- Session prompt context now includes current session id/token for callback wiring.
- LLM error handling now rolls back failed prompts from session history to avoid duplicate retries.
- KubeJS reload error parsing now filters common success lines (`0 errors`, `0 warnings`) to reduce false positives.

### Fixed

- Fixed dynamic creative-tab visibility desync after rejoin by synchronizing server-side dynamic state to clients.
- Fixed dynamic block/item/fluid runtime properties being lost after restart by persisting registry payload in world state.
- Fixed Vertex AI function-call/function-response turn mismatch recovery with in-session normalization and repair command support.
- Fixed `/mineclawd history` client UX to open the book screen directly without requiring an inventory slot.

## [1.1.0] - 2026-02-07

### Added

- Persistent session storage under `gameDir/mineclawd/sessions/`.
- Full session management commands:
- `/mineclawd sessions new`
- `/mineclawd sessions list`
- `/mineclawd sessions resume <session>`
- `/mineclawd sessions remove <session>`
- `/mineclawd new` alias for `/mineclawd sessions new`.
- Session reference parsing with both `uuid` and `uuid-title`.
- First-turn session title generation via provider-specific summarize models.
- Persona system with `mineclawd/souls/` storage.
- New persona commands:
- `/mineclawd persona`
- `/mineclawd persona <soul>`
- Built-in souls: `default` and `yuki`.
- Expanded agent toolset:
- `apply-instant-server-script`
- `execute-command`
- `list-server-scripts`
- `read-server-script`
- `write-server-script`
- `delete-server-script`
- `reload-game`
- KubeJS reload error capture and return-to-LLM flow for self-repair loops.
- Mod icon integration and Modrinth link metadata.

### Changed

- Improved system prompt guidance for progress updates, tool strategy, and MineDown support.
- Tool-call limit now defaults to `16`, with an enable/disable toggle.
- Agent/player chat presentation improved:
- Prompt echo format: `<playername> @MineClawd Original Prompt`
- Styled `[MineClawd]` prefix
- Markdown/MineDown message rendering
- Debug logging expanded for LLM outputs and tool activity.

### Fixed

- Fixed command routing and aliases for prompt/config/session flows.
- Fixed blank YACL config UI and key visibility behavior.
- Improved internal command execution path so real tool errors can be surfaced back to the model.

## [1.0.0] - 2026-02-06

### Added

- Initial public release for Fabric `1.20.1`.
- OpenAI and Google Vertex AI provider support.
- YACL config UI with provider-specific fields and API key masking.
- Core in-game commands:
- `/mineclawd prompt <request>`
- `/mclawd <request>`
- `/mineclawd config`
- Base KubeJS integration:
- Auto-generated `kubejs/server_scripts/mineclawd-internal-api.js`
- Internal execution command `/_exec_kubejs_internal <code>`
- Tool-loop execution model for request handling.

### Changed

- Project metadata, description, and license aligned for public release.
