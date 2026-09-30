package com.primegm.survivalfoodredux.nutrition;

import com.primegm.survivalfoodredux.network.NutritionUpdatePayload;
import com.primegm.survivalfoodredux.survival_food_redux.SFR;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class NutritionEvents {

    private static final int STARTING_NUTRITION = 30;

    public static void register() {

        registerEffectMonitoring();

        ServerPlayerEvents.JOIN.register(player -> {

            boolean isNewNutrition =
                    player.getAttached(
                            NutritionAttachments.NUTRITION
                    ) == null;

            if (isNewNutrition) {

                NutritionComponent component =
                        new NutritionComponent();

                component.set(
                        FoodGroup.PROTEIN,
                        STARTING_NUTRITION
                );

                component.set(
                        FoodGroup.FIBER,
                        STARTING_NUTRITION
                );

                component.set(
                        FoodGroup.SUGAR,
                        STARTING_NUTRITION
                );

                component.set(
                        FoodGroup.FAT,
                        STARTING_NUTRITION
                );

                NutritionStorage.set(
                        player,
                        component
                );

                SFR.LOGGER.info(
                        "Initialized nutrition for {}: {}",
                        player.getName().getString(),
                        component
                );
            }

            NutritionComponent comp =
                    NutritionStorage.get(player);

            SFR.LOGGER.info(
                    "JOIN nutrition for {}: {}",
                    player.getName().getString(),
                    comp
            );

            updateBuffs(player, comp);

            sendNutritionUpdate(player, comp);
        });
    }

    public static void processFood(
            ServerPlayer player,
            ItemStack stack
    ) {

        if (stack.isEmpty()) {
            return;
        }

        NutritionProfile profile =
                FoodRegistry.get(stack.getItem());

        if (profile.isEmpty()) {

            SFR.LOGGER.debug(
                    "Unregistered food: {}",
                    FoodRegistry.getItemKey(stack.getItem())
            );

            return;
        }

        NutritionComponent comp =
                player.modifyAttached(
                        NutritionAttachments.NUTRITION,
                        current -> {

                            if (current == null) {
                                current = new NutritionComponent();
                            }

                            for (FoodGroup group :
                                    FoodGroup.values()) {

                                if (!group.isValid()) {
                                    continue;
                                }

                                if (profile.has(group)) {

                                    current.add(
                                            group,
                                            profile.get(group)
                                    );
                                }
                            }

                            return current;
                        }
                );

        updateBuffs(player, comp);
        updateDebuffs(player, comp);

        sendNutritionUpdate(player, comp);

        SFR.LOGGER.debug(
                "Player {} nutrition: {}",
                player.getName().getString(),
                comp
        );
    }

    private static void registerEffectMonitoring() {

        ServerTickEvents.END_LEVEL_TICK.register(
                (ServerLevel level) -> {

                    for (ServerPlayer player : level.players()) {
                        SFREffects.checkExternalEffects(player);
                    }
                }
        );
    }

    public static void sendNutritionUpdate(
            ServerPlayer player,
            NutritionComponent comp
    ) {

        NutritionUpdatePayload payload =
                new NutritionUpdatePayload(
                        comp.get(FoodGroup.PROTEIN),
                        comp.get(FoodGroup.FIBER),
                        comp.get(FoodGroup.SUGAR),
                        comp.get(FoodGroup.FAT)
                );

        ServerPlayNetworking.send(
                player,
                payload
        );
    }

    public static void updateBuffs(
            ServerPlayer player,
            NutritionComponent comp
    ) {

        SFREffects.update(
                player,
                comp
        );

        SFR.LOGGER.info(
                "EFFECT CHECK - {} | Protein={} Fiber={} Sugar={} Fat={}",
                player.getName().getString(),
                comp.get(FoodGroup.PROTEIN),
                comp.get(FoodGroup.FIBER),
                comp.get(FoodGroup.SUGAR),
                comp.get(FoodGroup.FAT)
        );
    }

    public static void updateDebuffs(
            ServerPlayer player,
            NutritionComponent comp
    ) {
        // Intentionally empty.
    }
}