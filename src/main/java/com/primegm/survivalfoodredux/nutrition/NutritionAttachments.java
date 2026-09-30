package com.primegm.survivalfoodredux.nutrition;

import com.primegm.survivalfoodredux.survival_food_redux.SFR;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public class NutritionAttachments {

    public static final AttachmentType<NutritionComponent> NUTRITION =
            AttachmentRegistry.createPersistent(
                    Identifier.fromNamespaceAndPath(
                            SFR.MOD_ID,
                            "nutrition"
                    ),
                    NutritionComponent.CODEC
            );

    public static final AttachmentType<SFREffectState> EFFECT_STATE =
            AttachmentRegistry.createPersistent(
                    Identifier.fromNamespaceAndPath(
                            SFR.MOD_ID,
                            "effect_state"
                    ),
                    SFREffectState.CODEC
            );

    public static void register() {
        SFR.LOGGER.info("Nutrition attachment registered!");
    }
}