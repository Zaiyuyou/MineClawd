# MineClawd ModernUI GUI — AI Agent Context Document

> Purpose: Provide complete project context, architecture, class index, data flow, and conventions for AI coding agents to rapidly start developing MineClawd's ModernGUI.

> Generated: 2026-04-30 | Minecraft: 1.21.1 | Loader: NeoForge | UI: ModernUI-MC 3.12.0.2

---

## 1. Project Overview

### 1.1 What is MineClawd?

MineClawd is a Minecraft AI agent mod. Players interact with LLM (OpenAI/Vertex AI) via natural language in chat, and the AI modifies the game world through a tool system (KubeJS scripts, file operations, commands, web search, etc.).

**Architecture**: Architectury multi-platform (currently NeoForge 1.21.1 only).  
**Build system**: Gradle 8.8 + Architectury Loom 1.13.467 + ShadowJar.  
**JDK**: 21 (LibericaJDK-21 at `D:\jabba\Java\LibericaJDK-21`).  

### 1.2 Module Structure

```
MineClawD_RE/
├── common/                          # Cross-platform code (most code lives here)
│   ├── build.gradle                  # "common" platform, depends on libs/*.jar
│   └── src/main/java/com/mineclawd/
│       ├── MineClawd.java                # Main class (server+client logic, ~6600 lines)
│       ├── MineClawdClientNetworking.java # Network event registration (client)
│       ├── MineClawdNetworking.java       # Network channel identifiers
│       ├── AgentStreamEventType.java      # Stream event enum
│       └── foundation/
│           ├── agent/                     # Agent management
│           ├── assets/                    # Asset management
│           ├── chat/                      # ChatRole, ChatRoleConfig, ChatRoleManager
│           ├── client/
│           │   ├── ChatStreamBridge.java      # Stream event → Fragment bridge
│           │   ├── SessionPayloadBridge.java  # Session data → Fragment bridge
│           │   ├── CharInputBridge.java       # IME char input bridge
│           │   ├── MineClawdKeyBindings.java  # Key binding constants
│           │   ├── AgentResponseOverlay.java  # LEGACY overlay UI (~4500 lines, reference only)
│           │   └── ui/
│           │       ├── framework/
│           │       │   ├── ModernHudFragment.java     # MAIN GUI FRAGMENT (~1430 lines)
│           │       │   ├── MessageBubble.java          # Chat bubble with blocks system (~370 lines)
│           │       │   ├── MessageResponseHandler.java # HTTP response handler (unused?)
│           │       │   ├── HudOrbState.java            # Orb + sidebar position persistence
│           │       │   ├── EmojiEnabler.java           # NotoEmoji TTF font registration
│           │       │   ├── AttachmentHelper.java       # AWT FileDialog file picker
│           │       │   └── IMBlockerCompat.java        # IMBlocker reflection whitelist
│           │       ├── i18n/
│           │       │   └── MineClawdI18n.java          # i18n helper with fallback map
│           │       └── log/
│           │           ├── LogView.java       # In-GUI Log viewer (ModernUI)
│           │           ├── LogEntry.java      # Log entry model
│           │           └── LogManager.java    # Log event bus
│           ├── config/                   # Config system (YACL-based)
│           ├── llm/                      # LLM clients (OpenAI, Vertex AI)
│           ├── session/                  # Session management
│           │   ├── SessionManager.java        # Server-side session persistence
│           │   └── SessionOverlayPayload.java # Session data DTO
│           └── tool/                     # Tool system
│               └── ToolStatusDescriptor.java  # Tool status descriptor record
├── neoforge/                         # NeoForge platform module
│   ├── build.gradle                   # ShadowJar + forgeRuntimeLibrary
│   └── src/main/java/com/mineclawd/
│       ├── MineClawdNeoForge1211.java          # Mod entry point (server)
│       ├── MineClawdNeoForge1211Client.java    # Client init entry
│       └── foundation/client/
│           ├── HudOrbHandler.java              # Orb rendering (NeoForge events)
│           ├── ModernUINeoForgeWindowManager.java # Window toggle logic
│           ├── MineClawdNeoForgeKeyBindings.java # Key binding registration
│           └── mixin/
│               └── ScreenCharTypedMixin.java   # Char input mixin
├── libs/                            # Local JAR dependencies (ModernUI, KubeJS, etc.)
└── gradle.properties                # Version catalog
```

