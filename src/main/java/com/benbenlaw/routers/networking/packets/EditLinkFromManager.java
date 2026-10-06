package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.manager.ManagerScanner;
import com.benbenlaw.routers.manager.ManagerSnapshot;
import com.benbenlaw.routers.manager.ManagerSnapshot.Kind;
import com.benbenlaw.routers.manager.ManagerSnapshot.Node;
import com.benbenlaw.routers.screen.RouterManagerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

// Links or unlinks an exporter and an importer from the Router Manager, the same way the Connector does.
public record EditLinkFromManager(BlockPos managerPos, GlobalPos exporterPos, GlobalPos importerPos, boolean link) implements CustomPacketPayload {

    public static final Type<EditLinkFromManager> TYPE = new Type<>(Routers.identifier("edit_link_from_manager"));

    public static final IPayloadHandler<EditLinkFromManager> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!(player.containerMenu instanceof RouterManagerMenu menu)) return;
            if (!menu.getBlockPos().equals(packet.managerPos)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (packet.exporterPos.equals(packet.importerPos)) return;

            // both ends have to be in the network the player is looking at
            ManagerSnapshot snapshot = ManagerScanner.scan(level, packet.managerPos);
            Optional<Node> exporterNode = find(snapshot, packet.exporterPos);
            Optional<Node> importerNode = find(snapshot, packet.importerPos);
            if (exporterNode.isEmpty() || importerNode.isEmpty()) return;
            if (!exporterNode.get().kind().exports()) return;
            // a new link needs a loaded importer to point at; removing one may be cleaning up a missing router
            if (packet.link && !accepts(importerNode.get().kind())) return;

            ServerLevel exporterLevel = level.getServer().getLevel(packet.exporterPos.dimension());
            if (exporterLevel == null || !exporterLevel.isLoaded(packet.exporterPos.pos())) return;
            if (!(exporterLevel.getBlockEntity(packet.exporterPos.pos()) instanceof ExporterBlockEntity exporter)) return;
            if (exporter instanceof DistributorBlockEntity) return;

            // toggleImporterPosition flips the link and updates the importer too, so only call it when it changes something
            boolean linked = exporter.importerPositions != null && exporter.importerPositions.contains(packet.importerPos);
            if (linked != packet.link) exporter.toggleImporterPosition(packet.importerPos);

            PacketDistributor.sendToPlayer(player, ManagerScanner.scan(level, packet.managerPos));
        });
    };

    private static Optional<Node> find(ManagerSnapshot snapshot, GlobalPos pos) {
        return snapshot.nodes().stream().filter(node -> node.pos().equals(pos)).findFirst();
    }

    // the same targets the Connector allows an exporter to link to
    public static boolean accepts(Kind kind) {
        return kind == Kind.IMPORTER || kind == Kind.IMPORTER_EXPORTER || kind == Kind.DISTRIBUTOR;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, EditLinkFromManager> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, EditLinkFromManager::managerPos,
            GlobalPos.STREAM_CODEC, EditLinkFromManager::exporterPos,
            GlobalPos.STREAM_CODEC, EditLinkFromManager::importerPos,
            ByteBufCodecs.BOOL, EditLinkFromManager::link,
            EditLinkFromManager::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
