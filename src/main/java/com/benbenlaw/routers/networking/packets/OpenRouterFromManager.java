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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record OpenRouterFromManager(BlockPos managerPos, BlockPos targetPos) implements CustomPacketPayload {

    public static final Type<OpenRouterFromManager> TYPE = new Type<>(Routers.identifier("open_router_from_manager"));

    public static final IPayloadHandler<OpenRouterFromManager> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!(player.containerMenu instanceof RouterManagerMenu menu)) return;
            if (!menu.getBlockPos().equals(packet.managerPos)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (!ManagerSessions.allow(player, "action", 4)) return;
            // only routers that are actually part of the charted network can be opened this way
            if (!ManagerScanner.isCharted(level, packet.managerPos, packet.targetPos)) return;

            BlockEntity blockEntity = level.getBlockEntity(packet.targetPos);
            if (!(blockEntity instanceof MenuProvider provider)) return;

            ManagerSessions.begin(player, packet.managerPos, packet.targetPos);
            ManagerSessions.openMenu(player, provider, packet.targetPos);
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRouterFromManager> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenRouterFromManager::managerPos,
            BlockPos.STREAM_CODEC, OpenRouterFromManager::targetPos,
            OpenRouterFromManager::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
