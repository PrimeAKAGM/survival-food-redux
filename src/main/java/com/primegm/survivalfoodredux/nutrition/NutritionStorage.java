package com.primegm.survivalfoodredux.nutrition;

import net.minecraft.server.level.ServerPlayer;

public class NutritionStorage {

    public static NutritionComponent get(ServerPlayer player) {
        return player.getAttachedOrCreate(
                NutritionAttachments.NUTRITION,
                NutritionComponent::new
        );
    }

    public static void set(
            ServerPlayer player,
            NutritionComponent component
    ) {
        player.setAttached(
                NutritionAttachments.NUTRITION,
                component
        );
    }

    public static void remove(ServerPlayer player) {
        player.removeAttached(
                NutritionAttachments.NUTRITION
        );
    }
}