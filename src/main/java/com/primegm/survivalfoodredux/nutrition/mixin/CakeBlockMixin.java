package com.primegm.survivalfoodredux.nutrition.mixin;

import com.primegm.survivalfoodredux.nutrition.NutritionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {

    @Inject(
            method = "eat",
            at = @At("RETURN")
    )
    private static void sfr$onCakeEaten(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Player player,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && cir.getReturnValue().consumesAction()) {

            NutritionEvents.processFood(
                    serverPlayer,
                    new ItemStack(Items.CAKE)
            );
        }
    }
}