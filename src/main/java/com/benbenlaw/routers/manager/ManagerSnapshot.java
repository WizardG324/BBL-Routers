package com.benbenlaw.routers.manager;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.NamedRouter;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ManagerSnapshot(List<Node> nodes, List<Edge> edges, boolean truncated) implements CustomPacketPayload {

    public static final Type<ManagerSnapshot> TYPE = new Type<>(Routers.identifier("manager_snapshot"));

    public static final int ITEM = 1;
    public static final int FLUID = 2;
    public static final int ENERGY = 4;
    public static final int ROUND_ROBIN = 8;
    public static final int DIMENSIONAL = 16;
    public static final int BLACKLIST = 32;
    public static final int IGNORE_NBT = 64;
    public static final int FILTERED = 128;
    public static final int TYPE_MASK = ITEM | FLUID | ENERGY;

    public enum Kind {
        EXPORTER, IMPORTER, IMPORTER_EXPORTER, DISTRIBUTOR, UNLOADED;

        public boolean exports() {
            return this == EXPORTER || this == IMPORTER_EXPORTER;
        }

        public boolean imports() {
            return this == IMPORTER || this == IMPORTER_EXPORTER;
        }
    }

    // name is the player-given name from the Router Manager, empty when unnamed
    public record Node(GlobalPos pos, Kind kind, ItemStack adjacent, int exporterFlags, int importerFlags, boolean working, String name) {}

    // types is the ITEM/FLUID/ENERGY mask of what the exporting end can send down this link.
    // viaInventory marks an importer feeding an exporter that sits on the same inventory.
    public record Edge(int from, int to, int types, boolean viaInventory) {}

    public static final StreamCodec<RegistryFriendlyByteBuf, ManagerSnapshot> STREAM_CODEC = StreamCodec.of(
            (buf, snapshot) -> {
                buf.writeVarInt(snapshot.nodes.size());
                for (Node node : snapshot.nodes) {
                    GlobalPos.STREAM_CODEC.encode(buf, node.pos);
                    buf.writeEnum(node.kind);
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, node.adjacent);
                    buf.writeVarInt(node.exporterFlags);
                    buf.writeVarInt(node.importerFlags);
                    buf.writeBoolean(node.working);
                    buf.writeUtf(node.name, NamedRouter.MAX_NAME_LENGTH);
                }
                buf.writeVarInt(snapshot.edges.size());
                for (Edge edge : snapshot.edges) {
                    buf.writeVarInt(edge.from);
                    buf.writeVarInt(edge.to);
                    buf.writeVarInt(edge.types);
                    buf.writeBoolean(edge.viaInventory);
                }
                buf.writeBoolean(snapshot.truncated);
            },
            buf -> {
                int nodeCount = buf.readVarInt();
                List<Node> nodes = new ArrayList<>(nodeCount);
                for (int i = 0; i < nodeCount; i++) {
                    nodes.add(new Node(
                            GlobalPos.STREAM_CODEC.decode(buf),
                            buf.readEnum(Kind.class),
                            ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readBoolean(),
                            buf.readUtf(NamedRouter.MAX_NAME_LENGTH)));
                }
                int edgeCount = buf.readVarInt();
                List<Edge> edges = new ArrayList<>(edgeCount);
                for (int i = 0; i < edgeCount; i++) {
                    edges.add(new Edge(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean()));
                }
                return new ManagerSnapshot(nodes, edges, buf.readBoolean());
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
