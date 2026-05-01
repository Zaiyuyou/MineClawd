package com.mineclawd.foundation.client.ui.log;

import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MotionEvent;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

public class LogView extends LinearLayout {

    private static final int FADE_DELAY_MS = 8000;
    private static final int FLASH_DURATION_MS = 400;

    private final ScrollView mScrollView;
    private final LinearLayout mBubbleContainer;
    private final View mCollapsedButton;

    private boolean mExpanded = true;
    private boolean mHovered = false;
    private float mCurrentAlpha = 1.0f;
    private long mLastMessageTime = 0L;
    private final Runnable mFadeRunnable;

    public LogView(icyllis.modernui.core.Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);

        mBubbleContainer = new LinearLayout(ctx);
        mBubbleContainer.setOrientation(VERTICAL);
        mBubbleContainer.setPadding(dp(6), dp(4), dp(6), dp(4));

        mScrollView = new ScrollView(ctx);
        mScrollView.addView(mBubbleContainer, new LayoutParams(-1, -2));

        var expandedLp = new LayoutParams(-1, dp(120));
        addView(mScrollView, expandedLp);

        mCollapsedButton = new View(ctx) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Paint p = Paint.obtain();
                p.setColor(0xFF6385F4);
                p.setAlpha((int)(90 * mCurrentAlpha));
                float cx = getWidth() / 2f, cy = getHeight() / 2f;
                canvas.drawCircle(cx, cy, LogView.this.dp(10), p);
                p.recycle();
            }
        };
        mCollapsedButton.setLayoutParams(new LayoutParams(-1, dp(28)));
        mCollapsedButton.setOnClickListener(v -> expand());
        mCollapsedButton.setVisibility(GONE);
        addView(mCollapsedButton);

        Runnable fadeTask = new Runnable() {
            @Override
            public void run() {
                if (!mHovered && mExpanded && mCurrentAlpha > 0.01f) {
                    mCurrentAlpha -= 0.03f;
                    updateAlpha();
                    if (mCurrentAlpha > 0.01f) {
                        postDelayed(this, 80);
                    } else {
                        mCurrentAlpha = 0f;
                        updateAlpha();
                        collapse();
                    }
                }
            }
        };
        mFadeRunnable = fadeTask;

        LogManager.setListener(entry -> addBubble(entry));
    }

    public void addBubble(LogEntry entry) {
        post(() -> {
            mLastMessageTime = System.currentTimeMillis();
            mExpanded = true;
            mCollapsedButton.setVisibility(GONE);
            mScrollView.setVisibility(VISIBLE);
            removeCallbacks(mFadeRunnable);

            mCurrentAlpha = 1.3f;
            updateAlpha();
            postDelayed(() -> {
                mCurrentAlpha = 1.0f;
                updateAlpha();
                postDelayed(() -> {
                    mCurrentAlpha = 0.6f;
                    updateAlpha();
                    postDelayed(mFadeRunnable, FADE_DELAY_MS);
                }, 3000);
            }, FLASH_DURATION_MS);

            var row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(4), dp(3), dp(4), dp(3));

            var bar = new View(getContext());
            bar.setLayoutParams(new LayoutParams(dp(3), dp(14)));
            var barBg = new ShapeDrawable();
            barBg.setShape(ShapeDrawable.RECTANGLE);
            barBg.setColor(entry.level.color | 0xCC000000);
            bar.setBackground(barBg);

            var timeView = new TextView(getContext());
            timeView.setText(entry.getTimeString());
            timeView.setTextSize(9);
            timeView.setTextColor(0xFF666666);
            timeView.setPadding(dp(4), 0, dp(4), 0);

            var textView = new TextView(getContext());
            textView.setText(entry.text);
            textView.setTextSize(11);
            textView.setTextColor(0xFFAAAAAA);
            textView.setMaxLines(2);

            if (entry.clickable) {
                textView.setTextColor(0xFF6385F4);
            }

            row.addView(bar);
            row.addView(timeView);
            row.addView(textView, new LayoutParams(-1, -2, 1.0f));

            mBubbleContainer.addView(row);
            mScrollView.post(() -> mScrollView.scrollTo(0, mScrollView.getHeight()));
            if (mBubbleContainer.getChildCount() > 50) {
                mBubbleContainer.removeViewAt(0);
            }
        });
    }

    public void setHovered(boolean hovered) {
        mHovered = hovered;
        if (hovered) {
            removeCallbacks(mFadeRunnable);
            mExpanded = true;
            mCollapsedButton.setVisibility(GONE);
            mScrollView.setVisibility(VISIBLE);
            mCurrentAlpha = 1.0f;
            updateAlpha();
        }
    }

    private void expand() {
        mExpanded = true;
        mCollapsedButton.setVisibility(GONE);
        mScrollView.setVisibility(VISIBLE);
        mCurrentAlpha = 1.0f;
        updateAlpha();
        postDelayed(mFadeRunnable, FADE_DELAY_MS + 3000);
    }

    private void collapse() {
        mExpanded = false;
        mScrollView.setVisibility(GONE);
        mCollapsedButton.setVisibility(VISIBLE);
        mCurrentAlpha = 1.0f;
        updateAlpha();
    }

    private void updateAlpha() {
        mScrollView.setAlpha(Math.min(1.0f, mCurrentAlpha));
        mCollapsedButton.setAlpha(Math.min(1.0f, mCurrentAlpha));
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            setHovered(true);
        }
        if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            setHovered(false);
        }
        return super.onTouchEvent(event);
    }
}
