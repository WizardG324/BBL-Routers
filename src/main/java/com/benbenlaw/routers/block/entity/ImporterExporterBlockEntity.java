package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.screen.ImporterExporterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ImporterExporterBlockEntity extends ExporterBlockEntity implements ImporterHost {

    private final ImporterCore importerCore = new ImporterCore(this);
    private boolean viewingImporterSide;

    public ImporterExporterBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.IMPORTER_EXPORTER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ImporterCore getImporterCore() {
        return importerCore;
    }

    @Override
    public void tick() {
        super.tick();
        importerCore.tick();
    }

    public boolean isViewingImporterSide() {
        return viewingImporterSide;
    }

    public void toggleViewedSide() {
        viewingImporterSide = !viewingImporterSide;
        setChanged();
        sync();
    }

    public ConfigurableRouterBlockEntity getActiveConfigurable() {
        return viewingImporterSide ? importerCore : this;
    }

    public ResourceHandler<ItemResource> getActiveUpgradeHandler() {
        return viewingImporterSide ? importerCore.getUpgradeItemHandler() : getUpgradeItemHandler();
    }

    public boolean activeSideHasUpgradeTypeAlready(ItemStack stack) {
        return viewingImporterSide ? importerCore.hasUpgradeTypeAlready(stack) : hasUpgradeTypeAlready(stack);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.importer_exporter");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new ImporterExporterMenu(container, inventory, getBlockPos(), data);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        importerCore.save(output.child("importerSide"));
        output.putBoolean("viewingImporterSide", viewingImporterSide);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        importerCore.load(input.childOrEmpty("importerSide"));
        viewingImporterSide = input.getBooleanOr("viewingImporterSide", false);
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        dropInventoryContents(importerCore.getUpgradeItemHandler());
        importerCore.unlinkFromExporters(pos);
    }
}
