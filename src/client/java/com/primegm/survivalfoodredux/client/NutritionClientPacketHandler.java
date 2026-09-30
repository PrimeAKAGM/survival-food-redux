package com.primegm.survivalfoodredux.client;

import com.primegm.survivalfoodredux.network.NutritionUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Handles nutrition update packets on the client side.
 * Receives nutrition sync packets from the server.
 */
public class NutritionClientPacketHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                NutritionUpdatePayload.TYPE,
                (payload, context) -> {

                    // Update client-side nutrition cache
                    ClientNutritionData.updateAll(
                            payload.getProtein(),
                            payload.getFiber(),
                            payload.getSugar(),
                            payload.getFat()
                    );
                }
        );
    }
}