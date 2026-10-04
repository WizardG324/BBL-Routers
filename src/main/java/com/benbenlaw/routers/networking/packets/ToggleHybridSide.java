package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.screen.ImporterExporterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record ToggleHybridSide(BlockPos blockPos) implements CustomPacketPayload {

    public static final Type<ToggleHybridSide> TYPE = new Type<>(Routers.identifier("toggle_hybrid_side"));

    public static final IPayloadHandler<ToggleHybridSide> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!(player.containerMenu instanceof ImporterExporterMenu menu)) return;
            if (!menu.getBlockEntity().getBlockPos().equals(packet.blockPos)) return;

            ImporterExporterBlockEntity hybrid = menu.getBlockEntity();
            hybrid.toggleViewedSide();
            ManagerSessions.openMenu(player, hybrid, packet.blockPos);
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleHybridSide> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleHybridSide::blockPos,
            ToggleHybridSide::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
