package com.primegm.survivalfoodredux.nutrition;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;

public final class SFREffects {

    private static final int FIBER_REGENERATION_THRESHOLD = 70;

    private SFREffects() {
    }

    public static void update(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        updateSugarDebuff(player, comp);
        updateSugarRush(player, comp);

        updateProteinDebuff(player, comp);
        updateFiberDebuff(player, comp);

        updateBalancedDiet(player, comp);
        updateProteinTank(player, comp);
        updateFiberRegeneration(player, comp);
    }

    public static void removeAll(ServerPlayer player) {
        removeSugarDebuff(player);
        removeSugarRush(player);

        removeProteinDebuff(player);
        removeFiberDebuff(player);

        removeBalancedDiet(player);
        removeProteinTank(player);
        removeFiberRegeneration(player);
    }

    // ============================================================
    // EXTERNAL EFFECT MONITORING
    // ============================================================

    public static void checkExternalEffects(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (state.controllingSpeed()) {
            checkExternalSpeed(player, state);
        }

        if (state.controllingSlowness()) {
            checkExternalSlowness(player, state);
        }

        if (state.controllingWeakness()) {
            checkExternalWeakness(player, state);
        }

        if (state.controllingHunger()) {
            checkExternalHunger(player, state);
        }

        if (state.controllingStrength()) {
            checkExternalStrength(player, state);
        }

        if (state.controllingResistance()) {
            checkExternalResistance(player, state);
        }

        if (state.controllingRegeneration()) {
            checkExternalRegeneration(player, state);
        }
    }

    // ============================================================
    // EXTERNAL SPEED
    // ============================================================

    private static void checkExternalSpeed(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.SPEED);

        if (current == null) {
            return;
        }

        int expectedAmplifier;

        if (state.savedSpeed().isPresent()) {

            int savedAmplifier =
                    state.savedSpeed()
                            .get()
                            .getAmplifier();

            expectedAmplifier =
                    Math.min(savedAmplifier + 1, 1);

        } else {

            expectedAmplifier = 0;
        }

        boolean isOurEffect =
                current.getDuration() == -1
                        && current.getAmplifier() == expectedAmplifier;

