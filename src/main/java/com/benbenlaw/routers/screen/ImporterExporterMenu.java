package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.core.screen.util.slot.InputSlot;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;

public class ImporterExporterMenu extends SimpleAbstractContainerMenu {

    protected final ImporterExporterBlockEntity blockEntity;
    protected final BlockPos blockPos;
    public final boolean importerSide;

    public ImporterExporterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos(), new SimpleContainerData(2));
    }

    public ImporterExporterMenu(int containerID, Inventory inventory, BlockPos blockPos, ContainerData data) {
        super(RoutersMenuTypes.IMPORTER_EXPORTER_MENU.get(), containerID, inventory, blockPos, 9);
        this.blockPos = blockPos;
        this.blockEntity = (ImporterExporterBlockEntity) inventory.player.level().getBlockEntity(blockPos);
        this.importerSide = blockEntity.isViewingImporterSide();

        ItemStacksResourceHandler handler = (ItemStacksResourceHandler) blockEntity.getActiveUpgradeHandler();
        for (int i = 0; i < 9; i++) {
            this.addSlot(new InputSlot(handler, handler::set, i, 8 + i * 18, 54));
        }

        this.addDataSlots(data);
    }

    public ImporterExporterBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return ManagerSessions.allowsRemote(player, blockPos) || super.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = this.slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();

        if (pIndex >= 36) {
            return super.quickMoveStack(playerIn, pIndex);
        }

        if (!sourceStack.is(importerSide ? RoutersTags.Items.IMPORTER_UPGRADES : RoutersTags.Items.EXPORTER_UPGRADES)) {
            return ItemStack.EMPTY;
        }

        ItemStack single = sourceStack.copyWithCount(1);

        if (blockEntity.activeSideHasUpgradeTypeAlready(single)) {
            return ItemStack.EMPTY;
        }

        Slot targetSlot = null;
        for (int i = 36; i < 45; i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasItem()) {
                targetSlot = slot;
                break;
            }
        }

        if (targetSlot == null) {
            return ItemStack.EMPTY;
        }

        targetSlot.set(single);
        targetSlot.setChanged();

        sourceStack.shrink(1);
        sourceSlot.setChanged();

        return ItemStack.EMPTY;
    }
}
