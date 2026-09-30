package com.primegm.survivalfoodredux.nutrition.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class SelectedItemNameMixin {

    @Inject(
            method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD")
    )
    private void sfr$moveSelectedItemName(
            GuiGraphicsExtractor graphics,
            CallbackInfo ci
    ) {
        graphics.pose().pushMatrix();

        graphics.pose().translate(
                0.0F,
                -13.0F
        );
    }

    @Inject(
            method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("RETURN")
    )
    private void sfr$restoreSelectedItemName(
            GuiGraphicsExtractor graphics,
            CallbackInfo ci
    ) {
        graphics.pose().popMatrix();
    }
}