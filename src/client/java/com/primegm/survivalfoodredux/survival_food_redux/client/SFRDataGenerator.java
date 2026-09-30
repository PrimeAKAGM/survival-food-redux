package com.primegm.survivalfoodredux.survival_food_redux.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class SFRDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(
            FabricDataGenerator fabricDataGenerator
    ) {
        FabricDataGenerator.Pack pack =
                fabricDataGenerator.createPack();
    }
}