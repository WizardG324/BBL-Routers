package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.screen.ImporterMenu;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ImporterBlockEntity extends SyncableBlockEntity implements MenuProvider, ConfigurableRouterBlockEntity, ImporterHost {

    public final ContainerData data;
    private final ImporterCore core = new ImporterCore(this);

    public ImporterBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.IMPORTER_BLOCK_ENTITY.get(), pos, state);

        this.data = new ContainerData() {;
            @Override
            public int get(int index) {
                return 0;
            }

            @Override
            public void set(int index, int value) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    @Override
    public ImporterCore getImporterCore() {
        return core;
    }

    public void tick() {
        core.tick();
    }

    public boolean hasUpgradeTypeAlready(ItemStack stack) {
        return core.hasUpgradeTypeAlready(stack);
    }

    @Override
    public boolean hasUpgrade(ButtonType type) {
        return core.hasUpgrade(type);
    }

    public SyncableItemHandler getUpgradeItemHandler() {
        return core.getUpgradeItemHandler();
    }

    @Override
    public FilterItemHandler getFilterItemHandler() {
        return core.getFilterItemHandler();
    }

    @Override
    public FilterFluidHandler getFilterFluidHandler() {
        return core.getFilterFluidHandler();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.importer");
    }

    @Override
    public AbstractContainerMenu createMenu(int container, @NotNull Inventory inventory, @NotNull Player player) {
        return new ImporterMenu(container, inventory, this.getBlockPos(), data);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        core.save(output);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        core.load(input);
        super.loadAdditional(input);
    }

    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        dropInventoryContents(core.getUpgradeItemHandler());
        core.unlinkFromExporters(pos);
    }
}
