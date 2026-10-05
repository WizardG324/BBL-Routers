package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class EnergyTransfer extends SimpleCapabilityTransfer<EnergyHandler> {

    @Override
    public TagKey<Item> upgradeTag() {
        return RoutersTags.Items.RF_UPGRADES;
    }

    @Override
    public BlockCapability<EnergyHandler, Direction> capability() {
        return Capabilities.Energy.BLOCK;
    }

    @Override
    protected boolean isEmpty(EnergyHandler source) {
        return source.getAmountAsLong() <= 0;
    }

    @Override
    protected boolean move(EnergyHandler source, EnergyHandler target, int amount) {
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
    public EnergyHandler createDistributor(DistributorBlockEntity distributor) {
        return new DistributorHandlers.Energy(distributor);
    }
}
