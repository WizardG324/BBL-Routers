package com.benbenlaw.routers.integration.rifts;

import com.benbenlaw.rifts.block.capability.RiftEnergyHandler;
import com.benbenlaw.rifts.block.capability.RiftsCapabilities;
import com.benbenlaw.routers.api.transfers.SimpleCapabilityTransfer;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class RiftEnergyTransfer extends SimpleCapabilityTransfer<RiftEnergyHandler> {

    @Override
    public TagKey<Item> upgradeTag() {
        return RoutersTags.Items.RIFT_ENERGY_UPGRADES;
    }

    @Override
    public BlockCapability<RiftEnergyHandler, Direction> capability() {
        return RiftsCapabilities.RIFT_ENERGY;
    }

    @Override
    protected boolean isEmpty(RiftEnergyHandler source) {
        return !source.canExtract() || source.getAmountAsLong() <= 0;
    }

    @Override
    protected boolean move(RiftEnergyHandler source, RiftEnergyHandler target, int amount) {
        if (!source.canExtract() || !target.canInsert()) return false;

        int available;
        try (Transaction simulation = Transaction.open(null)) {
            available = source.extract(amount, simulation);
        }
        if (available <= 0) return false;

        try (Transaction tx = Transaction.open(null)) {
            int accepted = target.insert(available, tx);
            if (accepted > 0 && source.extract(accepted, tx) == accepted) {
                tx.commit();
                return true;
            }
        }
        return false;
    }

    @Override
    public RiftEnergyHandler createDistributor(DistributorBlockEntity distributor) {
        return new RiftEnergyDistributor(distributor);
    }
}
