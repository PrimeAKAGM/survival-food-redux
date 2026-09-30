package com.primegm.survivalfoodredux.nutrition;

import java.util.EnumMap;
import java.util.Map;

public class NutritionProfile {

    private final Map<FoodGroup, Integer> values =
            new EnumMap<>(FoodGroup.class);

    public NutritionProfile() {

        for (FoodGroup group : FoodGroup.values()) {

            if (group.isValid()) {
                values.put(group, 0);
            }
        }
    }

    public NutritionProfile(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        this();

        set(FoodGroup.PROTEIN, protein);
        set(FoodGroup.FIBER, fiber);
        set(FoodGroup.SUGAR, sugar);
        set(FoodGroup.FAT, fat);
    }

    public int get(FoodGroup group) {
        return values.getOrDefault(group, 0);
    }

    public boolean has(FoodGroup group) {
        return get(group) != 0;
    }

    public void set(
            FoodGroup group,
            int value
    ) {

        if (!group.isValid()) {
            return;
        }

        values.put(group, value);
    }

    public boolean isEmpty() {

        for (FoodGroup group : FoodGroup.values()) {

            if (group.isValid() && get(group) != 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {

        return String.format(
                "NutritionProfile[PROTEIN=%d, FIBER=%d, SUGAR=%d, FAT=%d]",
                get(FoodGroup.PROTEIN),
                get(FoodGroup.FIBER),
                get(FoodGroup.SUGAR),
                get(FoodGroup.FAT)
        );
    }
}