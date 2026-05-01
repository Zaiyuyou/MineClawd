package com.mineclawd.foundation.client.ui.framework;

import icyllis.modernui.core.Context;
import icyllis.modernui.markflow.Markflow;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import com.mineclawd.foundation.client.ui.framework.MessageBubble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 消息响应处理器 - 直接从HTTP响应层面处理消息显示
 */
public class MessageResponseHandler {
    private static final MessageResponseHandler INSTANCE = new MessageResponseHandler();
    
    private final Map<String, MessageBubble> activeBubbles;
    private FrameLayout window;
    private Context context;
    private Markflow markflow;
    
    private MessageResponseHandler() {
        this.activeBubbles = new ConcurrentHashMap<>();
    }
    
    public static MessageResponseHandler getInstance() {
        return INSTANCE;
    }
    
    /**
     * 初始化处理器
     */
    public void initialize(Context context, FrameLayout window, Markflow markflow) {
        this.context = context;
        this.window = window;
        this.markflow = markflow;
    }
    
    /**
     * 处理HTTP响应并创建消息气泡
     */
    public void handleHttpResponse(String responseId, String roleId, String content, boolean isStreaming) {
        if (window == null || context == null) {
            System.err.println("MessageResponseHandler not initialized!");
            return;
        }
        
        // 如果是流式响应，检查是否已有气泡
        if (isStreaming) {
            MessageBubble existingBubble = activeBubbles.get(responseId);
            if (existingBubble != null) {
                // 增量追加内容
                existingBubble.appendText(content);
                return;
            }
        }
        
        // 创建新气泡
        MessageBubble bubble = new MessageBubble(context, window, roleId, 
            isStreaming ? content : content, markflow);
        
        // 如果是流式响应，保存气泡引用
        if (isStreaming) {
            activeBubbles.put(responseId, bubble);
        }
        
        // 添加到窗口
        window.addView(bubble.getView());
        
        // 自动滚动到最新消息
        scrollToLatest();
    }
    
    /**
     * 完成流式响应
     */
    public void completeStreamingResponse(String responseId) {
        activeBubbles.remove(responseId);
        scrollToLatest();
    }
    
    /**
     * 处理错误响应
     */
    public void handleErrorResponse(String responseId, String errorMessage) {
        if (window == null || context == null) return;
        
        // 移除可能存在的流式气泡
        activeBubbles.remove(responseId);
        
        // 创建错误消息气泡
        MessageBubble errorBubble = new MessageBubble(context, window, "assistant", 
            "❌ " + errorMessage, markflow);
        
        window.addView(errorBubble.getView());
        scrollToLatest();
    }
    
    /**
     * 滚动到最新消息
     */
    private void scrollToLatest() {
        // 这里需要实现滚动逻辑
        // 可以调用ModernHudFragment的scrollChat方法
        if (window != null) {
            window.post(() -> {
                // 实现滚动逻辑
                ViewGroup parent = (ViewGroup) window.getParent();
                if (parent != null) {
                    parent.scrollTo(0, parent.getHeight());
                }
            });
        }
    }
    
    /**
     * 清理所有活动气泡
     */
    public void cleanup() {
        activeBubbles.clear();
        if (window != null) {
            window.removeAllViews();
        }
    }
}