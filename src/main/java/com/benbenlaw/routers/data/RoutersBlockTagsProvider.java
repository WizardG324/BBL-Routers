package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RoutersBlockTagsProvider extends BlockTagsProvider {

    RoutersBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Routers.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(RoutersBlocks.EXPORTER.get())
                .add(RoutersBlocks.IMPORTER.get())
                .add(RoutersBlocks.IMPORTER_EXPORTER.get())
                .add(RoutersBlocks.ROUTER_MANAGER.get())
                .add(RoutersBlocks.DISTRIBUTOR.get())
        ;
    }


    @Override
    public @NotNull String getName() {
        return Routers.MOD_ID + " Block Tags";
    }
}
