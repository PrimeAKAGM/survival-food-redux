package com.primegm.survivalfoodredux.nutrition.mixin;

import com.primegm.survivalfoodredux.nutrition.FoodGroup;
import com.primegm.survivalfoodredux.nutrition.NutritionComponent;
import com.primegm.survivalfoodredux.nutrition.NutritionEvents;
import com.primegm.survivalfoodredux.nutrition.NutritionStorage;
import com.primegm.survivalfoodredux.nutrition.SFREffectStorage;
import com.primegm.survivalfoodredux.survival_food_redux.SFR;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(
            method = "completeUsingItem",
            at = @At("HEAD")
    )
    private void sfr$onCompleteUsingItem(
            CallbackInfo ci
    ) {
        ServerPlayer player =
                (ServerPlayer) (Object) this;

        NutritionEvents.processFood(
                player,
                player.getUseItem()
        );
    }

    @Inject(
            method = "restoreFrom",
            at = @At("RETURN")
    )
    private void sfr$onRestoreFrom(
            ServerPlayer oldPlayer,
            boolean restoreAll,
            CallbackInfo ci
    ) {

        /*
         * restoreAll == false means the player died
         * and is being recreated for respawn.
         */
        if (restoreAll) {
            return;
        }

        ServerPlayer player =
                (ServerPlayer) (Object) this;

        NutritionComponent component =
                new NutritionComponent();

        component.set(
                FoodGroup.PROTEIN,
                30
        );

        component.set(
                FoodGroup.FIBER,
                30
        );

        component.set(
                FoodGroup.SUGAR,
                30
        );

        component.set(
                FoodGroup.FAT,
                30
        );

        NutritionStorage.set(
                player,
                component
        );

        /*
         * Do not carry Survival Food Redux nutrition
         * effect state from the previous life.
         */
        SFREffectStorage.remove(
                player
        );

        /*
         * Immediately recalculate nutrition effects
         * after respawn instead of waiting for the
         * next nutrition update.
         */
        NutritionEvents.updateBuffs(
                player,
                component
        );

        NutritionEvents.sendNutritionUpdate(
                player,
                component
        );

        SFR.LOGGER.info(
                "Reset nutrition after death for {}: {}",
                player.getName().getString(),
                component
        );
    }
}