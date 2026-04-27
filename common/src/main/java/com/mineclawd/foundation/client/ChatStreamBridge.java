package com.mineclawd.foundation.client;

import com.mineclawd.AgentStreamEventType;
import com.mineclawd.MineClawd;

import java.util.function.Consumer;

public class ChatStreamBridge {

    private static Consumer<StreamEvent> sListener;

    public static void setListener(Consumer<StreamEvent> listener) {
        MineClawd.LOGGER.info("[ChatStreamBridge] setListener called, listener={}", listener != null ? listener.getClass().getSimpleName() : "null");
        sListener = listener;
    }

    public static void clearListener() {
        MineClawd.LOGGER.info("[ChatStreamBridge] clearListener called");
        sListener = null;
    }

    public static void forward(String requestId, AgentStreamEventType type, String payload) {
        Consumer<StreamEvent> listener = sListener;
        if (listener != null) {
            MineClawd.LOGGER.info("[ChatStreamBridge] forward: calling listener for type={}", type);
            listener.accept(new StreamEvent(requestId, type, payload));
        } else {
            MineClawd.LOGGER.warn("[ChatStreamBridge] forward: NO LISTER REGISTERED! type={}, payload_len={}", type, payload != null ? payload.length() : 0);
        }
    }

    public record StreamEvent(String requestId, AgentStreamEventType type, String payload) {
    }
}
