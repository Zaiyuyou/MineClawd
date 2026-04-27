package com.mineclawd.foundation.client.mixin;

import com.mineclawd.foundation.client.CharInputBridge;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts Screen.charTyped to route characters to our CharInputBridge.
 * This bypasses ModernUI's broken findFocus() pipeline for IME input.
 */
@Mixin(Screen.class)
public class ScreenCharTypedMixin {

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void onCharTyped(char ch, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (CharInputBridge.onCharTyped(ch)) {
            cir.setReturnValue(true);
        }
    }
}
