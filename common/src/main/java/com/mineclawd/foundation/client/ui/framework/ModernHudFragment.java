package com.mineclawd.foundation.client.ui.framework;

import com.google.gson.JsonParser;
import com.mineclawd.MineClawd;
import com.mineclawd.foundation.client.ChatStreamBridge;
import com.mineclawd.foundation.client.CharInputBridge;
import com.mineclawd.foundation.client.SessionPayloadBridge;
import com.mineclawd.foundation.client.ui.i18n.MineClawdI18n;
import com.mineclawd.foundation.client.ui.log.LogView;
import com.mineclawd.foundation.client.ui.log.LogManager;
import com.mineclawd.foundation.client.ui.framework.MessageResponseHandler;
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
import icyllis.modernui.text.method.PasswordTransformationMethod;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.*;
import icyllis.modernui.widget.*;
import net.minecraft.client.MinecraftClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private static final int DRAG_THRESHOLD_DP = 8;
    private static final int RESIZE_NONE = 0, RESIZE_L=1, RESIZE_T=2, RESIZE_R=4, RESIZE_B=8;

    private FrameLayout mRoot, mWindow;
    private FrameLayout mSidebar;
    private View mLogView;
    private LinearLayout mSidebarContent;
    private View[] mResizeHandles;
    private boolean mMaximized, mSidebarCollapsed;
    private boolean mResizing, mDragging, mDragReady, mSidebarResizing;
    private float mResizeStartMouseX, mResizeStartMouseY, mResizeStartWNorm, mResizeStartHNorm, mResizeStartXNorm, mResizeStartYNorm;
    private float mDragStartX, mDragStartY, mLastTouchX, mLastTouchY;
    private int mResizingType, mSidebarResizeStartX, mSidebarResizeStartW;
    private float mXNorm, mYNorm, mWNorm, mHNorm;
    private int mTitleH, mNavW, mSidebarW, mDragThresh;
    private View mSidebarResizer;

    private FrameLayout mMainContent;
    private EditText mChatInput;
    private LinearLayout mInputArea;
    private final List<String> mPendingToolNames = new ArrayList<>();
    private List<TextView> mNavItems = new ArrayList<>();

    private String mActiveSessionId = "";
    private String mFailedRetryToken = "";
    private boolean mGenerating;
    private Button mSendBtn;
    private LinearLayout mBtnRow;
    private MessageBubble mCurrentStreamingMessageBubble = null;

    private static class SessionItemData {
        String id; String title; String token; long updatedAt; boolean active;
        SessionItemData(String i, String t, String tk, long u, boolean a) { id=i; title=t; token=tk; updatedAt=u; active=a; }
    }
    private final List<SessionItemData> mSessions = new ArrayList<>();
    private int mDragSessionIdx = -1;
    private float mDragSessionStartY;
    private boolean mFollowTail = true;

    private final java.util.Queue<Runnable> mPendingUiUpdates = new ConcurrentLinkedQueue<>();
    private boolean mFlushScheduled;

    private EditText mSettingsEndpoint, mSettingsModel, mSettingsApiKey;
    private EditText mSettingsVertexEndpoint, mSettingsVertexModel, mSettingsVertexApiKey;
    private Spinner mSettingsProviderSpinner;
    private boolean mSettingsUseVertex;
    private Switch mSettingsGuiToggle, mSettingsDebugToggle, mSettingsToolLimitToggle;

    private View mAgentCardView;
    private Markflow mMarkflow;
    private MessageResponseHandler mMessageHandler;
    private static class ChatMessage {
        String roleId;
        String content;
        ChatMessage(String roleId, String content) { this.roleId = roleId; this.content = content; }
    }
    private final List<ChatMessage> mMessages = new ArrayList<>();
    private final List<MessageBubble> mMessageBubbles = new ArrayList<>();
    private ScrollView mChatScroll;
    private LinearLayout mChatContainer;

    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);
        MineClawd.LOGGER.info("[GUI] onCreate called, registering listener");
        EmojiEnabler.ensure();
        ChatStreamBridge.setListener(this::onStreamEvent);
        SessionPayloadBridge.setListener(this::onSessionPayload);
    }

    @Override
    public void onStart() {
        super.onStart();
        MineClawd.LOGGER.info("[GUI] onStart called, ensuring listener is registered");
        ChatStreamBridge.setListener(this::onStreamEvent);
    }

    private void scheduleFlush() {
        if (mFlushScheduled) return;
        mFlushScheduled = true;
        View v = getView();
        if (v != null) v.post(this::flushPendingUpdates);
    }

    private void flushPendingUpdates() {
        mFlushScheduled = false;
        if (getView() == null) return;
        Runnable task;
        while ((task = mPendingUiUpdates.poll()) != null) task.run();
    }

    private void processQueuedEvents() { flushPendingUpdates(); }

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
        HudOrbState.saveSidebarState(mSidebarW, mSidebarCollapsed);
        super.onDestroy();
        MineClawd.LOGGER.info("[GUI] onDestroy called");
        CharInputBridge.deactivate();
        IMBlockerCompat.onScreenClosed();
    }

    private void requestSessionsFromServer() {
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null)
            mc.player.networkHandler.sendChatCommand("mineclawd sessions");
    }

    private void onSessionPayload(SessionOverlayPayload payload) {
        View v = getView();
        if (v == null) {
            mPendingUiUpdates.add(() -> onSessionPayload(payload));
            scheduleFlush();
            return;
        }
        v.post(() -> {
            if (getView() == null) return;
            mMessages.clear();
            mMessageBubbles.clear();
            mActiveSessionId = payload.activeSessionId();
            mSessions.clear();
            for (var s : payload.sessions())
                mSessions.add(new SessionItemData(s.id(), s.title(), s.token(), s.updatedAtEpochMillis(), s.active()));
            for (var h : payload.history())
                mMessages.add(new ChatMessage(h.assistant() ? "assistant" : "user", h.content()));
            updateSidebarContent(tr("nav.chat"));
            if (mSidebarContent != null) mSidebarContent.invalidate();
            if (!mGenerating && mActiveTab == 0) updateMainContent(tr("nav.chat"));
        });
    }

    // ===== STREAM EVENTS =====

    private String extractRetryToken(String payload) {
        if (payload == null || payload.isBlank()) return "";
        String[] lines = payload.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase(java.util.Locale.ROOT).contains("retry token:")) {
                int idx = trimmed.indexOf(':');
                if (idx >= 0 && idx + 1 < trimmed.length())
                    return trimmed.substring(idx + 1).trim().replaceAll("[`\"']", "").split("\\s")[0];
            }
            String prefix = "/mineclawd retry ";
            int ci = trimmed.toLowerCase(java.util.Locale.ROOT).indexOf(prefix);
            if (ci >= 0)
                return trimmed.substring(ci + prefix.length()).trim().replaceAll("[`\"']", "").split("\\s")[0];
        }
        return "";
    }

    private void onStreamEvent(ChatStreamBridge.StreamEvent event) {
        mPendingUiUpdates.add(() -> handleStreamEventSafe(event));
        scheduleFlush();
    }

    private void handleStreamEventSafe(ChatStreamBridge.StreamEvent event) {
        if (getView() == null) return;
        switch (event.type()) {
            case START -> handleStreamStart(event.payload());
            case DELTA -> handleStreamDelta(event.payload());
            case TOOL_STATUS -> handleToolStatus(event.payload());
            case TOOL_STATUS_CLEAR -> handleToolStatusClear(event.payload());
            case DONE -> handleStreamDone();
            case ERROR -> handleStreamError(event.payload());
        }
    }

    private void handleStreamStart(String payload) {
        mPendingToolNames.clear();
        mGenerating = true;
        mFailedRetryToken = "";
        mFollowTail = true;
        ensureChatPageExists();
        createStreamingMessage();
        if (mCurrentStreamingMessageBubble != null)
            mCurrentStreamingMessageBubble.clearToolCards();
        updateSendButton();
    }

    private void handleStreamDelta(String payload) {
        if (mCurrentStreamingMessageBubble == null && mChatContainer == null) return;
        if (mCurrentStreamingMessageBubble == null) createStreamingMessage();
        if (mCurrentStreamingMessageBubble != null) {
            mCurrentStreamingMessageBubble.appendText(payload);
            scrollChat();
        }
    }

    private void handleStreamDone() {
        if (mCurrentStreamingMessageBubble != null) {
            String content = mCurrentStreamingMessageBubble.getContent();
            if (!content.isBlank())
                mMessages.add(new ChatMessage("assistant", content));
        }
        cleanupStreamingState();
        mGenerating = false;
        updateSendButton();
        scrollChat();
    }

    private void handleStreamError(String payload) {
        String retryToken = extractRetryToken(payload);
        if (!retryToken.isBlank()) mFailedRetryToken = retryToken;
        String clean = payload != null ? payload.replaceAll("(?i)(retry token:|retry command:).*", "").trim() : "";
        if (clean != null && clean.contains("/mineclawd retry"))
            clean = clean.replaceAll("(?i)/mineclawd retry \\S+", "").trim();
        if (clean != null && !clean.isBlank()) {
            String errorText = "\u26A0 " + clean;
            mMessages.add(new ChatMessage("assistant", errorText));
            if (mCurrentStreamingMessageBubble != null) {
                mCurrentStreamingMessageBubble.setContent(errorText);
                mCurrentStreamingMessageBubble.getView().setAlpha(0.8f);
            } else if (mChatContainer != null) addMessageBubble("assistant", errorText);
            LogManager.error(clean);
            if (!retryToken.isBlank()) LogManager.info(tr("chat.retry_available"));
        } else { removeStreamingMessage(); }
        cleanupStreamingState();
        mGenerating = false;
        updateSendButton();
        scrollChat();
    }

    private void ensureChatPageExists() {
        if (mChatContainer == null && mActiveTab == 0 && mMainContent != null)
            updateMainContent(tr("nav.chat"));
    }

    private void createStreamingMessage() {
        ensureChatPageExists();
        if (mChatContainer != null) {
            mCurrentStreamingMessageBubble = addMessageBubble("assistant", "");
            scrollChat();
        }
    }

    private void removeStreamingMessage() {
        if (mCurrentStreamingMessageBubble != null) {
            mMessageBubbles.remove(mCurrentStreamingMessageBubble);
            var parent = (ViewGroup) mCurrentStreamingMessageBubble.getView().getParent();
            if (parent != null) parent.removeView(mCurrentStreamingMessageBubble.getView());
        }
    }

    private void cleanupStreamingState() {
        mCurrentStreamingMessageBubble = null;
        mPendingToolNames.clear();
    }

    // ===== TOOL CARD UI =====

    private void handleToolStatus(String payload) {
        if (payload == null || payload.isEmpty() || mCurrentStreamingMessageBubble == null) return;
        try {
            var json = JsonParser.parseString(payload).getAsJsonObject();
            String name = json.has("name") && json.get("name").isJsonPrimitive() ? json.get("name").getAsString() : "";
            if (name.isBlank()) return;
            mPendingToolNames.add(name);
            mCurrentStreamingMessageBubble.addToolCard(name);
            LogManager.tool(tr("chat.tool_running") + ": " + name);
            scrollChat();
        } catch (Exception ignored) {}
    }

    private void handleToolStatusClear(String payload) {
        if (payload == null || payload.isEmpty() || mCurrentStreamingMessageBubble == null) return;
        try {
            var json = JsonParser.parseString(payload).getAsJsonObject();
            String name = json.has("name") && json.get("name").isJsonPrimitive() ? json.get("name").getAsString() : "";
            if (name.isBlank()) return;
            String result = json.has("result") && json.get("result").isJsonPrimitive() ? json.get("result").getAsString() : "";
            String parsed = MessageBubble.extractResultText(result);
            mCurrentStreamingMessageBubble.updateToolCard(name, true, parsed);
            scrollChat();
        } catch (Exception ignored) {}
    }

    // ===== LIFECYCLE =====

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable DataSet savedInstanceState) {
        var ctx = requireContext();
        resolveThemeColors(ctx);
        var builder = Markflow.builder(requireContext());
        var monoFont = Typeface.getSystemFont("JetBrains Mono Medium");
        if (monoFont != Typeface.SANS_SERIF) {
            builder.usePlugin(new MarkflowPlugin() {
                @Override public void configureTheme(@NonNull MarkflowTheme.Builder themeBuilder) { themeBuilder.codeTypeface(monoFont); }
            });
        }
        mMarkflow = builder.build();
        mMessageHandler = MessageResponseHandler.getInstance();
        mMessageHandler.initialize(ctx, mWindow, mMarkflow);
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
        mWindow.setAlpha(0.85f);
        {
            TypedValue cardTv = new TypedValue();
            ctx.getTheme().resolveAttribute(R.ns, R.attr.colorSurfaceContainerLow, cardTv, true);
            ShapeDrawable cd = new ShapeDrawable();
            cd.setShape(ShapeDrawable.RECTANGLE);
            cd.setCornerRadius(mWindow.dp(10));
            cd.setColor(cardTv.data);
            mWindow.setBackground(cd);
            mWindow.setElevation(mWindow.dp(2));
        }
        mWindow.addView(createTitleBar());
        mWindow.addView(createBody(ctx));
        mRoot.addView(mWindow);
        createResizeHandles();
        mRoot.post(() -> { applyWindowLayout(); layoutResizeHandles();
            ObjectAnimator a1 = ObjectAnimator.ofFloat(mWindow, View.SCALE_X, 0.92f, 1.0f);
            a1.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED); a1.setDuration(400); a1.start();
            ObjectAnimator a2 = ObjectAnimator.ofFloat(mWindow, View.ALPHA, 0f, 1f);
            a2.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED); a2.setDuration(300); a2.start();
        });
        var lt = new LayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING); lt.setDuration(250);
        mMainContent.setLayoutTransition(lt);
        mRoot.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> { if(getView()!=null&&(l!=ol||t!=ot||r!=or||b!=ob)) reapplyWindow(); });
        mRoot.setOnTouchListener(this::onRootTouch);
        switchTab(0);
        flushPendingUpdates();
        return mRoot;
    }

    // ===== TITLE BAR =====

    private View createTitleBar() {
        var ctx = mWindow.getContext();
        var bar = new LinearLayout(ctx);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutParams(new FrameLayout.LayoutParams(-1, mTitleH));
        bar.setBackground(makeRoundedBg((mColorSurface & 0xFFFFFF) | 0x18000000, 0));
        bar.setPadding(mWindow.dp(10), 0, mWindow.dp(6), 0);
        var title = new TextView(ctx);
        title.setText("MineClawd");
        title.setTextSize(15);
        title.setTextColor(mColorPrimary);
        bar.addView(title, new LinearLayout.LayoutParams(-1, -2, 1.0f));
        recreateAgentCard(ctx, bar);
        var minBtn = new Button(ctx);
        minBtn.setText("\u2014");
        minBtn.setTextSize(14);
        minBtn.setTextColor(mColorOnSurfaceVariant);
        minBtn.setBackground(null);
        minBtn.setPadding(mWindow.dp(6), 0, mWindow.dp(6), 0);
        minBtn.setOnClickListener(v -> minimizeToOrb());
        bar.addView(minBtn);
        var clsBtn = new Button(ctx);
        clsBtn.setText("\u2715");
        clsBtn.setTextSize(14);
        clsBtn.setTextColor(mColorOnSurfaceVariant);
        clsBtn.setBackground(null);
        clsBtn.setPadding(mWindow.dp(6), 0, mWindow.dp(6), 0);
        clsBtn.setOnClickListener(v -> { HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm); minimizeToOrb(); });
        bar.addView(clsBtn);
        bar.setOnTouchListener((v,e)->{
            if (e.getAction()==MotionEvent.ACTION_DOWN&&!mMaximized){
                mDragStartX=(int)e.getRawX(); mDragStartY=(int)e.getRawY();
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

    private void recreateAgentCard(Context ctx, LinearLayout bar) {}

    // ===== BODY & SIDEBAR =====

    private View createBody(Context ctx) {
        var body = new FrameLayout(ctx);
        body.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        body.setPadding(0, mTitleH, 0, 0);
        var nav = new LinearLayout(ctx);
        nav.setOrientation(LinearLayout.VERTICAL);
        nav.setGravity(Gravity.CENTER_HORIZONTAL);
        nav.setPadding(0,mWindow.dp(6),0,0);
        nav.setLayoutParams(new FrameLayout.LayoutParams(mNavW,-1));
        nav.setBackground(makeRoundedBg(mColorSurface, 0));
        mNavItems = new ArrayList<>();
        String[] eicons={"\uD83D\uDCAC","\uD83D\uDCCB","\uD83D\uDCC1","\uD83D\uDD27","\uD83D\uDCE6","\u2699"};
        int navItemSize = mWindow.dp(40);
        for (int i=0;i<eicons.length;i++){final int ti=i;
            var item=new TextView(ctx); item.setText(eicons[i]); item.setTextSize(18);
            item.setTextColor(i==mActiveTab?mColorPrimary:mColorOnSurfaceVariant); item.setGravity(Gravity.CENTER);
            item.setLayoutParams(new LinearLayout.LayoutParams(navItemSize,navItemSize));
            if (i==mActiveTab){var abg=new ShapeDrawable(); abg.setShape(ShapeDrawable.RECTANGLE); abg.setColor((mColorPrimary&0xFFFFFF)|0x18000000); item.setBackground(abg);}
            item.setOnClickListener(v->switchTab(ti)); nav.addView(item);
            mNavItems.add(item);
        }
        body.addView(nav);
        mSidebar = new FrameLayout(ctx);
        mSidebar.setLayoutParams(new FrameLayout.LayoutParams(mSidebarCollapsed?0:mSidebarW,-1));
        ((FrameLayout.LayoutParams)mSidebar.getLayoutParams()).leftMargin=mNavW;
        mSidebar.setBackground(makeRoundedBg((mColorSurface&0xFFFFFF)|0x10000000, 0));
        mSidebarContent=new LinearLayout(ctx);
        mSidebarContent.setOrientation(LinearLayout.VERTICAL);
        mSidebarContent.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
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

    private void createResizeHandles(){
        mResizeHandles=new View[8];
        for(int i=0;i<8;i++){var h=new View(mRoot.getContext()); mResizeHandles[i]=h; mRoot.addView(h);}
        int[][] edges={{RESIZE_L|RESIZE_T},{RESIZE_R|RESIZE_T},{RESIZE_L|RESIZE_B},{RESIZE_R|RESIZE_B},{RESIZE_L},{RESIZE_R},{RESIZE_T},{RESIZE_B}};
        for(int i=0;i<8;i++){final int t=edges[i][0]; mResizeHandles[i].setOnTouchListener((v,e)->onResize(e,t));}
        layoutResizeHandles();
    }

    private boolean onResize(MotionEvent event, int type){
        if(mMaximized)return false;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN->{mResizing=true; mResizeStartMouseX=event.getRawX(); mResizeStartMouseY=event.getRawY();
                mResizeStartWNorm=mWNorm; mResizeStartHNorm=mHNorm; mResizeStartXNorm=mXNorm; mResizeStartYNorm=mYNorm; mWindow.setAlpha(0.75f); return true;}
            case MotionEvent.ACTION_MOVE->{
                if(!mResizing)return false;
                int dx=(int)(event.getRawX()-mResizeStartMouseX), dy=(int)(event.getRawY()-mResizeStartMouseY);
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
            case MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
                if(mResizing){mWindow.setAlpha(1.0f); HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm);}
                mResizing=false; return true;
            }
        } return false;
    }

    private void layoutResizeHandles() {
        int wx=normW(mXNorm),wy=normH(mYNorm),ww=normW(mWNorm),wh=normH(mHNorm),h=mWindow.dp(HANDLE_THICK_DP);
        int hs=h*3;
        layH(mResizeHandles[0],wx-hs,wy-hs,hs,hs); layH(mResizeHandles[1],wx+ww,wy-hs,hs,hs);
        layH(mResizeHandles[2],wx-hs,wy+wh,hs,hs); layH(mResizeHandles[3],wx+ww,wy+wh,hs,hs);
        layH(mResizeHandles[4],wx-hs,wy+h,hs,wh-h*2); layH(mResizeHandles[5],wx+ww,wy+h,hs,wh-h*2);
        layH(mResizeHandles[6],wx+hs,wy-hs,ww-hs*2,hs); layH(mResizeHandles[7],wx+hs,wy+wh,ww-hs*2,hs);
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

    private int normW(float n){return (int)(n*getScreenW());}
    private int normH(float n){return (int)(n*getScreenH());}
    private float wToNorm(int v){return getScreenW()>0?(float)v/getScreenW():0;}
    private float hToNorm(int v){return getScreenH()>0?(float)v/getScreenH():0;}
    private int getScreenW(){Context ctx = getContext(); if (ctx == null || mRoot == null) return 1; return ctx.getResources().getDisplayMetrics().widthPixels;}
    private int getScreenH(){Context ctx = getContext(); if (ctx == null || mRoot == null) return 1; return ctx.getResources().getDisplayMetrics().heightPixels;}
    private void clampTitleBarVisible(){
        int mw=normW(mWNorm); int maxL=getScreenW()-mw; int maxT=getScreenH()-mTitleH;
        mXNorm=wToNorm(Math.max(0,Math.min(maxL,normW(mXNorm))));
        mYNorm=hToNorm(Math.max(0,Math.min(maxT,normH(mYNorm))));
    }
    private void applyWindowLayout(){
        int sw=normW(mWNorm),sh=normH(mHNorm),sx=normW(mXNorm),sy=normH(mYNorm);
        var lp=(FrameLayout.LayoutParams)mWindow.getLayoutParams(); lp.width=sw; lp.height=sh; lp.leftMargin=sx; lp.topMargin=sy;
        mWindow.requestLayout();
    }
    private void reapplyWindow(){applyWindowLayout(); layoutResizeHandles();}

    // ===== TAB SYSTEM =====

    private int mActiveTab;
    private void switchTab(int idx){
        mActiveTab=idx;
        for (int i = 0; i < mNavItems.size(); i++) {
            var item = mNavItems.get(i);
            item.setTextColor(i == idx ? mColorPrimary : mColorOnSurfaceVariant);
            if (i == idx) {
                var abg = new ShapeDrawable();
                abg.setShape(ShapeDrawable.RECTANGLE);
                abg.setColor((mColorPrimary & 0xFFFFFF) | 0x18000000);
                item.setBackground(abg);
            } else {
                item.setBackground(null);
            }
            item.invalidate();
        }
        HudOrbState.saveSidebarState(mSidebarW, mSidebarCollapsed);
        String[] labels={tr("nav.chat"),tr("nav.sessions"),tr("nav.assets"),tr("nav.tools"),tr("nav.plugins"),tr("nav.settings")};
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
            for(int i=0;i<mSessions.size();i++){final int fi=i; mSidebarContent.addView(createSessionItem(fi));}
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
        }else{
            var l=new TextView(mSidebarContent.getContext()); l.setText(tab+" - Phase 2"); l.setTextSize(11);
            l.setTextColor(mColorOnSurfaceVariant); l.setPadding(dp14,mWindow.dp(8),dp14,mWindow.dp(8)); mSidebarContent.addView(l);
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
            var abg = new ShapeDrawable();
            abg.setShape(ShapeDrawable.RECTANGLE);
            abg.setColor((mColorPrimary & 0xFFFFFF) | 0x18000000);
            item.setBackground(abg);
        }
        var ico = new TextView(ctx);
        ico.setText("\uD83D\uDCC4");
        ico.setTextSize(14);
        item.addView(ico, new LinearLayout.LayoutParams(-2,-2));
        var info = new LinearLayout(ctx);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(mWindow.dp(8),0,0,0);
        var nm = new TextView(ctx);
        nm.setText(s.title);
        nm.setTextSize(12);
        nm.setTextColor(s.active?mColorOnSurface:mColorOnSurfaceVariant);
        info.addView(nm);
        var pv = new TextView(ctx);
        pv.setText(formatSessionTime(s.updatedAt));
        pv.setTextSize(10);
        pv.setTextColor(mColorOnSurfaceVariant);
        pv.setMaxLines(1);
        info.addView(pv);
        item.addView(info, new LinearLayout.LayoutParams(-1,-2,1.0f));
        var tm = new TextView(ctx);
        tm.setText(formatSessionTimeShort(s.updatedAt));
        tm.setTextSize(10);
        tm.setTextColor(mColorOnSurfaceVariant);
        item.addView(tm);
        item.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(Menu.NONE, idx, Menu.NONE, tr("chat.delete_session")).setOnMenuItemClickListener(itemClick -> { removeSession(idx); return true; });
        });
        return item;
    }

    private void rebuildSessionList() {
        if (mSidebarContent == null) return;
        updateSidebarContent(tr(mActiveTab == 0 ? "nav.chat" : "nav.sessions"));
    }

    private void removeSession(int idx) {
        if (idx < 0 || idx >= mSessions.size()) return;
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null)
            mc.player.networkHandler.sendChatCommand("mineclawd sessions remove " + mSessions.get(idx).id);
        mSessions.remove(idx);
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

    // ===== MAIN CONTENT =====

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

        mChatScroll = new ScrollView(ctx) {
            @Override protected void onScrollChanged(int l, int t, int oldL, int oldT) {
                super.onScrollChanged(l, t, oldL, oldT);
                View child = getChildAt(0);
                if (child != null) {
                    int contentBottom = child.getHeight() - getHeight();
                    mFollowTail = t >= contentBottom - 20;
                }
            }
        };
        mChatScroll.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mChatScroll.setFocusable(false);
        mChatScroll.setFocusableInTouchMode(false);
        mChatContainer = new LinearLayout(ctx);
        mChatContainer.setOrientation(LinearLayout.VERTICAL);
        mChatContainer.setPadding(mWindow.dp(14), mWindow.dp(10), mWindow.dp(14), mWindow.dp(10));
        mFollowTail = true;

        for (var msg : mMessages) {
            MessageBubble bubble = addMessageBubble(msg.roleId, "");
            if (bubble != null) {
                bubble.buildFromRawContent(msg.content);
            }
        }

        if (mGenerating && mCurrentStreamingMessageBubble == null)
            mCurrentStreamingMessageBubble = addMessageBubble("assistant", "");

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
        mChatInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == 66 && event.getAction() == 0) {
                if (event.isCtrlPressed() || event.isShiftPressed()) return false;
                sendChatMessage(); return true;
            }
            return false;
        });
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
                if (count > 0) { attachmentBadge.setText("+" + count); attachmentBadge.setVisibility(View.VISIBLE); mChatInput.setHint(count + " " + tr("chat.attached")); }
                else { attachmentBadge.setVisibility(View.GONE); mChatInput.setHint(tr("chat.input_hint")); }
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
            if (mGenerating) requestStopGeneration();
            else if (!mFailedRetryToken.isBlank()) requestRetry();
            else sendChatMessage();
        });
        mBtnRow.addView(mSendBtn);
        mInputArea.addView(mBtnRow);
        chatRoot.addView(mInputArea);
        mMainContent.addView(chatRoot);

        mInputArea.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            int h = b - t;
            var slp = (FrameLayout.LayoutParams) mChatScroll.getLayoutParams();
            if (slp.bottomMargin != h) { slp.bottomMargin = h; mChatScroll.setLayoutParams(slp); }
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
        mInputArea.setOnTouchListener((v, e) -> { if (e.getAction() == MotionEvent.ACTION_DOWN) mChatInput.requestFocus(); return false; });
        focusEditTextWithRetry(mChatInput, 8);
        CharInputBridge.activate(mChatInput, mChatInput.getText());
        updateSendButton();
    }

    private void updateAttachmentBadge(Context ctx) {
        int count = AttachmentHelper.getPendingFiles().size();
        if (mBtnRow != null && mBtnRow.getChildCount() > 1) {
            View badge = mBtnRow.getChildAt(1);
            if (badge instanceof TextView tv) {
                if (count > 0) { tv.setText("+" + count); tv.setVisibility(View.VISIBLE); }
                else tv.setVisibility(View.GONE);
            }
        }
    }

    private void updateSendButton() {
        if (mSendBtn == null) return;
        if (mGenerating) mSendBtn.setText("\u25A0 " + tr("chat.stop"));
        else if (!mFailedRetryToken.isBlank()) mSendBtn.setText("\u21BA " + tr("chat.retry"));
        else mSendBtn.setText(tr("chat.send"));
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

    private void focusEditTextWithRetry(EditText et, int maxRetries) {
        et.post(() -> {
            boolean focused = et.requestFocus();
            if (!focused && maxRetries > 0)
                et.postDelayed(() -> focusEditTextWithRetry(et, maxRetries - 1), 50);
        });
    }

    private void scrollChat() {
        if (mChatScroll != null && mFollowTail) {
            mChatScroll.post(() -> { if (mChatScroll != null) mChatScroll.fullScroll(View.FOCUS_DOWN); });
        }
    }

    private MessageBubble addMessageBubble(String roleId, String text) {
        if (mChatContainer == null && mActiveTab == 0 && mMainContent != null)
            updateMainContent(tr("nav.chat"));
        if (mChatContainer == null) return null;
        MessageBubble bubble = new MessageBubble(mChatContainer.getContext(), mWindow, roleId, text, mMarkflow);
        int bubbleBg = switch (roleId) {
            case "user" -> (mColorPrimary & 0xFFFFFF) | 0x25000000;
            default -> mColorSurfaceContainerHigh;
        };
        bubble.setBubbleBackgroundColor(bubbleBg);
        mChatContainer.addView(bubble.getView());
        mMessageBubbles.add(bubble);
        scrollChat();
        return bubble;
    }

    private void sendChatMessage() {
        if (mChatInput==null) return;
        String text=mChatInput.getText().toString().trim();
        if (text.isEmpty()) return;
        mChatInput.setText("");
        mMessages.add(new ChatMessage("user",text));
        addMessageBubble("user",text);
        mGenerating = true;
        updateSendButton();
        scrollChat();
        var mc=MinecraftClient.getInstance();
        if (mc.player!=null&&mc.player.networkHandler!=null) {
            var files = AttachmentHelper.getPendingFiles();
            if (files.isEmpty()) {
                mc.player.networkHandler.sendChatCommand("mclawd " + text);
            } else {
                boolean ok = com.mineclawd.MineClawdClientNetworking.sendPromptWithAttachments(mc, text, new java.util.ArrayList<>(files));
                if (!ok) mc.player.networkHandler.sendChatCommand("mclawd " + text);
                AttachmentHelper.clearPendingFiles();
                if (mBtnRow != null && mBtnRow.getChildCount() > 1) {
                    View badge = mBtnRow.getChildAt(1);
                    if (badge instanceof TextView tv) tv.setVisibility(View.GONE);
                }
            }
            if (mActiveSessionId.isBlank()) mc.player.networkHandler.sendChatCommand("mineclawd sessions new");
        }
    }

    private void requestStopGeneration() {
        mGenerating = false;
        mFailedRetryToken = "";
        updateSendButton();
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null)
            mc.player.networkHandler.sendChatCommand("mineclawd stop");
        cleanupStreamingState();
        LogManager.info(tr("chat.stopped"));
    }

    private void requestRetry() {
        String token = mFailedRetryToken;
        if (token.isBlank()) return;
        mFailedRetryToken = "";
        mGenerating = true;
        updateSendButton();
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null)
            mc.player.networkHandler.sendChatCommand("mineclawd retry " + token);
        LogManager.info(tr("chat.retrying"));
    }

    private void switchSession(int idx) {
        if (idx < 0 || idx >= mSessions.size()) return;
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null)
            mc.player.networkHandler.sendChatCommand("mineclawd sessions resume " + mSessions.get(idx).id);
        LogManager.info(tr("log.session_switched").replace("%s", mSessions.get(idx).title));
    }

    private void newSession() {
        var mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null)
            mc.player.networkHandler.sendChatCommand("mineclawd sessions new");
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
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSettingsUseVertex = position == 1;
                var sections = container.<LinearLayout>findViewById(0x7E57);
                if (sections != null) buildProviderSections(sections, ctx);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        providerRow.addView(mSettingsProviderSpinner); container.addView(providerRow);
        var sectionsContainer = new LinearLayout(ctx);
        sectionsContainer.setOrientation(LinearLayout.VERTICAL);
        sectionsContainer.setId(0x7E57);
        container.addView(sectionsContainer);
        buildProviderSections(sectionsContainer, ctx);
        addSectionHeader(container, tr("settings.general"));
        mSettingsGuiToggle = addSettingsSwitch(container, tr("settings.enable_gui"), cfg.enableGui);
        mSettingsDebugToggle = addSettingsSwitch(container, tr("settings.debug_mode"), cfg.debugMode);
        mSettingsToolLimitToggle = addSettingsSwitch(container, tr("settings.limit_tool_calls"), cfg.limitToolCalls);
        addSectionHeader(container, tr("settings.about"));
        var ver=new TextView(ctx); ver.setText("MineClawd"); ver.setTextSize(11);
        ver.setTextColor(mColorOnSurfaceVariant); ver.setPadding(0,mWindow.dp(4),0,0); container.addView(ver);
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
        mSettingsApiKey=addApiKeyInput(openaiSection, tr("settings.api_key"), MineClawdConfig.get().apiKey);
        container.addView(openaiSection);
        container.addView(new View(ctx), new LinearLayout.LayoutParams(-1, mWindow.dp(4)));
        boolean vertexActive = mSettingsUseVertex;
        var vertexSection = createBorderedSection(ctx, tr("settings.vertex_ai"), vertexActive);
        mSettingsVertexEndpoint=addSettingsInput(vertexSection, tr("settings.endpoint"), MineClawdConfig.get().vertexEndpoint);
        mSettingsVertexModel=addSettingsInput(vertexSection, tr("settings.model"), MineClawdConfig.get().vertexModel);
        mSettingsVertexApiKey=addApiKeyInput(vertexSection, tr("settings.api_key"), MineClawdConfig.get().vertexApiKey);
        container.addView(vertexSection);
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

    private EditText addApiKeyInput(ViewGroup parent, String label, String current) {
        var ctx=parent.getContext();
        var row=makeSettingsRow(ctx, label);
        var et=new EditText(ctx); et.setText(current!=null?current:""); et.setTextSize(11);
        et.setTextColor(mColorOnSurface); et.setHintTextColor(mColorOnSurfaceVariant);
        et.setSingleLine(); et.setPadding(mWindow.dp(6),mWindow.dp(4),mWindow.dp(6),mWindow.dp(4));
        et.setTransformationMethod(PasswordTransformationMethod.getInstance());
        var bg=new ShapeDrawable(); bg.setShape(ShapeDrawable.RECTANGLE); bg.setCornerRadius(mWindow.dp(4)); bg.setColor(mColorSurfaceContainerHigh);
        et.setBackground(bg);
        et.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,1.0f));
        row.addView(et);
        var toggle=new Button(ctx);
        toggle.setText(tr("settings.show_key"));
        toggle.setTextSize(10);
        toggle.setTextColor(mColorOnSurfaceVariant);
        toggle.setBackground(makeRoundedBg(mColorSurfaceContainerHighest, 4));
        toggle.setPadding(mWindow.dp(6),0,mWindow.dp(6),0);
        toggle.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        final boolean[] shown = {false};
        toggle.setOnClickListener(v->{
            shown[0] = !shown[0];
            et.setTransformationMethod(shown[0] ? null : PasswordTransformationMethod.getInstance());
            et.setSelection(et.getText().length());
            toggle.setText(shown[0] ? tr("settings.hide_key") : tr("settings.show_key"));
        });
        row.addView(toggle);
        parent.addView(row);
        return et;
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

    private String nonNull(CharSequence s) { return s == null ? "" : s.toString(); }

    private void minimizeToOrb() {
        HudOrbState.saveWindowPos(mXNorm,mYNorm,mWNorm,mHNorm);
        HudOrbState.saveSidebarState(mSidebarW, mSidebarCollapsed);
        MinecraftClient.getInstance().setScreen(null);
        HudOrbState.hideHud(); HudOrbState.show();
    }
}