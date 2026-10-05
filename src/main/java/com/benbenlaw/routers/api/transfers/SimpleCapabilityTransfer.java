package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.util.ResourceScanState;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleCapabilityTransfer<H> implements TransferModule<H> {

    protected abstract boolean isEmpty(H source);

    protected abstract boolean move(H source, H target, int amount);

    @Override
    public int push(ServerLevel level, ExporterBlockEntity exporter) {
        ResourceScanState scan = exporter.getScanState(capability().name());
        if (scan.shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        int amount = exporter.getUpgradeValue(upgradeTag());
        H source = exporter.getConnectedResources().get(capability());
        if (source == null || isEmpty(source)) return exporter.lastImporterIndex;

        boolean[] moved = {false};
        scan.nextScanStart(1);

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {
                    H target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    if (move(source, target, amount)) {
                        moved[0] = true;
                        return true;
                    }
                    return false;
                });

        scan.recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    @Override
    public int pull(ServerLevel level, ImporterCore importer) {
        ResourceScanState scan = importer.getScanState(capability().name());
        if (scan.shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        H target = importer.getConnectedResources().get(capability());
        if (target == null) return importer.lastExporterIndex;

        boolean[] moved = {false};
        scan.nextScanStart(1);

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {
                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(upgradeTag())) return false;

                    H source = getSourceHandler(srvLevel, exporter, imp, exporterPos);
                    if (source == null || isEmpty(source)) return false;

                    if (move(source, target, exporter.getUpgradeValue(upgradeTag()))) {
                        moved[0] = true;
                        return true;
                    }
                    return false;
                });

        scan.recordResult(level.getGameTime(), 1, moved[0]);
        return result;
    }

    @Nullable
    private H getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        if (targetLevel.getBlockEntity(pos.pos()) instanceof DistributorBlockEntity distributor) {
            return distributor.getDistributor(this);
        }

        if (ImporterCore.at(targetLevel, pos.pos()) == null) return null;

        return exporter.getTargetCache(capability()).get(targetLevel, pos);
    }

    @Nullable
    private static ExporterBlockEntity getExporterAt(ServerLevel importerLevel, GlobalPos exporterPos) {
        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;
        return exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter ? exporter : null;
    }

    @Nullable
    private H getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterCore importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        return importer.getSourceCache(capability()).get(exporterLevel, exporterPos);
    }
}
