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

    @Unique
    private float sfr$saturationLevelBeforeTick;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void sfr$beforeFoodTick(
            ServerPlayer player,
            CallbackInfo ci
    ) {
        FoodData foodData = (FoodData) (Object) this;

        sfr$foodLevelBeforeTick =
                foodData.getFoodLevel();

        sfr$saturationLevelBeforeTick =
                foodData.getSaturationLevel();
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void sfr$afterFoodTick(
            ServerPlayer player,
            CallbackInfo ci
    ) {
        FoodData foodData = (FoodData) (Object) this;

        int foodLevelAfterTick =
                foodData.getFoodLevel();

        float saturationLevelAfterTick =
                foodData.getSaturationLevel();

        boolean foodChanged =
                foodLevelAfterTick
                        < sfr$foodLevelBeforeTick;

        boolean saturationChanged =
                saturationLevelAfterTick
                        < sfr$saturationLevelBeforeTick;

        if (!foodChanged && !saturationChanged) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
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