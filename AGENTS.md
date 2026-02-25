This is a Minecraft Architectury Mod, which supports NeoForge 1.21.1, Forge/Fabric 1.20.1. It doesn't and won't support Fabric 1.21.1.

Always respond in English, regardless of the language used in the question.

There're two branches in this repository, 1.21.1 and 1.20.1.

This mod should be required on servers, but optional on clients. When adding new features, ensure they are implemented in a way that degrades gracefully for clients without the mod. When client is not running the mod, they should still be able to connect to the server and play without major issues. For example, degrade GUI interfaces to chat-based interactions, like using clickable chat messages instead of custom buttons, commands instead of GUI, etc.

You can use runClient configurations to test and ensure at least the game won't crash before you give it to me for testing.