package com.mineclawd.foundation.client.ui.framework;

import com.google.gson.JsonParser;
import com.mineclawd.MineClawd;
import com.mineclawd.foundation.client.ChatStreamBridge;
import com.mineclawd.foundation.client.CharInputBridge;
import com.mineclawd.foundation.client.SessionPayloadBridge;
import com.mineclawd.foundation.client.ui.i18n.MineClawdI18n;
import com.mineclawd.foundation.client.ui.log.LogView;
import com.mineclawd.foundation.client.ui.log.LogManager;
import com.mineclawd.foundation.config.MineClawdConfig;
import com.mineclawd.foundation.session.SessionOverlayPayload;
import icyllis.modernui.animation.LayoutTransition;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ObjectAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.R;
import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.markflow.Markflow;
import icyllis.modernui.markflow.MarkflowPlugin;
import icyllis.modernui.markflow.MarkflowTheme;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.Spannable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.*;
import icyllis.modernui.widget.*;
import net.minecraft.client.MinecraftClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModernHudFragment extends Fragment implements ScreenCallback {

    private int mColorPrimary;
    private int mColorOnSurface;
    private int mColorOnSurfaceVariant;
    private int mColorSurface;
    private int mColorSurfaceContainerLow;
    private int mColorSurfaceContainerHigh;
    private int mColorSurfaceContainerHighest;
    private int mColorOutline;
    private int mColorOutlineVariant;
    private int mColorError;

    private void resolveThemeColors(Context ctx) {
        TypedValue tv = new TypedValue();
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorPrimary, tv, true); mColorPrimary = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorOnSurface, tv, true); mColorOnSurface = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorOnSurfaceVariant, tv, true); mColorOnSurfaceVariant = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurface, tv, true); mColorSurface = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurfaceContainerLow, tv, true); mColorSurfaceContainerLow = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurfaceContainerHigh, tv, true); mColorSurfaceContainerHigh = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurfaceContainerHighest, tv, true); mColorSurfaceContainerHighest = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorOutline, tv, true); mColorOutline = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorOutlineVariant, tv, true); mColorOutlineVariant = tv.data;
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorError, tv, true); mColorError = tv.data;
    }

    private static final float WINDOW_W_RATIO = 0.75f;
    private static final float WINDOW_H_RATIO = 0.78f;
    private static final float MIN_W_RATIO = 0.40f;
    private static final float MIN_H_RATIO = 0.35f;
    private static final int TITLE_H_DP = 46;
    private static final int NAV_W_DP = 42;
    private static final int SIDEBAR_W_DP = 240;
    private static final int HANDLE_THICK_DP = 6;
    private static final int HANDLE_CORNER_DP = 14;
    private static final int DRAG_THRESHOLD_DP = 6;

    private static final int RESIZE_NONE = 0;
    private static final int RESIZE_L = 1;
    private static final int RESIZE_R = 2;
    private static final int RESIZE_T = 4;
    private static final int RESIZE_B = 8;

    private FrameLayout mRoot;
    private FrameLayout mWindow;
    private FrameLayout mSidebar;
    private LinearLayout mSidebarContent;
    private LogView mLogView;
    private FrameLayout mMainContent;
    private View[] mResizeHandles;
    private View mSidebarResizer;

    private float mXNorm, mYNorm, mWNorm, mHNorm;
    private int mTitleH, mNavW, mSidebarW, mDragThresh;
    private int mWindowRestoreX, mWindowRestoreY, mWindowRestoreW, mWindowRestoreH;
    private boolean mMaximized, mSidebarCollapsed;
    private boolean mDragging, mDragReady;
    private float mDragStartX, mDragStartY, mLastTouchX, mLastTouchY;
    private int mResizing;
    private int mResizeStartMouseX, mResizeStartMouseY;
    private float mResizeStartWNorm, mResizeStartHNorm;
    private float mResizeStartXNorm, mResizeStartYNorm;
    private boolean mSidebarResizing;
    private int mSidebarResizeStartX, mSidebarResizeStartW;
    private int mActiveTab;

    private Markflow mMarkflow;
    private static class ChatMessage {
        String role; String text;
        ChatMessage(String r, String t) { role=r; text=t; }
    }
    private final List<ChatMessage> mMessages = new ArrayList<>();
    private ScrollView mChatScroll;
    private LinearLayout mChatContainer;
    private EditText mChatInput;
    private LinearLayout mInputArea;
    private View mCurrentTyping;
    private String mCurrentTypingText = "";
    private final List<ToolCallInfo> mPendingToolCalls = new ArrayList<>();
    private LinearLayout mToolStatusContainer;

    private static class ToolCallInfo {
        String name; String args; String result; boolean done;
        ToolCallInfo(String n, String a) { name=n; args=a; }
    }

    private String mActiveSessionId = "";
    private String mFailedRetryToken = "";
    private boolean mGenerating;
    private Button mSendBtn;
    private LinearLayout mBtnRow;

    private static class SessionItemData {
        String id; String title; String token; long updatedAt; boolean active;
        SessionItemData(String i, String t, String tk, long u, boolean a) { id=i; title=t; token=tk; updatedAt=u; active=a; }
    }
    private final List<SessionItemData> mSessions = new ArrayList<>();
    private int mDragSessionIdx = -1;
    private float mDragSessionStartY;

    private final java.util.Queue<Runnable> mPendingUiUpdates = new java.util.ArrayDeque<>();
    private boolean mFlushScheduled;

    private EditText mSettingsEndpoint, mSettingsModel, mSettingsApiKey;
    private EditText mSettingsVertexEndpoint, mSettingsVertexModel, mSettingsVertexApiKey;
    private Spinner mSettingsProviderSpinner;
    private boolean mSettingsUseVertex;
    private Switch mSettingsGuiToggle, mSettingsDebugToggle, mSettingsToolLimitToggle;

    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);
        MineClawd.LOGGER.info("[GUI] onCreate called, registering listener");
        EmojiEnabler.ensure();
        ChatStreamBridge.setListener(this::onStreamEvent);
        SessionPayloadBridge.setListener(this::onSessionPayload);
    }

    private void scheduleFlush() {
        if (mFlushScheduled) return;
        mFlushScheduled = true;
        View v = getView();
        if (v != null) {
            v.post(this::flushPendingUpdates);
        }
        // If View is null, we'll retry when onCreateView creates the View
    }

    private void flushPendingUpdates() {
        mFlushScheduled = false;
        if (getView() == null) {
            return;
        }
        Runnable task;
        while ((task = mPendingUiUpdates.poll()) != null) {
            task.run();
        }
    }

    // Called when View is created to process any queued events
    private void processQueuedEvents() {
        flushPendingUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        IMBlockerCompat.onScreenOpened(MinecraftClient.getInstance().currentScreen);
        EmojiEnabler.onResourcesReady();
        restoreSidebarState();
        flushPendingUpdates();
        requestSessionsFromServer();
    }

    private void restoreSidebarState() {
        if (HudOrbState.hasSavedSidebarState()) {
            mSidebarW = HudOrbState.getSavedSidebarW();
            mSidebarCollapsed = HudOrbState.getSavedSidebarCollapsed();
        } else {
            mSidebarW = mRoot != null ? mRoot.dp(SIDEBAR_W_DP) : 0;
            mSidebarCollapsed = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MineClawd.LOGGER.info("[GUI] onDestroy called, clearing listener");
        ChatStreamBridge.clearListener();
        SessionPayloadBridge.clearListener();
        CharInputBridge.deactivate();
        IMBlockerCompat.onScreenClosed();
    }

    private void requestSessionsFromServer() {
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("mineclawd sessions");
        }
    }

    private void onSessionPayload(SessionOverlayPayload payload) {
        View v = getView();
        if (v == null) {
            mPendingUiUpdates.add(() -> onSessionPayload(payload));
            scheduleFlush();
            return;
        }
        // Update session list and active session ID
        mActiveSessionId = payload.activeSessionId();
        mSessions.clear();
        for (var s : payload.sessions()) {
            mSessions.add(new SessionItemData(s.id(), s.title(), s.token(), s.updatedAtEpochMillis(), s.active()));
        }
        // Only populate history if mMessages is empty (first load after opening the GUI)
        if (mMessages.isEmpty()) {
            for (var h : payload.history()) {
                mMessages.add(new ChatMessage(h.assistant() ? "assistant" : "user", h.content()));
            }
        }
        // Update sidebar content
        updateSidebarContent(tr("nav.chat"));
        // Only rebuild chat page if not generating AND chat page is not built yet
        if (!mGenerating && mChatContainer == null && mActiveTab == 0) {
            updateMainContent(tr("nav.chat"));
        } else if (!mGenerating && mChatContainer != null) {
            // If not generating but chat page exists, just update the message list without full rebuild
            // This preserves any ongoing UI state
        }
    }

    private String extractRetryToken(String payload) {
        if (payload == null || payload.isBlank()) return "";
        String[] lines = payload.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase(java.util.Locale.ROOT).contains("retry token:")) {
                int idx = trimmed.indexOf(':');
                if (idx >= 0 && idx + 1 < trimmed.length()) {
                    return trimmed.substring(idx + 1).trim().replaceAll("[`\"']", "").split("\\s")[0];
                }
            }
            String prefix = "/mineclawd retry ";
            int ci = trimmed.toLowerCase(java.util.Locale.ROOT).indexOf(prefix);
            if (ci >= 0) {
                return trimmed.substring(ci + prefix.length()).trim().replaceAll("[`\"']", "").split("\\s")[0];
            }
        }
        return "";
    }

    private void onStreamEvent(ChatStreamBridge.StreamEvent event) {
        // 直接处理流事件，不进行复杂检查
        switch (event.type()) {
            case START -> {
                mPendingToolCalls.clear();
                mCurrentTypingText = "";
                mGenerating = true;
                mFailedRetryToken = "";
                // 确保聊天页面存在
                if (mChatContainer == null && mActiveTab == 0) {
                    updateMainContent(tr("nav.chat"));
                }
                ensureTypingIndicator();
                if (mToolStatusContainer != null) mToolStatusContainer.removeAllViews();
                updateSendButton();
            }
            case DELTA -> {
                // 直接追加文本，像旧GUI一样简单
                if (mCurrentTyping instanceof TextView tv) {
                    mCurrentTypingText += event.payload();
                    tv.setText(mCurrentTypingText);
                    scrollChat();
                } else {
                    // 如果没有打字指示器，直接创建消息气泡
                    ensureTypingIndicator();
                    if (mCurrentTyping instanceof TextView tv2) {
                        mCurrentTypingText += event.payload();
                        tv2.setText(mCurrentTypingText);
                        scrollChat();
                    }
                }
            }
            case TOOL_STATUS -> handleToolStatus(event.payload());
            case DONE -> {
                if (mCurrentTypingText.length() > 0) {
                    mMessages.add(new ChatMessage("assistant", mCurrentTypingText));
                }
                // Replace typing indicator with a proper bubble
                if (mCurrentTyping != null) {
                    var parent = (ViewGroup) mCurrentTyping.getParent();
                    if (parent != null) {
                        parent.removeView(mCurrentTyping);
                        if (mCurrentTypingText.length() > 0) {
                            addMessageBubble("assistant", mCurrentTypingText);
                        }
                    }
                } else if (mCurrentTypingText.length() > 0 && mChatContainer != null) {
                    addMessageBubble("assistant", mCurrentTypingText);
                }
                mCurrentTyping = null;
                mCurrentTypingText = "";
                mPendingToolCalls.clear();
                mGenerating = false;
                updateSendButton();
                scrollChat();
            }
            case ERROR -> {
                if (mCurrentTyping != null) {
                    var parent = (ViewGroup) mCurrentTyping.getParent();
                    if (parent != null) parent.removeView(mCurrentTyping);
                }
                String payload = event.payload();
                String retryToken = extractRetryToken(payload);
                if (!retryToken.isBlank()) mFailedRetryToken = retryToken;
                if (payload != null && !payload.isBlank()) {
                    String clean = payload.replaceAll("(?i)(retry token:|retry command:).*", "").trim();
                    if (clean.contains("/mineclawd retry")) {
                        clean = clean.replaceAll("(?i)/mineclawd retry \\S+", "").trim();
                    }
                    if (!clean.isBlank()) {
                        mMessages.add(new ChatMessage("assistant", "\u26A0 " + clean));
                        addMessageBubble("assistant", "\u26A0 " + clean);
                    }
                    LogManager.error(clean.isBlank() ? tr("log.error_occurred").replace("%s", "Unknown") : clean);
                    if (!retryToken.isBlank()) LogManager.info(tr("chat.retry_available"));
                }
                mCurrentTyping = null;
                mCurrentTypingText = "";
                mGenerating = false;
                updateSendButton();
                scrollChat();
            }
        }
    }

    private void ensureTypingIndicator() {
        if (mChatContainer == null && mActiveTab == 0 && mMainContent != null) {
            updateMainContent(tr("nav.chat"));
        }
        if (mChatContainer == null) return;
        if (mCurrentTyping != null) return;
        var ctx = mChatContainer.getContext();
        var typing = new TextView(ctx);
        typing.setTextSize(12);
        typing.setTextColor(mColorOnSurfaceVariant);
        typing.setPadding(0, mWindow.dp(4), 0, mWindow.dp(4));
        typing.setTextIsSelectable(true);
        typing.setSpannableFactory(Spannable.NO_COPY_FACTORY);
        // Set current text if any
        if (!mCurrentTypingText.isEmpty()) {
            typing.setText(mCurrentTypingText);
        }
        mCurrentTyping = typing;
        mChatContainer.addView(typing);
        scrollChat();
    }

    private void handleToolStatus(String payload) {
        if (payload == null || payload.isEmpty()) return;
        try {
            var json = JsonParser.parseString(payload).getAsJsonObject();
            String type = json.has("type") ? json.get("type").getAsString() : "";
            String name = json.has("name") ? json.get("name").getAsString() : "unknown";
            String args = json.has("args") ? json.get("args").getAsString() : "{}";
            String result = json.has("result") ? json.get("result").getAsString() : "";
            if ("start".equals(type)) {
                mPendingToolCalls.add(new ToolCallInfo(name, args));
                addToolCard(name, args, false);
                LogManager.tool("> " + tr("chat.tool_running") + ": " + name);
            } else if ("end".equals(type)) {
                for (var tc : mPendingToolCalls) {
                    if (tc.name.equals(name)) { tc.done = true; tc.result = result; break; }
                }
                addToolCard(name, args, true);
                LogManager.success(tr("chat.tool_done") + ": " + name);
                if (result != null && !result.isEmpty()) addToolResultEcho(result);
            }
        } catch (Exception ignored) {}
    }

    private void addToolCard(String name, String args, boolean done) {
        if (mChatContainer == null) return;
        var ctx = mChatContainer.getContext();
        if (mToolStatusContainer == null) {
            mToolStatusContainer = new LinearLayout(ctx);
            mToolStatusContainer.setOrientation(LinearLayout.VERTICAL);
            mToolStatusContainer.setPadding(0, mWindow.dp(2), 0, mWindow.dp(2));
            mChatContainer.addView(mToolStatusContainer);
        }
        var card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(mWindow.dp(10), mWindow.dp(6), mWindow.dp(10), mWindow.dp(6));
        card.setMinimumHeight(mWindow.dp(32));
        var cardBg = new ShapeDrawable();
        cardBg.setShape(ShapeDrawable.RECTANGLE);
        cardBg.setCornerRadius(mWindow.dp(6));
        cardBg.setColor(done ? (mColorPrimary & 0xFFFFFF) | 0x14000000 : mColorSurfaceContainerHigh);
        if (done) {
            cardBg.setStroke(mWindow.dp(1), (mColorPrimary & 0xFFFFFF) | 0x40000000);
        } else {
            cardBg.setStroke(mWindow.dp(1), mColorOutlineVariant);
        }
        card.setBackground(cardBg);
        var icon = new TextView(ctx);
        icon.setText(done ? "\u2713" : "\u2699");
        icon.setTextSize(13);
        icon.setTextColor(done ? mColorPrimary : mColorOnSurfaceVariant);
        card.addView(icon, new LinearLayout.LayoutParams(-2, -2));
        var label = new TextView(ctx);
        label.setText(name);
        label.setTextSize(11);
        label.setTextColor(done ? mColorPrimary : mColorOnSurface);
        label.setPadding(mWindow.dp(6), 0, 0, 0);
        card.addView(label, new LinearLayout.LayoutParams(-1, -2, 1.0f));
        var statusIcon = new TextView(ctx);
        statusIcon.setText(done ? "\u2714" : "\u23F3");
        statusIcon.setTextSize(11);
        statusIcon.setTextColor(done ? mColorPrimary : mColorOnSurfaceVariant);
        card.addView(statusIcon, new LinearLayout.LayoutParams(-2, -2));
        mToolStatusContainer.addView(card);
        scrollChat();
    }

    private void addToolResultEcho(String result) {
        if (mChatContainer == null) return;
        var ctx = mChatContainer.getContext();
        var echo = new LinearLayout(ctx);
        echo.setOrientation(LinearLayout.HORIZONTAL);
        echo.setPadding(mWindow.dp(10), mWindow.dp(4), mWindow.dp(10), mWindow.dp(4));
        var echoBg = new ShapeDrawable();
        echoBg.setShape(ShapeDrawable.RECTANGLE);
        echoBg.setCornerRadius(mWindow.dp(4));
        echoBg.setColor(mColorSurfaceContainerLow);
        echo.setBackground(echoBg);
        var prefix = new TextView(ctx);
        prefix.setText("$ ");
        prefix.setTextSize(10);
        prefix.setTextColor(mColorOutline);
        echo.addView(prefix, new LinearLayout.LayoutParams(-2, -2));
        var content = new TextView(ctx);
        content.setText(result);
        content.setTextSize(10);
        content.setTextColor(mColorOnSurfaceVariant);
        content.setMaxLines(3);
        echo.addView(content, new LinearLayout.LayoutParams(-1, -2));
        mChatContainer.addView(echo);
        scrollChat();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        var ctx = requireContext();
        resolveThemeColors(ctx);

        var builder = Markflow.builder(requireContext());
        var monoFont = Typeface.getSystemFont("JetBrains Mono Medium");
        if (monoFont != Typeface.SANS_SERIF) {
            builder.usePlugin(new MarkflowPlugin() {
                @Override
                public void configureTheme(@NonNull MarkflowTheme.Builder themeBuilder) {
                    themeBuilder.codeTypeface(monoFont);
                }
            });
        }
        mMarkflow = builder.build();

        mRoot = new FrameLayout(ctx);
        mRoot.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        mTitleH = mRoot.dp(TITLE_H_DP);
        mNavW = mRoot.dp(NAV_W_DP);
        mDragThresh = mRoot.dp(DRAG_THRESHOLD_DP);
        mSidebarW = mRoot.dp(SIDEBAR_W_DP);

        restoreSidebarState();

        if (HudOrbState.hasSavedWindowPos()) {
            mXNorm = HudOrbState.getSavedXNorm();
            mYNorm = HudOrbState.getSavedYNorm();
            mWNorm = HudOrbState.getSavedWNorm();
            mHNorm = HudOrbState.getSavedHNorm();
            HudOrbState.clearSavedWindowPos();
        } else {
            mWNorm = WINDOW_W_RATIO; mHNorm = WINDOW_H_RATIO;
            mXNorm = (1.0f - mWNorm) / 2.0f; mYNorm = (1.0f - mHNorm) / 2.0f;
        }

        int screenW = ctx.getResources().getDisplayMetrics().widthPixels;
        int screenH = ctx.getResources().getDisplayMetrics().heightPixels;

        mWindow = new FrameLayout(ctx);
        var initP = new FrameLayout.LayoutParams((int)(screenW*mWNorm), (int)(screenH*mHNorm));
        initP.leftMargin = (int)(screenW*mXNorm);
        initP.topMargin = (int)(screenH*mYNorm);
        mWindow.setLayoutParams(initP);
        {
            TypedValue cardTv = new TypedValue();
            ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurfaceContainerLow, cardTv, true);
            ShapeDrawable cardBg = new ShapeDrawable();
            cardBg.setShape(ShapeDrawable.RECTANGLE);
            cardBg.setCornerRadius(mWindow.dp(10));
            cardBg.setColor(cardTv.data);
            mWindow.setBackground(cardBg);
            mWindow.setElevation(mWindow.dp(2));
        }

        mWindow.addView(createTitleBar());
        mWindow.addView(createBody(ctx));
        mRoot.addView(mWindow);
        createResizeHandles();

        mRoot.post(() -> {
            applyWindowLayout();
            layoutResizeHandles();
            ObjectAnimator anim = ObjectAnimator.ofFloat(mWindow, View.SCALE_X, 0.92f, 1.0f);
            anim.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
            anim.setDuration(400);
            anim.start();
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(mWindow, View.ALPHA, 0f, 1f);
            anim2.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED);
            anim2.setDuration(300);
            anim2.start();
        });

        LayoutTransition transition = new LayoutTransition();
        transition.enableTransitionType(LayoutTransition.CHANGING);
        transition.setDuration(250);
        mMainContent.setLayoutTransition(transition);
        mRoot.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> {
            if (getView()!=null && (l!=ol||t!=ot||r!=or||b!=ob)) reapplyWindow();
        });
        mRoot.setOnTouchListener(this::onRootTouch);

        switchTab(0);
        flushPendingUpdates();
        return mRoot;
    }

    private void applyWindowLayout() {
        var p = (FrameLayout.LayoutParams) mWindow.getLayoutParams();
        if (mMaximized) { p.width=-1; p.height=-1; p.leftMargin=0; p.topMargin=0; }
        else { p.width=normW(mWNorm); p.height=normH(mHNorm); p.leftMargin=normW(mXNorm); p.topMargin=normH(mYNorm); }
        mWindow.setLayoutParams(p);
    }
    private void reapplyWindow() { if (!mMaximized) { clampTitleBarVisible(); applyWindowLayout(); } layoutResizeHandles(); }

    private View createTitleBar() {
        var ctx = mWindow.getContext();
        var bar = new LinearLayout(ctx);
        bar.setOrientation(LinearLayout.HORIZONTAL); bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutParams(new FrameLayout.LayoutParams(-1, mTitleH));
        bar.setPadding(mWindow.dp(12), 0, mWindow.dp(10), 0);
        TypedValue tv = new TypedValue();
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurface, tv, true);
        ShapeDrawable barBg = new ShapeDrawable();
        barBg.setShape(ShapeDrawable.RECTANGLE);
        barBg.setColor(tv.data);
        bar.setBackground(barBg);
        var logo = new TextView(ctx); logo.setText("\u2699 MineClawd"); logo.setTextSize(14); logo.setTextColor(mColorOnSurface);
        bar.addView(logo, new LinearLayout.LayoutParams(-2,-2));
        var agentCard = new LinearLayout(ctx);
        agentCard.setOrientation(LinearLayout.HORIZONTAL); agentCard.setGravity(Gravity.CENTER_VERTICAL);
        agentCard.setPadding(mWindow.dp(10), mWindow.dp(4), mWindow.dp(12), mWindow.dp(4));
        agentCard.setLayoutParams(new LinearLayout.LayoutParams(-2,-1));
        ((LinearLayout.LayoutParams)agentCard.getLayoutParams()).setMargins(mWindow.dp(14),0,0,0);
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurfaceContainerHighest, tv, true);
        var cardBg=new ShapeDrawable(); cardBg.setShape(ShapeDrawable.RECTANGLE); cardBg.setCornerRadius(mWindow.dp(6)); cardBg.setColor(tv.data);
        agentCard.setBackground(cardBg);
        var agentIcon=new TextView(ctx); agentIcon.setText("\uD83E\uDD16"); agentIcon.setTextSize(16);
        agentCard.addView(agentIcon,new LinearLayout.LayoutParams(-2,-2));
        var agentInfo=new LinearLayout(ctx); agentInfo.setOrientation(LinearLayout.VERTICAL);
        var agentName=new TextView(ctx); agentName.setText("expert_builder"); agentName.setTextSize(11); agentName.setTextColor(mColorOnSurface);
        agentInfo.addView(agentName);
        var agentSub=new TextView(ctx); agentSub.setText("Yuki \u00b7 12 "+tr("chat.tools")); agentSub.setTextSize(9); agentSub.setTextColor(mColorOnSurfaceVariant);
        agentInfo.addView(agentSub); agentCard.addView(agentInfo);
        bar.addView(agentCard);
        bar.addView(new View(ctx), new LinearLayout.LayoutParams(-1,-1,1.0f));
        var maxBtn=new Button(ctx); maxBtn.setText("\u25A1"); maxBtn.setTextSize(13); maxBtn.setTextColor(mColorOnSurfaceVariant);
        maxBtn.setBackground(themedRipple(ctx));
        maxBtn.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(30),mWindow.dp(30)));
        maxBtn.setOnClickListener(v->toggleMaximize()); bar.addView(maxBtn);
        var minBtn=new Button(ctx); minBtn.setText("\u2500"); minBtn.setTextSize(13); minBtn.setTextColor(mColorOnSurfaceVariant);
        minBtn.setBackground(themedRipple(ctx));
        minBtn.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(30),mWindow.dp(30)));
        minBtn.setOnClickListener(v->minimizeToOrb()); bar.addView(minBtn);
        bar.setOnTouchListener((v,e)->{
            if (e.getAction()==MotionEvent.ACTION_DOWN&&!mMaximized){
                mDragStartX=e.getRawX(); mDragStartY=e.getRawY();
                mLastTouchX=mDragStartX; mLastTouchY=mDragStartY; mDragReady=false; mDragging=false; return true;
            }
            if (e.getAction()==MotionEvent.ACTION_MOVE&&mDragging){
                float dx=e.getRawX()-mLastTouchX, dy=e.getRawY()-mLastTouchY;
                mXNorm=wToNorm(normW(mXNorm)+(int)dx); mYNorm=hToNorm(normH(mYNorm)+(int)dy);
                clampTitleBarVisible(); applyWindowLayout(); layoutResizeHandles();
                mLastTouchX=e.getRawX(); mLastTouchY=e.getRawY(); return true;
            }
            if (e.getAction()==MotionEvent.ACTION_MOVE&&!mDragReady){
                float dx=e.getRawX()-mDragStartX, dy=e.getRawY()-mDragStartY;
                if (dx*dx+dy*dy>mDragThresh*mDragThresh){mDragReady=true; mDragging=true; mWindow.setAlpha(0.75f);}
                mLastTouchX=e.getRawX(); mLastTouchY=e.getRawY(); return true;
            }
            if (e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                if (mDragging){mWindow.setAlpha(1.0f); HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm);}
                mDragging=false; mDragReady=false; return true;
            }
            return false;
        });
        return bar;
    }

    private RippleDrawable themedRipple(Context ctx) {
        TypedValue tv = new TypedValue();
        ctx.getTheme().resolveAttribute(R.ns, R.attr.colorControlHighlight, tv, true);
        return new RippleDrawable(ColorStateList.valueOf(tv.data), null, null);
    }

    private ShapeDrawable makeRoundedBg(int color, int radiusDp) {
        var s = new ShapeDrawable();
        s.setShape(ShapeDrawable.RECTANGLE);
        s.setCornerRadius(mWindow.dp(radiusDp));
        s.setColor(color);
        return s;
    }

    private View createBody(@NonNull Context ctx) {
        var body=new FrameLayout(ctx);
        var bodyP=new FrameLayout.LayoutParams(-1,-1); bodyP.topMargin=mTitleH; body.setLayoutParams(bodyP);
        var nav=new LinearLayout(ctx); nav.setOrientation(LinearLayout.VERTICAL);
        nav.setGravity(Gravity.CENTER_HORIZONTAL); nav.setPadding(0,mWindow.dp(6),0,0);
        nav.setLayoutParams(new FrameLayout.LayoutParams(mNavW,-1));
        nav.setBackground(makeRoundedBg(mColorSurface, 0));
        String[] eicons={"\uD83D\uDCAC","\uD83D\uDCCB","\uD83D\uDCC1","\uD83D\uDD27","\uD83D\uDCE6","\u2699"};
        for (int i=0;i<eicons.length;i++){final int ti=i;
            var item=new TextView(ctx); item.setText(eicons[i]); item.setTextSize(17);
            item.setTextColor(i==mActiveTab?mColorPrimary:mColorOnSurfaceVariant); item.setGravity(Gravity.CENTER);
            item.setLayoutParams(new LinearLayout.LayoutParams(mNavW,mWindow.dp(34)));
            if (i==mActiveTab){var abg=new ShapeDrawable(); abg.setShape(ShapeDrawable.RECTANGLE); abg.setColor((mColorPrimary & 0xFFFFFF) | 0x18000000); item.setBackground(abg);}
            item.setOnClickListener(v->switchTab(ti)); nav.addView(item);
        }
        body.addView(nav);
        mSidebar=new FrameLayout(ctx);
        mSidebar.setLayoutParams(new FrameLayout.LayoutParams(mSidebarCollapsed?0:mSidebarW,-1));
        ((FrameLayout.LayoutParams)mSidebar.getLayoutParams()).leftMargin=mNavW;
        mSidebar.setBackground(makeRoundedBg((mColorSurface & 0xFFFFFF) | 0x10000000, 0));
        mSidebarContent=new LinearLayout(ctx); mSidebarContent.setOrientation(LinearLayout.VERTICAL);
        mSidebarContent.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
        LayoutTransition sidebarTransition = new LayoutTransition();
        sidebarTransition.enableTransitionType(LayoutTransition.CHANGING);
        sidebarTransition.setDuration(200);
        mSidebarContent.setLayoutTransition(sidebarTransition);
        mSidebar.addView(mSidebarContent);
        mLogView=new LogView(ctx);
        var logP=new FrameLayout.LayoutParams(-1,-2); logP.gravity=Gravity.BOTTOM;
        mLogView.setLayoutParams(logP); mSidebar.addView(mLogView);
        body.addView(mSidebar);
        mSidebarResizer=new View(ctx);
        mSidebarResizer.setLayoutParams(new FrameLayout.LayoutParams(mWindow.dp(6),-1));
        var srP=(FrameLayout.LayoutParams)mSidebarResizer.getLayoutParams();
        srP.leftMargin=mNavW+(mSidebarCollapsed?0:mSidebarW)-mWindow.dp(3);
        mSidebarResizer.setLayoutParams(srP);
        mSidebarResizer.setOnTouchListener((v,e)->{
            switch(e.getAction()){
                case MotionEvent.ACTION_DOWN->{mSidebarResizing=true; mSidebarResizeStartX=(int)e.getRawX(); mSidebarResizeStartW=mSidebarW; return true;}
                case MotionEvent.ACTION_MOVE->{
                    if(!mSidebarResizing)return false;
                    int dx=(int)e.getRawX()-mSidebarResizeStartX, nw=Math.max(60,mSidebarResizeStartW+dx);
                    if(nw<=60){mSidebarCollapsed=true; mSidebarW=0;}else{mSidebarCollapsed=false; mSidebarW=Math.min(nw,600);}
                    updateSidebarLayout(); return true;
                }
                case MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
                    mSidebarResizing=false;
                    HudOrbState.saveSidebarState(mSidebarW, mSidebarCollapsed);
                    return true;
                }
            } return false;
        });
        body.addView(mSidebarResizer);
        mMainContent=new FrameLayout(ctx);
        var mcP=new FrameLayout.LayoutParams(-1,-1); mcP.leftMargin=mNavW+(mSidebarCollapsed?0:mSidebarW);
        mMainContent.setLayoutParams(mcP);
        body.addView(mMainContent);
        return body;
    }

    private void updateSidebarLayout(){
        int sw=mSidebarCollapsed?0:mSidebarW;
        var sbP=(FrameLayout.LayoutParams)mSidebar.getLayoutParams(); sbP.width=sw; mSidebar.setLayoutParams(sbP);
        var srP=(FrameLayout.LayoutParams)mSidebarResizer.getLayoutParams(); srP.leftMargin=mNavW+sw-mWindow.dp(3); mSidebarResizer.setLayoutParams(srP);
        var mcP=(FrameLayout.LayoutParams)mMainContent.getLayoutParams(); mcP.leftMargin=mNavW+sw; mMainContent.setLayoutParams(mcP);
        mRoot.requestLayout();
    }

    private void createResizeHandles(){mResizeHandles=new View[8];
        for(int i=0;i<8;i++){var h=new View(mRoot.getContext()); mResizeHandles[i]=h; mRoot.addView(h);}
        int[][] edges={{RESIZE_L|RESIZE_T},{RESIZE_R|RESIZE_T},{RESIZE_L|RESIZE_B},{RESIZE_R|RESIZE_B},{RESIZE_L},{RESIZE_R},{RESIZE_T},{RESIZE_B}};
        for(int i=0;i<8;i++){final int t=edges[i][0]; mResizeHandles[i].setOnTouchListener((v,e)->onResize(e,t));}
        layoutResizeHandles();
    }
    private boolean onResize(MotionEvent event, int type){
        if(mMaximized)return false;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN->{mResizing=type; mResizeStartMouseX=(int)event.getRawX(); mResizeStartMouseY=(int)event.getRawY();
                mResizeStartWNorm=mWNorm; mResizeStartHNorm=mHNorm; mResizeStartXNorm=mXNorm; mResizeStartYNorm=mYNorm; mWindow.setAlpha(0.75f); return true;}
            case MotionEvent.ACTION_MOVE->{
                if(mResizing==RESIZE_NONE)return false;
                int dx=(int)event.getRawX()-mResizeStartMouseX, dy=(int)event.getRawY()-mResizeStartMouseY;
                int sw=normW(mResizeStartWNorm),sh=normH(mResizeStartHNorm),sx=normW(mResizeStartXNorm),sy=normH(mResizeStartYNorm);
                int minW=normW(MIN_W_RATIO),minH=normH(MIN_H_RATIO);
                int nx=sx,ny=sy,nw=sw,nh=sh;
                if((type&RESIZE_L)!=0){nx=sx+dx; nw=sw-dx;}
                if((type&RESIZE_R)!=0){nw=sw+dx;}
                if((type&RESIZE_T)!=0){ny=sy+dy; nh=sh-dy;}
                if((type&RESIZE_B)!=0){nh=sh+dy;}
                if(nw<minW){if((type&RESIZE_L)!=0)nx=sx+sw-minW; nw=minW;}
                if(nh<minH){if((type&RESIZE_T)!=0)ny=sy+sh-minH; nh=minH;}
                mXNorm=wToNorm(nx); mYNorm=hToNorm(ny); mWNorm=wToNorm(nw); mHNorm=hToNorm(nh);
                clampTitleBarVisible(); applyWindowLayout(); layoutResizeHandles(); return true;
            }
            case MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{mResizing=RESIZE_NONE; mWindow.setAlpha(1.0f);
                HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm); return true;}
        } return false;
    }
    private void layoutResizeHandles(){
        if(mMaximized){for(var h:mResizeHandles)h.setLayoutParams(new FrameLayout.LayoutParams(0,0)); return;}
        int c=mRoot.dp(HANDLE_CORNER_DP),t=mRoot.dp(HANDLE_THICK_DP),half=c/2;
        int wx=normW(mXNorm),wy=normH(mYNorm),ww=normW(mWNorm),wh=normH(mHNorm);
        layH(mResizeHandles[0],wx-half,wy-half,c,c);layH(mResizeHandles[1],wx+ww-half,wy-half,c,c);
        layH(mResizeHandles[2],wx-half,wy+wh-half,c,c);layH(mResizeHandles[3],wx+ww-half,wy+wh-half,c,c);
        layH(mResizeHandles[4],wx-t/2,wy+half,t,wh-c);layH(mResizeHandles[5],wx+ww-t/2,wy+half,t,wh-c);
        layH(mResizeHandles[6],wx+half,wy-t/2,ww-c,t);layH(mResizeHandles[7],wx+half,wy+wh-t/2,ww-c,t);
    }
    private void layH(View v,int x,int y,int w,int h){var p=new FrameLayout.LayoutParams(w,h); p.leftMargin=x; p.topMargin=y; v.setLayoutParams(p);}

    private boolean onRootTouch(View v, MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN&&!mMaximized){
            float x=event.getX(),y=event.getY();
            int wx=normW(mXNorm),wy=normH(mYNorm),ww=normW(mWNorm),wh=normH(mHNorm);
            if(x<wx||x>wx+ww||y<wy||y>wy+wh){
                HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm);
                minimizeToOrb();
                return true;
            }
        }
        return false;
    }

    private void switchTab(int idx){
        mActiveTab=idx;
        String[] icons={"\uD83D\uDCAC","\uD83D\uDCCB","\uD83D\uDCC1","\uD83D\uDD27","\uD83D\uDCE6","\u2699"};
        String[] labels={tr("nav.chat"),tr("nav.sessions"),tr("nav.files"),tr("nav.tools"),tr("nav.assets"),tr("nav.settings")};
        var nav=(ViewGroup)((ViewGroup)mWindow.getChildAt(1)).getChildAt(0);
        for(int i=0;i<nav.getChildCount();i++){
            var tv=(TextView)nav.getChildAt(i); tv.setTextColor(i==idx?mColorPrimary:mColorOnSurfaceVariant); tv.setText(icons[i]);
            if(i==idx){var abg=new ShapeDrawable(); abg.setShape(ShapeDrawable.RECTANGLE); abg.setColor((mColorPrimary & 0xFFFFFF) | 0x18000000); tv.setBackground(abg);}
            else tv.setBackground(null);
        }
        updateSidebarContent(labels[idx]); updateMainContent(labels[idx]);
    }

    private void updateSidebarContent(String tab){
        mSidebarContent.removeAllViews();
        int dp14=mWindow.dp(14);
        if(tab.equals(tr("nav.chat"))||tab.equals(tr("nav.sessions"))){
            var hdr=new LinearLayout(mSidebarContent.getContext()); hdr.setOrientation(LinearLayout.HORIZONTAL);
            hdr.setGravity(Gravity.CENTER_VERTICAL); hdr.setPadding(dp14,mWindow.dp(12),dp14,mWindow.dp(8));
            var t=new TextView(mSidebarContent.getContext()); t.setText(tr("sidebar.sessions")); t.setTextSize(10); t.setTextColor(mColorOnSurfaceVariant);
            hdr.addView(t,new LinearLayout.LayoutParams(-1,-2,1.0f));
            var ab=new Button(mSidebarContent.getContext()); ab.setText(tr("sidebar.new_session")); ab.setTextSize(14); ab.setTextColor(mColorOnSurfaceVariant);
            ab.setBackground(null); ab.setPadding(mWindow.dp(4),0,mWindow.dp(4),0); ab.setOnClickListener(v->newSession()); hdr.addView(ab);
            mSidebarContent.addView(hdr);
            if (mSessions.isEmpty()) {
                var empty=new TextView(mSidebarContent.getContext());
                empty.setText(tr("sidebar.no_sessions"));
                empty.setTextSize(11); empty.setTextColor(mColorOnSurfaceVariant);
                empty.setPadding(dp14,mWindow.dp(8),dp14,mWindow.dp(8));
                mSidebarContent.addView(empty);
            }
            for(int i=0;i<mSessions.size();i++){final int fi=i; var s=mSessions.get(i);
                var item=createSessionItem(fi);
                mSidebarContent.addView(item);
            }
        }else if(tab.equals(tr("nav.settings"))){
            var sbTitle=new TextView(mSidebarContent.getContext()); sbTitle.setText(tr("sidebar.current_state")); sbTitle.setTextSize(10);
            sbTitle.setTextColor(mColorOnSurfaceVariant); sbTitle.setPadding(dp14,mWindow.dp(14),dp14,mWindow.dp(6));
            mSidebarContent.addView(sbTitle);
            var cfg=MineClawdConfig.get();
            for(var row:new String[][]{{"Provider",cfg.provider.displayName()},{"Model",cfg.model},
                {"GUI",cfg.enableGui?tr("settings.enabled"):tr("settings.disabled")},
                {"Debug",cfg.debugMode?tr("settings.on"):tr("settings.off")},
                {"Tool Calls",cfg.limitToolCalls?tr("settings.limited_to")+" "+cfg.toolCallLimit:tr("settings.unlimited")}}){
                var l=new TextView(mSidebarContent.getContext()); l.setText(row[0]+": "+row[1]); l.setTextSize(11);
                l.setTextColor(mColorOnSurfaceVariant); l.setPadding(dp14,mWindow.dp(3),dp14,mWindow.dp(3)); mSidebarContent.addView(l);
            }
            var ver=new TextView(mSidebarContent.getContext()); ver.setText(tr("common.version")); ver.setTextSize(10);
            ver.setTextColor(mColorOnSurfaceVariant); ver.setPadding(dp14,mWindow.dp(16),dp14,0); mSidebarContent.addView(ver);
        }else{
            var sbTitle=new TextView(mSidebarContent.getContext()); sbTitle.setText(tab.toUpperCase()); sbTitle.setTextSize(10);
            sbTitle.setTextColor(mColorOnSurfaceVariant); sbTitle.setPadding(dp14,mWindow.dp(14),dp14,mWindow.dp(6)); mSidebarContent.addView(sbTitle);
            var pl=new TextView(mSidebarContent.getContext()); pl.setText("Phase 2: "+tab+" sidebar"); pl.setTextSize(12);
            pl.setTextColor(mColorOnSurfaceVariant); pl.setPadding(dp14,mWindow.dp(6),dp14,0); mSidebarContent.addView(pl);
        }
    }

    private View createSessionItem(int idx) {
        var s = mSessions.get(idx);
        var ctx = mSidebarContent.getContext();
        var item = new LinearLayout(ctx);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(mWindow.dp(14), mWindow.dp(8), mWindow.dp(14), mWindow.dp(8));
        item.setOnClickListener(v -> switchSession(idx));
        item.setBackground(themedRipple(ctx));
        item.setMinimumHeight(mWindow.dp(48));
        if (s.active) {
            var activeBg = new ShapeDrawable();
            activeBg.setShape(ShapeDrawable.RECTANGLE);
            activeBg.setColor((mColorPrimary & 0xFFFFFF) | 0x18000000);
            item.setBackground(activeBg);
        }

        // Drag-reorder handle
        var dragHandle = new TextView(ctx);
        dragHandle.setText("\u2630");
        dragHandle.setTextSize(14);
        dragHandle.setTextColor(mColorOutlineVariant);
        dragHandle.setPadding(0, 0, mWindow.dp(6), 0);
        item.addView(dragHandle, new LinearLayout.LayoutParams(-2, -2));
        final int fi = idx;
        dragHandle.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                mDragSessionIdx = fi;
                mDragSessionStartY = e.getRawY();
                v.setAlpha(0.5f);
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_MOVE && mDragSessionIdx == fi) {
                float dy = e.getRawY() - mDragSessionStartY;
                if (Math.abs(dy) > mWindow.dp(24)) {
                    int dir = dy > 0 ? 1 : -1;
                    int targetIdx = fi + dir;
                    if (targetIdx >= 0 && targetIdx < mSessions.size()) {
                        Collections.swap(mSessions, fi, targetIdx);
                        mDragSessionIdx = targetIdx;
                        mDragSessionStartY = e.getRawY();
                        rebuildSessionList();
                    }
                }
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                if (mDragSessionIdx == fi) {
                    v.setAlpha(1f);
                    mDragSessionIdx = -1;
                }
                return true;
            }
            return false;
        });

        var ico=new TextView(ctx); ico.setText("\uD83D\uDCC4"); ico.setTextSize(14);
        item.addView(ico,new LinearLayout.LayoutParams(-2,-2));
        var info=new LinearLayout(ctx); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(mWindow.dp(8),0,0,0);
        var nm=new TextView(ctx); nm.setText(s.title); nm.setTextSize(12); nm.setTextColor(s.active?mColorOnSurface:mColorOnSurfaceVariant);
        info.addView(nm);
        var pv=new TextView(ctx);
        pv.setText(formatSessionTime(s.updatedAt));
        pv.setTextSize(10); pv.setTextColor(mColorOnSurfaceVariant); pv.setMaxLines(1);
        info.addView(pv);
        item.addView(info,new LinearLayout.LayoutParams(-1,-2,1.0f));
        var tm=new TextView(ctx);
        tm.setText(formatSessionTimeShort(s.updatedAt));
        tm.setTextSize(10); tm.setTextColor(mColorOnSurfaceVariant);
        item.addView(tm);

        // Right-click context menu for deletion (ModernUI uses OnCreateContextMenuListener)
        item.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(Menu.NONE, fi, Menu.NONE, tr("chat.delete_session"))
                    .setOnMenuItemClickListener(itemClick -> {
                        removeSession(fi);
                        return true;
                    });
        });

        return item;
    }

    private void rebuildSessionList() {
        if (mSidebarContent == null) return;
        String tab = tr(mActiveTab == 0 ? "nav.chat" : "nav.sessions");
        updateSidebarContent(tab);
    }

    private void removeSession(int idx) {
        if (idx < 0 || idx >= mSessions.size()) return;
        var session = mSessions.get(idx);
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("mineclawd sessions remove " + session.id);
        }
        mSessions.remove(idx);
        mMessages.clear();
        rebuildSessionList();
        if (mActiveTab == 0) updateMainContent(tr("nav.chat"));
        LogManager.info(tr("log.session_deleted"));
    }

    private static final DateTimeFormatter SESSION_TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private String formatSessionTime(long epochMillis) {
        if (epochMillis <= 0) return "";
        return SESSION_TIME_FMT.format(Instant.ofEpochMilli(epochMillis));
    }

    private String formatSessionTimeShort(long epochMillis) {
        if (epochMillis <= 0) return "";
        long diff = System.currentTimeMillis() - epochMillis;
        if (diff < 60000) return tr("session.just_now");
        if (diff < 3600000) return (diff / 60000) + "m";
        if (diff < 86400000) return (diff / 3600000) + "h";
        return (diff / 86400000) + "d";
    }

    private String tr(String key){return MineClawdI18n.tr(key);}

    private void updateMainContent(String tab){
        mMainContent.removeAllViews();
        switch(mActiveTab){
            case 0->buildChatPage();
            case 5->buildSettingsPage();
            default->{var l=new TextView(mMainContent.getContext()); l.setText(tab+" - Phase 2 content"); l.setTextSize(13); l.setTextColor(mColorOnSurfaceVariant); l.setGravity(Gravity.CENTER); mMainContent.addView(l);}
        }
    }

    // ===== CHAT PAGE =====

    private void buildChatPage() {
        var ctx = mMainContent.getContext();
        var chatRoot = new FrameLayout(ctx);
        chatRoot.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        mChatScroll = new ScrollView(ctx);
        mChatScroll.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mChatScroll.setFocusable(false);
        mChatScroll.setFocusableInTouchMode(false);
        mChatContainer = new LinearLayout(ctx);
        mChatContainer.setOrientation(LinearLayout.VERTICAL);
        mChatContainer.setPadding(mWindow.dp(14), mWindow.dp(10), mWindow.dp(14), mWindow.dp(10));
        for (var msg : mMessages) {
            addMessageBubble(msg.role, msg.text);
        }
        // Re-attach typing indicator if we're in the middle of streaming
        if (mCurrentTyping != null) {
            // Make sure the typing indicator has the current text
            if (mCurrentTyping instanceof TextView tv) {
                tv.setText(mCurrentTypingText);
            }
            mChatContainer.addView(mCurrentTyping);
        } else if (mGenerating) {
            // If generating but no typing indicator, create one
            ensureTypingIndicator();
        }
        mChatScroll.addView(mChatContainer);
        chatRoot.addView(mChatScroll);

        mInputArea = new LinearLayout(ctx);
        mInputArea.setOrientation(LinearLayout.VERTICAL);
        var iap = new FrameLayout.LayoutParams(-1, -2);
        iap.gravity = Gravity.BOTTOM;
        mInputArea.setLayoutParams(iap);
        mInputArea.setFocusable(false);
        mInputArea.setFocusableInTouchMode(false);
        var iabg = new ShapeDrawable();
        iabg.setShape(ShapeDrawable.RECTANGLE);
        iabg.setColor(mColorSurface);
        mInputArea.setBackground(iabg);

        mChatInput = new EditText(ctx, null, R.attr.editTextOutlinedStyle);
        mChatInput.setHint(tr("chat.input_hint"));
        mChatInput.setTextSize(12);
        mChatInput.setTextColor(mColorOnSurface);
        mChatInput.setHintTextColor(mColorOnSurfaceVariant);
        mChatInput.setPadding(mWindow.dp(10), mWindow.dp(8), mWindow.dp(10), mWindow.dp(8));
        mChatInput.setMinLines(1);
        mChatInput.setMaxLines(6);
        mChatInput.setHorizontallyScrolling(false);
        mChatInput.setVerticalScrollBarEnabled(true);
        mChatInput.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mChatInput.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        mChatInput.setFocusable(true);
        mChatInput.setFocusableInTouchMode(true);
        mInputArea.addView(mChatInput);

        mBtnRow = new LinearLayout(ctx);
        mBtnRow.setOrientation(LinearLayout.HORIZONTAL);
        mBtnRow.setGravity(Gravity.CENTER_VERTICAL);
        mBtnRow.setPadding(mWindow.dp(6), mWindow.dp(4), mWindow.dp(6), mWindow.dp(4));
        mBtnRow.setMinimumHeight(mWindow.dp(32));
        mBtnRow.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        mBtnRow.setFocusable(false);
        mBtnRow.setFocusableInTouchMode(false);

        var attachBtn = new Button(ctx);
        attachBtn.setText("\uD83D\uDCCE");
        attachBtn.setTextSize(14);
        attachBtn.setTextColor(mColorOnSurfaceVariant);
        attachBtn.setFocusable(false);
        attachBtn.setBackground(makeRoundedBg(mColorSurfaceContainerHighest, 4));
        attachBtn.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(30), mWindow.dp(26)));
        attachBtn.setOnClickListener(v -> AttachmentHelper.openFileDialog(() -> updateAttachmentBadge(ctx)));
        mBtnRow.addView(attachBtn);

        var attachmentBadge = new TextView(ctx);
        attachmentBadge.setTextSize(8);
        attachmentBadge.setTextColor(mColorPrimary);
        attachmentBadge.setVisibility(View.GONE);
        attachmentBadge.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        mBtnRow.addView(attachmentBadge);

        AttachmentHelper.setCallback(files -> {
            requireView().post(() -> {
                int count = files.size();
                if (count > 0) {
                    attachmentBadge.setText("+" + count);
                    attachmentBadge.setVisibility(View.VISIBLE);
                    mChatInput.setHint(count + " " + tr("chat.attached"));
                } else {
                    attachmentBadge.setVisibility(View.GONE);
                    mChatInput.setHint(tr("chat.input_hint"));
                }
            });
        });

        mBtnRow.addView(new View(ctx), new LinearLayout.LayoutParams(-1, -1, 1.0f));

        mSendBtn = new Button(ctx);
        mSendBtn.setText(tr("chat.send"));
        mSendBtn.setTextSize(12);
        mSendBtn.setTextColor(0xFFFFFFFF);
        mSendBtn.setFocusable(false);
        updateSendButtonBg();
        mSendBtn.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(64), mWindow.dp(26)));
        mSendBtn.setOnClickListener(v -> {
            if (mGenerating) { requestStopGeneration(); }
            else if (!mFailedRetryToken.isBlank()) { requestRetry(); }
            else { sendChatMessage(); }
        });
        mBtnRow.addView(mSendBtn);
        mInputArea.addView(mBtnRow);
        chatRoot.addView(mInputArea);
        mMainContent.addView(chatRoot);

        mInputArea.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            int h = b - t;
            var slp = (FrameLayout.LayoutParams) mChatScroll.getLayoutParams();
            if (slp.bottomMargin != h) {
                slp.bottomMargin = h;
                mChatScroll.setLayoutParams(slp);
            }
        });

        mChatInput.addTextChangedListener(new TextWatcher() {
            int prevLines = mChatInput.getLineCount();
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                int curLines = mChatInput.getLineCount();
                if (curLines != prevLines) { prevLines = curLines; mChatInput.requestLayout(); }
            }
        });

        chatRoot.setFocusable(false); chatRoot.setFocusableInTouchMode(false);
        mChatScroll.setFocusable(false); mChatScroll.setFocusableInTouchMode(false);
        mChatContainer.setFocusable(false); mChatContainer.setFocusableInTouchMode(false);
        mInputArea.setFocusable(false); mInputArea.setFocusableInTouchMode(false);
        mBtnRow.setFocusable(false); mBtnRow.setFocusableInTouchMode(false);
        attachBtn.setFocusable(false); attachBtn.setFocusableInTouchMode(false);
        mSendBtn.setFocusable(false); mSendBtn.setFocusableInTouchMode(false);

        mInputArea.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) { mChatInput.requestFocus(); }
            return false;
        });

        focusEditTextWithRetry(mChatInput, 8);
        CharInputBridge.activate(mChatInput, mChatInput.getText());
        startFocusKeepalive();
        updateSendButton();
    }

    private void updateAttachmentBadge(Context ctx) {
        int count = AttachmentHelper.getPendingFiles().size();
        if (mBtnRow != null && mBtnRow.getChildCount() > 1) {
            View badge = mBtnRow.getChildAt(1);
            if (badge instanceof TextView tv) {
                if (count > 0) { tv.setText("+" + count); tv.setVisibility(View.VISIBLE); }
                else { tv.setVisibility(View.GONE); }
            }
        }
    }

    private void updateSendButton() {
        if (mSendBtn == null) return;
        if (mGenerating) {
            mSendBtn.setText("\u25A0 " + tr("chat.stop"));
        } else if (!mFailedRetryToken.isBlank()) {
            mSendBtn.setText("\u21BA " + tr("chat.retry"));
        } else {
            mSendBtn.setText(tr("chat.send"));
        }
        mSendBtn.setTextColor(0xFFFFFFFF);
        updateSendButtonBg();
    }

    private void updateSendButtonBg() {
        if (mSendBtn == null) return;
        int bgColor;
        if (mGenerating) bgColor = mColorError;
        else if (!mFailedRetryToken.isBlank()) bgColor = 0xFFFF8C00;
        else bgColor = mColorPrimary;
        var sbg = new ShapeDrawable();
        sbg.setShape(ShapeDrawable.RECTANGLE);
        sbg.setCornerRadius(mWindow.dp(4));
        sbg.setColor(bgColor);
        mSendBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x60FFFFFF), sbg, null));
    }

    private void startFocusKeepalive() {
        mChatInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mChatInput != null && getView() != null) {
                    if (!mChatInput.isFocused()) { mChatInput.requestFocus(); }
                    mChatInput.postDelayed(this, 200);
                }
            }
        }, 200);
    }

    private void focusEditTextWithRetry(EditText et, int maxRetries) {
        et.post(() -> {
            boolean focused = et.requestFocus();
            if (!focused && maxRetries > 0) {
                et.postDelayed(() -> focusEditTextWithRetry(et, maxRetries - 1), 50);
            }
        });
    }

    private void scrollChat() {
        if (mChatScroll != null) {
            // Direct scroll without post() to ensure real-time following
            mChatScroll.fullScroll(View.FOCUS_DOWN);
        }
    }

    private void addMessageBubble(String role, String text) {
        if (mChatContainer == null && mActiveTab == 0 && mMainContent != null) {
            updateMainContent(tr("nav.chat"));
        }
        if (mChatContainer==null) return;
        var ctx=mChatContainer.getContext();
        var bubble=new LinearLayout(ctx); bubble.setOrientation(LinearLayout.HORIZONTAL);
        bubble.setPadding(0,mWindow.dp(4),0,mWindow.dp(4)); bubble.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        if ("assistant".equals(role)) {
            var icon=new TextView(ctx); icon.setText("\uD83E\uDD16"); icon.setTextSize(14); icon.setPadding(0,0,mWindow.dp(8),0);
            bubble.addView(icon);
            var content=new TextView(ctx); content.setText(text); content.setTextSize(12); content.setTextColor(mColorOnSurface);
            content.setLineSpacing(mWindow.dp(2),1.0f);
            content.setTextIsSelectable(true);
            content.setSpannableFactory(Spannable.NO_COPY_FACTORY);
            if (mMarkflow!=null) mMarkflow.setMarkdown(content,text);
            var bg=new ShapeDrawable(); bg.setShape(ShapeDrawable.RECTANGLE); bg.setCornerRadius(mWindow.dp(6)); bg.setColor(mColorSurfaceContainerHigh);
            content.setBackground(bg); content.setPadding(mWindow.dp(10),mWindow.dp(8),mWindow.dp(10),mWindow.dp(8));
            content.setMaxWidth((int)(mWindow.getWidth()*0.88f));
            bubble.addView(content,new LinearLayout.LayoutParams(-1,-2,1.0f));
        } else {
            bubble.addView(new View(ctx),new LinearLayout.LayoutParams(-1,-2,1.0f));
            var content=new TextView(ctx); content.setText(text); content.setTextSize(12); content.setTextColor(mColorOnSurface);
            content.setTextIsSelectable(true);
            content.setSpannableFactory(Spannable.NO_COPY_FACTORY);
            var bg=new ShapeDrawable(); bg.setShape(ShapeDrawable.RECTANGLE); bg.setCornerRadius(mWindow.dp(6)); bg.setColor(mColorSurfaceContainerLow);
            content.setBackground(bg); content.setPadding(mWindow.dp(10),mWindow.dp(8),mWindow.dp(10),mWindow.dp(8));
            content.setMaxWidth((int)(mWindow.getWidth()*0.88f)); bubble.addView(content);
        }
        mChatContainer.addView(bubble); scrollChat();
    }

    private void sendChatMessage() {
        if (mChatInput==null) return;
        String text=mChatInput.getText().toString().trim();
        if (text.isEmpty()) return;
        mChatInput.setText("");
        mMessages.add(new ChatMessage("user",text));
        addMessageBubble("user",text);
        mCurrentTypingText="";
        mToolStatusContainer = null;
        mGenerating = true;
        updateSendButton();
        scrollChat();
        var mc=MinecraftClient.getInstance();
        if (mc.player!=null&&mc.player.networkHandler!=null) {
            var files = AttachmentHelper.getPendingFiles();
            if (files.isEmpty()) {
                mc.player.networkHandler.sendChatCommand("mclawd " + text);
            } else {
                boolean ok = com.mineclawd.MineClawdClientNetworking.sendPromptWithAttachments(
                        mc, text, new java.util.ArrayList<>(files));
                if (!ok) mc.player.networkHandler.sendChatCommand("mclawd " + text);
                AttachmentHelper.clearPendingFiles();
                if (mBtnRow != null && mBtnRow.getChildCount() > 1) {
                    View badge = mBtnRow.getChildAt(1);
                    if (badge instanceof TextView tv) tv.setVisibility(View.GONE);
                }
            }
            if (mActiveSessionId.isBlank()) {
                mc.player.networkHandler.sendChatCommand("mineclawd sessions new");
            }
        }
    }

    private void requestStopGeneration() {
        mGenerating = false;
        mFailedRetryToken = "";
        updateSendButton();
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("mineclawd stop");
        }
        if (mCurrentTyping != null) {
            var parent = (ViewGroup) mCurrentTyping.getParent();
            if (parent != null) parent.removeView(mCurrentTyping);
            mCurrentTyping = null;
            mCurrentTypingText = "";
        }
        mPendingToolCalls.clear();
        LogManager.info(tr("chat.stopped"));
    }

    private void requestRetry() {
        String token = mFailedRetryToken;
        if (token.isBlank()) return;
        mFailedRetryToken = "";
        mGenerating = true;
        updateSendButton();
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("mineclawd retry " + token);
        }
        LogManager.info(tr("chat.retrying"));
    }

    private void switchSession(int idx) {
        if (idx < 0 || idx >= mSessions.size()) return;
        var session = mSessions.get(idx);
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("mineclawd sessions resume " + session.id);
        }
        LogManager.info(tr("log.session_switched").replace("%s", session.title));
    }

    private void newSession() {
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand("mineclawd sessions new");
        }
        LogManager.info(tr("sidebar.new_session"));
    }

    // ===== SETTINGS =====
    private void buildSettingsPage() {
        var ctx=mMainContent.getContext();
        var scroll=new ScrollView(ctx); scroll.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
        var container=new LinearLayout(ctx); container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(mWindow.dp(20),mWindow.dp(16),mWindow.dp(20),mWindow.dp(16));

        var title=new TextView(ctx); title.setText(tr("settings.title")); title.setTextSize(20);
        title.setTextColor(mColorPrimary); container.addView(title);

        var cfg=MineClawdConfig.get();
        mSettingsUseVertex=cfg.provider==MineClawdConfig.LlmProvider.VERTEX_AI;

        addSectionHeader(container, tr("settings.llm_provider"));
        var providerRow = makeSettingsRow(ctx, tr("settings.llm_provider"));
        var providerLabels = new String[]{"OpenAI", "Vertex AI"};
        var providerAdapter = new ArrayAdapter<>(ctx, List.of(providerLabels));
        mSettingsProviderSpinner = new Spinner(ctx);
        mSettingsProviderSpinner.setAdapter(providerAdapter);
        mSettingsProviderSpinner.setSelection(mSettingsUseVertex ? 1 : 0);
        mSettingsProviderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSettingsUseVertex = position == 1;
                rebuildSettingsBorders(container);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        providerRow.addView(mSettingsProviderSpinner);
        container.addView(providerRow);
        container.addView(new View(ctx), new LinearLayout.LayoutParams(-1, mWindow.dp(4)));

        var sectionsContainer = new LinearLayout(ctx);
        sectionsContainer.setOrientation(LinearLayout.VERTICAL);
        sectionsContainer.setId(0x7E57);
        container.addView(sectionsContainer);
        buildProviderSections(sectionsContainer, ctx);

        addSectionHeader(container, tr("settings.interface_section"));
        mSettingsGuiToggle=addSettingsSwitch(container, tr("settings.enable_gui"), cfg.enableGui);
        mSettingsDebugToggle=addSettingsSwitch(container, tr("settings.debug_mode"), cfg.debugMode);
        mSettingsToolLimitToggle=addSettingsSwitch(container, tr("settings.limit_tool_calls"), cfg.limitToolCalls);

        var buttonRow=new LinearLayout(ctx); buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0,mWindow.dp(16),0,0);
        var saveBtn=new Button(ctx); saveBtn.setText(tr("settings.save")); saveBtn.setTextSize(13); saveBtn.setTextColor(0xFFFFFFFF);
        var sbg=new ShapeDrawable(); sbg.setShape(ShapeDrawable.RECTANGLE); sbg.setCornerRadius(mWindow.dp(5)); sbg.setColor(mColorPrimary);
        saveBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x60FFFFFF),sbg,null));
        saveBtn.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(80),mWindow.dp(32)));
        ((LinearLayout.LayoutParams)saveBtn.getLayoutParams()).setMargins(0,0,mWindow.dp(10),0);
        saveBtn.setOnClickListener(v->saveSettings()); buttonRow.addView(saveBtn);
        var cancelBtn=new Button(ctx); cancelBtn.setText(tr("settings.cancel")); cancelBtn.setTextSize(13); cancelBtn.setTextColor(mColorOnSurfaceVariant);
        var cbg=new ShapeDrawable(); cbg.setShape(ShapeDrawable.RECTANGLE); cbg.setCornerRadius(mWindow.dp(5)); cbg.setColor(mColorSurfaceContainerHigh);
        cancelBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x30FFFFFF),cbg,null));
        cancelBtn.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(80),mWindow.dp(32)));
        cancelBtn.setOnClickListener(v->{
            mSettingsEndpoint.setText(cfg.endpoint); mSettingsModel.setText(cfg.model); mSettingsApiKey.setText(cfg.apiKey);
            mSettingsVertexEndpoint.setText(cfg.vertexEndpoint); mSettingsVertexModel.setText(cfg.vertexModel); mSettingsVertexApiKey.setText(cfg.vertexApiKey);
            mSettingsUseVertex=cfg.provider==MineClawdConfig.LlmProvider.VERTEX_AI;
            mSettingsProviderSpinner.setSelection(mSettingsUseVertex?1:0);
            mSettingsGuiToggle.setChecked(cfg.enableGui); mSettingsDebugToggle.setChecked(cfg.debugMode); mSettingsToolLimitToggle.setChecked(cfg.limitToolCalls);
        }); buttonRow.addView(cancelBtn);
        container.addView(buttonRow);
        scroll.addView(container);
        mMainContent.addView(scroll);
    }

    private void buildProviderSections(LinearLayout container, Context ctx) {
        container.removeAllViews();
        boolean openaiActive = !mSettingsUseVertex;
        var openaiSection = createBorderedSection(ctx, "OpenAI", openaiActive);
        mSettingsEndpoint=addSettingsInput(openaiSection, tr("settings.endpoint"), MineClawdConfig.get().endpoint);
        mSettingsModel=addSettingsInput(openaiSection, tr("settings.model"), MineClawdConfig.get().model);
        mSettingsApiKey=addSettingsInput(openaiSection, tr("settings.api_key"), MineClawdConfig.get().apiKey);
        container.addView(openaiSection);
        container.addView(new View(ctx), new LinearLayout.LayoutParams(-1, mWindow.dp(4)));
        boolean vertexActive = mSettingsUseVertex;
        var vertexSection = createBorderedSection(ctx, tr("settings.vertex_ai"), vertexActive);
        mSettingsVertexEndpoint=addSettingsInput(vertexSection, tr("settings.endpoint"), MineClawdConfig.get().vertexEndpoint);
        mSettingsVertexModel=addSettingsInput(vertexSection, tr("settings.model"), MineClawdConfig.get().vertexModel);
        mSettingsVertexApiKey=addSettingsInput(vertexSection, tr("settings.api_key"), MineClawdConfig.get().vertexApiKey);
        container.addView(vertexSection);
    }

    private void rebuildSettingsBorders(LinearLayout outerContainer) {
        var sectionsContainer = outerContainer.<LinearLayout>findViewById(0x7E57);
        if (sectionsContainer != null) buildProviderSections(sectionsContainer, outerContainer.getContext());
    }

    private LinearLayout createBorderedSection(Context ctx, String title, boolean active) {
        var section = new LinearLayout(ctx);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(mWindow.dp(12), mWindow.dp(6), mWindow.dp(12), mWindow.dp(6));
        int borderColor = active ? mColorPrimary : mColorOutlineVariant;
        int fillColor = active ? (mColorPrimary & 0xFFFFFF) | 0x08000000 : mColorSurface;
        var sectionBg = new ShapeDrawable();
        sectionBg.setShape(ShapeDrawable.RECTANGLE);
        sectionBg.setCornerRadius(mWindow.dp(8));
        sectionBg.setColor(fillColor);
        sectionBg.setStroke(mWindow.dp(active ? 2 : 1), borderColor);
        section.setBackground(sectionBg);
        var hdr = new LinearLayout(ctx);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setGravity(Gravity.CENTER_VERTICAL);
        hdr.setPadding(0, 0, 0, mWindow.dp(4));
        var dot = new TextView(ctx);
        dot.setText(active ? "\u25CF" : "\u25CB");
        dot.setTextSize(12);
        dot.setTextColor(borderColor);
        hdr.addView(dot, new LinearLayout.LayoutParams(-2, -2));
        var lbl = new TextView(ctx);
        lbl.setText(title);
        lbl.setTextSize(14);
        lbl.setTextColor(active ? mColorPrimary : mColorOnSurfaceVariant);
        lbl.setPadding(mWindow.dp(6), 0, 0, 0);
        hdr.addView(lbl, new LinearLayout.LayoutParams(-1, -2));
        if (active) {
            var badge = new TextView(ctx);
            badge.setText(tr("settings.active_provider"));
            badge.setTextSize(9);
            badge.setTextColor(mColorPrimary);
            badge.setPadding(mWindow.dp(8), mWindow.dp(2), mWindow.dp(8), mWindow.dp(2));
            var badgeBg = new ShapeDrawable();
            badgeBg.setShape(ShapeDrawable.RECTANGLE);
            badgeBg.setCornerRadius(mWindow.dp(6));
            badgeBg.setColor((mColorPrimary & 0xFFFFFF) | 0x18000000);
            badge.setBackground(badgeBg);
            hdr.addView(badge, new LinearLayout.LayoutParams(-2, -2));
        }
        section.addView(hdr);
        return section;
    }

    private void addSectionHeader(LinearLayout container, String title) {
        var hdr=new TextView(container.getContext()); hdr.setText(title); hdr.setTextSize(11);
        hdr.setTextColor(mColorOnSurfaceVariant); hdr.setPadding(0,mWindow.dp(16),0,mWindow.dp(4));
        container.addView(hdr);
    }

    private LinearLayout makeSettingsRow(Context ctx, String label) {
        var row=new LinearLayout(ctx); row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,mWindow.dp(3),0,mWindow.dp(3));
        var lbl=new TextView(ctx); lbl.setText(label); lbl.setTextSize(12); lbl.setTextColor(mColorOnSurfaceVariant);
        lbl.setLayoutParams(new LinearLayout.LayoutParams(mWindow.dp(90),-2));
        row.addView(lbl); return row;
    }

    private EditText addSettingsInput(ViewGroup parent, String label, String current) {
        var ctx=parent.getContext();
        var row=makeSettingsRow(ctx, label);
        var et=new EditText(ctx); et.setText(current!=null?current:""); et.setTextSize(11);
        et.setTextColor(mColorOnSurface); et.setHintTextColor(mColorOnSurfaceVariant);
        et.setSingleLine(); et.setPadding(mWindow.dp(6),mWindow.dp(4),mWindow.dp(6),mWindow.dp(4));
        var bg=new ShapeDrawable(); bg.setShape(ShapeDrawable.RECTANGLE); bg.setCornerRadius(mWindow.dp(4)); bg.setColor(mColorSurfaceContainerHigh);
        et.setBackground(bg);
        et.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,1.0f));
        row.addView(et); parent.addView(row); return et;
    }

    private Switch addSettingsSwitch(ViewGroup parent, String label, boolean current) {
        var ctx=parent.getContext();
        var row=makeSettingsRow(ctx, label);
        var sw=new Switch(ctx); sw.setChecked(current);
        sw.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        row.addView(sw); parent.addView(row); return sw;
    }

    private void saveSettings() {
        var cfg=MineClawdConfig.get();
        cfg.provider=mSettingsUseVertex?MineClawdConfig.LlmProvider.VERTEX_AI:MineClawdConfig.LlmProvider.OPENAI;
        cfg.endpoint=nonNull(mSettingsEndpoint.getText()); cfg.model=nonNull(mSettingsModel.getText()); cfg.apiKey=nonNull(mSettingsApiKey.getText());
        cfg.vertexEndpoint=nonNull(mSettingsVertexEndpoint.getText()); cfg.vertexModel=nonNull(mSettingsVertexModel.getText()); cfg.vertexApiKey=nonNull(mSettingsVertexApiKey.getText());
        cfg.enableGui=mSettingsGuiToggle.isChecked(); cfg.debugMode=mSettingsDebugToggle.isChecked(); cfg.limitToolCalls=mSettingsToolLimitToggle.isChecked();
        MineClawdConfig.HANDLER.save();
        LogManager.info(tr("settings.saved"));
        HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm);
    }

    private static String nonNull(CharSequence s) { return s==null?"":s.toString(); }

    private void toggleMaximize() {
        mMaximized=!mMaximized;
        if (mMaximized){mWindowRestoreX=normW(mXNorm);mWindowRestoreY=normH(mYNorm);mWindowRestoreW=normW(mWNorm);mWindowRestoreH=normH(mHNorm);}
        else {mXNorm=wToNorm(mWindowRestoreX);mYNorm=hToNorm(mWindowRestoreY);mWNorm=wToNorm(mWindowRestoreW);mHNorm=hToNorm(mWindowRestoreH);}
        applyWindowLayout(); layoutResizeHandles();
    }

    private void minimizeToOrb() {
        HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm);
        HudOrbState.saveSidebarState(mSidebarW, mSidebarCollapsed);
        MinecraftClient.getInstance().setScreen(null);
        HudOrbState.hideHud(); HudOrbState.show();
    }

    private int normW(float n){return (int)(n*getScreenW());}
    private int normH(float n){return (int)(n*getScreenH());}
    private float wToNorm(int p){return (float)p/(float)getScreenW();}
    private float hToNorm(int p){return (float)p/(float)getScreenH();}
    private int getScreenW(){return requireContext().getResources().getDisplayMetrics().widthPixels;}
    private int getScreenH(){return requireContext().getResources().getDisplayMetrics().heightPixels;}

    private void clampTitleBarVisible() {
        int screenW=getScreenW(), screenH=getScreenH();
        int wx=normW(mXNorm), wy=normH(mYNorm), ww=normW(mWNorm), wh=normH(mHNorm);
        int titleBarBottom=wy+mTitleH;
        if (titleBarBottom<0) wy=-wy; else if (wy>screenH-mTitleH) wy=screenH-mTitleH;
        if (wx+ww<50) wx=50-wx; else if (wx>screenW-50) wx=screenW-50;
        mXNorm=wToNorm(wx); mYNorm=hToNorm(wy);
    }
}
