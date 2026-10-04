package com.benbenlaw.routers.api;

import com.benbenlaw.routers.block.entity.ImporterCore;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface IImporterResourceTransfer {

    boolean tryTransfer(ServerLevel importerLevel, ImporterCore importer, GlobalPos exporterPos);
}
