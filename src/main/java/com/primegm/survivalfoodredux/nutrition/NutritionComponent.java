package com.primegm.survivalfoodredux.nutrition;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks nutrition values per food group for a single player.
 * Values are clamped between 0 and 100.
 */
public class NutritionComponent {

    public static final Codec<NutritionComponent> CODEC = Codec.INT
            .listOf(4, 4)
            .xmap(
                    values -> {
                        NutritionComponent component =
                                new NutritionComponent();

                        component.set(
                                FoodGroup.PROTEIN,
                                values.get(0)
                        );

                        component.set(
                                FoodGroup.FIBER,
                                values.get(1)
                        );

                        component.set(
                                FoodGroup.SUGAR,
                                values.get(2)
                        );

                        component.set(
                                FoodGroup.FAT,
                                values.get(3)
                        );

                        return component;
                    },
                    component -> java.util.List.of(
                            component.get(FoodGroup.PROTEIN),
                            component.get(FoodGroup.FIBER),
                            component.get(FoodGroup.SUGAR),
                            component.get(FoodGroup.FAT)
                    )
            );

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 100;

    private final Map<FoodGroup, Integer> values =
            new EnumMap<>(FoodGroup.class);

    public NutritionComponent() {

        // Initialize all groups to 0
        for (FoodGroup group : FoodGroup.values()) {

            if (group.isValid()) {
                values.put(group, 0);
            }
        }
    }

    /**
     * Add nutrition to a food group, clamped to 0-100.
     *
     * @param group the food group
     * @param amount the amount to add (can be negative for decay)
     */
    public void add(
            FoodGroup group,
            int amount
    ) {

        if (!group.isValid()) {
            return;
        }

        int current =
                values.getOrDefault(group, 0);

        values.put(
                group,
                Mth.clamp(
                        current + amount,
                        MIN_VALUE,
                        MAX_VALUE
                )
        );
    }

    /**
     * Get the current value for a food group.
     *
     * @param group the food group
     * @return the nutrition value (0-100)
     */
    public int get(FoodGroup group) {
        return values.getOrDefault(group, 0);
    }

    /**
     * Set a specific value for a food group.
     *
     * @param group the food group
     * @param value the value to set (will be clamped)
     */
    public void set(
            FoodGroup group,
            int value
    ) {

        if (!group.isValid()) {
            return;
        }

        values.put(
                group,
                Mth.clamp(
                        value,
                        MIN_VALUE,
                        MAX_VALUE
                )
        );
    }

    /**
     * Decay all nutrition values according to NutritionRules.
     */
    public void decayAll() {

        for (FoodGroup group : FoodGroup.values()) {

            if (group.isValid()) {

                int current =
                        get(group);

                int decay =
                        NutritionRules.getDecayAmount(current);

                add(
                        group,
                        -decay
                );
            }
        }
    }

    /**
     * Reset all nutrition values to 0.
     */
    public void reset() {

        for (FoodGroup group : FoodGroup.values()) {

            if (group.isValid()) {
                values.put(group, 0);
            }
        }
    }

    @Override
    public String toString() {

        return String.format(
                "Nutrition[PROTEIN=%d, FIBER=%d, SUGAR=%d, FAT=%d]",
                get(FoodGroup.PROTEIN),
                get(FoodGroup.FIBER),
                get(FoodGroup.SUGAR),
                get(FoodGroup.FAT)
        );
    }
}