package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.manager.ManagerSessions;
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
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record OpenMenu(BlockPos blockPos, ButtonType buttonType) implements CustomPacketPayload {

    public static final Type<OpenMenu> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Routers.MOD_ID, "open_menu"));

    public static final IPayloadHandler<OpenMenu> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (packet.buttonType != null) {
                ManagerSessions.switchMenu(player, () -> player.openMenu(new SimpleMenuProvider((windowId, playerInventory, playerEntity) ->
                        new FilterMenu(windowId, playerInventory, packet.blockPos, packet.buttonType, new SimpleContainerData(2)),
                        Component.translatable(packet.buttonType.getMenuName())
                ), buf -> {
                    buf.writeBlockPos(packet.blockPos);
                    buf.writeIdentifier(packet.buttonType.getId());
                }));
            }
        });
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMenu> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenMenu::blockPos,
            Identifier.STREAM_CODEC.map(
                    RouterButtonTypes.BUTTONS::get,
                    ButtonType::getId
            ), OpenMenu::buttonType,
            OpenMenu::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}