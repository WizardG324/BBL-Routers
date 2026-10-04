package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class EnergyTransfer {

    public static int transferEnergy(ServerLevel level, ExporterBlockEntity exporter) {

        if (exporter.getEnergyScanState().shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);
        EnergyHandler source = exporter.getConnectedResources().getEnergyHandler();

        if (source == null || source.getAmountAsLong() <= 0) return exporter.lastImporterIndex;

        boolean[] moved = {false};
        exporter.getEnergyScanState().nextScanStart(1);

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetGlobalPos) -> {

                    EnergyHandler target = getTargetHandler(srvLevel, entity, targetGlobalPos);
                    if (target == null) return false;

                    if (moveEnergy(source, target, amount)) {
                        moved[0] = true;
                        return true;
                    }
                    return false;
                });

        exporter.getEnergyScanState().recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    private static EnergyHandler getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        if (targetLevel.getBlockEntity(pos.pos()) instanceof DistributorBlockEntity distributor) {
            return distributor.getEnergyDistributor();
        }

        if (ImporterCore.at(targetLevel, pos.pos()) == null) return null;

        return exporter.getEnergyTargetCache().get(targetLevel, pos);
    }

    public static int pullEnergy(ServerLevel level, ImporterCore importer) {

        if (importer.getEnergyScanState().shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        EnergyHandler target = importer.getConnectedResources().getEnergyHandler();

        if (target == null) return importer.lastExporterIndex;

        boolean[] moved = {false};
        importer.getEnergyScanState().nextScanStart(1);

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.RF_UPGRADES)) return false;

                    EnergyHandler source = getSourceHandler(srvLevel, exporter, imp, exporterPos);
                    if (source == null || source.getAmountAsLong() <= 0) return false;

                    int amount = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);

                    if (moveEnergy(source, target, amount)) {
                        moved[0] = true;
                        return true;
                    }
                    return false;
                });

        importer.getEnergyScanState().recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    // Only takes from the source what the target actually accepts. Extracting first and then inserting in one
    // transaction would commit the whole extraction even when the target only took part of it.
    private static boolean moveEnergy(EnergyHandler source, EnergyHandler target, int amount) {
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

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private static EnergyHandler getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterCore importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        return importer.getEnergySourceCache().get(exporterLevel, exporterPos);
    }
}
