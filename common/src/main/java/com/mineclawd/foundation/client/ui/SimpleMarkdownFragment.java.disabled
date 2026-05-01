/*
 * MineClawd - Simple Markdown Fragment Test
 * Copyright (C) 2024 MineClawd Team. All rights reserved.
 */

package com.mineclawd.foundation.client.ui;

import icyllis.modernui.annotation.NonNull;
import icyllis.modernui.annotation.Nullable;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.markflow.Markflow;
import icyllis.modernui.text.Editable;
import icyllis.modernui.text.TextWatcher;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;

/**
 * 简单的MarkdownFragment测试 - 基于ModernUI-MC的MarkdownFragment实现
 * 参考ModernUI-MC的MarkdownFragment架构
 */
public class SimpleMarkdownFragment extends Fragment {

    private Markflow mMarkflow;
    private EditText mInput;
    private TextView mPreview;

    private final Runnable mRenderMarkdown = () -> {
        if (mMarkflow != null && mPreview != null && mInput != null) {
            mMarkflow.setMarkdown(mPreview, mInput.getText());
        }
    };

    @Override
    public void onCreate(@Nullable DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化Markflow引擎
        var builder = Markflow.builder(requireContext());
        mMarkflow = builder.build();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable DataSet savedInstanceState) {
        var context = requireContext();
        
        // 创建水平布局容器
        var layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        
        // 左侧编辑区域
        {
            EditText input = mInput = new EditText(context);
            int padding = input.dp(16);
            input.setPadding(padding, padding, padding, padding);
            input.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_LTR);
            input.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            
            // 设置默认Markdown内容
            input.setText("""
                # MineClawd Markdown测试
                
                ## 欢迎使用ModernUI-MC
                
                这是一个简单的Markdown编辑器测试，基于ModernUI-MC的Markflow引擎。
                
                ### 功能特性
                
                - ✅ 实时Markdown预览
                - ✅ 代码高亮支持
                - ✅ 现代UI设计
                - ✅ 响应式布局
                
                ### 代码示例
                
                ```java
                public class HelloWorld {
                    public static void main(String[] args) {
                        System.out.println("Hello, MineClawd!");
                    }
                }
                ```
                
                **粗体文本** 和 *斜体文本*
                
                > 引用块示例
                
                1. 有序列表项1
                2. 有序列表项2
                3. 有序列表项3
                
                ---
                
                *MineClawd Team*  
                *2024*
                """);
            
            var params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 
                1.0f // 权重1
            );
            layout.addView(input, params);
        }
        
        // 右侧预览区域
        {
            TextView preview = mPreview = new TextView(context);
            int padding = preview.dp(16);
            preview.setPadding(padding, padding, padding, padding);
            preview.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_LTR);
            preview.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            preview.setTextIsSelectable(true);
            
            var params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 
                1.0f // 权重1
            );
            layout.addView(preview, params);
        }
        
        // 初始渲染Markdown
        mRenderMarkdown.run();
        
        // 添加文本变化监听器
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 文本变化前
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文本变化中
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文本变化后，延迟渲染Markdown
                if (mPreview != null) {
                    mPreview.removeCallbacks(mRenderMarkdown);
                    mPreview.postDelayed(mRenderMarkdown, 300); // 300ms延迟
                }
            }
        });
        
        return layout;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理资源
        mInput = null;
        mPreview = null;
    }
}