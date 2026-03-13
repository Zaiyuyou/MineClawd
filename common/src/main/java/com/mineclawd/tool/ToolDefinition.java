d:\MC\MechanoMunch\versions\XechanoMunch\mods\projects\MineClawd\common\src\main\java\com\mineclawd\tool\ToolDefinition.java
package com.mineclawd.tool;

import com.google.gson.JsonObject;

public record ToolDefinition(
    String name,
    String description,
    JsonObject parameters
) {}