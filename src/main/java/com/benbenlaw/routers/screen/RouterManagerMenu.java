package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.block.RoutersBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RouterManagerMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final ContainerLevelAccess access;

    public RouterManagerMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory, extraData.readBlockPos());
    }

    public RouterManagerMenu(int containerID, Inventory inventory, BlockPos blockPos) {
        super(RoutersMenuTypes.ROUTER_MANAGER_MENU.get(), containerID);
        this.blockPos = blockPos;
        this.access = ContainerLevelAccess.create(inventory.player.level(), blockPos);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, RoutersBlocks.ROUTER_MANAGER.get());
    }
}
