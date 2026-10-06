package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.NamedRouter;
import com.benbenlaw.routers.manager.ManagerScanner;
import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.routers.screen.RouterManagerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

// Sets the name a router shows in the Router Manager. An empty name clears it.
public record RenameRouterFromManager(BlockPos managerPos, BlockPos targetPos, String name) implements CustomPacketPayload {

    public static final Type<RenameRouterFromManager> TYPE = new Type<>(Routers.identifier("rename_router_from_manager"));

    public static final IPayloadHandler<RenameRouterFromManager> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!(player.containerMenu instanceof RouterManagerMenu menu)) return;
            if (!menu.getBlockPos().equals(packet.managerPos)) return;
            if (!(player.level() instanceof ServerLevel level)) return;
            if (!ManagerSessions.allow(player, "action", 4)) return;
            if (!apply(level, packet.managerPos, packet.targetPos, packet.name)) return;

            // send the renamed chart straight back instead of waiting for the next refresh
            PacketDistributor.sendToPlayer(player, ManagerScanner.scanFresh(level, packet.managerPos));
        });
    };

    // False if the router isn't part of the manager's network or can't be named.
    public static boolean apply(ServerLevel level, BlockPos managerPos, BlockPos targetPos, String name) {
        if (!ManagerScanner.isCharted(level, managerPos, targetPos)) return false;
        if (!(level.getBlockEntity(targetPos) instanceof NamedRouter router)) return false;

        // same cleanup an anvil does, so section signs and control characters can't sneak in
        router.setRouterName(StringUtil.filterText(name).strip());
        return true;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, RenameRouterFromManager> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RenameRouterFromManager::managerPos,
            BlockPos.STREAM_CODEC, RenameRouterFromManager::targetPos,
            ByteBufCodecs.stringUtf8(NamedRouter.MAX_NAME_LENGTH), RenameRouterFromManager::name,
            RenameRouterFromManager::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