### 1.3 Key Build Commands

| Command | Purpose |
|---------|---------|
| `.\gradlew :neoforge:compileJava` | Quick compile check |
| `.\gradlew --rerun-tasks :neoforge:compileJava` | Force recompile (bypass UP-TO-DATE) |
| `.\gradlew build` | Full build + remap + shadowJar |
| `.\gradlew --stop` | Kill cached daemons before build |

---

## 2. ModernUI-MC Framework (3.12.0.2)

### 2.1 What is ModernUI-MC?

A desktop UI framework embedded in Minecraft. Think Android layouts + GPU-accelerated rendering + Arc3D text layout engine. Sits as an overlay on top of Minecraft's rendering pipeline.

**Maven coordinates**: Local JAR at `libs/ModernUI-NeoForge-*.jar`  
**Base package**: `icyllis.modernui.*`

### 2.2 Key Classes

| Class | Role |
|-------|------|
| `icyllis.modernui.fragment.Fragment` | Base UI container. Has `onCreate`, `onCreateView`, `onStart`, `onResume`, `onDestroy` lifecycle. |
| `icyllis.modernui.mc.ScreenCallback` | Interface to control Minecraft Screen behavior (e.g. `isPauseScreen()`). Must be implemented by Fragment. |
| `icyllis.modernui.core.Context` | ModernUI context (not Minecraft's). Obtained via `requireContext()` or `getView().getContext()`. |
| `icyllis.modernui.view.View` | Base UI element. Has `post(Runnable)` for UI thread dispatching. |
| `icyllis.modernui.view.ViewGroup` | Container. Supports `addView`, `removeView`, etc. |
| `icyllis.modernui.widget.FrameLayout` | Simple container (like Android FrameLayout). |
| `icyllis.modernui.widget.LinearLayout` | Flex container. Set `setOrientation(HORIZONTAL / VERTICAL)`. |
| `icyllis.modernui.widget.ScrollView` | Vertical scroll container. |
| `icyllis.modernui.widget.TextView` | Text display. `setText(CharSequence)`, `setTextSize(int)`. |
| `icyllis.modernui.widget.EditText` | Text input. Supports `addTextChangedListener`, `requestFocus()`. |
| `icyllis.modernui.widget.Button` | Clickable button. `setOnClickListener()`. |
| `icyllis.modernui.widget.Spinner` | Dropdown selector. |
| `icyllis.modernui.widget.Switch` | Toggle switch. |
| `icyllis.modernui.graphics.drawable.ShapeDrawable` | Shape-based background. `setShape(RECTANGLE)`, `setCornerRadius()`, `setColor(int)`, `setStroke()`. |
| `icyllis.modernui.graphics.drawable.RippleDrawable` | Touch ripple effect. Wrap around ShapeDrawable. |
| `icyllis.modernui.animation.ObjectAnimator` | Property animation. `ofFloat(target, "property", from, to)`. |
| `icyllis.modernui.animation.LayoutTransition` | Auto-animate layout changes. **Must be on ModernUI Looper thread**. |
| `icyllis.modernui.markflow.Markflow` | Markdown renderer. `setMarkdown(TextView, String)`. |
| `icyllis.modernui.mc.MuiModApi` | API to open ModernUI screens. `MuiModApi.openScreen(new Fragment())`. |
| `icyllis.modernui.resources.TypedValue` | Resolve theme colors. |
| `icyllis.modernui.text.method.PasswordTransformationMethod` | Mask EditText content with dots (used for API key inputs). |

### 2.3 Critical ModernUI Rules

1. **ModernUI has its own Looper/UI thread**. All view mutations MUST happen on the ModernUI thread. Use `View.post(Runnable)` to dispatch from Minecraft's thread.
2. **View lifecycle is separate from Fragment lifecycle**. Always check `getView() != null` before accessing views.
3. **`LayoutTransition` animations require the ModernUI Looper thread**. Calling `removeAllViews()` on a view with LayoutTransition from Minecraft's thread will crash with "Animators may only be run on Looper threads".
4. **No `Canvas`/`Paint` in Minecraft render hooks**. ModernUI Canvas only works inside its own render thread. For HUD rendering (e.g. via `RenderGuiEvent`), use Minecraft's `DrawContext`.
5. **Fragment's `requireActivity()` does NOT exist**. Close screen with `MinecraftClient.getInstance().setScreen(null)`.

### 2.4 Theme Colors (resolved via `R.ns`)

| Attribute | Usage |
|-----------|-------|
| `colorPrimary` | Active elements, send button, selected state |
| `colorOnSurface` | Primary text color |
| `colorOnSurfaceVariant` | Secondary text, icons |
| `colorSurface` | Panel background |
| `colorSurfaceContainerLow` | Window bg, card fill |
| `colorSurfaceContainerHigh` | Assistant bubbles, input bg |
| `colorSurfaceContainerHighest` | Button bg |
| `colorOutline` | Borders |
| `colorOutlineVariant` | Inactive borders |
| `colorError` | Stop button |
| `colorControlHighlight` | Ripple effect |

### 2.5 Markdown Rendering Setup

```java
var builder = Markflow.builder(requireContext());
var monoFont = Typeface.getSystemFont("JetBrains Mono Medium");
if (monoFont != Typeface.SANS_SERIF) {
    builder.usePlugin(new MarkflowPlugin() {
        @Override public void configureTheme(@NonNull MarkflowTheme.Builder tb) {
            tb.codeTypeface(monoFont);
        }
    });
}
mMarkflow = builder.build();
// Usage:
mMarkflow.setMarkdown(textView, markdownString);
```

### 2.6 ShapeDrawable + RippleDrawable Pattern

```java
// Background shape
var bg = new ShapeDrawable();
bg.setShape(ShapeDrawable.RECTANGLE);
bg.setCornerRadius(mWindow.dp(8));
bg.setColor(mColorSurfaceContainerHigh);
bg.setStroke(mWindow.dp(1), mColorOutlineVariant);
view.setBackground(bg);

// Button with ripple
var ripple = new RippleDrawable(
    ColorStateList.valueOf(mColorPrimary),
    shapeDrawable,
    null
);
button.setBackground(ripple);
```

---

## 3. MineClawd Main Classes

### 3.1 MineClawd.java (`common/.../MineClawd.java`)

The massive (~6600 lines) main class handling server-side logic:

- **Command registration**: `/mineclawd`, `/mclawd` commands
- **LLM request orchestration**: `handleRequest()` → `runOpenAiAgent()` / `runVertexAgent()`
- **Stream event outbound**: `sendAgentStreamEvent()` → `sendAgentStreamPacket()`
- **Tool execution**: `executeOpenAiToolCallsSequential()` → `announceToolCallProgress()` / `clearToolCallProgress()`
- **Chat fallback**: When `clientStreamEnabled=false`, sends `sendAgentMessage()` to Minecraft chat
- **Session management**: Delegates to `SessionManager`
- **AgentRuntime**: Inner class/record holding request context (sessionId, ownerKey, clientStreamEnabled flag, etc.)

**Critical condition for stream events**:
```java
canUseGui(player, MineClawdNetworking.AGENT_STREAM_EVENT)
// → AGENT_STREAM_EVENT now bypasses CLIENT_GUI_ENABLED check
// → always returns true if mod is loaded
```

**TOOL_STATUS payload format**:
```json
{"short": "Executing command...", "hover": "detailed info", "name": "toolname"}
```
Payload includes `"name"` field so the GUI can identify which tool the card belongs to.

**TOOL_STATUS_CLEAR payload format**:
```json
{"name": "toolname", "short": "✓ done", "result": "command output..."}
```
`result` field contains the tool execution output for display.

**STREAM START payload format**:
```json
{"sessionId": "a3f8", "request": "build a house"}
```

**ERROR payload format**:
```
Oops! Something went wrong
Retry token: abc12345
```

**Server-side history formatting** (`collectOpenAiHistoryEntries`):
- Tool calls in assistant messages → `@@TC|toolname|running|@@`
- Tool responses → `@@TC|toolname|done|parsed_result@@`
- These `@@TC` markers are the serialization format that the GUI uses to reconstruct tool cards on history reload.

### 3.2 MineClawdClientNetworking.java (`common/.../MineClawdClientNetworking.java`)

Client-side network registration. On `init()`:
1. Registers all S2C network receivers
2. The critical `AGENT_STREAM_EVENT` receiver:
   ```java
   // wire format: requestId(String,64) + type(byte) + payload(String,262144)
   AgentResponseOverlay.handleStreamEvent(...)  // LEGACY
   ChatStreamBridge.forward(...)                 // NEW: to Fragment
   ```
3. Registers `OPEN_SESSIONS` receiver → `AgentResponseOverlay` + `SessionPayloadBridge.forward()`

### 3.3 AgentStreamEventType.java (`common/.../AgentStreamEventType.java`)

```java
START(0), DELTA(1), DONE(2), ERROR(3), TOOL_STATUS(4), TOOL_STATUS_CLEAR(5)
```

Wire protocol: `writeByte(type.id())` / `readByte()` → 1 byte. (Was `readInt()` causing IndexOutOfBoundsException — fixed to `readByte()`.)

### 3.4 ChatStreamBridge.java (`common/.../client/ChatStreamBridge.java`)

Static bridge pattern. `setListener(Consumer<StreamEvent>)` → `onStreamEvent` in Fragment. `forward()` → calls listener. **Listener is NOT cleared in `onDestroy()`** to avoid race conditions.

### 3.5 SessionPayloadBridge.java (`common/.../client/SessionPayloadBridge.java`)

Same static bridge pattern. Forwards `SessionOverlayPayload` to Fragment.

### 3.6 CharInputBridge.java (`common/.../client/CharInputBridge.java`)

Bridges Minecraft `Screen.charTyped()` to ModernUI `EditText`. Activated on `buildChatPage()`, deactivated on `onDestroy()`.

---

## 4. GUI Framework Classes (IN DETAIL)

### 4.1 ModernHudFragment.java (`/common/.../ui/framework/ModernHudFragment.java`)

The main GUI. ~1430 lines, extends `Fragment implements ScreenCallback`.

**Package**: `com.mineclawd.foundation.client.ui.framework`  
**Location**: `common/src/main/java/.../ModernHudFragment.java`

#### Lifecycle

```
onCreate       → set ChatStreamBridge/SessionPayloadBridge listeners
onCreateView   → build entire UI tree (window + sidebar + content)
                 → returns mRoot
onStart        → re-set ChatStreamBridge listener
onResume       → restore sidebar, flushPendingUpdates, requestSessionsFromServer
onDestroy      → save sidebar state, CharInputBridge.deactivate(),
                 IMBlockerCompat.onScreenClosed()
                 (NO longer clears ChatStreamBridge/SessionPayloadBridge listeners)
```

#### Key Fields

| Field | Type | Purpose |
|-------|------|---------|
| `mWindow` | `FrameLayout` | The draggable/resizable window overlay |
| `mRoot` | `FrameLayout` | Root (full-screen touch interceptor) |
| `mSidebar` | `FrameLayout` | Sidebar panel |
| `mSidebarContent` | `LinearLayout` | Inside sidebar, holds session list or status |
| `mMainContent` | `FrameLayout` | Right content area, swapped by `updateMainContent()` |
| `mChatContainer` | `LinearLayout` | Chat message list inside ScrollView |
| `mChatScroll` | `ScrollView` | Contains mChatContainer |
| `mChatInput` | `EditText` | Text input at bottom of chat |
| `mSendBtn` | `Button` | Send/Stop/Retry button |
| `mBtnRow` | `LinearLayout` | Button row (attach, badge, spacer, send) |
| `mCurrentStreamingMessageBubble` | `MessageBubble` | The bubble being streamed into |
| `mGenerating` | `boolean` | Whether LLM is currently generating |
| `mFailedRetryToken` | `String` | Retry token for failed requests |
| `mActiveSessionId` | `String` | Currently active session (short hex id) |
| `mSessions` | `List<SessionItemData>` | Session list from server |
| `mMessages` | `List<ChatMessage>` | Chat message history (data model) |
| `mMessageBubbles` | `List<MessageBubble>` | View list parallel to mMessages |
| `mMarkflow` | `Markflow` | Markdown renderer |
| `mPendingToolNames` | `List<String>` | Currently running tool names (for tracking) |
| `mColorPrimary` etc | `int` | Resolved theme colors |
| `mFollowTail` | `boolean` | Whether auto-scroll is enabled (true when user is near bottom) |
| `mPendingUiUpdates` | `ConcurrentLinkedQueue<Runnable>` | Thread-safe queue for UI-thread dispatching |

#### ChatMessage Data Model

```java
private static class ChatMessage {
    String roleId;  // "user" or "assistant"
    String content; // Raw content with @@TC markers for tool cards
}
```

`content` uses `@@TC` markers to serialize tool card data inline with text:
```
@@TC|toolname|status|result@@
```
- `status` = `"running"` or `"done"`
- `result` = parsed result text (JSON `result` field extracted, pipes/@@ sanitized)

This single-string format ensures tool cards maintain their correct position in the chat history.

#### Navigation System

6 tabs, controlled by `mActiveTab` (0-5):

| Index | Icon | Label Key | Page Builder |
|-------|------|-----------|-------------|
| 0 | 💬 | `nav.chat` | `buildChatPage()` |
| 1 | 📋 | `nav.sessions` | (shows session list in sidebar, chat fallback in content) |
| 2 | 📁 | Phase 2 | placeholder |
| 3 | 🔧 | Phase 2 | placeholder |
| 4 | 📦 | Phase 2 | placeholder |
| 5 | ⚙ | `nav.settings` | `buildSettingsPage()` |

#### Window System

- **Position**: `mXNorm`, `mYNorm`, `mWNorm`, `mHNorm` (normalized 0..1)
- **Saving**: `HudOrbState.saveWindowPos()` / `HudOrbState.hasSavedWindowPos()`
- **Dragging**: Title bar `onTouchListener` (ACTION_DOWN → MOVE → UP)
- **Resizing**: 8 handles (4 corners + 4 edges), `onResize()` + `layoutResizeHandles()`
- **Minimize to Orb**: `minimizeToOrb()` → save everything → set Minecraft screen to null
- **Outside click**: `onRootTouch()` → if touch is outside window bounds → minimize

#### Sidebar

- Contains session list or settings status
- Draggable resize handle (`mSidebarResizer`)
- **State persistence**: `HudOrbState.saveSidebarState()` on resize + `onDestroy()`
- **State flag fix**: `hasSavedSidebarState()` uses explicit boolean (not `width > 0`), so collapsed state is correctly saved

#### Chat Page (`buildChatPage()`)

Layout structure:
```
chatRoot (FrameLayout)
  ├── mChatScroll (ScrollView)
  │   └── mChatContainer (LinearLayout, vertical)
  │       ├── [MessageBubble views]
  │       │   each bubble contains (inside mBlocksContainer):
  │       │   ├── [TextView: Markdown text]        ← TextBlock
  │       │   ├── [LinearLayout: tool card view]    ← ToolCardData block
  │       │   └── ... (in event order)
  │       └── mCurrentStreamingMessageBubble
  └── mInputArea (LinearLayout)
      ├── mChatInput (EditText)
      └── mBtnRow (LinearLayout)
          ├── attachBtn (Button)
          ├── attachmentBadge (TextView)
          ├── spacer (View)
          └── mSendBtn (Button)
```

### 4.2 MessageBubble.java (`/common/.../ui/framework/MessageBubble.java`)

The chat bubble component (~370 lines) — refactored to a **blocks-based architecture**.

#### Architecture

```
bubbleLayout (LinearLayout HORIZONTAL)
  ├── [avatar icon / spacer]
  └── mBlocksContainer (LinearLayout VERTICAL, rounded bg with bubbleBgColor)
      ├── [TextView: Markdown text]            ← TextBlock
      ├── [LinearLayout: tool card view]       ← ToolCardData
      └── ... (in event arrival order)
```

#### Blocks System

```java
private sealed interface Block permits TextBlock, ToolCardData {}
private static final class TextBlock implements Block {
    final SpannableStringBuilder text = new SpannableStringBuilder();
}
private record ToolCardData(String toolName, boolean done, String result) implements Block {}

private final List<Block> mBlocks = new ArrayList<>();
```

`mBlocks` preserves the exact order of events as they arrived. Each block generates exactly one view in `mBlocksContainer`.

#### Key Methods

| Method | Purpose |
|--------|---------|
| `appendText(text)` | Add/reuse TextBlock at end (merges with last if TextBlock). Calls `rebuildAllViews()`. |
| `addToolCard(name)` | Append running tool card block. Calls `addToolCardView()`. |
| `updateToolCard(name, done, result)` | Replace existing ToolCardData with same name. Updates view in-place. |
| `buildFromRawContent(raw)` | Parse `@@TC` markers + text, build full `mBlocks` list, then `rebuildAllViews()`. **Shared by streaming and history rebuild.** |
| `getContent()` | Serialize `mBlocks` back to string with `@@TC` markers. Single source of truth. |
| `rebuildAllViews()` | Clear container, iterate `mBlocks` to create TextView or tool card view. |
| `clearToolCards()` | Remove all `ToolCardData` blocks and rebuild. |
| `setBubbleBackgroundColor(color)` | Set the bubble container background. |

#### Tool Card View

```
card (LinearLayout VERTICAL, rounded bg with color-coded state)
  ├── header (LinearLayout HORIZONTAL)
  │   ├── icon (TextView: ⚙ running / ✓ done)
  │   ├── nameLabel (TextView: tool name)
  │   └── stateIcon (TextView: ⏳ running / ✔ done)
  ├── toggleBtn (Button: ▼ Result, only when done + has result)
  └── resultArea (LinearLayout VERTICAL, collapsible, View.GONE by default)
      └── resultText (TextView, parsed result)
```

Color coding:
- Running: semi-transparent primary bg, green-tinted border
- Done (no result): semi-transparent green bg
- Done (with result): dark green bg `#1B3D2A`, green border `#2D6A4F`, green text `#52B788`

### 4.3 HudOrbState.java (`/common/.../ui/framework/HudOrbState.java`)

Static state management for the floating orb. Persists window position, size, sidebar width/collapse across Fragment open/close cycles.

Key fix: `hasSavedSidebarState()` uses explicit `sHasSavedSidebar` boolean instead of `sSavedSidebarW > 0`, because collapsed sidebar has `mSidebarW = 0`.

### 4.4 HudOrbHandler.java (`neoforge/.../client/HudOrbHandler.java`)

NeoForge event subscriber. Renders orb via `RenderGuiEvent.Post` using Minecraft `DrawContext`. Handles orb click → `MuiModApi.openScreen(new ModernHudFragment())`. Orb drag → snap to screen edges.

### 4.5 ModernUINeoForgeWindowManager.java (`neoforge/.../client/ModernUINeoForgeWindowManager.java`)

`toggleFloatingWindow()` → cycle between: orb → GUI → orb → ...

### 4.6 LogView.java, EmojiEnabler.java, AttachmentHelper.java, IMBlockerCompat.java

Support utilities. See file comments for details.

---

## 5. Data Flow Patterns

### 5.1 LLM Response → GUI Bubble

```
Server (MineClawd.java):
  handleRequest()
    → runOpenAiAgent() / runVertexAgent()
    → sendAgentStreamEvent() [START]
    → (tool calls)
        → announceToolCallProgress() → TOOL_STATUS {"name":"exec","short":"..."}
        → executeToolCallAsync()
        → clearToolCallProgress()  → TOOL_STATUS_CLEAR {"name":"exec","result":"..."}
    → (model response streaming)
        → sendAgentStreamEvent() [DELTA] "Here is your item..."
    → sendAgentStreamEvent() [DONE]

Network:
  → RegistryByteBuf (requestId + byte(typeId) + payload)
  → NetworkManager S2C → MineClawdNetworking.AGENT_STREAM_EVENT

Client (MineClawdClientNetworking.java):
  → buf.readString(64) + buf.readByte() + buf.readString(262144)
  → AgentResponseOverlay.handleStreamEvent()  // LEGACY
  → ChatStreamBridge.forward()                // NEW: to Fragment

Fragment (ModernHudFragment.java):
  → onStreamEvent()
    → mPendingUiUpdates.add(() -> handleStreamEventSafe(event))
    → scheduleFlush() → v.post(flushPendingUpdates)
    → flushPendingUpdates() → handleStreamEventSafe()
  → switch(event.type):
      START → createStreamingMessage() [empty bubble]
              → clearToolCards()
      DELTA → mCurrentStreamingMessageBubble.appendText(payload)
              → append to last TextBlock → rebuildAllViews()
      TOOL_STATUS → mCurrentStreamingMessageBubble.addToolCard(name)
                    → mBlocks += ToolCardData(running) → addCardView
      TOOL_STATUS_CLEAR → mCurrentStreamingMessageBubble.updateToolCard(name, done, result)
                          → replace ToolCardData + view in-place
      DONE  → mCurrentStreamingMessageBubble.getContent() → mMessages.add()
              → cleanupStreamingState()
```

### 5.2 Session Data Flow

```
User clicks session → switchSession(idx):
  → sendChatCommand("mineclawd sessions resume " + session.id)
  → Server: resumeSession() → sendSessionsOverlayToPlayer()
    → SESSION_MANAGER.loadActiveSession()
    → collectVisibleHistoryEntries() (OpenAI or Vertex)
    → collectOpenAiHistoryEntries()
      → parses tool calls → @@TC|toolname|running|@@ markers
      → parses tool results → @@TC|toolname|done|result@@ markers
      → mergeAssistantEntries() (consecutive assistant entries merged)
    → build SessionOverlayPayload JSON
    → send via NetworkManager → OPEN_SESSIONS channel

Client:
  → MineClawdClientNetworking → SessionPayloadBridge.forward(payload)
  → Fragment.onSessionPayload()
    → mMessages.clear()
    → for each HistoryEntry → new ChatMessage(role, rawContent)
    → updateMainContent("nav.chat") → buildChatPage()
    → addMessageBubble("", "") → MessageBubble.buildFromRawContent(content)
      → parse @@TC markers → build mBlocks → rebuildAllViews()
```

### 5.3 History Rebuild vs Streaming — SAME PATH

Both paths now use `MessageBubble`'s internal block system:

| | Streaming | History Rebuild |
|---|---|---|
| **Text** | `bubble.appendText()` → merge to last TextBlock | `buildFromRawContent()` → `appendTextInternal()` → new TextBlock per fragment |
| **Tool Cards** | `bubble.addToolCard()` → append + `bubble.updateToolCard()` → replace in-place | `buildFromRawContent()` → parse marker → find/replace or append `ToolCardData` |
| **Final View** | `renderAllTextBlocks()` | `rebuildAllViews()` |

### 5.4 Tool Card Marker Format (`@@TC`)

```
Format:  @@TC|toolname|status|result@@
Example: @@TC|execute-command|done|gave 1 gold_ingot to player@@
Example: @@TC|execute-command|running|@@@@TC|execute-command|done|gave 1@@Hello text@@TC|search-web|running|@@...
```

- `status`: `"running"` or `"done"`
- `result`: Already parsed (JSON `result` field extracted), pipes and `@@` sanitized
- Multiple markers create multiple `ToolCardData` blocks in order
- Consecutive `running` + `done` for the same tool are merged into one (done replaces running)

### 5.5 Thread Safety Architecture

```
Event arrives: Minecraft Client Thread (client.execute)
                    ↓
mPendingUiUpdates.add(() -> handleStreamEventSafe(event))
scheduleFlush() → v.post(this::flushPendingUpdates)
                    ↓
ModernUI Looper Thread
    ↓
flushPendingUpdates() → while(poll) → task.run()
```

This two-level queue ensures:
1. Minecraft thread → fast enqueue (never blocks)
2. ModernUI Looper → sequential processing (thread-safe view mutations)

---

## 6. Settings & Configuration

### 6.1 Settings Page (`buildSettingsPage()`)

- LLM Provider section (OpenAI / Vertex AI via Spinner)
  - Endpoint, Model, API Key inputs
  - API Key inputs use `PasswordTransformationMethod` + Show/Hide toggle button
- General section
  - Enable GUI toggle
  - Debug Mode toggle
  - Limit Tool Calls toggle
- About section (version display)

### 6.2 API Key Input (`addApiKeyInput`)

Each API key field has:
- `PasswordTransformationMethod.getInstance()` — masks content with dots
- Toggle button (Show → Hide) — toggles between masked/visible
- Uses i18n keys `settings.show_key` / `settings.hide_key`

### 6.3 Save/Cancel

- Save → writes to `MineClawdConfig.HANDLER.save()` + saves window position
- Cancel → restores config values to EditTexts + switches

---

## 7. API Key Input Show/Hide

The `addApiKeyInput()` method in ModernHudFragment creates an EditText with a toggle:

```java
// Default: masked
et.setTransformationMethod(PasswordTransformationMethod.getInstance());

// Toggle: Show / Hide
toggle.setOnClickListener(v -> {
    shown[0] = !shown[0];
    et.setTransformationMethod(shown[0] ? null : PasswordTransformationMethod.getInstance());
    et.setSelection(et.getText().length());
    toggle.setText(shown[0] ? tr("settings.hide_key") : tr("settings.show_key"));
});
```

Uses i18n keys:
- `settings.show_key` → "Show"
- `settings.hide_key` → "Hide"

---

## 8. Stream Event Thread Safety

The `ModernHudFragment` uses a two-level thread safety pattern:

1. **Event Bridge Queue**: `mPendingUiUpdates` (ConcurrentLinkedQueue) — any thread can add
2. **Flush Schedule**: `scheduleFlush()` → `v.post(this::flushPendingUpdates)` — dispatches to ModernUI Looper
3. **Direct post**: `onSessionPayload()` uses `v.post(() -> ...)` directly (no queue needed for session data)

```java
private void scheduleFlush() {
    if (mFlushScheduled) return;
    mFlushScheduled = true;
    View v = getView();
    if (v != null) v.post(this::flushPendingUpdates);
}

private void flushPendingUpdates() {
    mFlushScheduled = false;
    if (getView() == null) return;
    Runnable task;
    while ((task = mPendingUiUpdates.poll()) != null) task.run();
}
```

---

## 9. Settings & Configuration

### 9.1 Settings Page (`buildSettingsPage()`)

- LLM Provider section (OpenAI / Vertex AI via Spinner)
  - Endpoint, Model, API Key inputs
- General section
  - Enable GUI toggle
  - Debug Mode toggle
  - Limit Tool Calls toggle
- Save/Cancel buttons

### 9.2 Save/Cancel

- Save → writes to `MineClawdConfig.HANDLER.save()` + saves window position
- Cancel → restores config values to EditTexts + switches

---

## 10. Class Line Count Reference (Updated)

| File | Lines | Package |
|------|-------|---------|
| `MineClawd.java` | ~6600 | `com.mineclawd` |
| `MineClawdClientNetworking.java` | ~560 | `com.mineclawd` |
| `ModernHudFragment.java` | ~1430 | `...ui.framework` |
| `MessageBubble.java` | ~370 | `...ui.framework` |
| `MessageResponseHandler.java` | ~126 | `...ui.framework` |
| `HudOrbState.java` | ~51 | `...ui.framework` |
| `MineClawdI18n.java` | ~120 | `...ui.i18n` |
| `LogView.java` | ~186 | `...ui.log` |
| `SessionOverlayPayload.java` | ~240 | `..session` |
| `AgentResponseOverlay.java` | ~4515 | `..client` (LEGACY) |
| `ChatStreamBridge.java` | ~22 | `..client` |
| `SessionPayloadBridge.java` | ~22 | `..client` |
| `HudOrbHandler.java` | ~232 | `..client` (neoforge) |

---

## 11. Rules & Conventions

### Architecture Rules
1. **All new GUI development should go into `ModernHudFragment.java` and `MessageBubble.java`**. The `AgentResponseOverlay.java` is legacy and should not be modified.
2. **Static bridges pattern**: `ChatStreamBridge` / `SessionPayloadBridge` for inter-class communication.
3. **ConcurrentLinkedQueue for thread safety**: Enqueue on Minecraft thread, process on ModernUI Looper.

### Anti-Patterns to Avoid
1. ❌ `clearListener()` in `onDestroy()` — causes race conditions. Just let the new Fragment's `setListener` override.
2. ❌ `LayoutTransition` on sidebars that get `removeAllViews` from Minecraft thread.
3. ❌ `readInt()` for agent stream event type (wire format is `writeByte/readByte`).
4. ❌ Text-only tool status display in bubbles — use `MessageBubble.addToolCard/updateToolCard`.

### Convention Summary
1. **`mPendingUiUpdates` for all stream events** → `scheduleFlush()` → `v.post`
2. **`v.post(() -> ...)` directly for session payload** (less frequent)
3. **`MessageBubble.getContent()` as single source of truth** — no separate tracking needed
4. **`@@TC` markers for serialization** — parsed by `MessageBubble.buildFromRawContent()`
5. **`session.id` (short hex) for commands** — NOT `session.token` which contains hyphens
6. **Always use `readByte()` for stream event type**, matching server's `writeByte()`
