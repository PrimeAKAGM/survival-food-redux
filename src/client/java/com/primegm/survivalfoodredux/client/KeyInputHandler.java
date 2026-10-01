package com.primegm.survivalfoodredux.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Handles key input for the nutrition system.
 * Registers "Z" key to open the nutrition screen.
 */
public class KeyInputHandler {

    public static final String KEY_NUTRITION =
            "key.survival_food_redux.nutrition";

    public static KeyMapping NUTRITION_KEY;

    public static void register() {
        System.out.println(
                "[Survival Food Redux] KeyInputHandler.register() called"
        );

        // Register the Z key binding using Category.MISC
        NUTRITION_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        KEY_NUTRITION,
                        90,
                        KeyMapping.Category.MISC
                )
        );

        System.out.println(
                "[Survival Food Redux] Registered key: "
                        + NUTRITION_KEY.getName()
        );

        System.out.println(
                "[Survival Food Redux] Options exists: "
                        + (Minecraft.getInstance().options != null)
        );

        if (Minecraft.getInstance().options != null) {

            System.out.println(
                    "[Survival Food Redux] Options keyMappings: "
                            + Minecraft.getInstance().options.keyMappings.length
            );

            boolean found = false;

            for (KeyMapping key :
                    Minecraft.getInstance().options.keyMappings) {

                if (key == NUTRITION_KEY) {
                    found = true;
                    break;
                }
            }

            System.out.println(
                    "[Survival Food Redux] Nutrition key in Options: "
                            + found
            );
        }

        // Listen for key press each tick
        // Listen for key press each tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (NUTRITION_KEY.consumeClick()) {
				toggleNutritionScreen(client);
			}
		});
    }

    private static void toggleNutritionScreen(
        Minecraft client
	) {
		if (client.player == null) {
			return;
		}

		// Close the nutrition screen if it is already open
		if (client.gui.screen() instanceof NutritionScreen) {
			client.setScreenAndShow(null);
			return;
		}

		// Open the nutrition screen if no other screen is open
		if (client.gui.screen() == null) {
			client.setScreenAndShow(
					new NutritionScreen()
			);
		}
	}
}