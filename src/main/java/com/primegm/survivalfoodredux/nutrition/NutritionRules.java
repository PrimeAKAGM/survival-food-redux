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
        return getDominantGroup(
                protein,
                fiber,
                sugar,
                fat
        ) != FoodGroup.UNKNOWN;
    }

    public static FoodGroup getDominantGroup(
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

        FoodGroup[] groups = {
                FoodGroup.PROTEIN,
                FoodGroup.FIBER,
                FoodGroup.SUGAR,
                FoodGroup.FAT
        };

        for (int i = 0; i < values.length; i++) {

            /*
             * The nutrient itself must be strictly above 45.
             */
            if (values[i] <= DOMINANT_THRESHOLD) {
                continue;
            }

            /*
             * A nutrient is dominant when at least two
             * of the other three nutrients are strictly
             * below 25.
             */
            int othersBelow25 = 0;

            for (int j = 0; j < values.length; j++) {

                if (i == j) {
                    continue;
                }

                if (values[j] < BALANCED_SECONDARY_THRESHOLD) {
                    othersBelow25++;
                }
            }

            if (othersBelow25 >= 2) {
                return groups[i];
            }
        }

        return FoodGroup.UNKNOWN;
    }
}