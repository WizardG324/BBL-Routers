package com.benbenlaw.routers.block.entity;

import com.benbenlaw.routers.api.transfers.DistributorHandlers;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Never ticks and has no storage of its own. Exporters link to it like an importer, and whatever they push
// into it is passed straight on to the machines around it, as far as its own upgrades and filters allow.
// It reuses the Exporter's upgrade slots, filters and screen for that configuration.
public class DistributorBlockEntity extends ExporterBlockEntity implements ImporterHost {

    private final ImporterCore importerCore = new ImporterCore(this);

    private final DistributorHandlers.Items itemDistributor = new DistributorHandlers.Items(this);
    private final DistributorHandlers.Fluids fluidDistributor = new DistributorHandlers.Fluids(this);
    private final DistributorHandlers.Energy energyDistributor = new DistributorHandlers.Energy(this);

    private List<Target> targets = List.of();
    private long nextRefresh;

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(RoutersBlockEntities.DISTRIBUTOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public boolean acceptsUpgrade(ItemStack stack) {
        return stack.is(RoutersTags.Items.DISTRIBUTOR_UPGRADES);
    }

    @Override
    public ImporterCore getImporterCore() {
        return importerCore;
    }

    @Override
    public void tick() {
    }

    @Nullable
    public ResourceHandler<ItemResource> getItemDistributor() {
        return hasCorrectUpgrade(RoutersTags.Items.ITEM_UPGRADES) ? itemDistributor : null;
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidDistributor() {
        return hasCorrectUpgrade(RoutersTags.Items.FLUID_UPGRADES) ? fluidDistributor : null;
    }

    @Nullable
    public EnergyHandler getEnergyDistributor() {
        return hasCorrectUpgrade(RoutersTags.Items.RF_UPGRADES) ? energyDistributor : null;
    }

    // The machines within range. Rebuilt every few seconds rather than every operation, from the loaded
    // chunks' block entities instead of scanning every position in the cube.
    public List<Target> getTargets(ServerLevel serverLevel) {
        long now = serverLevel.getGameTime();
        if (now >= nextRefresh) {
            nextRefresh = now + StartupConfig.distributorRefreshTicks.get();
            refreshTargets(serverLevel);
        }
        return targets;
    }

    private void refreshTargets(ServerLevel serverLevel) {
        int range = StartupConfig.distributorRange.get();
        BlockPos origin = worldPosition;

        List<BlockPos> found = new ArrayList<>();

        for (int chunkX = (origin.getX() - range) >> 4; chunkX <= (origin.getX() + range) >> 4; chunkX++) {
            for (int chunkZ = (origin.getZ() - range) >> 4; chunkZ <= (origin.getZ() + range) >> 4; chunkZ++) {
                LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (pos.equals(origin)) continue;
                    if (Math.abs(pos.getX() - origin.getX()) > range
                            || Math.abs(pos.getY() - origin.getY()) > range
                            || Math.abs(pos.getZ() - origin.getZ()) > range) continue;
                    if (blockEntity.getBlockState().getBlock() instanceof RouterBlock) continue;

                    Direction side = sideToward(pos, origin);
                    if (exposesAnything(serverLevel, pos, side)) found.add(pos.immutable());
                }
            }
        }

        found.sort(Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(origin)).thenComparingLong(BlockPos::asLong));
        int max = StartupConfig.distributorMaxTargets.get();
        if (found.size() > max) found = new ArrayList<>(found.subList(0, max));

        Map<BlockPos, Target> previous = new HashMap<>();
        for (Target target : targets) previous.put(target.pos, target);

        List<Target> refreshed = new ArrayList<>(found.size());
        for (BlockPos pos : found) {
            Target existing = previous.get(pos);
            refreshed.add(existing != null ? existing : new Target(pos, sideToward(pos, origin)));
        }
        targets = refreshed;
    }

    private static boolean exposesAnything(ServerLevel level, BlockPos pos, Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side) != null
                || level.getCapability(Capabilities.Item.BLOCK, pos, null) != null
                || level.getCapability(Capabilities.Fluid.BLOCK, pos, side) != null
                || level.getCapability(Capabilities.Fluid.BLOCK, pos, null) != null
                || level.getCapability(Capabilities.Energy.BLOCK, pos, side) != null
                || level.getCapability(Capabilities.Energy.BLOCK, pos, null) != null;
    }

    // the face of the machine that points at the distributor
    private static Direction sideToward(BlockPos machine, BlockPos distributor) {
        int dx = distributor.getX() - machine.getX();
        int dy = distributor.getY() - machine.getY();
        int dz = distributor.getZ() - machine.getZ();

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);

        if (ax >= ay && ax >= az) return dx > 0 ? Direction.EAST : Direction.WEST;
        if (ay >= az) return dy > 0 ? Direction.UP : Direction.DOWN;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // One machine in range, with its capabilities looked up once and then kept up to date by the cache.
    public static final class Target {
        private final BlockPos pos;
        private final Lookup<ResourceHandler<ItemResource>> items = new Lookup<>(Capabilities.Item.BLOCK);
        private final Lookup<ResourceHandler<FluidResource>> fluids = new Lookup<>(Capabilities.Fluid.BLOCK);
        private final Lookup<EnergyHandler> energy = new Lookup<>(Capabilities.Energy.BLOCK);
        private final Direction side;

        private Target(BlockPos pos, Direction side) {
            this.pos = pos;
            this.side = side;
        }

        @Nullable
        public ResourceHandler<ItemResource> items(ServerLevel level) {
            return items.get(level, pos, side);
        }

        @Nullable
        public ResourceHandler<FluidResource> fluids(ServerLevel level) {
            return fluids.get(level, pos, side);
        }

        @Nullable
        public EnergyHandler energy(ServerLevel level) {
            return energy.get(level, pos, side);
        }
    }

    // Looks for the capability on the face towards the distributor first, then on the unsided one.
    private static final class Lookup<T> {
        private final BlockCapability<T, Direction> capability;
        private BlockCapabilityCache<T, Direction> sided;
        private BlockCapabilityCache<T, Direction> unsided;

        private Lookup(BlockCapability<T, Direction> capability) {
            this.capability = capability;
        }

        @Nullable
        private T get(ServerLevel level, BlockPos pos, Direction side) {
            if (sided == null) sided = BlockCapabilityCache.create(capability, level, pos, side);
            T found = sided.getCapability();
            if (found != null) return found;

            if (unsided == null) unsided = BlockCapabilityCache.create(capability, level, pos, null);
            return unsided.getCapability();
        }
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        importerCore.save(output.child("linkedExporters"));
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        importerCore.load(input.childOrEmpty("linkedExporters"));
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        importerCore.unlinkFromExporters(pos);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.routers.distributor");
    }
}
