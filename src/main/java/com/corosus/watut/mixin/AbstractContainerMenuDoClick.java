package com.corosus.watut.mixin;

import com.corosus.watut.WatutMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuDoClick {

    @Inject(method = "doClick", at = @At("HEAD"))
    private void doClickPre(int pSlotId, int pButton, ClickType pClickType, Player pPlayer, CallbackInfo ci) {
        if (!pPlayer.level().isClientSide()) {
            WatutMod.getPlayerStatusManagerServer().doClickPre(pPlayer);
        }
    }

    @Inject(method = "doClick", at = @At("TAIL"))
    private void doClickPost(int pSlotId, int pButton, ClickType pClickType, Player pPlayer, CallbackInfo ci) {
        if (!pPlayer.level().isClientSide()) {
            WatutMod.getPlayerStatusManagerServer().doClickPost(pPlayer);
        }
    }
}