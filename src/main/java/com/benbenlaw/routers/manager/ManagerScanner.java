package com.benbenlaw.routers.manager;

import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.block.entity.ImporterHost;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.manager.ManagerSnapshot.Edge;
import com.benbenlaw.routers.manager.ManagerSnapshot.Kind;
import com.benbenlaw.routers.manager.ManagerSnapshot.Node;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManagerScanner {

    private record Link(GlobalPos exporter, GlobalPos importer) {}

    // Finds the loaded routers around the manager, then follows their links outwards (across
    // dimensions, loaded or not) so the whole connected network ends up in the snapshot.
    public static ManagerSnapshot scan(ServerLevel origin, BlockPos center) {
        int radius = StartupConfig.managerSeedRadius.get();
        int maxRouters = StartupConfig.managerMaxRouters.get();
        MinecraftServer server = origin.getServer();

        List<GlobalPos> seeds = new ArrayList<>();
        int minChunkX = (center.getX() - radius) >> 4;
        int maxChunkX = (center.getX() + radius) >> 4;
        int minChunkZ = (center.getZ() - radius) >> 4;
        int maxChunkZ = (center.getZ() + radius) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = origin.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!isRouter(blockEntity)) continue;
                    if (blockEntity.getBlockPos().distSqr(center) > (double) radius * radius) continue;
                    seeds.add(GlobalPos.of(origin.dimension(), blockEntity.getBlockPos()));
                }
            }
        }

        seeds.sort(positionOrder());

        Map<GlobalPos, Node> nodes = new HashMap<>();
        Set<Link> links = new LinkedHashSet<>();
        Set<GlobalPos> queued = new LinkedHashSet<>();
        Deque<GlobalPos> queue = new ArrayDeque<>();
        boolean truncated = false;

        for (GlobalPos seed : seeds) {
            if (queued.size() >= maxRouters) {
                truncated = true;
                break;
            }
            if (queued.add(seed)) queue.add(seed);
        }

        while (!queue.isEmpty()) {
            GlobalPos pos = queue.poll();
            ServerLevel level = server.getLevel(pos.dimension());
            BlockEntity blockEntity = level != null && level.isLoaded(pos.pos()) ? level.getBlockEntity(pos.pos()) : null;

            nodes.put(pos, describe(pos, level, blockEntity));

            List<GlobalPos> neighbours = new ArrayList<>();

            if (blockEntity instanceof ExporterBlockEntity exporter && exporter.importerPositions != null) {
                for (GlobalPos importerPos : exporter.importerPositions) {
                    links.add(new Link(pos, importerPos));
                    neighbours.add(importerPos);
                }
            }

            if (blockEntity instanceof ImporterHost importerHost) {
                ImporterCore core = importerHost.getImporterCore();
                if (core.exporterPositions != null) {
                    for (GlobalPos exporterPos : core.exporterPositions) {
                        links.add(new Link(exporterPos, pos));
                        neighbours.add(exporterPos);
                    }
                }
            }

            for (GlobalPos neighbour : neighbours) {
                if (queued.contains(neighbour)) continue;
                if (queued.size() >= maxRouters) {
                    truncated = true;
                    continue;
                }
                queued.add(neighbour);
                queue.add(neighbour);
            }
        }

        List<GlobalPos> ordered = new ArrayList<>(nodes.keySet());
        ordered.sort(positionOrder());

        Map<GlobalPos, Integer> indices = new HashMap<>();
        List<Node> nodeList = new ArrayList<>(ordered.size());
        for (GlobalPos pos : ordered) {
            indices.put(pos, nodeList.size());
            nodeList.add(nodes.get(pos));
        }

        List<Edge> edges = new ArrayList<>();
        for (Link link : links) {
            Integer from = indices.get(link.exporter);
            Integer to = indices.get(link.importer);
            if (from == null || to == null) continue;
            edges.add(new Edge(from, to, nodeList.get(from).exporterTypes(), false));
        }

        addSharedInventoryLinks(server, nodeList, edges);

        return new ManagerSnapshot(nodeList, edges, truncated);
    }

    // An importer and an exporter on the same inventory pass items along through it, so chart that as a link too.
    private static void addSharedInventoryLinks(MinecraftServer server, List<Node> nodes, List<Edge> edges) {
        Map<GlobalPos, List<Integer>> importersByInventory = new HashMap<>();
        Map<GlobalPos, List<Integer>> exportersByInventory = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.kind() == Kind.UNLOADED) continue;

            GlobalPos inventory = inventoryKey(server, node.pos());
            if (inventory == null) continue;

            if (node.kind().imports()) importersByInventory.computeIfAbsent(inventory, key -> new ArrayList<>()).add(i);
            if (node.kind().exports()) exportersByInventory.computeIfAbsent(inventory, key -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<GlobalPos, List<Integer>> entry : importersByInventory.entrySet()) {
            for (int importer : entry.getValue()) {
                for (int exporter : exportersByInventory.getOrDefault(entry.getKey(), List.of())) {
                    if (importer == exporter) continue;
                    edges.add(new Edge(importer, exporter, nodes.get(exporter).exporterTypes(), true));
                }
            }
        }
    }

    // Identifies the inventory a router faces. Both halves of a double chest count as the same inventory.
    private static GlobalPos inventoryKey(MinecraftServer server, GlobalPos routerPos) {
        ServerLevel level = server.getLevel(routerPos.dimension());
        if (level == null) return null;

        BlockState routerState = level.getBlockState(routerPos.pos());
        if (!routerState.hasProperty(RouterBlock.FACING)) return null;

        BlockPos target = routerPos.pos().relative(routerState.getValue(RouterBlock.FACING));
        if (!level.isLoaded(target)) return null;

        BlockState targetState = level.getBlockState(target);
        if (targetState.isAir()) return null;

        if (targetState.getBlock() instanceof ChestBlock && targetState.hasProperty(ChestBlock.TYPE)
                && targetState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos other = target.relative(ChestBlock.getConnectedDirection(targetState));
            if (other.asLong() < target.asLong()) target = other;
        }

        return GlobalPos.of(routerPos.dimension(), target);
    }

    private static boolean isRouter(BlockEntity blockEntity) {
        return blockEntity instanceof ExporterBlockEntity || blockEntity instanceof ImporterHost;
    }

    private static Comparator<GlobalPos> positionOrder() {
        return Comparator.<GlobalPos, String>comparing(pos -> pos.dimension().identifier().toString())
                .thenComparingLong(pos -> pos.pos().asLong());
    }

    private static Node describe(GlobalPos pos, ServerLevel level, BlockEntity blockEntity) {
        if (level == null || blockEntity == null || !isRouter(blockEntity)) {
            return new Node(pos, Kind.UNLOADED, ItemStack.EMPTY, 0, 0, 0, 0, true);
        }

        BlockState state = blockEntity.getBlockState();

        ItemStack adjacent = ItemStack.EMPTY;
        if (state.hasProperty(RouterBlock.FACING)) {
            BlockPos adjacentPos = pos.pos().relative(state.getValue(RouterBlock.FACING));
            if (level.isLoaded(adjacentPos)) {
                BlockState adjacentState = level.getBlockState(adjacentPos);
                if (!adjacentState.isAir()) adjacent = new ItemStack(adjacentState.getBlock().asItem());
            }
        }

        boolean working = !state.hasProperty(RouterBlock.WORKING) || state.getValue(RouterBlock.WORKING);

        int exporterTypes = 0;
        int exporterFlags = 0;
        int importerTypes = 0;
        int importerFlags = 0;
        Kind kind;

        if (blockEntity instanceof DistributorBlockEntity distributor) {
            exporterTypes = types(distributor);
            exporterFlags = exporterFlags(distributor);
            kind = Kind.DISTRIBUTOR;
            adjacent = ItemStack.EMPTY;
        } else if (blockEntity instanceof ExporterBlockEntity exporter) {
            exporterTypes = types(exporter);
            exporterFlags = exporterFlags(exporter);
            kind = blockEntity instanceof ImporterExporterBlockEntity ? Kind.IMPORTER_EXPORTER : Kind.EXPORTER;
        } else {
            kind = Kind.IMPORTER;
        }

        if (kind != Kind.DISTRIBUTOR && blockEntity instanceof ImporterHost importerHost) {
            importerTypes = types(importerHost.getImporterCore());
            importerFlags = importerFlags(importerHost.getImporterCore());
        }

        return new Node(pos, kind, adjacent, exporterTypes, exporterFlags, importerTypes, importerFlags, working);
    }

    // Which resources a router carries, one bit per registered resource.
    private static int types(ConfigurableRouterBlockEntity router) {
        int mask = 0;
        for (ButtonType type : RouterButtonTypes.all()) {
            if (router.hasUpgrade(type)) mask |= RoutersTransfers.bit(type.getId());
        }
        return mask;
    }

    private static int exporterFlags(ExporterBlockEntity exporter) {
        int flags = 0;
        if (exporter.isRoundRobin) flags |= ManagerSnapshot.ROUND_ROBIN;
        if (exporter.canDoDimensionalTravel()) flags |= ManagerSnapshot.DIMENSIONAL;
        if (exporter.isBlacklist()) flags |= ManagerSnapshot.BLACKLIST;
        if (exporter.isIgnoreNbt()) flags |= ManagerSnapshot.IGNORE_NBT;
        if (!ResourceHandlerUtil.isEmpty(exporter.getFilterItemHandler()) || !ResourceHandlerUtil.isEmpty(exporter.getFilterFluidHandler())) {
            flags |= ManagerSnapshot.FILTERED;
        }
        return flags;
    }

    private static int importerFlags(ImporterCore importer) {
        int flags = 0;
        if (importer.isRoundRobin) flags |= ManagerSnapshot.ROUND_ROBIN;
        if (importer.isBlacklist()) flags |= ManagerSnapshot.BLACKLIST;
        if (importer.isIgnoreNbt()) flags |= ManagerSnapshot.IGNORE_NBT;
        if (!ResourceHandlerUtil.isEmpty(importer.getFilterItemHandler()) || !ResourceHandlerUtil.isEmpty(importer.getFilterFluidHandler())) {
            flags |= ManagerSnapshot.FILTERED;
        }
        return flags;
    }
}
