package com.primegm.survivalfoodredux.nutrition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Optional;

public record SFREffectState(
        boolean controllingSpeed,
        Optional<MobEffectInstance> savedSpeed,

        boolean controllingSlowness,
        Optional<MobEffectInstance> savedSlowness,

        boolean controllingWeakness,
        Optional<MobEffectInstance> savedWeakness,

        boolean controllingHunger,
        Optional<MobEffectInstance> savedHunger,

        boolean controllingStrength,
        Optional<MobEffectInstance> savedStrength,

        boolean controllingResistance,
        Optional<MobEffectInstance> savedResistance,

        boolean controllingRegeneration,
        Optional<MobEffectInstance> savedRegeneration
) {

    public static final Codec<SFREffectState> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(

                    Codec.BOOL.fieldOf("controlling_speed")
                            .forGetter(SFREffectState::controllingSpeed),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_speed")
                            .forGetter(SFREffectState::savedSpeed),

                    Codec.BOOL.fieldOf("controlling_slowness")
                            .forGetter(SFREffectState::controllingSlowness),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_slowness")
                            .forGetter(SFREffectState::savedSlowness),

                    Codec.BOOL.fieldOf("controlling_weakness")
                            .forGetter(SFREffectState::controllingWeakness),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_weakness")
                            .forGetter(SFREffectState::savedWeakness),

                    Codec.BOOL.fieldOf("controlling_hunger")
                            .forGetter(SFREffectState::controllingHunger),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_hunger")
                            .forGetter(SFREffectState::savedHunger),

                    Codec.BOOL.fieldOf("controlling_strength")
                            .forGetter(SFREffectState::controllingStrength),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_strength")
                            .forGetter(SFREffectState::savedStrength),

                    Codec.BOOL.fieldOf("controlling_resistance")
                            .forGetter(SFREffectState::controllingResistance),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_resistance")
                            .forGetter(SFREffectState::savedResistance),

                    Codec.BOOL.optionalFieldOf(
                                    "controlling_regeneration",
                                    false
                            )
                            .forGetter(SFREffectState::controllingRegeneration),

                    MobEffectInstance.CODEC.optionalFieldOf("saved_regeneration")
                            .forGetter(SFREffectState::savedRegeneration)

            ).apply(instance, SFREffectState::new));

    public static SFREffectState empty() {
        return new SFREffectState(
                false,
                Optional.empty(),

                false,
                Optional.empty(),

                false,
                Optional.empty(),

                false,
                Optional.empty(),

                false,
                Optional.empty(),

                false,
                Optional.empty(),

                false,
                Optional.empty()
        );
    }
}