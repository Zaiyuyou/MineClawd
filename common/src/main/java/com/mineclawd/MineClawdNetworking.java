package com.mineclawd;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.util.Identifier;

import java.util.List;

public final class MineClawdNetworking {
    public static final Identifier CLIENT_READY = Identifier.of(MineClawd.MOD_ID, "client_ready");
    public static final Identifier CLIENT_GUI_PREF = Identifier.of(MineClawd.MOD_ID, "client_gui_pref");
    public static final Identifier OPEN_CONFIG = Identifier.of(MineClawd.MOD_ID, "open_config");
    public static final Identifier SYNC_BROADCAST_TARGET = Identifier.of(MineClawd.MOD_ID, "sync_broadcast_target");
    public static final Identifier SYNC_ASSISTIVE_TOUCH = Identifier.of(MineClawd.MOD_ID, "sync_assistive_touch");
    public static final Identifier SYNC_DYNAMIC_CONTENT = Identifier.of(MineClawd.MOD_ID, "sync_dynamic_content");
    public static final Identifier OPEN_QUESTION = Identifier.of(MineClawd.MOD_ID, "open_question");
    public static final Identifier QUESTION_RESPONSE = Identifier.of(MineClawd.MOD_ID, "question_response");
    public static final Identifier OPEN_SESSIONS = Identifier.of(MineClawd.MOD_ID, "open_sessions");
    public static final Identifier OPEN_ASSETS = Identifier.of(MineClawd.MOD_ID, "open_assets");
    public static final Identifier OPEN_HISTORY_BOOK = Identifier.of(MineClawd.MOD_ID, "open_history_book");
    public static final Identifier AGENT_STREAM_EVENT = Identifier.of(MineClawd.MOD_ID, "agent_stream_event");
    private static final List<Identifier> SERVER_TO_CLIENT_CHANNELS = List.of(
            OPEN_CONFIG,
            SYNC_BROADCAST_TARGET,
            SYNC_ASSISTIVE_TOUCH,
            SYNC_DYNAMIC_CONTENT,
            OPEN_QUESTION,
            OPEN_SESSIONS,
            OPEN_ASSETS,
            OPEN_HISTORY_BOOK,
            AGENT_STREAM_EVENT
    );

    private MineClawdNetworking() {
    }

    public static void register() {
        if (Platform.getEnvironment() != Env.SERVER) {
            return;
        }
        for (Identifier channel : SERVER_TO_CLIENT_CHANNELS) {
            NetworkManager.registerS2CPayloadType(channel);
        }
        MineClawd.LOGGER.info("[MineClawd] Registered {} server-to-client payload channels.", SERVER_TO_CLIENT_CHANNELS.size());
    }
}
