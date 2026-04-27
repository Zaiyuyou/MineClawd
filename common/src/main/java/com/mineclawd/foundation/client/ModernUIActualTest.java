package com.mineclawd.foundation.client;

/**
 * ModernUI 实际API调用测试
 * 真正实例化和使用ModernUI类来验证依赖是否真正工作
 */
public class ModernUIActualTest {
    
    /**
     * 测试ModernUI类的实际实例化
     */
    public static boolean testModernUIInstantiation() {
        try {
            // 测试Paint类的实例化
            Class<?> paintClass = Class.forName("icyllis.modernui.graphics.Paint");
            Object paint = paintClass.getDeclaredConstructor().newInstance();
            System.out.println("✓ ModernUI Paint 实例化成功");
            
            // 测试Paint方法调用
            java.lang.reflect.Method setColorMethod = paintClass.getMethod("setColor", int.class);
            setColorMethod.invoke(paint, 0xFFFFFFFF);
            System.out.println("✓ ModernUI Paint.setColor() 方法调用成功");
            
            // 测试TextPaint类的实例化
            Class<?> textPaintClass = Class.forName("icyllis.modernui.text.TextPaint");
            Object textPaint = textPaintClass.getDeclaredConstructor().newInstance();
            System.out.println("✓ ModernUI TextPaint 实例化成功");
            
            // 测试TextPaint方法调用
            java.lang.reflect.Method setTextSizeMethod = textPaintClass.getMethod("setTextSize", float.class);
            setTextSizeMethod.invoke(textPaint, 14.0f);
            System.out.println("✓ ModernUI TextPaint.setTextSize() 方法调用成功");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ ModernUI 实例化测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 测试ModernUI Fragment的实际功能
     */
    public static boolean testModernUIFragment() {
        try {
            // 测试Fragment类的实例化
            Class<?> fragmentClass = Class.forName("icyllis.modernui.fragment.Fragment");
            Object fragment = fragmentClass.getDeclaredConstructor().newInstance();
            System.out.println("✓ ModernUI Fragment 实例化成功");
            
            // 测试Fragment方法调用
            java.lang.reflect.Method onCreateViewMethod = fragmentClass.getMethod("onCreateView", 
                Class.forName("icyllis.modernui.view.ViewGroup"));
            System.out.println("✓ ModernUI Fragment.onCreateView() 方法存在");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ ModernUI Fragment 测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 测试ModernUI View的实际功能
     */
    public static boolean testModernUIView() {
        try {
            // 测试View类的实例化
            Class<?> viewClass = Class.forName("icyllis.modernui.view.View");
            Class<?> contextClass = Class.forName("icyllis.modernui.core.Context");
            
            // 获取Context实例（可能需要从Minecraft获取）
            java.lang.reflect.Method getContextMethod = Class.forName("icyllis.modernui.mc.UIManager")
                .getMethod("getContext");
            Object context = getContextMethod.invoke(null);
            
            if (context != null) {
                // 使用Context实例化View
                Object view = viewClass.getDeclaredConstructor(contextClass).newInstance(context);
                System.out.println("✓ ModernUI View 实例化成功");
                
                // 测试View方法调用
                java.lang.reflect.Method onDrawMethod = viewClass.getMethod("onDraw", 
                    Class.forName("icyllis.modernui.graphics.Canvas"));
                System.out.println("✓ ModernUI View.onDraw() 方法存在");
                
                return true;
            } else {
                System.err.println("✗ 无法获取ModernUI Context");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("✗ ModernUI View 测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 测试ModernUI API的实际调用
     */
    public static boolean testModernUIAPICalls() {
        try {
            // 测试MuiModApi的实际调用
            Class<?> muiModApiClass = Class.forName("icyllis.modernui.mc.MuiModApi");
            java.lang.reflect.Method getMethod = muiModApiClass.getMethod("get");
            Object muiApi = getMethod.invoke(null);
            
            if (muiApi != null) {
                System.out.println("✓ ModernUI MuiModApi.get() 调用成功");
                
                // 测试createScreen方法
                java.lang.reflect.Method createScreenMethod = muiApi.getClass().getMethod(
                    "createScreen", 
                    Class.forName("icyllis.modernui.fragment.Fragment"),
                    Class.forName("icyllis.modernui.mc.ScreenCallback"),
                    Class.forName("net.minecraft.client.gui.screen.Screen"),
                    CharSequence.class
                );
                System.out.println("✓ ModernUI createScreen() 方法存在");
                
                return true;
            } else {
                System.err.println("✗ MuiModApi.get() 返回null");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("✗ ModernUI API 调用测试失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 运行完整的ModernUI实际功能测试
     */
    public static void runActualModernUITest() {
        System.out.println("=== ModernUI 实际功能测试开始 ===");
        
        boolean instantiationTest = testModernUIInstantiation();
        boolean fragmentTest = testModernUIFragment();
        boolean viewTest = testModernUIView();
        boolean apiTest = testModernUIAPICalls();
        
        if (instantiationTest && fragmentTest && viewTest && apiTest) {
            System.out.println("✓ ModernUI 实际功能测试全部通过！");
            System.out.println("✓ 依赖真正配置成功，可以正常使用ModernUI API！");
        } else {
            System.err.println("✗ ModernUI 实际功能测试失败！");
            System.err.println("✗ 依赖可能没有真正配置成功！");
        }
        
        System.out.println("=== ModernUI 实际功能测试结束 ===");
    }
}