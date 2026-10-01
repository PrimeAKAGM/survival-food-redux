package com.primegm.survivalfoodredux.client;

import com.primegm.survivalfoodredux.nutrition.FoodGroup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Nutrition GUI Screen - Opens with "Z" key.
 * Shows nutrition bars for each food group in a centered panel.
 * Compact design with item icons and percentage displays.
 */
public class NutritionScreen extends Screen {

    // Panel dimensions
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 150;

    // Bar dimensions
    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 14;
    private static final int ROW_HEIGHT = 28;
    private static final int ICON_SIZE = 16;

    // Food group colors
    private static final int[] NUTRITION_COLORS = {
            0xFF996633, // Protein
            0xFF33CC33, // Fiber
            0xFF9933CC, // Sugar
            0xFFCC9933  // Fat
    };

    // Food group labels
    private static final String[] GROUP_LABELS = {
            "Protein",
            "Fiber",
            "Sugar",
            "Fat"
    };

    // Icons for each food group
    private static final ItemStack[] GROUP_ICONS = {
            new ItemStack(Items.COOKED_MUTTON), // Protein
            new ItemStack(Items.CARROT),        // Fiber
            new ItemStack(Items.HONEY_BOTTLE),  // Sugar
            new ItemStack(Items.GOLDEN_APPLE)   // Fat
    };

    public NutritionScreen() {
        super(Component.literal("Nutrition"));
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // Render background blur FIRST
        this.extractTransparentBackground(guiGraphics);

        // Call super.render AFTER background
        super.extractRenderState(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        int panelX =
                (this.width - PANEL_WIDTH) / 2;

        int panelY =
                (this.height - PANEL_HEIGHT) / 2;

        // Draw panel background with border
        drawPanelBackground(
                guiGraphics,
                panelX,
                panelY
        );

        // Draw title "Diet"
        Component title =
                Component.literal("Diet");

        int titleWidth =
                this.font.width(title);

        guiGraphics.text(
                this.font,
                title,
                panelX + (PANEL_WIDTH - titleWidth) / 2,
                panelY + 8,
                0xFFFFFFFF,
                false
        );

        // Draw separator line
        guiGraphics.fill(
                panelX + 15,
                panelY + 22,
                panelX + PANEL_WIDTH - 15,
                panelY + 23,
                0xFF555555
        );

        // Draw nutrition rows
        int startY = panelY + 35;

        for (int i = 0; i < 4; i++) {

            int y =
                    startY + (i * ROW_HEIGHT);

            int value =
                    getValueForFoodGroup(i);

            renderNutritionRow(
                    guiGraphics,
                    panelX + 15,
                    y,
                    GROUP_LABELS[i],
                    value,
                    NUTRITION_COLORS[i],
                    GROUP_ICONS[i]
            );
        }
    }

    private int getValueForFoodGroup(int index) {

        switch (index) {

            case 0: // Protein
                return ClientNutritionData.get(
                        FoodGroup.PROTEIN
                );

            case 1: // Fiber
                return ClientNutritionData.get(
                        FoodGroup.FIBER
                );

            case 2: // Sugar
                return ClientNutritionData.get(
                        FoodGroup.SUGAR
                );

            case 3: // Fat
                return ClientNutritionData.get(
                        FoodGroup.FAT
                );

            default:
                return 0;
        }
    }

    private void drawPanelBackground(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y
    ) {
        // Outer border (dark)
        guiGraphics.fill(
                x - 1,
                y - 1,
                x + PANEL_WIDTH + 1,
                y + PANEL_HEIGHT + 1,
                0xFF000000
        );

        // Main background - DARK THEME
        guiGraphics.fill(
                x,
                y,
                x + PANEL_WIDTH,
                y + PANEL_HEIGHT,
                0xE0202020
        );
    }

    private void renderNutritionRow(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            String label,
            int value,
            int color,
            ItemStack icon
    ) {
        // Draw the item icon
        guiGraphics.item(
                icon,
                x,
                y
        );

        // Draw label text
        int labelX =
                x + ICON_SIZE + 6;

        guiGraphics.text(
                this.font,
                label,
                labelX,
                y + 4,
                0xFFFFFFFF
        );

        // Draw nutrition bar
        int barX =
                labelX + 75;

        int barY =
                y + 2;

        drawNutritionBar(
                guiGraphics,
                barX,
                barY,
                value,
                color
        );

        // Draw percentage on the far right
        String percentageText =
                value + "%";

        int percentX =
                x + PANEL_WIDTH - 55;

        guiGraphics.text(
                this.font,
                percentageText,
                percentX,
                y + 4,
                0xFFFFFFFF
        );
    }

    private void drawNutritionBar(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            int value,
            int color
    ) {
        // Bar background border
        guiGraphics.fill(
                x - 1,
                y - 1,
                x + BAR_WIDTH + 1,
                y + BAR_HEIGHT + 1,
                0xFF000000
        );

        // Dark bar background
        guiGraphics.fill(
                x,
                y,
                x + BAR_WIDTH,
                y + BAR_HEIGHT,
                0xFF3A3A3A
        );

        // Calculate filled width
        int filledWidth =
                Math.max(
                        0,
                        (int) ((value / 100.0f) * BAR_WIDTH)
                );

        if (filledWidth > 0) {

            // Draw filled bar
            guiGraphics.fill(
                    x,
                    y,
                    x + filledWidth,
                    y + BAR_HEIGHT,
                    color
            );

            // Draw highlight stripe
            guiGraphics.fill(
                    x,
                    y,
                    x + filledWidth,
                    y + 2,
                    adjustBrightness(
                            color,
                            1.4f
                    )
            );

            // Draw shadow stripe
            guiGraphics.fill(
                    x,
                    y + BAR_HEIGHT - 2,
                    x + filledWidth,
                    y + BAR_HEIGHT,
                    adjustBrightness(
                            color,
                            0.6f
                    )
            );
        }

        // Draw threshold markers at 25%, 50%, 75%
        int[] thresholds = {
                25,
                50,
                75
        };

        for (int threshold : thresholds) {

            int markerX =
                    x + (int) (
                            (threshold / 100.0f)
                                    * BAR_WIDTH
                    );

            guiGraphics.fill(
                    markerX,
                    y,
                    markerX + 1,
                    y + BAR_HEIGHT,
                    0xAA000000
            );
        }
    }

    // Helper method to adjust color brightness
    private int adjustBrightness(
            int color,
            float factor
    ) {
        int a =
                (color >> 24) & 0xFF;

        int r =
                (color >> 16) & 0xFF;

        int g =
                (color >> 8) & 0xFF;

        int b =
                color & 0xFF;

        r =
                Math.min(
                        255,
                        (int) (r * factor)
                );

        g =
                Math.min(
                        255,
                        (int) (g * factor)
                );

        b =
                Math.min(
                        255,
                        (int) (b * factor)
                );

        return
                (a << 24)
                        | (r << 16)
                        | (g << 8)
                        | b;
    }
	
	@Override
	public boolean keyPressed(KeyEvent event) {

		// Close the nutrition screen using the same
		// configurable key that opens it.
		if (KeyInputHandler.NUTRITION_KEY.matches(event)) {
			this.onClose();
			return true;
		}

		return super.keyPressed(event);
	}
	
	@Override
	public boolean mouseClicked(
			MouseButtonEvent event,
			boolean doubleClick
	) {

		if (KeyInputHandler.NUTRITION_KEY.matchesMouse(event)) {
			this.onClose();
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}