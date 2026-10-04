package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.routers.api.ImporterPullEngine;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;

public class FluidTransfer {

    public static int transferFluids(ServerLevel level, ExporterBlockEntity exporter) {

        if (exporter.getFluidScanState().shouldSkip(level.getGameTime())) return exporter.lastImporterIndex;

        ResourceHandler<FluidResource> source = exporter.getConnectedResources().getFluidHandler();

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        boolean[] moved = {false};

        int result = TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<FluidResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    ImporterCore importer = ImporterCore.at(srvLevel, targetPos.pos());

                    int amount = entity.getUpgradeValue(RoutersTags.Items.FLUID_UPGRADES);
                    int start = entity.getFluidScanState().nextScanStart(source.size());

                    boolean success = moveFirstBounded(source, target, start, amount, res -> {
                        boolean isWhitelist = !entity.isBlacklist();
                        boolean matches = true;

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterFluidHandler())) {
                            matches = entity.getFilterFluidHandler().matchesFluid(FluidResource.of(res.toStack(1000)), isWhitelist, entity.isIgnoreNbt());
                        }

                        if (matches && importer != null && !ResourceHandlerUtil.isEmpty(importer.getFilterFluidHandler())) {
                            matches = importer.getFilterFluidHandler().matchesFluid(
                                    FluidResource.of(res.toStack(1000)), !importer.isBlacklist(), importer.isIgnoreNbt());
                        }

                        return matches;
                    });
                    moved[0] |= success;
                    return success;
                });

        exporter.getFluidScanState().recordResult(level.getGameTime(), source.size(), moved[0]);
        return result;
    }

    private static ResourceHandler<FluidResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        if (targetLevel.getBlockEntity(pos.pos()) instanceof DistributorBlockEntity distributor) {
            return distributor.getFluidDistributor();
        }

        return exporter.getFluidTargetCache().get(targetLevel, pos);
    }

    public static int pullFluids(ServerLevel level, ImporterCore importer) {

        if (importer.getFluidScanState().shouldSkip(level.getGameTime())) return importer.lastExporterIndex;

        ResourceHandler<FluidResource> target = importer.getConnectedResources().getFluidHandler();

        if (target == null) return importer.lastExporterIndex;

        boolean[] moved = {false};
        int[] lastSourceSize = {1};

        int result = ImporterPullEngine.run(level, importer, importer.exporterPositions, importer.lastExporterIndex,
                (srvLevel, imp, exporterPos) -> {

                    ExporterBlockEntity exporter = getExporterAt(srvLevel, exporterPos);
                    if (exporter == null || !exporter.hasCorrectUpgrade(RoutersTags.Items.FLUID_UPGRADES)) return false;

                    ResourceHandler<FluidResource> source = getSourceHandler(srvLevel, exporter, imp, exporterPos);
                    if (source == null) return false;

                    int amount = exporter.getUpgradeValue(RoutersTags.Items.FLUID_UPGRADES);
                    lastSourceSize[0] = Math.max(source.size(), 1);
                    int start = imp.getFluidScanState().nextScanStart(lastSourceSize[0]);

                    boolean success = moveFirstBounded(source, target, start, amount, res -> {
                        boolean isWhitelist = !exporter.isBlacklist();
                        boolean matches = true;

                        if (!ResourceHandlerUtil.isEmpty(exporter.getFilterFluidHandler())) {
                            matches = exporter.getFilterFluidHandler().matchesFluid(FluidResource.of(res.toStack(1000)), isWhitelist, exporter.isIgnoreNbt());
                        }

                        if (matches && !ResourceHandlerUtil.isEmpty(imp.getFilterFluidHandler())) {
                            matches = imp.getFilterFluidHandler().matchesFluid(
                                    FluidResource.of(res.toStack(1000)), !imp.isBlacklist(), imp.isIgnoreNbt());
                        }

                        return matches;
                    });
                    moved[0] |= success;
                    return success;
                });

        importer.getFluidScanState().recordResult(level.getGameTime(), lastSourceSize[0], moved[0]);
        return result;
    }

    private interface FluidPredicate {
        boolean test(FluidResource resource);
    }

    private static boolean moveFirstBounded(ResourceHandler<FluidResource> source, ResourceHandler<FluidResource> target,
                                             int start, int amount, FluidPredicate predicate) {
        if (amount <= 0) return false;

        int size = source.size();
        int scanLimit = Math.min(size, StartupConfig.maxInventoryScanPerOperation.get());

        for (int offset = 0; offset < scanLimit; offset++) {
            int slot = (start + offset) % size;
            FluidResource res = source.getResource(slot);
            if (res.isEmpty()) continue;
            if (!predicate.test(res)) continue;

            try (Transaction tx = Transaction.open(null)) {
                int available = Math.min(source.getAmountAsInt(slot), amount);
                int accepted = target.insert(res, available, tx);

                if (accepted > 0 && source.extract(slot, res, accepted, tx) > 0) {
                    tx.commit();
                    return true;
                }
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
    private static ResourceHandler<FluidResource> getSourceHandler(ServerLevel importerLevel, ExporterBlockEntity exporter, ImporterCore importer, GlobalPos exporterPos) {
        if (!exporterPos.dimension().equals(importerLevel.dimension()) && !exporter.canDoDimensionalTravel()) return null;

        ServerLevel exporterLevel = importerLevel.getServer().getLevel(exporterPos.dimension());
        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) return null;

        return importer.getFluidSourceCache().get(exporterLevel, exporterPos);
    }
}
