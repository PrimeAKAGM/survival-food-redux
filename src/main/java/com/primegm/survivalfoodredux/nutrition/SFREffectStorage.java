package com.primegm.survivalfoodredux.nutrition;

import net.minecraft.server.level.ServerPlayer;

public final class SFREffectStorage {

    private SFREffectStorage() {
    }

    public static SFREffectState get(ServerPlayer player) {
        return player.getAttachedOrCreate(
                NutritionAttachments.EFFECT_STATE,
                SFREffectState::empty
        );
    }

    public static void set(
            ServerPlayer player,
            SFREffectState state
    ) {
        player.setAttached(
                NutritionAttachments.EFFECT_STATE,
                state
        );
    }

    public static void remove(ServerPlayer player) {
        player.removeAttached(
                NutritionAttachments.EFFECT_STATE
        );
    }
}