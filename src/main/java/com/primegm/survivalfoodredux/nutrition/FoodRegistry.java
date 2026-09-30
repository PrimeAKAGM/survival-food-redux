package com.primegm.survivalfoodredux.nutrition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FoodRegistry {

    private static final Map<Item, NutritionProfile> FOOD_MAP = new HashMap<>();

    public static void init() {

        // =========================
        // MEAT & FISH
        // =========================

        register(Items.COOKED_BEEF, profile(15, 0, 0, 5));
        register(Items.COOKED_PORKCHOP, profile(15, 0, 0, 8));
        register(Items.COOKED_CHICKEN, profile(15, 0, 0, 3));
        register(Items.COOKED_MUTTON, profile(15, 0, 0, 7));
        register(Items.COOKED_RABBIT, profile(15, 0, 0, 2));
        register(Items.COOKED_COD, profile(15, 0, 0, 2));
        register(Items.COOKED_SALMON, profile(15, 0, 0, 6));

        register(Items.BEEF, profile(5, 0, 0, 5));
        register(Items.PORKCHOP, profile(5, 0, 0, 8));
        register(Items.CHICKEN, profile(5, 0, 0, 3));
        register(Items.MUTTON, profile(5, 0, 0, 7));
        register(Items.RABBIT, profile(5, 0, 0, 2));
        register(Items.COD, profile(5, 0, 0, 2));
        register(Items.SALMON, profile(5, 0, 0, 6));
        register(Items.TROPICAL_FISH, profile(5, 0, 0, 3));

        register(Items.RABBIT_STEW, profile(15, 10, 0, 5));
        register(Items.MUSHROOM_STEW, profile(2, 10, 0, 3));
        register(Items.BEETROOT_SOUP, profile(1, 10, 2, 1));
        register(Items.SUSPICIOUS_STEW, profile(2, 10, 2, 2));

        // =========================
        // FRUITS & VEGETABLES
        // =========================

        register(Items.CARROT, profile(0, 10, 2, 0));
        register(Items.GOLDEN_CARROT, profile(0, 10, 3, 2));

        register(Items.POTATO, profile(0, 8, 1, 1));
        register(Items.BAKED_POTATO, profile(0, 10, 1, 2));

        register(Items.BEETROOT, profile(0, 8, 2, 0));

        register(Items.APPLE, profile(0, 8, 3, 0));

        register(Items.MELON_SLICE, profile(0, 6, 4, 0));

        register(Items.DRIED_KELP, profile(0, 5, 0, 0));

        register(Items.SWEET_BERRIES, profile(0, 5, 6, 0));
        register(Items.GLOW_BERRIES, profile(0, 5, 6, 0));

        // =========================
        // BASIC FOODS
        // =========================

        register(Items.BREAD, profile(2, 10, 1, 0));
        register(Items.COOKIE, profile(1, 1, 8, 2));
        register(Items.CAKE, profile(2, 3, 8, 5));
        register(Items.PUMPKIN_PIE, profile(0, 5, 8, 5));
        register(Items.HONEY_BOTTLE, profile(0, 0, 10, 0));

        // =========================
        // SPECIAL FOODS
        // =========================

        register(Items.CHORUS_FRUIT, profile(0, 0, 8, 0));

        register(Items.GOLDEN_APPLE, profile(0, 8, 5, 12));
        register(Items.ENCHANTED_GOLDEN_APPLE, profile(0, 10, 8, 15));

        register(Items.SPIDER_EYE, profile(5, 0, 0, 8));
        register(Items.ROTTEN_FLESH, profile(5, 0, 0, 8));
        register(Items.POISONOUS_POTATO, profile(0, 8, 2, 1));
        register(Items.PUFFERFISH, profile(8, 0, 0, 4));
    }

    /**
     * Creates a nutrition profile.
     *
     * Order:
     * Protein, Fiber, Sugar, Fat
     */
    private static NutritionProfile profile(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        return new NutritionProfile(
                protein,
                fiber,
                sugar,
                fat
        );
    }

    /**
     * Registers a food with its nutrition profile.
     */
    public static void register(
            Item item,
            NutritionProfile profile
    ) {
        FOOD_MAP.put(item, profile);
    }

    /**
     * Returns the nutrition profile of a food.
     */
    public static NutritionProfile get(Item item) {
        return FOOD_MAP.getOrDefault(
                item,
                new NutritionProfile()
        );
    }

    /**
     * Returns the nutrition profile as an Optional.
     */
    public static Optional<NutritionProfile> getOptional(Item item) {
        return Optional.ofNullable(
                FOOD_MAP.get(item)
        );
    }

    /**
     * Checks whether an item is registered as food.
     */
    public static boolean isRegistered(Item item) {
        return FOOD_MAP.containsKey(item);
    }

    /**
     * Returns the registry key of an item.
     */
    public static String getItemKey(Item item) {
        return BuiltInRegistries.ITEM
                .getKey(item)
                .toString();
    }
}