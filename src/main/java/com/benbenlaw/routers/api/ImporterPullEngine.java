package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ImporterCore;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class ImporterPullEngine {

    public static int run(ServerLevel level, ImporterCore importer, List<GlobalPos> exporters, int lastIndex, IImporterResourceTransfer transferLogic) {
        if (exporters == null || exporters.isEmpty()) return lastIndex;

        int size = exporters.size();

        for (int i = 0; i < size; i++) {
            int currentIndex = (lastIndex + i) % size;
            GlobalPos exporterPos = exporters.get(currentIndex);

            if (level.getBlockState(exporterPos.pos()).getBlock() instanceof RouterBlock) {
                boolean isWorking = level.getBlockState(exporterPos.pos()).getValue(RouterBlock.WORKING);
                if (isWorking && transferLogic.tryTransfer(level, importer, exporterPos)) {
                    return (currentIndex + 1) % size;
                }
            }
        }
        return lastIndex;
    }
}
