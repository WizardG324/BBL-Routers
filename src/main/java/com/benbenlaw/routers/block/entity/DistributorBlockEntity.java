package com.benbenlaw.routers.block.entity;

import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.transfers.RoutersTransfers;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributorBlockEntity extends ExporterBlockEntity implements ImporterHost {

    private final ImporterCore importerCore = new ImporterCore(this);

    private final Map<TransferModule<?>, Object> distributors = new HashMap<>();

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
    @SuppressWarnings("unchecked")
    public <H> H getDistributor(TransferModule<H> module) {
        if (!hasCorrectUpgrade(module.upgradeTag())) return null;
        return (H) distributors.computeIfAbsent(module, key -> module.createDistributor(this));
    }

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
        for (TransferModule<?> module : RoutersTransfers.TRANSFER_MODULES_REGISTRY) {
            if (exposes(level, module, pos, side)) return true;
        }
        return false;
    }

    private static <H> boolean exposes(ServerLevel level, TransferModule<H> module, BlockPos pos, Direction side) {
        return level.getCapability(module.capability(), pos, side) != null
                || level.getCapability(module.capability(), pos, null) != null;
    }

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

    public static final class Target {
        private final BlockPos pos;
        private final Direction side;
        private final Map<BlockCapability<?, Direction>, Lookup<?>> lookups = new HashMap<>();

        private Target(BlockPos pos, Direction side) {
            this.pos = pos;
            this.side = side;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <H> H get(BlockCapability<H, Direction> capability, ServerLevel level) {
            Lookup<H> lookup = (Lookup<H>) lookups.computeIfAbsent(capability, key -> new Lookup<>(capability));
            return lookup.get(level, pos, side);
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
