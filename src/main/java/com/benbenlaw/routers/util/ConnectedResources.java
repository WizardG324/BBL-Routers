package com.benbenlaw.routers.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ConnectedResources {

    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;

    private final BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> itemCache;
    private final BlockCapabilityCache<ResourceHandler<FluidResource>, Direction> fluidCache;
    private final Map<BlockCapability<?, Direction>, BlockCapabilityCache<?, Direction>> otherCaches = new HashMap<>();

    public ConnectedResources(ServerLevel level, BlockPos pos, Direction side) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.itemCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, level, pos, side);
        this.fluidCache = BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, level, pos, side);
    }

    @Nullable
    public ResourceHandler<ItemResource> getItemHandler() {
        return itemCache.getCapability();
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler() {
        return fluidCache.getCapability();
    }

    // Any other capability on the connected block, cached the first time it's asked for.
    @Nullable
    @SuppressWarnings("unchecked")
    public <H> H get(BlockCapability<H, Direction> capability) {
        BlockCapabilityCache<H, Direction> cache = (BlockCapabilityCache<H, Direction>) otherCaches.computeIfAbsent(
                capability, key -> BlockCapabilityCache.create(capability, level, pos, side));
        return cache.getCapability();
    }
}
