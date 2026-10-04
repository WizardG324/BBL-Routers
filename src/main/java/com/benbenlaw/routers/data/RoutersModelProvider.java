package com.benbenlaw.routers.data;

import com.benbenlaw.core.block.SyncableBlock;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.minecraft.client.data.models.BlockModelGenerators.*;
import static net.minecraft.client.data.models.BlockModelGenerators.X_ROT_180;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_180;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_270;
import static net.minecraft.client.data.models.BlockModelGenerators.Y_ROT_90;

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


        createMachineBlock(RoutersBlocks.ROUTER_MANAGER.get(), blockModels.blockStateOutput, blockModels.modelOutput);

    }

    public void createMachineBlock(Block block, Consumer<BlockModelDefinitionGenerator> blockStateOutput, BiConsumer<Identifier, ModelInstance> modelOutput) {
        TextureMapping idleTextureMapping = (new TextureMapping()).put(TextureSlot.TOP, new Material(Routers.identifier("block/machine_top"))).put(TextureSlot.SIDE, new Material(Routers.identifier("block/machine_side_idle"))).put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"));

        MultiVariant multivariant = plainVariant(ModelTemplates.CUBE_ORIENTABLE.create(block, idleTextureMapping, modelOutput));
        MultiVariant multivariant1 = plainVariant(ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(block, idleTextureMapping, modelOutput));

        blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.FACING)
                                .select(Direction.DOWN, multivariant1.with(X_ROT_180))
                                .select(Direction.UP, multivariant1)
                                .select(Direction.NORTH, multivariant)
                                .select(Direction.EAST, multivariant.with(Y_ROT_90))
                                .select(Direction.SOUTH, multivariant.with(Y_ROT_180))
                                .select(Direction.WEST, multivariant.with(Y_ROT_270))));

    }

    @Override
    protected @NotNull Stream<? extends Holder<Block>> getKnownBlocks() {
        return RoutersBlocks.BLOCKS.getEntries().stream().filter(x ->
                !x.is(RoutersBlocks.EXPORTER.getId()) &&
                !x.is(RoutersBlocks.IMPORTER.getId()) &&
                !x.is(RoutersBlocks.IMPORTER_EXPORTER.getId()) &&
                !x.is(RoutersBlocks.ROUTER_MANAGER.getId()) &&
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