package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class RoutersModelProvider extends ModelProvider {

    public RoutersModelProvider(PackOutput output) {
        super(output, Routers.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        //Items
        itemModels.generateFlatItem(RoutersItems.CONNECTOR.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModels.generateFlatItem(RoutersItems.RF_UPGRADE_1.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.RF_UPGRADE_2.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.RF_UPGRADE_3.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.RF_UPGRADE_4.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.RF_UPGRADE_5.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(RoutersItems.ITEM_UPGRADE_1.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.ITEM_UPGRADE_2.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.ITEM_UPGRADE_3.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.ITEM_UPGRADE_4.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.ITEM_UPGRADE_5.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(RoutersItems.FLUID_UPGRADE_1.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.FLUID_UPGRADE_2.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.FLUID_UPGRADE_3.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.FLUID_UPGRADE_4.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.FLUID_UPGRADE_5.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(RoutersItems.SPEED_UPGRADE_1.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.SPEED_UPGRADE_2.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.SPEED_UPGRADE_3.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.SPEED_UPGRADE_4.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(RoutersItems.ROUND_ROBIN_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.MOD_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.TAG_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.STOCK_FILTER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.DIMENSIONAL_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.BLACKLIST_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RoutersItems.IGNORE_NBT_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    protected @NotNull Stream<? extends Holder<Block>> getKnownBlocks() {
        return RoutersBlocks.BLOCKS.getEntries().stream().filter(x ->
                !x.is(RoutersBlocks.EXPORTER.getId()) &&
                !x.is(RoutersBlocks.IMPORTER.getId()) &&
                !x.is(RoutersBlocks.DISTRIBUTOR.getId())
        );
    }

    @Override
    protected @NotNull Stream<? extends Holder<Item>> getKnownItems() {
        return RoutersItems.ITEMS.getEntries().stream();
    }

    @Override
    public @NotNull String getName() {
        return Routers.MOD_ID + " Models";
    }
}