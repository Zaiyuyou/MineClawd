d:\MC\MechanoMunch\versions\XechanoMunch\mods\projects\MineClawd\common\src\main\java\com\mineclawd\tool\ToolExecutionResult.java
package com.mineclawd.tool;

public record ToolExecutionResult(
    boolean success,
    String output
) {
    public static ToolExecutionResult success(String output) {
        return new ToolExecutionResult(true, output);
    }
    
    public static ToolExecutionResult failure(String error) {
        return new ToolExecutionResult(false, error);
    }
}