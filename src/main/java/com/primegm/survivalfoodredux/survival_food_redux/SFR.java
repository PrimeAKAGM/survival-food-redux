package com.primegm.survivalfoodredux.survival_food_redux;

import com.primegm.survivalfoodredux.network.NutritionUpdatePayload;
import com.primegm.survivalfoodredux.nutrition.FoodRegistry;
import com.primegm.survivalfoodredux.nutrition.NutritionAttachments;
import com.primegm.survivalfoodredux.nutrition.NutritionEvents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFR implements ModInitializer {

    public static final String MOD_ID = "survival_food_redux";
    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        LOGGER.info("Survival Food Redux: Booting...");

        FoodRegistry.init();
        NutritionAttachments.register();
        NutritionEvents.register();
        NutritionUpdatePayload.register();

        LOGGER.info("Survival Food Redux: All systems initialized!");
    }
}