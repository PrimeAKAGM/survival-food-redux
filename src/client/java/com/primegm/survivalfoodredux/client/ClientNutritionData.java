package com.primegm.survivalfoodredux.client;

import com.primegm.survivalfoodredux.nutrition.FoodGroup;

import java.util.EnumMap;
import java.util.Map;

/**
 * Client-side cached nutrition data for the local player.
 * Synced from server via network packets.
 */
public class ClientNutritionData {

    private static final Map<FoodGroup, Integer> NUTRITION_VALUES =
            new EnumMap<>(FoodGroup.class);

    static {
        // Initialize all groups to 0
        for (FoodGroup group : FoodGroup.values()) {
            if (group.isValid()) {
                NUTRITION_VALUES.put(group, 0);
            }
        }
    }

    /**
     * Get the nutrition value for a food group.
     *
     * @param group the food group
     * @return the nutrition value (0-100)
     */
    public static int get(FoodGroup group) {
        return NUTRITION_VALUES.getOrDefault(group, 0);
    }

    /**
     * Set the nutrition value for a food group.
     *
     * @param group the food group
     * @param value the value (0-100)
     */
    public static void set(FoodGroup group, int value) {
        NUTRITION_VALUES.put(
                group,
                Math.max(0, Math.min(100, value))
        );
    }

    /**
     * Update all nutrition values at once.
     *
     * @param protein protein value
     * @param fiber fiber value
     * @param sugar sugar value
     * @param fat fat value
     */
    public static void updateAll(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        set(FoodGroup.PROTEIN, protein);
        set(FoodGroup.FIBER, fiber);
        set(FoodGroup.SUGAR, sugar);
        set(FoodGroup.FAT, fat);
    }

    /**
     * Reset all nutrition values to 0.
     */
    public static void reset() {
        for (FoodGroup group : FoodGroup.values()) {
            if (group.isValid()) {
                NUTRITION_VALUES.put(group, 0);
            }
        }
    }
}