package com.benbenlaw.routers.block.entity;

import com.benbenlaw.core.block.entity.SyncableBlockEntity;
import com.benbenlaw.core.block.entity.handler.fluid.FilterFluidHandler;
import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.api.transfers.EnergyTransfer;
import com.benbenlaw.routers.api.transfers.FluidTransfer;
import com.benbenlaw.routers.api.transfers.ItemTransfer;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.util.ConnectedResources;
import com.benbenlaw.routers.util.LinkedCapabilityCache;
import com.benbenlaw.routers.util.ResourceScanState;
import com.benbenlaw.routers.util.RoutersTags;
import com.benbenlaw.routers.util.UpgradeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ImporterCore implements ConfigurableRouterBlockEntity {

    private final SyncableBlockEntity host;

    public List<GlobalPos> exporterPositions = new ArrayList<>();
    public boolean isRoundRobin;
    public int lastExporterIndex = 0;

    private boolean ignoreNbt;
    private boolean isBlacklist;
    private Set<Identifier> linkedUnlockedButtons = new HashSet<>();

    private ConnectedResources connectedResources;

    private final LinkedCapabilityCache<ResourceHandler<ItemResource>> itemSourceCache = new LinkedCapabilityCache<>(Capabilities.Item.BLOCK);
    private final LinkedCapabilityCache<ResourceHandler<FluidResource>> fluidSourceCache = new LinkedCapabilityCache<>(Capabilities.Fluid.BLOCK);
    private final LinkedCapabilityCache<EnergyHandler> energySourceCache = new LinkedCapabilityCache<>(Capabilities.Energy.BLOCK);

    private final ResourceScanState itemScanState = new ResourceScanState();
    private final ResourceScanState fluidScanState = new ResourceScanState();
    private final ResourceScanState energyScanState = new ResourceScanState();

    private final SyncableItemHandler upgradeItemHandler;
    private final FilterItemHandler filterItemHandler;
    private final FilterFluidHandler filterFluidHandler;

    public ImporterCore(SyncableBlockEntity host) {
        this.host = host;
        this.filterItemHandler = new FilterItemHandler(host, 18);
        this.filterFluidHandler = new FilterFluidHandler(host, 18);

        this.upgradeItemHandler = new SyncableItemHandler(host, 9, (i, stack) ->
                stack.is(RoutersTags.Items.IMPORTER_UPGRADES) && !hasUpgradeTypeAlready(stack), i -> false) {
            @Override
            protected int getCapacity(int index, ItemResource resource) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int index, ItemStack previousContents) {
                ignoreNbt = false;
                isBlacklist = false;
                isRoundRobin = false;

                for (int i = 0; i < size(); i++) {
                    ItemStack stack = getResource(i).toStack();
                    if (stack.isEmpty()) continue;

                    if (stack.is(RoutersItems.IGNORE_NBT_UPGRADE)) {
                        ignoreNbt = true;
                    }
                    if (stack.is(RoutersItems.BLACKLIST_UPGRADE)) {
                        isBlacklist = true;
                    }
                    if (stack.is(RoutersItems.ROUND_ROBIN_UPGRADE)) {
                        isRoundRobin = true;
                    }
                }

                super.onContentsChanged(index, previousContents);
                recomputeLinkedUpgrades();
            }
        };
    }

    @Nullable
    public static ImporterCore at(@Nullable Level level, BlockPos pos) {
        return level != null && level.getBlockEntity(pos) instanceof ImporterHost importerHost ? importerHost.getImporterCore() : null;
    }

    @Nullable
    private Level level() {
        return host.getLevel();
    }

    public SyncableBlockEntity getHost() {
        return host;
    }

    public BlockPos getBlockPos() {
        return host.getBlockPos();
    }

    public void tick() {
        Level level = level();
        assert level != null;
        if (level.isClientSide()) return;

        if (level.getGameTime() % 100 == 0) {
            validateExporterPositions();
            recomputeLinkedUpgrades();
        }

        if (isRoundRobin && exporterPositions != null && !exporterPositions.isEmpty()) {
            BlockState currentState = level.getBlockState(getBlockPos());
            if (!currentState.hasProperty(RouterBlock.WORKING) || !currentState.getValue(RouterBlock.WORKING)) return;

            if ((level.getGameTime() + getBlockPos().hashCode()) % StartupConfig.defaultSpeedPerOperation.get() == 0) {
                pullResources();
            }
        }
    }

    private void pullResources() {
        ServerLevel serverLevel = (ServerLevel) level();
        lastExporterIndex = ItemTransfer.pullItems(serverLevel, this);
        lastExporterIndex = FluidTransfer.pullFluids(serverLevel, this);
        lastExporterIndex = EnergyTransfer.pullEnergy(serverLevel, this);
    }

    public void validateExporterPositions() {
        if (exporterPositions == null || exporterPositions.isEmpty()) return;
        Level level = level();
        assert level != null;
        exporterPositions.removeIf(pos -> {
            ServerLevel exporterLevel = Objects.requireNonNull(level.getServer()).getLevel(pos.dimension());
            return exporterLevel == null || exporterLevel.getBlockEntity(pos.pos()) == null;
        });
        host.sync();
    }

    public void recomputeLinkedUpgrades() {
        Level level = level();
        if (level == null || level.isClientSide() || level.getServer() == null) return;

        GlobalPos thisPos = GlobalPos.of(level.dimension(), getBlockPos());
        Set<Identifier> unlocked = new HashSet<>();
        for (GlobalPos pos : exporterPositions) {
            ServerLevel exporterLevel = level.getServer().getLevel(pos.dimension());
            if (exporterLevel == null || !exporterLevel.isLoaded(pos.pos())) continue;

            if (exporterLevel.getBlockEntity(pos.pos()) instanceof ExporterBlockEntity exporter) {
                for (ButtonType type : RouterButtonTypes.BUTTONS.values()) {
                    if (exporter.hasUpgrade(type)) {
                        unlocked.add(type.getId());
                    }
                }

                exporter.setImporterPullsOwnResources(thisPos, isRoundRobin);
            }
        }

        if (!unlocked.equals(linkedUnlockedButtons)) {
            linkedUnlockedButtons = unlocked;
            host.setChanged();
            host.sync();
        }
    }

    public boolean addExporterPosition(GlobalPos exporterGlobalPos) {
        for (GlobalPos pos : exporterPositions) {
            if (pos.dimension().equals(exporterGlobalPos.dimension()) && pos.pos().equals(exporterGlobalPos.pos())) {
                return false;
            }
        }
        exporterPositions.add(exporterGlobalPos);
        host.setChanged();
        notifyClient();
        recomputeLinkedUpgrades();
        itemSourceCache.remove(exporterGlobalPos);
        fluidSourceCache.remove(exporterGlobalPos);
        energySourceCache.remove(exporterGlobalPos);
        return true;
    }

    public boolean removeExporterPosition(GlobalPos exporterGlobalPos) {
        boolean removed = exporterPositions.removeIf(pos ->
                pos.dimension().equals(exporterGlobalPos.dimension()) && pos.pos().equals(exporterGlobalPos.pos()));
        if (removed) {
            host.setChanged();
            notifyClient();
            recomputeLinkedUpgrades();
            itemSourceCache.remove(exporterGlobalPos);
            fluidSourceCache.remove(exporterGlobalPos);
            energySourceCache.remove(exporterGlobalPos);
        }
        return removed;
    }

    private void notifyClient() {
        Level level = level();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), host.getBlockState(), host.getBlockState(), 3);
        }
    }

    public boolean isIgnoreNbt() {
        return ignoreNbt;
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public boolean hasUpgradeTypeAlready(ItemStack stack) {
        return UpgradeUtil.hasUpgradeTypeAlready(upgradeItemHandler, stack);
    }

    @Override
    public boolean hasUpgrade(ButtonType type) {
        return UpgradeUtil.hasUpgrade(upgradeItemHandler, type) || linkedUnlockedButtons.contains(type.getId());
    }

    public SyncableItemHandler getUpgradeItemHandler() {
        return upgradeItemHandler;
    }

    public ConnectedResources getConnectedResources() {
        if (connectedResources == null) {
            Level level = level();
            Direction facing = level.getBlockState(getBlockPos()).getValue(RouterBlock.FACING);
            connectedResources = new ConnectedResources((ServerLevel) level, getBlockPos().relative(facing), facing.getOpposite());
        }
        return connectedResources;
    }

    public LinkedCapabilityCache<ResourceHandler<ItemResource>> getItemSourceCache() {
        return itemSourceCache;
    }

    public LinkedCapabilityCache<ResourceHandler<FluidResource>> getFluidSourceCache() {
        return fluidSourceCache;
    }

    public LinkedCapabilityCache<EnergyHandler> getEnergySourceCache() {
        return energySourceCache;
    }

    public ResourceScanState getItemScanState() {
        return itemScanState;
    }

    public ResourceScanState getFluidScanState() {
        return fluidScanState;
    }

    public ResourceScanState getEnergyScanState() {
        return energyScanState;
    }

    @Override
    public FilterItemHandler getFilterItemHandler() {
        return filterItemHandler;
    }

    @Override
    public FilterFluidHandler getFilterFluidHandler() {
        return filterFluidHandler;
    }

    public void save(ValueOutput output) {
        upgradeItemHandler.serialize(output.child("upgradeItems"));
        filterItemHandler.serialize(output.child("itemFilter"));
        filterFluidHandler.serialize(output.child("fluidFilter"));

        output.putBoolean("ignoreNbt", ignoreNbt);
        output.putBoolean("isBlacklist", isBlacklist);
        output.putBoolean("isRoundRobin", isRoundRobin);

        if (exporterPositions != null && !exporterPositions.isEmpty()) {
            var list = output.list("exporterPositions", GlobalPos.CODEC);
            for (GlobalPos pos : exporterPositions) {
                list.add(pos);
            }
        }

        if (!linkedUnlockedButtons.isEmpty()) {
            var list = output.list("linkedUnlockedButtons", Identifier.CODEC);
            for (Identifier id : linkedUnlockedButtons) {
                list.add(id);
            }
        }
    }

    public void load(ValueInput input) {
        upgradeItemHandler.deserialize(input.childOrEmpty("upgradeItems"));
        filterItemHandler.deserialize(input.childOrEmpty("itemFilter"));
        filterFluidHandler.deserialize(input.childOrEmpty("fluidFilter"));

        ignoreNbt = input.getBooleanOr("ignoreNbt", false);
        isBlacklist = input.getBooleanOr("isBlacklist", false);
        isRoundRobin = input.getBooleanOr("isRoundRobin", false);

        exporterPositions = new ArrayList<>();
        input.listOrEmpty("exporterPositions", GlobalPos.CODEC)
                .forEach(exporterPositions::add);

        linkedUnlockedButtons = new HashSet<>();
        input.listOrEmpty("linkedUnlockedButtons", Identifier.CODEC)
                .forEach(linkedUnlockedButtons::add);
    }

    public void unlinkFromExporters(BlockPos pos) {
        Level level = level();
        if (level == null || level.isClientSide() || exporterPositions == null || exporterPositions.isEmpty()) return;

        GlobalPos thisImporterPos = GlobalPos.of(level.dimension(), pos);

        for (GlobalPos exporterGlobalPos : new ArrayList<>(exporterPositions)) {
            ServerLevel exporterLevel = level.getServer() != null ? level.getServer().getLevel(exporterGlobalPos.dimension()) : null;
            if (exporterLevel == null) continue;

            if (exporterLevel.getBlockEntity(exporterGlobalPos.pos()) instanceof ExporterBlockEntity exporter) {
                exporter.removeImporterPosition(thisImporterPos);
            }
        }
    }
}
