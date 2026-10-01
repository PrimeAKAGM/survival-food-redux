package com.primegm.survivalfoodredux.nutrition.mixin;

import com.primegm.survivalfoodredux.nutrition.NutritionComponent;
import com.primegm.survivalfoodredux.nutrition.NutritionEvents;
import com.primegm.survivalfoodredux.nutrition.NutritionStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Unique
    private int sfr$foodLevelBeforeTick;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void sfr$beforeFoodTick(
            ServerPlayer player,
            CallbackInfo ci
    ) {
        FoodData foodData =
                (FoodData) (Object) this;

        sfr$foodLevelBeforeTick =
                foodData.getFoodLevel();
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void sfr$afterFoodTick(
            ServerPlayer player,
            CallbackInfo ci
    ) {
        FoodData foodData =
                (FoodData) (Object) this;

        int foodLevelAfterTick =
                foodData.getFoodLevel();

        float saturationLevelAfterTick =
                foodData.getSaturationLevel();

        /*
         * Nutrition should not decay while the player
         * still has saturation available.
         *
         * Saturation represents the energy reserves from
         * recently consumed food. Nutrition only begins
         * to decay once those reserves are exhausted.
         */
        if (saturationLevelAfterTick > 0.0F) {
            return;
        }

        /*
         * Only decay nutrition when the actual hunger
         * level decreases.
         */
        boolean foodChanged =
                foodLevelAfterTick
                        < sfr$foodLevelBeforeTick;

        if (!foodChanged) {
            return;
        }

        if (player.isCreative()
                || player.isSpectator()) {
            return;
        }

        NutritionComponent component =
                NutritionStorage.get(player);

        component.decayAll();

        NutritionEvents.updateBuffs(
                player,
                component
        );

        NutritionEvents.sendNutritionUpdate(
                player,
                component
        );
    }
}