package com.benbenlaw.routers.integration.rifts;

import com.benbenlaw.rifts.block.capability.RiftEnergyHandler;
import com.benbenlaw.rifts.block.capability.RiftsCapabilities;
import com.benbenlaw.routers.api.transfers.DistributorHandlers;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

public class RiftEnergyDistributor implements RiftEnergyHandler {

    private final DistributorBlockEntity distributor;
    private final DistributorHandlers.Spreader spreader = new DistributorHandlers.Spreader();

    public RiftEnergyDistributor(DistributorBlockEntity distributor) {
        this.distributor = distributor;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (amount <= 0) return 0;
        if (!(distributor.getLevel() instanceof ServerLevel level)) return 0;

        int cap = distributor.getUpgradeValue(RoutersTags.Items.RIFT_ENERGY_UPGRADES);
        if (cap <= 0) return 0;

        List<RiftEnergyHandler> handlers = new ArrayList<>();
        for (DistributorBlockEntity.Target target : distributor.getTargets(level)) {
            RiftEnergyHandler handler = target.get(RiftsCapabilities.RIFT_ENERGY, level);
            if (handler != null && handler.canInsert()) handlers.add(handler);
        }

        return spreader.spread(Math.min(amount, cap), handlers.size(), distributor.isRoundRobin,
                (index, share) -> handlers.get(index).insert(share, transaction));
    }

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }
}
