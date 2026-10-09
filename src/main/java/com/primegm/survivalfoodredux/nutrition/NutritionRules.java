
package com.primegm.survivalfoodredux.nutrition;

public final class NutritionRules {

    public static final int BALANCED_DIET_THRESHOLD = 50;
    public static final int SUGAR_RUSH_THRESHOLD = 80;
    public static final int PROTEIN_TANK_THRESHOLD = 70;

    public static final int PROTEIN_GAIN = 15;
    public static final int FIBER_GAIN = 10;
    public static final int SUGAR_GAIN = 8;
    public static final int FAT_GAIN = 12;

    public static final int BALANCED_DIET_DURATION = 20 * 60 * 10;
    public static final int SUGAR_RUSH_DURATION = 20 * 60 * 10;
    public static final int PROTEIN_TANK_DURATION = 20 * 60 * 10;

    public static final int DECAY_LOW = 0;
    public static final int DECAY_MEDIUM = 1;
    public static final int DECAY_HIGH = 2;
    public static final int DECAY_VERY_HIGH = 3;

    public static final int DEFICIENCY_THRESHOLD = 15;
    public static final int DOMINANT_THRESHOLD = 45;
    public static final int BALANCED_MAIN_THRESHOLD = 35;
    public static final int BALANCED_SECONDARY_THRESHOLD = 25;

    private NutritionRules() {
    }

    public static int getGainForGroup(FoodGroup group) {
        return switch (group) {
            case PROTEIN -> PROTEIN_GAIN;
            case FIBER -> FIBER_GAIN;
            case SUGAR -> SUGAR_GAIN;
            case FAT -> FAT_GAIN;
            case UNKNOWN -> 0;
        };
    }

    public static int getDecayAmount(int value) {
        if (value <= 0) {
            return 0;
        } else if (value < 50) {
            return 1;
        } else if (value < 75) {
            return 2;
        } else {
            return 3;
        }
    }

    public static boolean isDeficient(int value) {
        return value < DEFICIENCY_THRESHOLD;
    }

    public static boolean isBalanced(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        int[] values = {
                protein,
                fiber,
                sugar,
                fat
        };

        int above35 = 0;
        int above25 = 0;

        for (int value : values) {

            if (value > BALANCED_MAIN_THRESHOLD) {
                above35++;
            }

            if (value > BALANCED_SECONDARY_THRESHOLD) {
                above25++;
            }
        }

        if (above35 < 1) {
            return false;
        }

        return above25 >= 3;
    }

    public static boolean isDominantDiet(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        return isDominant(
                FoodGroup.PROTEIN,
                protein, fiber, sugar, fat
        ) || isDominant(
                FoodGroup.FIBER,
                protein, fiber, sugar, fat
        ) || isDominant(
                FoodGroup.SUGAR,
                protein, fiber, sugar, fat
        ) || isDominant(
                FoodGroup.FAT,
                protein, fiber, sugar, fat
        );
    }

    public static boolean isDominant(
            FoodGroup group,
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        if (group == null || group == FoodGroup.UNKNOWN) {
            return false;
        }

        int[] values = {
                protein,
                fiber,
                sugar,
                fat
        };

        int groupIndex = switch (group) {
            case PROTEIN -> 0;
            case FIBER -> 1;
            case SUGAR -> 2;
            case FAT -> 3;
            case UNKNOWN -> -1;
        };

        if (groupIndex < 0) {
            return false;
        }

        // The nutrient must be strictly above 45%.
        if (values[groupIndex] <= DOMINANT_THRESHOLD) {
            return false;
        }

        // At least two of the other three nutrients
        // must be strictly below 25%.
        int othersBelow25 = 0;

        for (int i = 0; i < values.length; i++) {

            if (i == groupIndex) {
                continue;
            }

            if (values[i] < BALANCED_SECONDARY_THRESHOLD) {
                othersBelow25++;
            }
        }

        return othersBelow25 >= 2;
    }

    public static FoodGroup getDominantGroup(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        FoodGroup[] groups = {
                FoodGroup.PROTEIN,
                FoodGroup.FIBER,
                FoodGroup.SUGAR,
                FoodGroup.FAT
        };

        for (FoodGroup group : groups) {

            if (isDominant(
                    group,
                    protein,
                    fiber,
                    sugar,
                    fat
            )) {
                return group;
            }
        }

        return FoodGroup.UNKNOWN;
    }
}
