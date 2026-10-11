package com.primegm.survivalfoodredux.nutrition;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Optional;

public final class SFREffects {

    private static final int FIBER_REGENERATION_THRESHOLD = 70;

    // Prevent internally constructed finite display effects from being
    // intercepted as new external potion applications by our own mixin.
    private static final ThreadLocal<Boolean> APPLYING_INTERNAL_EFFECT =
            ThreadLocal.withInitial(() -> false);

    private static void addInternalEffect(ServerPlayer player, MobEffectInstance effect) {
        boolean previous = APPLYING_INTERNAL_EFFECT.get();
        APPLYING_INTERNAL_EFFECT.set(true);
        try {
            player.addEffect(effect);
        } finally {
            APPLYING_INTERNAL_EFFECT.set(previous);
        }
    }

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
        reconcileStrengthWeakness(player);
        reconcileSpeedSlowness(player);
    }

    public static void removeAll(ServerPlayer player) {
        removeSugarDebuff(player);
        removeSugarRush(player);

        removeProteinDebuff(player);
        removeFiberDebuff(player);

        removeBalancedDiet(player);
        removeProteinTank(player);
        removeFiberRegeneration(player);
        reconcileStrengthWeakness(player);
        reconcileSpeedSlowness(player);
    }

    // Called after vanilla clears all effects (e.g. drinking milk).
    // The nutritional conditions remain, but every captured external
    // contribution must be discarded, including fully suppressed potions.
    // Reset ownership before rebuilding so no saved potion can be restored.
    public static void onAllEffectsCleared(ServerPlayer player) {
        SFREffectStorage.set(player, SFREffectState.empty());
        update(player, NutritionStorage.get(player));
    }

    // External finite-duration effects are captured before vanilla merges them
    // with SFR's infinite-duration effect. The mixin calls this method.
    public static boolean handleIncomingExternalEffect(
            ServerPlayer player, MobEffectInstance incoming
    ) {
        // SFR's own effects use infinite duration. Never intercept those.
        if (APPLYING_INTERNAL_EFFECT.get() || incoming.getDuration() <= 0) return false;

        SFREffectState state = SFREffectStorage.get(player);
        int slot = effectSlot(incoming);
        if (slot < 0) return false;
        boolean strengthPair = (slot == 2 || slot == 4)
                && (state.controllingStrength() || state.controllingWeakness());
        boolean speedPair = (slot == 0 || slot == 1)
                && (state.controllingSpeed() || state.controllingSlowness());
        boolean opposingPair = strengthPair || speedPair;
        if (!controls(state, slot) && !opposingPair) return false;

        MobEffectInstance saved = saved(state, slot).orElse(null);
        // A new external application replaces the stored contribution if
        // stronger, or if it renews an equal-strength effect.
        if (saved == null || incoming.getAmplifier() > saved.getAmplifier()
                || (incoming.getAmplifier() == saved.getAmplifier()
                && incoming.getDuration() > saved.getDuration())) {
            state = withSaved(state, slot, Optional.of(new MobEffectInstance(incoming)));
            SFREffectStorage.set(player, state);
        }
        if (strengthPair) reconcileStrengthWeakness(player);
        else if (speedPair) reconcileSpeedSlowness(player);
        else refreshCombinedEffect(player, slot, state);
        return true; // Vanilla must not put a second, hidden effect in the stack.
    }

    private static int effectSlot(MobEffectInstance effect) {
        if (effect.is(MobEffects.SPEED)) return 0;
        if (effect.is(MobEffects.SLOWNESS)) return 1;
        if (effect.is(MobEffects.WEAKNESS)) return 2;
        if (effect.is(MobEffects.HUNGER)) return 3;
        if (effect.is(MobEffects.STRENGTH)) return 4;
        if (effect.is(MobEffects.RESISTANCE)) return 5;
        if (effect.is(MobEffects.REGENERATION)) return 6;
        return -1;
    }

    private static boolean controls(SFREffectState s, int i) {
        return switch (i) {
            case 0 -> s.controllingSpeed();
            case 1 -> s.controllingSlowness();
            case 2 -> s.controllingWeakness();
            case 3 -> s.controllingHunger();
            case 4 -> s.controllingStrength();
            case 5 -> s.controllingResistance();
            case 6 -> s.controllingRegeneration();
            default -> false;
        };
    }

    private static Optional<MobEffectInstance> saved(SFREffectState s, int i) {
        return switch (i) {
            case 0 -> s.savedSpeed();
            case 1 -> s.savedSlowness();
            case 2 -> s.savedWeakness();
            case 3 -> s.savedHunger();
            case 4 -> s.savedStrength();
            case 5 -> s.savedResistance();
            case 6 -> s.savedRegeneration();
            default -> Optional.empty();
        };
    }

    private static SFREffectState withSaved(SFREffectState s, int i,
                                              Optional<MobEffectInstance> effect) {
        return new SFREffectState(
                s.controllingSpeed(), i == 0 ? effect : s.savedSpeed(),
                s.controllingSlowness(), i == 1 ? effect : s.savedSlowness(),
                s.controllingWeakness(), i == 2 ? effect : s.savedWeakness(),
                s.controllingHunger(), i == 3 ? effect : s.savedHunger(),
                s.controllingStrength(), i == 4 ? effect : s.savedStrength(),
                s.controllingResistance(), i == 5 ? effect : s.savedResistance(),
                s.controllingRegeneration(), i == 6 ? effect : s.savedRegeneration()
        );
    }

    // The displayed duration follows the captured external contribution.
    // A pure nutritional effect remains infinite.
    private static int displayedDuration(SFREffectState state, int... slots) {
        int duration = -1;
        for (int slot : slots) {
            Optional<MobEffectInstance> external = saved(state, slot);
            if (external.isPresent() && external.get().getDuration() > 0) {
                duration = Math.max(duration, external.get().getDuration());
            }
        }
        return duration;
    }

    // Avoid recreating a finite effect every tick. The vanilla instance already
    // counts down; one tick of difference is acceptable due to tick ordering.
    private static boolean matchesDisplayed(MobEffectInstance current,
                                            int amplifier, int duration) {
        if (current == null || current.getAmplifier() != amplifier) return false;
        if (duration == -1) return current.getDuration() == -1;
        return current.getDuration() > 0
                && Math.abs(current.getDuration() - duration) <= 2;
    }

    private static void refreshCombinedEffect(ServerPlayer player, int slot,
                                               SFREffectState state) {
        if ((slot == 0 || slot == 1)
                && (state.controllingSpeed() || state.controllingSlowness())) {
            reconcileSpeedSlowness(player);
            return;
        }
        if ((slot == 2 || slot == 4)
                && (state.controllingStrength() || state.controllingWeakness())) {
            reconcileStrengthWeakness(player);
            return;
        }
        var type = switch (slot) {
            case 0 -> MobEffects.SPEED;
            case 1 -> MobEffects.SLOWNESS;
            case 2 -> MobEffects.WEAKNESS;
            case 3 -> MobEffects.HUNGER;
            case 4 -> MobEffects.STRENGTH;
            case 5 -> MobEffects.RESISTANCE;
            case 6 -> MobEffects.REGENERATION;
            default -> throw new IllegalArgumentException("Unknown effect slot");
        };
        int amplifier = 0;
        Optional<MobEffectInstance> external = saved(state, slot);
        if (external.isPresent()) {
            int extAmp = external.get().getAmplifier();
            amplifier = Math.min(extAmp + 1, 1);
        }
        int duration = displayedDuration(state, slot);
        MobEffectInstance current = player.getEffect(type);
        if (matchesDisplayed(current, amplifier, duration)) return;
        player.removeEffect(type);
        addInternalEffect(player, new MobEffectInstance(type, duration, amplifier, false, true, true));
    }

    private static void tickSavedExternalEffects(ServerPlayer player) {
        SFREffectState state = SFREffectStorage.get(player);
        for (int i = 0; i < 7; i++) {
            if (!controls(state, i)
                    && !((i == 0 || i == 1)
                    && (state.controllingSpeed() || state.controllingSlowness()))
                    && !((i == 2 || i == 4)
                    && (state.controllingStrength() || state.controllingWeakness()))) continue;
            Optional<MobEffectInstance> stored = saved(state, i);
            if (stored.isEmpty()) continue;
            MobEffectInstance effect = stored.get();
            if (effect.getDuration() < 0) continue;
            int remaining = effect.getDuration() - 1;
            Optional<MobEffectInstance> next = remaining <= 0
                    ? Optional.empty()
                    : Optional.of(new MobEffectInstance(effect.getEffect(), remaining,
                            effect.getAmplifier(), effect.isAmbient(),
                            effect.isVisible(), effect.showIcon()));
            state = withSaved(state, i, next);
            SFREffectStorage.set(player, state);
            if (remaining <= 0) refreshCombinedEffect(player, i, state);
        }
    }

    // Strength <-> Weakness: net levels are shown only while SFR contributes.
    // The saved vanilla instances keep ticking even when fully suppressed.
    private static void reconcileStrengthWeakness(ServerPlayer player) {
        SFREffectState state = SFREffectStorage.get(player);
        boolean sfrStrength = state.controllingStrength();
        boolean sfrWeakness = state.controllingWeakness();
        MobEffectInstance shownStrength = player.getEffect(MobEffects.STRENGTH);
        MobEffectInstance shownWeakness = player.getEffect(MobEffects.WEAKNESS);

        if (!sfrStrength && !sfrWeakness) {
            // Restore a temporarily hidden opposing potion after SFR ends.
            if (state.savedStrength().isPresent()) {
                MobEffectInstance restored = state.savedStrength().get();
                if (shownStrength == null || shownStrength.getDuration() == -1) {
                    player.removeEffect(MobEffects.STRENGTH);
                    addInternalEffect(player, new MobEffectInstance(restored));
                }
                state = withSaved(state, 4, Optional.empty());
            }
            if (state.savedWeakness().isPresent()) {
                MobEffectInstance restored = state.savedWeakness().get();
                if (shownWeakness == null || shownWeakness.getDuration() == -1) {
                    player.removeEffect(MobEffects.WEAKNESS);
                    addInternalEffect(player, new MobEffectInstance(restored));
                }
                state = withSaved(state, 2, Optional.empty());
            }
            SFREffectStorage.set(player, state);
            return;
        }

        // An unowned finite effect may have been active before SFR started.
        if (!sfrStrength && state.savedStrength().isEmpty()
                && shownStrength != null && shownStrength.getDuration() > 0) {
            state = withSaved(state, 4, Optional.of(new MobEffectInstance(shownStrength)));
        }
        if (!sfrWeakness && state.savedWeakness().isEmpty()
                && shownWeakness != null && shownWeakness.getDuration() > 0) {
            state = withSaved(state, 2, Optional.of(new MobEffectInstance(shownWeakness)));
        }
        SFREffectStorage.set(player, state);

        int strength = (sfrStrength ? 1 : 0)
                + state.savedStrength().map(e -> Math.min(e.getAmplifier() + 1, 2)).orElse(0);
        int weakness = (sfrWeakness ? 1 : 0)
                + state.savedWeakness().map(e -> Math.min(e.getAmplifier() + 1, 2)).orElse(0);
        int net = strength - weakness;
        int strengthAmp = net > 0 ? Math.min(net, 2) - 1 : -1;
        int weaknessAmp = net < 0 ? Math.min(-net, 2) - 1 : -1;

        int strengthDuration = displayedDuration(state, 4, 2);
        if (strengthAmp < 0) {
            if (shownStrength != null) player.removeEffect(MobEffects.STRENGTH);
        } else if (!matchesDisplayed(shownStrength, strengthAmp, strengthDuration)) {
            player.removeEffect(MobEffects.STRENGTH);
            addInternalEffect(player, new MobEffectInstance(MobEffects.STRENGTH, strengthDuration,
                    strengthAmp, false, true, true));
        }
        int weaknessDuration = displayedDuration(state, 4, 2);
        if (weaknessAmp < 0) {
            if (shownWeakness != null) player.removeEffect(MobEffects.WEAKNESS);
        } else if (!matchesDisplayed(shownWeakness, weaknessAmp, weaknessDuration)) {
            player.removeEffect(MobEffects.WEAKNESS);
            addInternalEffect(player, new MobEffectInstance(MobEffects.WEAKNESS, weaknessDuration,
                    weaknessAmp, false, true, true));
        }
    }

    // Speed <-> Slowness: net levels are shown only while SFR contributes.
    // The saved vanilla instances keep ticking even when fully suppressed.
    private static void reconcileSpeedSlowness(ServerPlayer player) {
        SFREffectState state = SFREffectStorage.get(player);
        boolean sfrSpeed = state.controllingSpeed();
        boolean sfrSlowness = state.controllingSlowness();
        MobEffectInstance shownSpeed = player.getEffect(MobEffects.SPEED);
        MobEffectInstance shownSlowness = player.getEffect(MobEffects.SLOWNESS);

        if (!sfrSpeed && !sfrSlowness) {
            // Restore a temporarily hidden opposing potion after SFR ends.
            if (state.savedSpeed().isPresent()) {
                MobEffectInstance restored = state.savedSpeed().get();
                if (shownSpeed == null || shownSpeed.getDuration() == -1) {
                    player.removeEffect(MobEffects.SPEED);
                    addInternalEffect(player, new MobEffectInstance(restored));
                }
                state = withSaved(state, 0, Optional.empty());
            }
            if (state.savedSlowness().isPresent()) {
                MobEffectInstance restored = state.savedSlowness().get();
                if (shownSlowness == null || shownSlowness.getDuration() == -1) {
                    player.removeEffect(MobEffects.SLOWNESS);
                    addInternalEffect(player, new MobEffectInstance(restored));
                }
                state = withSaved(state, 1, Optional.empty());
            }
            SFREffectStorage.set(player, state);
            return;
        }

        // An unowned finite effect may have been active before SFR started.
        if (!sfrSpeed && state.savedSpeed().isEmpty()
                && shownSpeed != null && shownSpeed.getDuration() > 0) {
            state = withSaved(state, 0, Optional.of(new MobEffectInstance(shownSpeed)));
        }
        if (!sfrSlowness && state.savedSlowness().isEmpty()
                && shownSlowness != null && shownSlowness.getDuration() > 0) {
            state = withSaved(state, 1, Optional.of(new MobEffectInstance(shownSlowness)));
        }
        SFREffectStorage.set(player, state);

        int speed = (sfrSpeed ? 1 : 0)
                + state.savedSpeed().map(e -> Math.min(e.getAmplifier() + 1, 2)).orElse(0);
        int slowness = (sfrSlowness ? 1 : 0)
                + state.savedSlowness().map(e -> Math.min(e.getAmplifier() + 1, 2)).orElse(0);
        int net = speed - slowness;
        int speedAmp = net > 0 ? Math.min(net, 2) - 1 : -1;
        int slownessAmp = net < 0 ? Math.min(-net, 2) - 1 : -1;

        int speedDuration = displayedDuration(state, 0, 1);
        if (speedAmp < 0) {
            if (shownSpeed != null) player.removeEffect(MobEffects.SPEED);
        } else if (!matchesDisplayed(shownSpeed, speedAmp, speedDuration)) {
            player.removeEffect(MobEffects.SPEED);
            addInternalEffect(player, new MobEffectInstance(MobEffects.SPEED, speedDuration,
                    speedAmp, false, true, true));
        }
        int slownessDuration = displayedDuration(state, 0, 1);
        if (slownessAmp < 0) {
            if (shownSlowness != null) player.removeEffect(MobEffects.SLOWNESS);
        } else if (!matchesDisplayed(shownSlowness, slownessAmp, slownessDuration)) {
            player.removeEffect(MobEffects.SLOWNESS);
            addInternalEffect(player, new MobEffectInstance(MobEffects.SLOWNESS, slownessDuration,
                    slownessAmp, false, true, true));
        }
    }

    // ============================================================
    // EXTERNAL EFFECT MONITORING
    // ============================================================

    public static void checkExternalEffects(
            ServerPlayer player
    ) {
        SFREffectState state =
                SFREffectStorage.get(player);

        // Milk and other effect-clearing actions can remove effects
        // without changing our persisted ownership flags. If an effect
        // disappears, discard its saved external effect as well: it must
        // not be resurrected when the nutrition condition later ends.
        boolean speedPairActive = state.controllingSpeed() || state.controllingSlowness();
        boolean missingSpeed = state.controllingSpeed() && !speedPairActive
                && player.getEffect(MobEffects.SPEED) == null;
        boolean missingSlowness = state.controllingSlowness() && !speedPairActive
                && player.getEffect(MobEffects.SLOWNESS) == null;
        boolean pairActive = state.controllingStrength() || state.controllingWeakness();
        boolean missingWeakness = state.controllingWeakness() && !pairActive
                && player.getEffect(MobEffects.WEAKNESS) == null;
        boolean missingHunger = state.controllingHunger()
                && player.getEffect(MobEffects.HUNGER) == null;
        boolean missingStrength = state.controllingStrength() && !pairActive
                && player.getEffect(MobEffects.STRENGTH) == null;
        boolean missingResistance = state.controllingResistance()
                && player.getEffect(MobEffects.RESISTANCE) == null;
        boolean missingRegeneration = state.controllingRegeneration()
                && player.getEffect(MobEffects.REGENERATION) == null;

        if (missingSpeed || missingSlowness || missingWeakness
                || missingHunger || missingStrength || missingResistance
                || missingRegeneration) {

            SFREffectState repaired = new SFREffectState(
                    !missingSpeed && state.controllingSpeed(),
                    missingSpeed ? Optional.empty() : state.savedSpeed(),
                    !missingSlowness && state.controllingSlowness(),
                    missingSlowness ? Optional.empty() : state.savedSlowness(),
                    !missingWeakness && state.controllingWeakness(),
                    missingWeakness ? Optional.empty() : state.savedWeakness(),
                    !missingHunger && state.controllingHunger(),
                    missingHunger ? Optional.empty() : state.savedHunger(),
                    !missingStrength && state.controllingStrength(),
                    missingStrength ? Optional.empty() : state.savedStrength(),
                    !missingResistance && state.controllingResistance(),
                    missingResistance ? Optional.empty() : state.savedResistance(),
                    !missingRegeneration && state.controllingRegeneration(),
                    missingRegeneration ? Optional.empty() : state.savedRegeneration()
            );

            SFREffectStorage.set(player, repaired);
            update(player, NutritionStorage.get(player));
            state = SFREffectStorage.get(player);
        }

        tickSavedExternalEffects(player);
        state = SFREffectStorage.get(player);

        if (state.controllingSpeed() && !speedPairActive) {
            checkExternalSpeed(player, state);
        }

        if (state.controllingSlowness() && !speedPairActive) {
            checkExternalSlowness(player, state);
        }

        if (state.controllingWeakness() && !pairActive) {
            checkExternalWeakness(player, state);
        }

        if (state.controllingHunger()) {
            refreshCombinedEffect(player, 3, SFREffectStorage.get(player));
        }

        if (state.controllingStrength() && !pairActive) {
            checkExternalStrength(player, state);
        }

        if (state.controllingResistance()) {
            refreshCombinedEffect(player, 5, SFREffectStorage.get(player));
        }

        if (state.controllingRegeneration()) {
            refreshCombinedEffect(player, 6, SFREffectStorage.get(player));
        }
        reconcileStrengthWeakness(player);
        reconcileSpeedSlowness(player);
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

        addInternalEffect(player, new MobEffectInstance(
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

    private static void checkExternalSlowness(ServerPlayer player, SFREffectState state) {
        MobEffectInstance current = player.getEffect(MobEffects.SLOWNESS);
        if (current == null) return;

        int expectedAmplifier = state.savedSlowness()
                .map(effect -> Math.min(effect.getAmplifier() + 1, 1))
                .orElse(0);
        if (current.getDuration() == -1 && current.getAmplifier() == expectedAmplifier) return;

        MobEffectInstance external = new MobEffectInstance(current);
        int combinedAmplifier = Math.min(external.getAmplifier() + 1, 1);
        player.removeEffect(MobEffects.SLOWNESS);
        addInternalEffect(player, new MobEffectInstance(MobEffects.SLOWNESS, -1,
                combinedAmplifier, false, true, true));
        SFREffectStorage.set(player, withSaved(state, 1, Optional.of(external)));
    }

    // ============================================================
    // EXTERNAL WEAKNESS
    // ============================================================

    private static void checkExternalWeakness(ServerPlayer player, SFREffectState state) {
        MobEffectInstance current = player.getEffect(MobEffects.WEAKNESS);
        if (current == null) return;

        int expectedAmplifier = state.savedWeakness()
                .map(effect -> Math.min(effect.getAmplifier() + 1, 1))
                .orElse(0);
        if (current.getDuration() == -1 && current.getAmplifier() == expectedAmplifier) return;

        MobEffectInstance external = new MobEffectInstance(current);
        int combinedAmplifier = Math.min(external.getAmplifier() + 1, 1);
        player.removeEffect(MobEffects.WEAKNESS);
        addInternalEffect(player, new MobEffectInstance(MobEffects.WEAKNESS, -1,
                combinedAmplifier, false, true, true));
        SFREffectStorage.set(player, withSaved(state, 2, Optional.of(external)));
    }

    // ============================================================
    // EXTERNAL HUNGER
    // ============================================================

    private static void checkExternalHunger(ServerPlayer player, SFREffectState state) {
        MobEffectInstance current = player.getEffect(MobEffects.HUNGER);
        if (current == null) return;

        int expectedAmplifier = state.savedHunger()
                .map(effect -> Math.min(effect.getAmplifier() + 1, 1))
                .orElse(0);
        if (current.getDuration() == -1 && current.getAmplifier() == expectedAmplifier) return;

        MobEffectInstance external = new MobEffectInstance(current);
        int combinedAmplifier = Math.min(external.getAmplifier() + 1, 1);
        player.removeEffect(MobEffects.HUNGER);
        addInternalEffect(player, new MobEffectInstance(MobEffects.HUNGER, -1,
                combinedAmplifier, false, true, true));
        SFREffectStorage.set(player, withSaved(state, 3, Optional.of(external)));
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

        addInternalEffect(player, new MobEffectInstance(
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

        addInternalEffect(player, new MobEffectInstance(
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

        addInternalEffect(player, new MobEffectInstance(
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
                        NutritionRules.isDominant(
                                FoodGroup.SUGAR,
                        comp.get(FoodGroup.PROTEIN),
                                comp.get(FoodGroup.FIBER),
                                sugar,
                                comp.get(FoodGroup.FAT))
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

                if (current != null) {
                    player.removeEffect(MobEffects.SLOWNESS);
                }
                int combinedAmplifier = current == null
                        ? 0 : Math.min(current.getAmplifier() + 1, 1);
                addInternalEffect(player, new MobEffectInstance(
                        MobEffects.SLOWNESS,
                        -1,
                        combinedAmplifier,
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

        Optional<MobEffectInstance> externalToRestore = state.savedSlowness();

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

        player.removeEffect(MobEffects.SLOWNESS);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
        );
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
                NutritionRules.isDominant(
                                FoodGroup.SUGAR,
                        protein,
                        fiber,
                        sugar,
                        fat);

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

                addInternalEffect(player, new MobEffectInstance(
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

        Optional<MobEffectInstance> externalToRestore = state.savedSpeed();

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

        player.removeEffect(MobEffects.SPEED);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
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
                        NutritionRules.isDominant(
                                FoodGroup.PROTEIN,
                        protein,
                                comp.get(FoodGroup.FIBER),
                                comp.get(FoodGroup.SUGAR),
                                comp.get(FoodGroup.FAT))
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

                if (current != null) {
                    player.removeEffect(MobEffects.WEAKNESS);
                }
                int combinedAmplifier = current == null
                        ? 0 : Math.min(current.getAmplifier() + 1, 1);
                addInternalEffect(player, new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        -1,
                        combinedAmplifier,
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

        Optional<MobEffectInstance> externalToRestore = state.savedWeakness();

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

        player.removeEffect(MobEffects.WEAKNESS);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
        );
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
                        NutritionRules.isDominant(
                                FoodGroup.FIBER,
                        comp.get(FoodGroup.PROTEIN),
                                fiber,
                                comp.get(FoodGroup.SUGAR),
                                comp.get(FoodGroup.FAT))
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

                if (current != null) {
                    player.removeEffect(MobEffects.HUNGER);
                }
                int combinedAmplifier = current == null
                        ? 0 : Math.min(current.getAmplifier() + 1, 1);
                addInternalEffect(player, new MobEffectInstance(
                        MobEffects.HUNGER,
                        -1,
                        combinedAmplifier,
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

        Optional<MobEffectInstance> externalToRestore = state.savedHunger();

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

        player.removeEffect(MobEffects.HUNGER);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
        );
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

                addInternalEffect(player, new MobEffectInstance(
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

        Optional<MobEffectInstance> externalToRestore = state.savedStrength();

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

        player.removeEffect(MobEffects.STRENGTH);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
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
                NutritionRules.isDominant(
                                FoodGroup.PROTEIN,
                        protein,
                        comp.get(FoodGroup.FIBER),
                        comp.get(FoodGroup.SUGAR),
                        comp.get(FoodGroup.FAT));

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

                addInternalEffect(player, new MobEffectInstance(
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

        Optional<MobEffectInstance> externalToRestore = state.savedResistance();

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

        player.removeEffect(MobEffects.RESISTANCE);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
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
                NutritionRules.isDominant(
                                FoodGroup.FIBER,
                        comp.get(FoodGroup.PROTEIN),
                        fiber,
                        comp.get(FoodGroup.SUGAR),
                        comp.get(FoodGroup.FAT));

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

                addInternalEffect(player, new MobEffectInstance(
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

        Optional<MobEffectInstance> externalToRestore = state.savedRegeneration();

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

        player.removeEffect(MobEffects.REGENERATION);
        externalToRestore.ifPresent(effect ->
                addInternalEffect(player, new MobEffectInstance(effect))
        );
    }
}