package com.benbenlaw.routers.screen.upgrade;

import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.core.screen.SimpleAbstractContainerMenu;
import com.benbenlaw.core.screen.util.slot.FilterFluidSlot;
import com.benbenlaw.core.screen.util.slot.FilterSlot;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.screen.RoutersMenuTypes;
import com.benbenlaw.routers.api.screen.ScreenModule;
import com.benbenlaw.routers.api.screen.RouterUIRegistries;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class FilterMenu extends SimpleAbstractContainerMenu {

    public final ConfigurableRouterBlockEntity blockEntity;
    protected final Level level;
    protected final Player player;
    public final BlockPos blockPos;
    public final ButtonType buttonType;

    public FilterMenu(int containerID, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerID, inventory,
                extraData.readBlockPos(),
                RouterButtonTypes.get(extraData.readIdentifier()),
                new SimpleContainerData(2));
    }

    public FilterMenu(int containerID, Inventory inventory, BlockPos blockPos, ButtonType buttonType, ContainerData data) {
        super(RoutersMenuTypes.FILTER_MENU.get(), containerID, inventory, blockPos, 0);
        this.player = inventory.player;
        this.blockPos = blockPos;
        this.buttonType = buttonType;
        this.level = inventory.player.level();
        this.blockEntity = ConfigurableRouterBlockEntity.resolve(this.level.getBlockEntity(blockPos));

        if (blockEntity != null && buttonType != null) {
            Registry<ScreenModule> registry = level.registryAccess().lookupOrThrow(RouterUIRegistries.SCREEN_MODULE_KEY);

            for (ScreenModule module : registry) {
                // Compare the registered ButtonType instances
                if (module.buttonType().equals(buttonType.getId())) {
                    module.slotAdder().accept(this, blockEntity, 0);
                    break;
                }
            }
        }
        this.addDataSlots(data);
    }

    public void addSlotPublic(Slot slot) {
        this.addSlot(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return ManagerSessions.allowsRemote(player, blockPos) || super.stillValid(player);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = this.slots.get(slotId);

            if (slot instanceof FilterSlot filterSlot) {
                ItemStack carried = this.getCarried();
                filterSlot.set(carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
                return;
            }

            if (slot instanceof FilterFluidSlot filterSlot) {
                ItemStack carried = this.getCarried();
                if (carried.isEmpty()) {
                    filterSlot.setEmpty();
                } else {
                    FluidStack fluidInStack = FluidUtil.getFirstStackContained(carried);
                    if (!fluidInStack.isEmpty()) {
                        filterSlot.set(fluidInStack);
                    }
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }
}