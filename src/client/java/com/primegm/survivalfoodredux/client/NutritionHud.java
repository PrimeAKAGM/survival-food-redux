package com.primegm.survivalfoodredux.client;

import com.primegm.survivalfoodredux.nutrition.FoodGroup;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class NutritionHud {

    private static final Identifier HUD_ID =
            Identifier.fromNamespaceAndPath(
                    "survival_food_redux",
                    "nutrition_hud"
            );

    private static final int HUD_HEIGHT = 10;

    private static final Item[] GROUP_ITEMS = {
            Items.COOKED_MUTTON,
            Items.CARROT,
            Items.HONEY_BOTTLE,
            Items.GOLDEN_APPLE
    };

    private static final RandomSource RANDOM =
            RandomSource.create();

    private NutritionHud() {
    }

    public static void register() {

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.FOOD_BAR,
                HUD_ID,
                (graphics, deltaTracker) -> render(graphics)
        );

        HudStatusBarHeightRegistry.addRight(
                HUD_ID,
                player -> shouldRender(player)
                        ? HUD_HEIGHT
                        : 0
        );
    }

    private static boolean shouldRender(Player player) {

        if (player == null) {
            return false;
        }

        if (player.isSpectator()) {
            return false;
        }

        return true;
    }

    private static void render(
            GuiGraphicsExtractor graphics
    ) {

        Player player = Minecraft.getInstance().player;

        if (!shouldRender(player)) {
            return;
        }

        int right =
                graphics.guiWidth() / 2 + 90;

        int top =
                graphics.guiHeight()
                        - HudStatusBarHeightRegistry.getHeight(HUD_ID)
                        - 3;

        renderGroup(
                graphics,
                right,
                top,
                ClientNutritionData.get(FoodGroup.PROTEIN),
                GROUP_ITEMS[0],
                player
        );

        renderGroup(
                graphics,
                right - 20,
                top,
                ClientNutritionData.get(FoodGroup.FIBER),
                GROUP_ITEMS[1],
                player
        );

        renderGroup(
                graphics,
                right - 40,
                top,
                ClientNutritionData.get(FoodGroup.SUGAR),
                GROUP_ITEMS[2],
                player
        );

        renderGroup(
                graphics,
                right - 60,
                top,
                ClientNutritionData.get(FoodGroup.FAT),
                GROUP_ITEMS[3],
                player
        );
    }

    private static void renderGroup(
            GuiGraphicsExtractor graphics,
            int right,
            int top,
            int value,
            Item item,
            Player player
    ) {

        int filled = getFilledIcons(value);

        if (filled <= 0) {
            return;
        }

        ItemStack icon = new ItemStack(item);

        boolean shake =
                value < 15
                        && player.tickCount % (value * 3 + 1) == 0;

        for (int i = 0; i < filled; i++) {

            int x =
                    right
                            - i * 5
                            - 9;

            int y = top;

            if (shake && i == 0) {
                y += RANDOM.nextInt(3) - 1;
            }

            graphics.pose().pushMatrix();

            graphics.pose().translate(
                    x + 4.5F,
                    y + 4.5F
            );

            graphics.pose().scale(
                    5.5F / 9.0F,
                    5.5F / 9.0F
            );

            graphics.pose().translate(
                    -4.5F,
                    -4.5F
            );

            graphics.item(
                    icon,
                    0,
                    0
            );

            graphics.pose().popMatrix();
        }
    }

    private static int getFilledIcons(int value) {

        if (value < 25) {
            return 1;
        }

        if (value < 45) {
            return 2;
        }

        if (value < 70) {
            return 3;
        }

        return 4;
    }
}