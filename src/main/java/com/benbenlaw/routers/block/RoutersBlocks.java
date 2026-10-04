package com.benbenlaw.routers.block;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.custom.DistributorBlock;
import com.benbenlaw.routers.block.custom.ExporterBlock;
import com.benbenlaw.routers.block.custom.ImporterExporterBlock;
import com.benbenlaw.routers.block.custom.ImporterBlock;
import com.benbenlaw.routers.block.custom.RouterManagerBlock;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RoutersBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Routers.MOD_ID);


    public static final DeferredBlock<Block> IMPORTER = registerBlock("importer",
            () -> new ImporterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .noOcclusion().setId(createID("importer"))));

    public static final DeferredBlock<Block> EXPORTER = registerBlock("exporter",
            () -> new ExporterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .noOcclusion().setId(createID("exporter"))));

    public static final DeferredBlock<Block> IMPORTER_EXPORTER = registerBlock("importer_exporter",
            () -> new ImporterExporterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .noOcclusion().setId(createID("importer_exporter"))));

    public static final DeferredBlock<Block> ROUTER_MANAGER = registerBlock("router_manager",
            () -> new RouterManagerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .setId(createID("router_manager"))));

    public static final DeferredBlock<Block> DISTRIBUTOR = registerBlock("distributor",
            () -> new DistributorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .noOcclusion().setId(createID("distributor"))));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
       RoutersItems.ITEMS.registerItem(name, (properties) -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static ResourceKey<Block> createID(String name) {
        return ResourceKey.create(Registries.BLOCK, Routers.identifier(name));
    }
}
