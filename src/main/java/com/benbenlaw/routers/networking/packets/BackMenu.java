package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.screen.ExporterMenu;
import com.benbenlaw.routers.screen.upgrade.FilterMenu;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record BackMenu(BlockPos blockPos) implements CustomPacketPayload {

    public static final Type<BackMenu> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Routers.MOD_ID, "back_menu"));

    public static final IPayloadHandler<BackMenu> HANDLER = (packet, context) -> {

        ServerPlayer player = (ServerPlayer) context.player();

        BlockEntity entity = player.level().getBlockEntity(packet.blockPos);

        if (entity instanceof ExporterBlockEntity entity1) {
            ManagerSessions.openMenu(player, entity1, packet.blockPos);
        } else if (entity instanceof ImporterBlockEntity entity1) {
            ManagerSessions.openMenu(player, entity1, packet.blockPos);
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, BackMenu> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BackMenu::blockPos,
            BackMenu::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
