package com.primegm.survivalfoodredux.survival_food_redux.client;

import com.primegm.survivalfoodredux.client.KeyInputHandler;
import com.primegm.survivalfoodredux.client.NutritionClientPacketHandler;
import com.primegm.survivalfoodredux.client.NutritionHud;
import net.fabricmc.api.ClientModInitializer;

public class SFRClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Register key input handler for nutrition GUI (Z key)
        KeyInputHandler.register();

        // Register nutrition network packet handler
        NutritionClientPacketHandler.register();

        // Register nutrition HUD
        NutritionHud.register();
    }
}