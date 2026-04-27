package com.mineclawd.foundation.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public record SessionAttachment(String workspacePath, String originalName, boolean image) {
    public SessionAttachment {
        workspacePath = normalize(workspacePath);
        originalName = originalName == null ? "" : originalName.trim();
    }

    public boolean isValid() {
        return !workspacePath.isBlank();
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("workspacePath", workspacePath);
        root.addProperty("originalName", originalName);
        root.addProperty("image", image);
        return root;
    }

    public static String toJsonArray(List<SessionAttachment> attachments) {
        JsonArray array = new JsonArray();
        if (attachments != null) {
            for (SessionAttachment attachment : attachments) {
                if (attachment == null || !attachment.isValid()) {
                    continue;
                }
                array.add(attachment.toJson());
            }
        }
        return array.toString();
    }

    public static List<SessionAttachment> fromJsonArray(String json) {
        List<SessionAttachment> attachments = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return attachments;
        }
        try {
            JsonElement rootElement = JsonParser.parseString(json);
            if (!rootElement.isJsonArray()) {
                return attachments;
            }
            JsonArray array = rootElement.getAsJsonArray();
            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject object = element.getAsJsonObject();
                String workspacePath = readString(object, "workspacePath");
                if (workspacePath.isBlank()) {
                    workspacePath = readString(object, "path");
                }
                SessionAttachment attachment = new SessionAttachment(
                        workspacePath,
                        readString(object, "originalName"),
                        readBoolean(object, "image")
                );
                if (attachment.isValid()) {
                    attachments.add(attachment);
                }
            }
        } catch (Exception ignored) {
        }
        return attachments;
    }

    private static String normalize(String value) {
        String normalized = value == null ? "" : value.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static String readString(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static boolean readBoolean(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) {
            return false;
        }
        try {
            return object.get(key).getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }
}
