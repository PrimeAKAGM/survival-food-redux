package com.primegm.survivalfoodredux.network;

import com.primegm.survivalfoodredux.survival_food_redux.SFR;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Network packet for syncing nutrition data to the client.
 * Sent from server whenever nutrition values change.
 */
public class NutritionUpdatePayload implements CustomPacketPayload {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(
                    SFR.MOD_ID,
                    "nutrition_update"
            );

    // Codec for serialization/deserialization
    public static final StreamCodec<FriendlyByteBuf, NutritionUpdatePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    NutritionUpdatePayload::getProtein,
                    ByteBufCodecs.VAR_INT,
                    NutritionUpdatePayload::getFiber,
                    ByteBufCodecs.VAR_INT,
                    NutritionUpdatePayload::getSugar,
                    ByteBufCodecs.VAR_INT,
                    NutritionUpdatePayload::getFat,
                    NutritionUpdatePayload::new
            );

    public static final CustomPacketPayload.Type<NutritionUpdatePayload> TYPE =
            new CustomPacketPayload.Type<>(ID);

    private final int protein;
    private final int fiber;
    private final int sugar;
    private final int fat;

    public NutritionUpdatePayload(
            int protein,
            int fiber,
            int sugar,
            int fat
    ) {
        this.protein = protein;
        this.fiber = fiber;
        this.sugar = sugar;
        this.fat = fat;
    }

    @Override
    public CustomPacketPayload.Type<?> type() {
        return TYPE;
    }

    public int getProtein() {
        return protein;
    }

    public int getFiber() {
        return fiber;
    }

    public int getSugar() {
        return sugar;
    }

    public int getFat() {
        return fat;
    }

    /**
     * Register the payload type with Fabric networking.
     * Must be called during mod initialization.
     */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(
                TYPE,
                CODEC
        );
    }
}