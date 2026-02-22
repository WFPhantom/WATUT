package com.corosus.watut.mixin.client;

import com.corosus.watut.WatutMod;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerKeyPress {

    @Inject(method = "keyPress", at = @At("TAIL"))
    private void keyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo info) {
        WatutMod.getPlayerStatusManagerClient().onKey();
    }
}