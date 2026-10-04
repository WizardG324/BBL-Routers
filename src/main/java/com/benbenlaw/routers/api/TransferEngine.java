package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class TransferEngine {
    public static int run(ServerLevel level, ExporterBlockEntity exporter, List<GlobalPos> importers, boolean isRoundRobin, int lastIndex, IResourceTransfer transferLogic) {
        if (importers == null || importers.isEmpty()) return lastIndex;

        int size = importers.size();

        if (isRoundRobin) {
            for (int i = 0; i < size; i++) {
                if (level.getBlockState(importers.get((lastIndex + i) % size).pos()).getBlock() instanceof RouterBlock) {
                    int currentIndex = (lastIndex + i) % size;
                    boolean isWorking = level.getBlockState(importers.get(currentIndex).pos()).getValue(RouterBlock.WORKING);
                    if (isWorking && !pullsOwnResources(level, importers.get(currentIndex))) {
                        if (transferLogic.tryTransfer(level, exporter, importers.get(currentIndex))) {
                            return (currentIndex + 1) % size;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (level.getBlockState(importers.get(i).pos()).getBlock() instanceof RouterBlock) {
                    boolean isWorking = level.getBlockState(importers.get(i).pos()).getValue(RouterBlock.WORKING);
                    if (isWorking && !pullsOwnResources(level, importers.get(i))) {
                        if (transferLogic.tryTransfer(level, exporter, importers.get(i))) {
                            return lastIndex;
                        }
                    }
                }
            }
        }
        return lastIndex;
    }

    private static boolean pullsOwnResources(ServerLevel level, GlobalPos importerPos) {
        ImporterCore importer = ImporterCore.at(level, importerPos.pos());
        return importer != null && importer.isRoundRobin;
    }
}