package com.mineclawd.foundation.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * MineClawd 可拖动悬浮窗口
 * 基于 AgentResponseOverlay 的实现模式
 */
public class MineClawdFloatingWindow extends Screen {
    
    // 窗口尺寸和位置
    private int windowX = 50;
    private int windowY = 50;
    private int windowWidth = 350;
    private int windowHeight = 250;
    private int headerHeight = 20;
    
    // 拖动状态
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    // UI 组件
    private TextFieldWidget inputField;
    private ButtonWidget sendButton;
    private ButtonWidget closeButton;
    
    public MineClawdFloatingWindow() {
        super(Text.literal("MineClawd"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 初始化窗口位置到屏幕中心
        if (windowX < 0 || windowY < 0) {
            windowX = (this.width - windowWidth) / 2;
            windowY = (this.height - windowHeight) / 2;
        }
        
        // 创建输入框
        this.inputField = new TextFieldWidget(
            this.textRenderer, 
            windowX + 10, 
            windowY + headerHeight + 10, 
            windowWidth - 20, 
            20, 
            Text.literal("输入指令...")
        );
        this.inputField.setMaxLength(256);
        this.inputField.setPlaceholder(Text.literal("输入你的问题或指令..."));
        this.addDrawableChild(this.inputField);
        
        // 创建发送按钮
        this.sendButton = ButtonWidget.builder(
            Text.literal("发送"), 
            button -> handleUserInput(this.inputField.getText())
        ).dimensions(
            windowX + 10, 
            windowY + headerHeight + 40, 
            80, 
            20
        ).build();
        this.addDrawableChild(this.sendButton);
        
        // 创建关闭按钮
        this.closeButton = ButtonWidget.builder(
            Text.literal("✕"), 
            button -> this.close()
        ).dimensions(
            windowX + windowWidth - 25, 
            windowY + 5, 
            20, 
            10
        ).build();
        this.addDrawableChild(this.closeButton);
        
        // 创建功能按钮
        createFunctionButtons();
    }
    
    private void createFunctionButtons() {
        int buttonY = windowY + headerHeight + 70;
        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        
        // 快速聊天按钮
        ButtonWidget quickChatBtn = ButtonWidget.builder(
            Text.literal("💬 聊天"), 
            button -> this.inputField.setText("@MineClawd 你好！")
        ).dimensions(
            windowX + 10, 
            buttonY, 
            buttonWidth, 
            buttonHeight
        ).build();
        this.addDrawableChild(quickChatBtn);
        
        // 工具按钮
        ButtonWidget toolsBtn = ButtonWidget.builder(
            Text.literal("🛠️ 工具"), 
            button -> showToolsPanel()
        ).dimensions(
            windowX + 10 + buttonWidth + buttonSpacing, 
            buttonY, 
            buttonWidth, 
            buttonHeight
        ).build();
        this.addDrawableChild(toolsBtn);
        
        // 设置按钮
        ButtonWidget settingsBtn = ButtonWidget.builder(
            Text.literal("⚙️ 设置"), 
            button -> showSettingsPanel()
        ).dimensions(
            windowX + 10 + (buttonWidth + buttonSpacing) * 2, 
            buttonY, 
            buttonWidth, 
            buttonHeight
        ).build();
        this.addDrawableChild(settingsBtn);
        
        // 第二行按钮
        buttonY += buttonHeight + 10;
        
        ButtonWidget historyBtn = ButtonWidget.builder(
            Text.literal("📚 历史"), 
            button -> showHistoryPanel()
        ).dimensions(
            windowX + 10, 
            buttonY, 
            buttonWidth, 
            buttonHeight
        ).build();
        this.addDrawableChild(historyBtn);
        
        ButtonWidget helpBtn = ButtonWidget.builder(
            Text.literal("❓ 帮助"), 
            button -> showHelpPanel()
        ).dimensions(
            windowX + 10 + buttonWidth + buttonSpacing, 
            buttonY, 
            buttonWidth, 
            buttonHeight
        ).build();
        this.addDrawableChild(helpBtn);
        
        ButtonWidget aboutBtn = ButtonWidget.builder(
            Text.literal("ℹ️ 关于"), 
            button -> showAboutPanel()
        ).dimensions(
            windowX + 10 + (buttonWidth + buttonSpacing) * 2, 
            buttonY, 
            buttonWidth, 
            buttonHeight
        ).build();
        this.addDrawableChild(aboutBtn);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        // 渲染半透明背景
        renderBackground(context, mouseX, mouseY, partialTick);
        
        // 渲染窗口背景
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xB0101018);
        
        // 渲染窗口边框
        context.fill(windowX, windowY, windowX + windowWidth, windowY + 1, 0xC0507088);
        context.fill(windowX, windowY + windowHeight - 1, windowX + windowWidth, windowY + windowHeight, 0xC0507088);
        context.fill(windowX, windowY, windowX + 1, windowY + windowHeight, 0xC0507088);
        context.fill(windowX + windowWidth - 1, windowY, windowX + windowWidth, windowY + windowHeight, 0xC0507088);
        
        // 渲染标题栏
        context.fill(windowX, windowY, windowX + windowWidth, windowY + headerHeight, 0xCC1C2532);
        
        // 渲染标题
        context.drawCenteredTextWithShadow(
            this.textRenderer, 
            Text.literal("MineClawd"), 
            windowX + windowWidth / 2, 
            windowY + 6, 
            0xFFFFFF
        );
        
        // 渲染拖动提示
        if (isMouseOverHeader(mouseX, mouseY)) {
            context.drawTextWithShadow(
                this.textRenderer, 
                Text.literal("拖动移动"), 
                windowX + 5, 
                windowY + 6, 
                0xAAAAAA
            );
        }
        
        super.render(context, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverHeader(mouseX, mouseY)) {
            // 开始拖动
            dragging = true;
            dragOffsetX = (int) mouseX - windowX;
            dragOffsetY = (int) mouseY - windowY;
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            // 更新窗口位置
            windowX = (int) mouseX - dragOffsetX;
            windowY = (int) mouseY - dragOffsetY;
            
            // 限制窗口在屏幕内
            clampWindowToScreen();
            
            // 更新 UI 组件位置
            updateWidgetPositions();
            
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC 键关闭窗口
        if (keyCode == 256) {
            this.close();
            return true;
        }
        
        // Enter 键发送消息
        if (keyCode == 257 && this.inputField != null && this.inputField.isFocused()) {
            handleUserInput(this.inputField.getText());
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private boolean isMouseOverHeader(double mouseX, double mouseY) {
        return mouseX >= windowX && mouseX <= windowX + windowWidth &&
               mouseY >= windowY && mouseY <= windowY + headerHeight;
    }
    
    private void clampWindowToScreen() {
        windowX = Math.max(0, Math.min(windowX, this.width - windowWidth));
        windowY = Math.max(0, Math.min(windowY, this.height - windowHeight));
    }
    
    private void updateWidgetPositions() {
        if (this.inputField != null) {
            this.inputField.setX(windowX + 10);
            this.inputField.setY(windowY + headerHeight + 10);
        }
        
        if (this.sendButton != null) {
            this.sendButton.setX(windowX + 10);
            this.sendButton.setY(windowY + headerHeight + 40);
        }
        
        if (this.closeButton != null) {
            this.closeButton.setX(windowX + windowWidth - 25);
            this.closeButton.setY(windowY + 5);
        }
        
        // 更新功能按钮位置
        updateFunctionButtonsPositions();
    }
    
    private void updateFunctionButtonsPositions() {
        // 这里需要实现功能按钮位置更新的逻辑
        // 由于按钮是通过 addDrawableChild 添加的，需要重新设置位置
    }
    
    private void handleUserInput(String input) {
        if (input != null && !input.trim().isEmpty()) {
            // 处理用户输入
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(
                    Text.literal("✨ [MineClawd] 指令已发送: " + input), 
                    false
                );
            }
            
            // 清空输入框
            if (this.inputField != null) {
                this.inputField.setText("");
            }
        }
    }
    
    private void showToolsPanel() {
        // 显示工具面板逻辑
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("🛠️ 打开工具面板"), false);
        }
    }
    
    private void showSettingsPanel() {
        // 显示设置面板逻辑
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("⚙️ 打开设置面板"), false);
        }
    }
    
    private void showHistoryPanel() {
        // 显示历史记录面板逻辑
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("📚 打开历史记录"), false);
        }
    }
    
    private void showHelpPanel() {
        // 显示帮助面板逻辑
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("❓ 打开帮助文档"), false);
        }
    }
    
    private void showAboutPanel() {
        // 显示关于面板逻辑
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("ℹ️ 关于 MineClawd"), false);
        }
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
    
    public boolean isPauseScreen() {
        return false;
    }
}