<div align="center">
    <img src="public/mascot.png" width="150" alt="MineClawd Mascot">

# MineClawd

<p>
    <strong>Bring OpenClaw into Minecraft! The Minecraft Agent that actually do things.</strong>
</p>

<p>Mascot: <strong>Yuki</strong></p>

[![Modrinth](https://img.shields.io/badge/Modrinth-RWXmIPlB-00AF5C?style=flat&logo=modrinth)](https://modrinth.com/project/RWXmIPlB)
[![Wiki](https://img.shields.io/badge/Wiki-Docs-blue?style=flat&logo=bookstack)](https://docs.baimoqilin.com)
[![重构版本](https://img.shields.io/badge/重构版本-v2.0--重构版-orange?style=flat)](TOOL_ARCHITECTURE.md)

</div>

https://github.com/user-attachments/assets/baf1b5c6-56be-43ee-90d1-097e9fbc75fe

> [!WARNING]
> MineClawd is an experimental project. It can execute generated JavaScript in-game. Treat OP access and API credentials as sensitive, and only use this mod in environments you trust.

> [!NOTE]
> **🎉 重大更新：完全重构版本已发布！**
> 此版本对工具系统进行了完全重构，采用模块化架构、配置驱动设计和热重载支持。查看 [架构文档](TOOL_ARCHITECTURE.md) 了解详情。

**MineClawd** is an Architectury mod that brings the power of AI to Minecraft, letting you modify the game using natural language.

## 🚀 重构亮点

**完全重构的工具系统** - 从硬编码到模块化架构的彻底革新：

- **🏗️ 模块化架构**：工具按功能分类，支持热重载和动态配置
- **⚙️ 配置驱动**：支持运行时工具启用/禁用，无需重启游戏
- **🔧 29个内置工具**：涵盖文件操作、网络搜索、KubeJS集成等
- **📋 命令管理**：`/mineclawd tools` 命令支持工具状态管理
- **🔄 热重载支持**：与 Minecraft reload 指令完美联动

**Supported platforms on branch `1.21.1` (v1.3.0):**

- NeoForge `1.21.1`
- Fabric `1.21.1` (pending KubeJS Fabric support)

## What it can do

**Turn your words into reality, instantly.**

MineClawd is like a magic wand for your world. You do not need to know how to code - just tell the game what you want, and watch it happen!

- **Dream it, Say it, Play it**: Want a sword made of cookies? A helmet that gives you night vision? Or maybe you want it to rain diamonds? Just describe it in the chat!

- **No Restarts Needed**: Forget about closing the game to add mods. New blocks, items, and recipes are created right before your eyes in real-time.

- **Unleash Your Imagination**: Ever wanted to give your cat a suit of armor? Or walk on clouds that spawn under your feet? With MineClawd, your wildest ideas are just a sentence away.

## Features

### 🔧 重构版本新特性

- **🏗️ 模块化工具系统**：29个内置工具按功能分类，支持热重载
- **⚙️ 配置驱动架构**：运行时工具管理，无需重启游戏
- **🔄 热重载支持**：与 Minecraft reload 指令完美联动
- **📋 工具状态管理**：`/mineclawd tools` 命令支持启用/禁用工具
- **🔍 揭露式提示词**：三层信息揭露机制，优化token使用

### 🎯 核心功能

- **Natural language control**: ask for gameplay help, world edits, automation, or admin tasks directly in chat.
- **Real actions, not fixed macros**: MineClawd can inspect state and adapt steps while it works.
- **Persistent automation support**: it can write and update server-side logic for commands, recipes, drops, listeners, and tick behavior.
- **Session management built for long tasks**: create, list, resume, and remove sessions without losing context.
- **Persona system**: switch between `default`, `yuki`, or your own custom souls.
- **Rich chat UX**: readable progress updates and Markdown/MineDown formatted replies in-game.
- **Multi-provider LLM support**: OpenAI-compatible endpoints and Google Vertex AI.
- **Practical guardrails**: optional tool-call limits, debug logs, and OP-only command access.

## 🛠️ 工具管理命令

重构版本新增了强大的工具管理系统：

```bash
# 查看所有工具状态
/mineclawd tools

# 启用特定工具
/mineclawd tools enable search

# 禁用特定工具  
/mineclawd tools disable curl

# 重载工具配置
/mineclawd tools reload
```

**配置示例** (`config/mineclawd-tools.json`):
```json
{
  "execute-command": {
    "enabled": true,
    "category": "KubeJS"
  },
  "search": {
    "enabled": false,
    "category": "Web"
  }
}
```

## Quick Start

Moved to <https://docs.baimoqilin.com>

## Build

From the project root:

```bash
./gradlew collectJars
```

Release jars are collected into `build/libs/`:

- `mineclawd-neoforge-1.3.0.jar` (NeoForge `1.21.1`)

## Security

- MineClawd can execute generated KubeJS JavaScript and run commands.
- Restrict OP access.
- Use least-privilege API keys and rotate them if exposed.

## Credits

- **Project Author**: Zhou-Shilin (BaimoQilin)
- **Coding Support**: OpenAI Codex
- **Libraries and Upstream Projects**:
  - Architectury API and Architectury Loom by the Architectury contributors.
  - Fabric Loader, Fabric API, and Yarn mappings by FabricMC contributors.
  - NeoForge and Forge maintainers/contributors.
  - KubeJS by the KubeJS maintainers and contributors.
  - YetAnotherConfigLib (YACL) by isXander and contributors.
  - Mod Menu by TerraformersMC contributors.
  - Better Modlist contributors.
  - MineDown Adventure by Phoenix616 and contributors.
  - Adventure (Kyori) by kyori and contributors.
  - Gson by Google.

## License

Apache License 2.0. See `LICENSE`.
