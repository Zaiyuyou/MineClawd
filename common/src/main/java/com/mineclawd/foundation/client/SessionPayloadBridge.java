package com.mineclawd.foundation.client;

import com.mineclawd.foundation.session.SessionOverlayPayload;

import java.util.function.Consumer;

public class SessionPayloadBridge {

    private static Consumer<SessionOverlayPayload> sListener;

    public static void setListener(Consumer<SessionOverlayPayload> listener) {
        sListener = listener;
    }

    public static void clearListener() {
        sListener = null;
    }

    public static void forward(SessionOverlayPayload payload) {
        Consumer<SessionOverlayPayload> listener = sListener;
        if (listener != null) {
            listener.accept(payload);
        }
    }
}
