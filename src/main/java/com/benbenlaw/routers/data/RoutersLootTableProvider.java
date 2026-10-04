package com.benbenlaw.routers.data;

import com.benbenlaw.routers.block.RoutersBlocks;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RoutersLootTableProvider extends VanillaBlockLoot {

    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();


    public RoutersLootTableProvider(HolderLookup.Provider provider) {
        super(provider);
    }

    @Override
    protected void generate() {

        this.dropSelf(RoutersBlocks.IMPORTER.get());
        this.dropSelf(RoutersBlocks.EXPORTER.get());
        this.dropSelf(RoutersBlocks.IMPORTER_EXPORTER.get());
        this.dropSelf(RoutersBlocks.ROUTER_MANAGER.get());
        this.dropSelf(RoutersBlocks.DISTRIBUTOR.get());
    }

    @Override
    protected void add(@NotNull Block block, @NotNull LootTable.Builder table) {
        super.add(block, table);
        knownBlocks.add(block);
    }

    @NotNull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

}
