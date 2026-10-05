package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public interface TransferModule<H> {

    TagKey<Item> upgradeTag();

    BlockCapability<H, Direction> capability();

    int push(ServerLevel level, ExporterBlockEntity exporter);

    default int pull(ServerLevel level, ImporterCore importer) {
        return importer.lastExporterIndex;
    }

    @Nullable
    default H createDistributor(DistributorBlockEntity distributor) {
        return null;
    }
}
