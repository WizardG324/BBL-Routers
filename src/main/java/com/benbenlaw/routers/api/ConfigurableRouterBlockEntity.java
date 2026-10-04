package com.benbenlaw.routers.api;

import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface ConfigurableRouterBlockEntity {
    FilterItemHandler getFilterItemHandler();
    FilterFluidHandler getFilterFluidHandler();
    boolean hasUpgrade(ButtonType type);

    // The Importer Exporter has two independent configurations on one block; this picks the one
    // the player is currently editing. Every other block just is its own configuration.
    @Nullable
    static ConfigurableRouterBlockEntity resolve(@Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof ImporterExporterBlockEntity hybrid) return hybrid.getActiveConfigurable();
        return blockEntity instanceof ConfigurableRouterBlockEntity configurable ? configurable : null;
    }
}