        if (isOurEffect) {
            return;
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int combinedAmplifier =
                Math.min(
                        externalEffect.getAmplifier() + 1,
                        1
                );

        player.removeEffect(MobEffects.SPEED);

        player.addEffect(new MobEffectInstance(
                MobEffects.SPEED,
                -1,
                combinedAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        true,
                        Optional.of(externalEffect),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // EXTERNAL SLOWNESS
    // ============================================================

    private static void checkExternalSlowness(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.SLOWNESS);

        if (current == null) {
            return;
        }

        if (current.getDuration() == -1
                && current.getAmplifier() == 0) {
            return;
        }

        if (state.savedSlowness().isPresent()) {

            MobEffectInstance saved =
                    state.savedSlowness().get();

            int expectedAmplifier =
                    saved.getAmplifier();

            if (current.getDuration() == -1
                    && current.getAmplifier() == expectedAmplifier) {
                return;
            }
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int externalAmplifier =
                externalEffect.getAmplifier();

        player.removeEffect(MobEffects.SLOWNESS);

        player.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                -1,
                externalAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        true,
                        Optional.of(externalEffect),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // EXTERNAL WEAKNESS
    // ============================================================

    private static void checkExternalWeakness(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.WEAKNESS);

        if (current == null) {
            return;
        }

        if (current.getDuration() == -1
                && current.getAmplifier() == 0) {
            return;
        }

        if (state.savedWeakness().isPresent()) {

            MobEffectInstance saved =
                    state.savedWeakness().get();

            int expectedAmplifier =
                    saved.getAmplifier();

            if (current.getDuration() == -1
                    && current.getAmplifier() == expectedAmplifier) {
                return;
            }
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int externalAmplifier =
                externalEffect.getAmplifier();

        player.removeEffect(MobEffects.WEAKNESS);

        player.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                -1,
                externalAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        true,
                        Optional.of(externalEffect),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // EXTERNAL HUNGER
    // ============================================================

    private static void checkExternalHunger(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.HUNGER);

        if (current == null) {
            return;
        }

        if (current.getDuration() == -1
                && current.getAmplifier() == 0) {
            return;
        }

        if (state.savedHunger().isPresent()) {

            MobEffectInstance saved =
                    state.savedHunger().get();

            int expectedAmplifier =
                    saved.getAmplifier();

            if (current.getDuration() == -1
                    && current.getAmplifier() == expectedAmplifier) {
                return;
            }
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int externalAmplifier =
                externalEffect.getAmplifier();

        player.removeEffect(MobEffects.HUNGER);

        player.addEffect(new MobEffectInstance(
                MobEffects.HUNGER,
                -1,
                externalAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        true,
                        Optional.of(externalEffect),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // EXTERNAL STRENGTH
    // ============================================================

    private static void checkExternalStrength(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.STRENGTH);

        if (current == null) {
            return;
        }

        int expectedAmplifier;

        if (state.savedStrength().isPresent()) {

            int savedAmplifier =
                    state.savedStrength()
                            .get()
                            .getAmplifier();

            expectedAmplifier =
                    Math.min(savedAmplifier + 1, 1);

        } else {

            expectedAmplifier = 0;
        }

        boolean isOurEffect =
                current.getDuration() == -1
                        && current.getAmplifier() == expectedAmplifier;

        if (isOurEffect) {
            return;
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int combinedAmplifier =
                Math.min(
                        externalEffect.getAmplifier() + 1,
                        1
                );

        player.removeEffect(MobEffects.STRENGTH);

        player.addEffect(new MobEffectInstance(
                MobEffects.STRENGTH,
                -1,
                combinedAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        true,
                        Optional.of(externalEffect),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // EXTERNAL RESISTANCE
    // ============================================================

    private static void checkExternalResistance(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.RESISTANCE);

        if (current == null) {
            return;
        }

        int expectedAmplifier;

        if (state.savedResistance().isPresent()) {

            int savedAmplifier =
                    state.savedResistance()
                            .get()
                            .getAmplifier();

            expectedAmplifier =
                    Math.min(savedAmplifier + 1, 1);

        } else {

            expectedAmplifier = 0;
        }

        boolean isOurEffect =
                current.getDuration() == -1
                        && current.getAmplifier() == expectedAmplifier;

        if (isOurEffect) {
            return;
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int combinedAmplifier =
                Math.min(
                        externalEffect.getAmplifier() + 1,
                        1
                );

        player.removeEffect(MobEffects.RESISTANCE);

        player.addEffect(new MobEffectInstance(
                MobEffects.RESISTANCE,
                -1,
                combinedAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        true,
                        Optional.of(externalEffect),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // EXTERNAL REGENERATION
    // ============================================================

    private static void checkExternalRegeneration(
            ServerPlayer player,
            SFREffectState state
    ) {
        MobEffectInstance current =
                player.getEffect(MobEffects.REGENERATION);

        if (current == null) {
            return;
        }

        int expectedAmplifier;

        if (state.savedRegeneration().isPresent()) {

            int savedAmplifier =
                    state.savedRegeneration()
                            .get()
                            .getAmplifier();

            expectedAmplifier =
                    Math.min(savedAmplifier + 1, 1);

        } else {

            expectedAmplifier = 0;
        }

        boolean isOurEffect =
                current.getDuration() == -1
                        && current.getAmplifier() == expectedAmplifier;

        if (isOurEffect) {
            return;
        }

        MobEffectInstance externalEffect =
                new MobEffectInstance(current);

        int combinedAmplifier =
                Math.min(
                        externalEffect.getAmplifier() + 1,
                        1
                );

        player.removeEffect(MobEffects.REGENERATION);

        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                -1,
                combinedAmplifier,
                false,
                true,
                true
        ));

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        true,
                        Optional.of(externalEffect)
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // SUGAR - SLOWNESS
    // ============================================================

    private static void updateSugarDebuff(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        int sugar = comp.get(FoodGroup.SUGAR);

        boolean shouldHaveDebuff =
                NutritionRules.isDeficient(sugar)
                        || (
                        sugar > NutritionRules.DOMINANT_THRESHOLD
                                && NutritionRules.getDominantGroup(
                                comp.get(FoodGroup.PROTEIN),
                                comp.get(FoodGroup.FIBER),
                                sugar,
                                comp.get(FoodGroup.FAT)
                        ) == FoodGroup.SUGAR
                );

        SFREffectState state =
                SFREffectStorage.get(player);

        if (shouldHaveDebuff) {

            if (!state.controllingSlowness()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.SLOWNESS);

                Optional<MobEffectInstance> saved =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOWNESS,
                        -1,
                        0,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        true,
                        saved,

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

                SFREffectStorage.set(player, state);
            }

        } else {
            removeSugarDebuff(player);
        }
    }

    private static void removeSugarDebuff(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingSlowness()) {
            return;
        }

        player.removeEffect(MobEffects.SLOWNESS);

        state.savedSlowness().ifPresent(effect ->
                player.addEffect(new MobEffectInstance(effect))
        );

        state = new SFREffectState(
                state.controllingSpeed(),
                state.savedSpeed(),

                false,
                Optional.empty(),

                state.controllingWeakness(),
                state.savedWeakness(),

                state.controllingHunger(),
                state.savedHunger(),

                state.controllingStrength(),
                state.savedStrength(),

                state.controllingResistance(),
                state.savedResistance(),

                state.controllingRegeneration(),
                state.savedRegeneration()
        );

        SFREffectStorage.set(player, state);
    }

    // ============================================================
    // SUGAR - SPEED
    // ============================================================

    private static void updateSugarRush(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        int sugar = comp.get(FoodGroup.SUGAR);

        int protein = comp.get(FoodGroup.PROTEIN);
        int fiber = comp.get(FoodGroup.FIBER);
        int fat = comp.get(FoodGroup.FAT);

        int otherNutrientsAbove25 = 0;

        if (protein > NutritionRules.BALANCED_SECONDARY_THRESHOLD) {
            otherNutrientsAbove25++;
        }

        if (fiber > NutritionRules.BALANCED_SECONDARY_THRESHOLD) {
            otherNutrientsAbove25++;
        }

        if (fat > NutritionRules.BALANCED_SECONDARY_THRESHOLD) {
            otherNutrientsAbove25++;
        }

        boolean sugarDominant =
                sugar > NutritionRules.DOMINANT_THRESHOLD
                        && NutritionRules.getDominantGroup(
                        protein,
                        fiber,
                        sugar,
                        fat
                ) == FoodGroup.SUGAR;

        boolean sugarDeficient =
                NutritionRules.isDeficient(sugar);

        boolean shouldHaveBuff =
                sugar >= NutritionRules.SUGAR_RUSH_THRESHOLD
                        && otherNutrientsAbove25 >= 2
                        && !sugarDeficient
                        && !sugarDominant;

        SFREffectState state =
                SFREffectStorage.get(player);

        if (shouldHaveBuff) {

            if (!state.controllingSpeed()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.SPEED);

                Optional<MobEffectInstance> saved =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                int vanillaAmplifier =
                        current != null
                                ? current.getAmplifier()
                                : -1;

                int finalAmplifier =
                        Math.min(vanillaAmplifier + 1, 1);

                if (current != null) {
                    player.removeEffect(MobEffects.SPEED);
                }

                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED,
                        -1,
                        finalAmplifier,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        true,
                        saved,

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

                SFREffectStorage.set(player, state);
            }

        } else {
            removeSugarRush(player);
        }
    }

    private static void removeSugarRush(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingSpeed()) {
            return;
        }

        player.removeEffect(MobEffects.SPEED);

        if (state.savedSpeed().isPresent()) {

            MobEffectInstance saved =
                    state.savedSpeed().get();

            MobEffectInstance restored =
                    new MobEffectInstance(saved);

            player.addEffect(restored);
        }

        SFREffectState updated =
                new SFREffectState(
                        false,
                        Optional.empty(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // PROTEIN - WEAKNESS
    // ============================================================

    private static void updateProteinDebuff(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        int protein = comp.get(FoodGroup.PROTEIN);

        boolean shouldHaveDebuff =
                NutritionRules.isDeficient(protein)
                        || (
                        protein > NutritionRules.DOMINANT_THRESHOLD
                                && NutritionRules.getDominantGroup(
                                protein,
                                comp.get(FoodGroup.FIBER),
                                comp.get(FoodGroup.SUGAR),
                                comp.get(FoodGroup.FAT)
                        ) == FoodGroup.PROTEIN
                );

        SFREffectState state =
                SFREffectStorage.get(player);

        if (shouldHaveDebuff) {

            if (!state.controllingWeakness()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.WEAKNESS);

                Optional<MobEffectInstance> saved =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                player.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        -1,
                        0,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        true,
                        saved,

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

                SFREffectStorage.set(player, state);
            }

        } else {
            removeProteinDebuff(player);
        }
    }

    private static void removeProteinDebuff(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingWeakness()) {
            return;
        }

        player.removeEffect(MobEffects.WEAKNESS);

        state.savedWeakness().ifPresent(effect ->
                player.addEffect(new MobEffectInstance(effect))
        );

        state = new SFREffectState(
                state.controllingSpeed(),
                state.savedSpeed(),

                state.controllingSlowness(),
                state.savedSlowness(),

                false,
                Optional.empty(),

                state.controllingHunger(),
                state.savedHunger(),

                state.controllingStrength(),
                state.savedStrength(),

                state.controllingResistance(),
                state.savedResistance(),

                state.controllingRegeneration(),
                state.savedRegeneration()
        );

        SFREffectStorage.set(player, state);
    }

    // ============================================================
    // FIBER - HUNGER
    // ============================================================

    private static void updateFiberDebuff(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        int fiber = comp.get(FoodGroup.FIBER);

        boolean shouldHaveDebuff =
                NutritionRules.isDeficient(fiber)
                        || (
                        fiber > NutritionRules.DOMINANT_THRESHOLD
                                && NutritionRules.getDominantGroup(
                                comp.get(FoodGroup.PROTEIN),
                                fiber,
                                comp.get(FoodGroup.SUGAR),
                                comp.get(FoodGroup.FAT)
                        ) == FoodGroup.FIBER
                );

        SFREffectState state =
                SFREffectStorage.get(player);

        if (shouldHaveDebuff) {

            if (!state.controllingHunger()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.HUNGER);

                Optional<MobEffectInstance> saved =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                player.addEffect(new MobEffectInstance(
                        MobEffects.HUNGER,
                        -1,
                        0,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        true,
                        saved,

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

                SFREffectStorage.set(player, state);
            }

        } else {
            removeFiberDebuff(player);
        }
    }

    private static void removeFiberDebuff(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingHunger()) {
            return;
        }

        player.removeEffect(MobEffects.HUNGER);

        state.savedHunger().ifPresent(effect ->
                player.addEffect(new MobEffectInstance(effect))
        );

        state = new SFREffectState(
                state.controllingSpeed(),
                state.savedSpeed(),

                state.controllingSlowness(),
                state.savedSlowness(),

                state.controllingWeakness(),
                state.savedWeakness(),

                false,
                Optional.empty(),

                state.controllingStrength(),
                state.savedStrength(),

                state.controllingResistance(),
                state.savedResistance(),

                state.controllingRegeneration(),
                state.savedRegeneration()
        );

        SFREffectStorage.set(player, state);
    }

    // ============================================================
    // BALANCED DIET - STRENGTH
    // ============================================================

    private static void updateBalancedDiet(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        boolean balanced =
                NutritionRules.isBalanced(
                        comp.get(FoodGroup.PROTEIN),
                        comp.get(FoodGroup.FIBER),
                        comp.get(FoodGroup.SUGAR),
                        comp.get(FoodGroup.FAT)
                );

        SFREffectState state =
                SFREffectStorage.get(player);

        if (balanced) {

            if (!state.controllingStrength()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.STRENGTH);

                Optional<MobEffectInstance> saved =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                int vanillaAmplifier =
                        current != null
                                ? current.getAmplifier()
                                : -1;

                int finalAmplifier =
                        Math.min(vanillaAmplifier + 1, 1);

                if (current != null) {
                    player.removeEffect(MobEffects.STRENGTH);
                }

                player.addEffect(new MobEffectInstance(
                        MobEffects.STRENGTH,
                        -1,
                        finalAmplifier,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        true,
                        saved,

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

                SFREffectStorage.set(player, state);
            }

        } else {
            removeBalancedDiet(player);
        }
    }

    private static void removeBalancedDiet(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingStrength()) {
            return;
        }

        player.removeEffect(MobEffects.STRENGTH);

        if (state.savedStrength().isPresent()) {

            MobEffectInstance saved =
                    state.savedStrength().get();

            MobEffectInstance restored =
                    new MobEffectInstance(saved);

            player.addEffect(restored);
        }

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        false,
                        Optional.empty(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // PROTEIN TANK - RESISTANCE
    // ============================================================

    private static void updateProteinTank(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        int protein = comp.get(FoodGroup.PROTEIN);

        boolean proteinDominant =
                protein > NutritionRules.DOMINANT_THRESHOLD
                        && NutritionRules.getDominantGroup(
                        protein,
                        comp.get(FoodGroup.FIBER),
                        comp.get(FoodGroup.SUGAR),
                        comp.get(FoodGroup.FAT)
                ) == FoodGroup.PROTEIN;

        boolean shouldHaveBuff =
                protein >= NutritionRules.PROTEIN_TANK_THRESHOLD
                        && !NutritionRules.isDeficient(protein)
                        && !proteinDominant;

        SFREffectState state =
                SFREffectStorage.get(player);

        if (shouldHaveBuff) {

            if (!state.controllingResistance()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.RESISTANCE);

                Optional<MobEffectInstance> savedResistance =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                int vanillaAmplifier =
                        current != null
                                ? current.getAmplifier()
                                : -1;

                int finalAmplifier =
                        Math.min(vanillaAmplifier + 1, 1);

                if (current != null) {
                    player.removeEffect(MobEffects.RESISTANCE);
                }

                player.addEffect(new MobEffectInstance(
                        MobEffects.RESISTANCE,
                        -1,
                        finalAmplifier,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        true,
                        savedResistance,

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

                SFREffectStorage.set(player, state);
            }

        } else {
            removeProteinTank(player);
        }
    }

    private static void removeProteinTank(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingResistance()) {
            return;
        }

        player.removeEffect(MobEffects.RESISTANCE);

        if (state.savedResistance().isPresent()) {

            MobEffectInstance saved =
                    state.savedResistance().get();

            MobEffectInstance restored =
                    new MobEffectInstance(saved);

            player.addEffect(restored);
        }

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        false,
                        Optional.empty(),

                        state.controllingRegeneration(),
                        state.savedRegeneration()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }

    // ============================================================
    // FIBER - REGENERATION
    // ============================================================

    private static void updateFiberRegeneration(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        int fiber =
                comp.get(FoodGroup.FIBER);

        boolean fiberDominant =
                fiber > NutritionRules.DOMINANT_THRESHOLD
                        && NutritionRules.getDominantGroup(
                        comp.get(FoodGroup.PROTEIN),
                        fiber,
                        comp.get(FoodGroup.SUGAR),
                        comp.get(FoodGroup.FAT)
                ) == FoodGroup.FIBER;

        boolean shouldHaveBuff =
                fiber >= FIBER_REGENERATION_THRESHOLD
                        && !NutritionRules.isDeficient(fiber)
                        && !fiberDominant;

        SFREffectState state =
                SFREffectStorage.get(player);

        if (shouldHaveBuff) {

            if (!state.controllingRegeneration()) {

                MobEffectInstance current =
                        player.getEffect(MobEffects.REGENERATION);

                Optional<MobEffectInstance> savedRegeneration =
                        current != null
                                ? Optional.of(new MobEffectInstance(current))
                                : Optional.empty();

                int vanillaAmplifier =
                        current != null
                                ? current.getAmplifier()
                                : -1;

                int finalAmplifier =
                        Math.min(vanillaAmplifier + 1, 1);

                if (current != null) {
                    player.removeEffect(MobEffects.REGENERATION);
                }

                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        -1,
                        finalAmplifier,
                        false,
                        true,
                        true
                ));

                state = new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        true,
                        savedRegeneration
                );

                SFREffectStorage.set(
                        player,
                        state
                );
            }

        } else {
            removeFiberRegeneration(player);
        }
    }

    private static void removeFiberRegeneration(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        if (!state.controllingRegeneration()) {
            return;
        }

        player.removeEffect(
                MobEffects.REGENERATION
        );

        if (state.savedRegeneration().isPresent()) {

            MobEffectInstance saved =
                    state.savedRegeneration().get();

            MobEffectInstance restored =
                    new MobEffectInstance(saved);

            player.addEffect(restored);
        }

        SFREffectState updated =
                new SFREffectState(
                        state.controllingSpeed(),
                        state.savedSpeed(),

                        state.controllingSlowness(),
                        state.savedSlowness(),

                        state.controllingWeakness(),
                        state.savedWeakness(),

                        state.controllingHunger(),
                        state.savedHunger(),

                        state.controllingStrength(),
                        state.savedStrength(),

                        state.controllingResistance(),
                        state.savedResistance(),

                        false,
                        Optional.empty()
                );

        SFREffectStorage.set(
                player,
                updated
        );
    }
}