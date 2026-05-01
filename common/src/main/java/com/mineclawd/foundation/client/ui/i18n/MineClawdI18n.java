package com.mineclawd.foundation.client.ui.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MineClawdI18n {

    private static Locale sLocale = Locale.getDefault();
    private static final Map<String, String> FALLBACK = new HashMap<>();

    static {
        FALLBACK.put("window.title", "MineClawd");
        FALLBACK.put("nav.chat", "Chat");
        FALLBACK.put("nav.sessions", "Sessions");
        FALLBACK.put("nav.files", "Files");
        FALLBACK.put("nav.tools", "Tools");
        FALLBACK.put("nav.assets", "Assets");
        FALLBACK.put("nav.settings", "Settings");
        FALLBACK.put("chat.input_hint", "Ask MineClawd anything...");
        FALLBACK.put("chat.send", "Send");
        FALLBACK.put("chat.attach", "Attach");
        FALLBACK.put("chat.assistant", "Assistant");
        FALLBACK.put("chat.you", "You");
        FALLBACK.put("chat.thinking", "Thinking...");
        FALLBACK.put("sidebar.sessions", "SESSIONS");
        FALLBACK.put("sidebar.current_state", "CURRENT STATE");
        FALLBACK.put("sidebar.new_session", "+");
        FALLBACK.put("session.new_title", "New Session");
        FALLBACK.put("session.new_preview", "Start a new conversation...");
        FALLBACK.put("session.new_time", "now");
        FALLBACK.put("settings.title", "Settings");
        FALLBACK.put("settings.llm_provider", "LLM Provider");
        FALLBACK.put("settings.toggle_switch", "Toggle to switch");
        FALLBACK.put("settings.openai", "OpenAI");
        FALLBACK.put("settings.vertex_ai", "Google Vertex AI");
        FALLBACK.put("settings.endpoint", "Endpoint");
        FALLBACK.put("settings.model", "Model");
        FALLBACK.put("settings.api_key", "API Key");
        FALLBACK.put("settings.interface_section", "Interface");
        FALLBACK.put("settings.enable_gui", "Enable GUI");
        FALLBACK.put("settings.debug_mode", "Debug Mode");
        FALLBACK.put("settings.limit_tool_calls", "Limit Tool Calls");
        FALLBACK.put("settings.show_key", "Show");
        FALLBACK.put("settings.hide_key", "Hide");
        FALLBACK.put("settings.save", "Save");
        FALLBACK.put("settings.cancel", "Cancel");
        FALLBACK.put("settings.saved", "Settings saved. Changes will apply to new conversations.");
        FALLBACK.put("settings.full_config", "Full settings are available via /mineclawd config");
        FALLBACK.put("settings.provider", "Provider");
        FALLBACK.put("settings.gui_status", "GUI");
        FALLBACK.put("settings.debug_status", "Debug");
        FALLBACK.put("settings.tool_calls_status", "Tool Calls");
        FALLBACK.put("settings.enabled", "Enabled");
        FALLBACK.put("settings.disabled", "Disabled");
        FALLBACK.put("settings.on", "On");
        FALLBACK.put("settings.off", "Off");
        FALLBACK.put("settings.limited_to", "Limited to");
        FALLBACK.put("settings.unlimited", "Unlimited");
        FALLBACK.put("settings.not_set", "(not set)");
        FALLBACK.put("common.version", "MineClawd v2.0.1");
        FALLBACK.put("log.title", "LOG");
        FALLBACK.put("log.empty", "No messages");
        FALLBACK.put("log.tool_executed", "Tool executed: %s");
        FALLBACK.put("log.task_complete", "Task complete: %s");
        FALLBACK.put("log.error_occurred", "Error: %s");
        FALLBACK.put("log.info", "Info: %s");
        FALLBACK.put("log.session_switched", "Switched to session: %s");
        FALLBACK.put("log.settings_saved", "Settings saved");
        FALLBACK.put("emoji.robot", "\uD83E\uDD16");
        FALLBACK.put("emoji.logo", "");
        FALLBACK.put("emoji.nav_chat", "\uD83D\uDCAC");
        FALLBACK.put("emoji.nav_sessions", "\uD83D\uDCDA");
        FALLBACK.put("emoji.nav_files", "\uD83D\uDCC1");
        FALLBACK.put("emoji.nav_tools", "\uD83D\uDD27");
        FALLBACK.put("emoji.nav_assets", "\uD83D\uDCE6");
        FALLBACK.put("emoji.nav_settings", "\u2699\uFE0F");
        FALLBACK.put("emoji.session_icon", "\uD83D\uDCAC");
        FALLBACK.put("emoji.attach", "\uD83D\uDCCE");
        FALLBACK.put("emoji.checkmark", "\u2705");
        FALLBACK.put("emoji.toggle_on", "\u2705");
        FALLBACK.put("emoji.toggle_off", "\u274C");
        FALLBACK.put("emoji.error", "\u26A0\uFE0F");
        FALLBACK.put("chat.tools", "tools");
        FALLBACK.put("chat.stop", "Stop");
        FALLBACK.put("chat.retry", "Retry");
        FALLBACK.put("chat.retry_available", "Retry available. Click retry to regenerate.");
        FALLBACK.put("chat.retrying", "Retrying last request...");
        FALLBACK.put("chat.stopped", "Generation stopped.");
        FALLBACK.put("chat.attached", "file(s) attached");
        FALLBACK.put("chat.tool_running", "Running tool");
        FALLBACK.put("chat.tool_done", "Tool completed");
        FALLBACK.put("chat.tool_prefix", "\u2699");
        FALLBACK.put("chat.tool_prefix_done", "\u2713");
        FALLBACK.put("chat.tool_result", "Result");
        FALLBACK.put("sidebar.no_sessions", "No sessions yet. Start a new conversation!");
        FALLBACK.put("session.just_now", "just now");
        FALLBACK.put("chat.delete_session", "Delete Session");
        FALLBACK.put("log.session_deleted", "Session deleted");
    }

    public static String tr(String key) {
        String translated = net.minecraft.client.resource.language.I18n.translate("mineclawd." + key);
        if (translated != null && !translated.equals("mineclawd." + key)) {
            return translated;
        }
        return FALLBACK.getOrDefault(key, key);
    }

    public static String tr(String key, Object... args) {
        String translated = net.minecraft.client.resource.language.I18n.translate("mineclawd." + key, args);
        if (translated != null && !translated.equals("mineclawd." + key)) {
            return translated;
        }
        String fallback = FALLBACK.getOrDefault(key, key);
        return String.format(fallback, args);
    }

    public static String emoji(String key) {
        String translated = net.minecraft.client.resource.language.I18n.translate("mineclawd.emoji." + key);
        if (translated != null && !translated.equals("mineclawd.emoji." + key)) {
            return translated;
        }
        return FALLBACK.getOrDefault("emoji." + key, "");
    }

    public static void setLocale(Locale locale) {
        sLocale = locale;
    }
}
