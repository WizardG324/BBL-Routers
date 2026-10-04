package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.core.screen.util.slot.InputSlot;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ImporterMenu extends SimpleAbstractContainerMenu {

    protected ImporterBlockEntity blockEntity;
    protected Level level;
    protected ContainerData data;
    protected Player player;
    protected BlockPos blockPos;

    public ImporterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos(), new SimpleContainerData(2));
    }

    public ImporterMenu(int containerID, Inventory inventory, BlockPos blockPos, ContainerData data) {
        super(RoutersMenuTypes.IMPORTER_MENU.get(), containerID, inventory, blockPos, 9);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.level = inventory.player.level();
        this.blockEntity = (ImporterBlockEntity) this.level.getBlockEntity(blockPos);

        for (int i = 0; i < 9; i++) {
            assert blockEntity != null;
            this.addSlot(new InputSlot(blockEntity.getUpgradeItemHandler(), blockEntity.getUpgradeItemHandler()::set,
                    i, 8 + i * 18, 54));
        }

        this.addDataSlots(data);
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

        if (!sourceStack.is(RoutersTags.Items.IMPORTER_UPGRADES)) {
            return ItemStack.EMPTY;
        }

        ItemStack single = sourceStack.copyWithCount(1);

        if (blockEntity.hasUpgradeTypeAlready(single)) {
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
