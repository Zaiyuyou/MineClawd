d:\MC\MechanoMunch\versions\XechanoMunch\mods\projects\MineClawd\common\src\main\java\com\mineclawd\tool\ToolExecutor.java
package com.mineclawd.tool;

import com.google.gson.JsonObject;
import net.minecraft.server.command.ServerCommandSource;

public interface ToolExecutor {
    String getName();
    
    String getDescription();
    
    JsonObject getParameters();
    
    ToolExecutionResult execute(ServerCommandSource source, JsonObject arguments);
}