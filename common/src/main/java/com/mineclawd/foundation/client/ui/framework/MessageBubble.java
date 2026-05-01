package com.mineclawd.foundation.client.ui.framework;

import com.google.gson.JsonParser;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Color;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.markflow.Markflow;
import icyllis.modernui.text.Spannable;
import icyllis.modernui.text.SpannableStringBuilder;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

import com.mineclawd.foundation.chat.ChatRole;
import com.mineclawd.foundation.chat.ChatRoleConfig;
import com.mineclawd.foundation.chat.ChatRoleManager;
import icyllis.modernui.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageBubble {
    private final Context context;
    private final FrameLayout window;
    private final ChatRoleConfig roleConfig;
    private final LinearLayout bubbleLayout;
    private final LinearLayout mBlocksContainer;
    private Markflow markflow;
    private boolean isVisible = true;
    private int bubbleBgColor = 0xFF2D3748;

    private static final Pattern MARKER_PATTERN = Pattern.compile("@@TC\\|([^|]+)\\|([^|]+)\\|(.*?)@@", Pattern.DOTALL);

    private sealed interface Block permits TextBlock, ToolCardData {}
    private static final class TextBlock implements Block {
        final SpannableStringBuilder text = new SpannableStringBuilder();
    }
    private record ToolCardData(String toolName, boolean done, String result) implements Block {}

    private final List<Block> mBlocks = new ArrayList<>();

    public MessageBubble(Context context, FrameLayout window, String roleId, String initialContent, Markflow markflow) {
        this.context = context;
        this.window = window;
        this.roleConfig = ChatRoleManager.getInstance().getRoleConfig(roleId);
        this.markflow = markflow;

        this.bubbleLayout = createOuterLayout();
        this.mBlocksContainer = createBlocksContainer();
        bubbleLayout.addView(mBlocksContainer);

        if (initialContent != null && !initialContent.isEmpty()) {
            appendText(initialContent);
        }
    }

    public MessageBubble(Context context, FrameLayout window, String roleId, String initialContent) {
        this(context, window, roleId, initialContent, null);
    }

    private LinearLayout createOuterLayout() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(0, window.dp(4), 0, window.dp(4));
        layout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

        switch (roleConfig.getAlignment()) {
            case LEFT -> addAvatarIcon(layout);
            case RIGHT, CENTER -> addSpacer(layout);
        }
        return layout;
    }

    private LinearLayout createBlocksContainer() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        ShapeDrawable bg = new ShapeDrawable();
        bg.setShape(ShapeDrawable.RECTANGLE);
        bg.setCornerRadius(window.dp(6));
        bg.setColor(bubbleBgColor);
        container.setBackground(bg);
        container.setPadding(window.dp(10), window.dp(8), window.dp(10), window.dp(8));

        if (roleConfig.getAlignment() == ChatRole.Alignment.LEFT) {
            container.setMinimumWidth(window.dp(60));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.weight = 1.0f;
            container.setLayoutParams(params);
        } else {
            container.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        }
        return container;
    }

    private void addAvatarIcon(LinearLayout layout) {
        TextView icon = new TextView(context);
        icon.setText(roleConfig.getAvatar());
        icon.setTextSize(14);
        icon.setPadding(0, 0, window.dp(8), 0);
        layout.addView(icon);
    }

    private void addSpacer(LinearLayout layout) {
        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        layout.addView(spacer);
    }

    public View getView() { return bubbleLayout; }
    public ChatRoleConfig getRoleConfig() { return roleConfig; }

    public void setBubbleBackgroundColor(int color) {
        bubbleBgColor = color;
        ShapeDrawable bg = (ShapeDrawable) mBlocksContainer.getBackground();
        if (bg != null) { bg.setColor(color); mBlocksContainer.invalidate(); }
    }

    public static String extractResultText(String raw) {
        if (raw == null || raw.isBlank()) return "";
        raw = raw.trim();
        if (raw.startsWith("{")) {
            try {
                var json = JsonParser.parseString(raw).getAsJsonObject();
                StringBuilder sb = new StringBuilder();
                for (var entry : json.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue().isJsonPrimitive() ? entry.getValue().getAsString() : entry.getValue().toString();
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(key).append(": ").append(val);
                }
                return sb.toString();
            } catch (Exception ignored) {}
        }
        return raw;
    }

    // ===== BLOCK PARSING (shared by streaming and history rebuild) =====

    public void buildFromRawContent(String raw) {
        mBlocks.clear();
        if (raw == null || raw.isBlank()) {
            mBlocksContainer.removeAllViews();
            return;
        }

        Matcher m = MARKER_PATTERN.matcher(raw);
        int lastEnd = 0;

        while (m.find()) {
            if (m.start() > lastEnd) {
                String text = raw.substring(lastEnd, m.start()).trim();
                if (!text.isEmpty()) appendTextInternal(text);
            }
            String toolName = m.group(1);
            boolean done = "done".equals(m.group(2));
            String result = extractResultText(m.group(3));
            int existingIdx = -1;
            for (int i = 0; i < mBlocks.size(); i++) {
                if (mBlocks.get(i) instanceof ToolCardData tc && tc.toolName().equals(toolName)) {
                    existingIdx = i;
                    break;
                }
            }
            if (existingIdx >= 0) {
                mBlocks.set(existingIdx, new ToolCardData(toolName, done, result));
            } else {
                mBlocks.add(new ToolCardData(toolName, done, result));
            }
            lastEnd = m.end();
        }
        if (lastEnd < raw.length()) {
            String text = raw.substring(lastEnd).trim();
            if (!text.isEmpty()) appendTextInternal(text);
        }
        rebuildAllViews();
    }

    // ===== TEXT BLOCK =====

    public void appendText(String text) {
        if (text == null || text.isEmpty()) return;
        text = roleConfig.applyIndent(text);
        if (!mBlocks.isEmpty() && mBlocks.get(mBlocks.size() - 1) instanceof TextBlock last) {
            last.text.append(text);
        } else {
            TextBlock block = new TextBlock();
            block.text.append(text);
            mBlocks.add(block);
        }
        rebuildAllViews();
    }

    private void appendTextInternal(String text) {
        if (text == null || text.isEmpty()) return;
        text = roleConfig.applyIndent(text);
        TextBlock block = new TextBlock();
        block.text.append(text);
        mBlocks.add(block);
    }

    public void setContent(String text) {
        mBlocks.clear();
        mBlocksContainer.removeAllViews();
        appendText(text);
    }

    public void clearContent() {
        mBlocks.clear();
        mBlocksContainer.removeAllViews();
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();
        for (Block block : mBlocks) {
            if (block instanceof TextBlock tb) sb.append(tb.text);
            else if (block instanceof ToolCardData tc) sb.append("@@TC|").append(tc.toolName()).append("|").append(tc.done() ? "done" : "running").append("|").append(tc.result()).append("@@");
        }
        return sb.toString();
    }

    // ===== TOOL CARD BLOCK =====

    public void addToolCard(String toolName) {
        mBlocks.add(new ToolCardData(toolName, false, ""));
        addToolCardView(toolName, false, "");
    }

    public void updateToolCard(String toolName, boolean done, String result) {
        for (int i = 0; i < mBlocks.size(); i++) {
            Block block = mBlocks.get(i);
            if (block instanceof ToolCardData tc && tc.toolName().equals(toolName)) {
                mBlocks.set(i, new ToolCardData(toolName, done, result));
                // Replace the corresponding view
                int viewIdx = blockViewIndex(i);
                if (viewIdx >= 0) {
                    mBlocksContainer.removeViewAt(viewIdx);
                    mBlocksContainer.addView(createCardView(toolName, done, result), viewIdx);
                }
                return;
            }
        }
    }

    private int blockViewIndex(int blockIdx) {
        // Count views corresponding to blocks before this index
        int viewIdx = 0;
        for (int i = 0; i < blockIdx && i < mBlocks.size(); i++) {
            Block block = mBlocks.get(i);
            if (block instanceof TextBlock tb) {
                // TextBlocks are merged into one view per block
                // But in the container, TextBlocks = 1 view, ToolCards = 1 view
                viewIdx++;
            } else {
                viewIdx++;
            }
        }
        return viewIdx;
    }

    public boolean hasToolCard(String toolName) {
        for (Block block : mBlocks) {
            if (block instanceof ToolCardData tc && tc.toolName().equals(toolName)) return true;
        }
        return false;
    }

    public void clearToolCards() {
        mBlocks.removeIf(b -> b instanceof ToolCardData);
        rebuildAllViews();
    }

    // ===== VIEW REBUILD =====

    private void rebuildAllViews() {
        mBlocksContainer.removeAllViews();
        for (Block block : mBlocks) {
            if (block instanceof TextBlock tb) {
                TextView tv = createTextView();
                String text = tb.text.toString();
                if (markflow != null) markflow.setMarkdown(tv, toMarkdownLineBreaks(text));
                else tv.setText(text);
                mBlocksContainer.addView(tv);
            } else if (block instanceof ToolCardData tc) {
                mBlocksContainer.addView(createCardView(tc.toolName(), tc.done(), tc.result()));
            }
        }
    }

    private void renderAllTextBlocks() {
        mBlocksContainer.removeAllViews();
        for (Block block : mBlocks) {
            if (block instanceof TextBlock tb) {
                TextView tv = createTextView();
                String text = tb.text.toString();
                if (markflow != null) markflow.setMarkdown(tv, toMarkdownLineBreaks(text));
                else tv.setText(text);
                mBlocksContainer.addView(tv);
            } else if (block instanceof ToolCardData tc) {
                mBlocksContainer.addView(createCardView(tc.toolName(), tc.done(), tc.result()));
            }
        }
    }

    private static String toMarkdownLineBreaks(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        text = text.replace("\n\n", "\u0000\u0000");
        text = text.replace("\n", "  \n");
        text = text.replace("\u0000\u0000", "\n\n");
        return text;
    }

    private void addToolCardView(String toolName, boolean done, String result) {
        mBlocksContainer.addView(createCardView(toolName, done, result));
    }

    private TextView createTextView() {
        TextView tv = new TextView(context);
        tv.setTextSize(12);
        tv.setLineSpacing(window.dp(2), 1.0f);
        tv.setTextIsSelectable(true);
        tv.setSpannableFactory(Spannable.NO_COPY_FACTORY);
        tv.setTextColor(0xFFE2E8F0);
        tv.setMaxWidth((int)(window.getWidth() * 0.88f));
        if (roleConfig.getAlignment() == ChatRole.Alignment.RIGHT) {
            tv.setGravity(icyllis.modernui.view.Gravity.RIGHT);
        }
        return tv;
    }

    private static boolean looksLikeError(String text) {
        if (text == null || text.isBlank()) return false;
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("error") || lower.contains("fail") || lower.contains("exception") || lower.contains("timeout");
    }

    private View createCardView(String toolName, boolean done, String result) {
        var card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);

        boolean hasResult = done && !result.isEmpty();
        boolean isError = done && looksLikeError(result);

        var bg = new ShapeDrawable();
        bg.setShape(ShapeDrawable.RECTANGLE);
        bg.setCornerRadius(window.dp(6));
        int bgColor, strokeColor;
        if (!done) {
            bgColor = 0x182B3D5C;
            strokeColor = 0x402B3D5C;
        } else if (isError) {
            bgColor = 0xFF3D1B1B;
            strokeColor = 0xFF6F2D2D;
        } else if (result.isEmpty()) {
            bgColor = 0xFF2D2D2D;
            strokeColor = 0xFF444444;
        } else {
            bgColor = 0xFF1B3D2A;
            strokeColor = 0xFF2D6A4F;
        }
        bg.setColor(bgColor);
        bg.setStroke(window.dp(1), strokeColor);
        card.setBackground(bg);
        int pad = window.dp(12);
        card.setPadding(pad, window.dp(6), pad, window.dp(6));

        var header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(icyllis.modernui.view.Gravity.CENTER_VERTICAL);

        var toggleLabel = new TextView(context);
        toggleLabel.setText("\u25B6");
        toggleLabel.setTextSize(9);
        toggleLabel.setTextColor(hasResult ? 0xFF95D5B2 : 0x4095D5B2);
        toggleLabel.setPadding(0, 0, window.dp(4), 0);
        toggleLabel.setMinimumWidth(0);
        toggleLabel.setMinimumHeight(0);
        header.addView(toggleLabel, new LinearLayout.LayoutParams(-2, -2));

        var icon = new TextView(context);
        icon.setText("\u2699");
        icon.setTextSize(12);
        int iconColor = done ? (isError ? 0xFFE57373 : 0xFF52B788) : 0xFF81C784;
        icon.setTextColor(iconColor);
        header.addView(icon, new LinearLayout.LayoutParams(-2, -2));

        var nameLabel = new TextView(context);
        nameLabel.setText(toolName);
        nameLabel.setTextSize(11);
        int nameColor = done ? (isError ? 0xFFFFCDD2 : 0xFFD8F3DC) : 0xFFE2E8F0;
        nameLabel.setTextColor(nameColor);
        nameLabel.setPadding(window.dp(6), 0, 0, 0);
        nameLabel.setSingleLine(true);
        nameLabel.setEllipsize(TextUtils.TruncateAt.END);
        header.addView(nameLabel, new LinearLayout.LayoutParams(0, -2, 1.0f));

        var stateIcon = new TextView(context);
        stateIcon.setText(done ? (isError ? "\u2716" : "\u2714") : "\u23F3");
        stateIcon.setTextSize(11);
        int stateColor = done ? (isError ? 0xFFE57373 : 0xFF52B788) : 0xFF81C784;
        stateIcon.setTextColor(stateColor);
        header.addView(stateIcon, new LinearLayout.LayoutParams(-2, -2));

        card.addView(header);

        if (hasResult) {
            var resultArea = new LinearLayout(context);
            resultArea.setOrientation(LinearLayout.VERTICAL);
            resultArea.setPadding(0, window.dp(4), 0, 0);
            resultArea.setVisibility(View.GONE);

            var resultScroll = new ScrollView(context);
            resultScroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));

            var resultText = new TextView(context);
            resultText.setText(result);
            resultText.setTextSize(9);
            resultText.setTextColor(isError ? 0xFFFFCDD2 : 0xFF95D5B2);
            resultText.setTextIsSelectable(true);
            resultScroll.addView(resultText);
            resultArea.addView(resultScroll);
            card.addView(resultArea);

            resultScroll.post(() -> {
                int maxH = window.dp(150);
                if (resultScroll.getHeight() > maxH) {
                    var lp = resultScroll.getLayoutParams();
                    lp.height = maxH;
                    resultScroll.setLayoutParams(lp);
                }
            });

            final boolean[] collapsed = {true};
            header.setOnClickListener(v -> {
                collapsed[0] = !collapsed[0];
                resultArea.setVisibility(collapsed[0] ? View.GONE : View.VISIBLE);
                toggleLabel.setText(collapsed[0] ? "\u25B6" : "\u25BC");
            });
        }
        return card;
    }

    // ===== LEGACY METHODS (kept for backward compat) =====

    public void setVisible(boolean visible) {
        isVisible = visible;
        bubbleLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    public boolean isVisible() { return isVisible; }
    public void destroy() {
        if (bubbleLayout != null && bubbleLayout.getParent() != null)
            ((ViewGroup) bubbleLayout.getParent()).removeView(bubbleLayout);
    }
}
