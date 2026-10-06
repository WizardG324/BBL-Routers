package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.manager.ManagerScanner;
import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.routers.screen.RouterManagerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record RequestManagerSnapshot(BlockPos blockPos) implements CustomPacketPayload {

    public static final Type<RequestManagerSnapshot> TYPE = new Type<>(Routers.identifier("request_manager_snapshot"));

    public static final IPayloadHandler<RequestManagerSnapshot> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!(player.containerMenu instanceof RouterManagerMenu menu)) return;
            if (!menu.getBlockPos().equals(packet.blockPos)) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            // the screen asks twice a second at most; anything faster is ignored
            if (!ManagerSessions.allow(player, "request", 10)) return;

            PacketDistributor.sendToPlayer(player, ManagerScanner.scanCached(level, packet.blockPos));
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestManagerSnapshot> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RequestManagerSnapshot::blockPos,
            RequestManagerSnapshot::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
