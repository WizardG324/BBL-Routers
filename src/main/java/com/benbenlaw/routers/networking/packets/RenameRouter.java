package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.NamedRouter;
import com.benbenlaw.routers.manager.ManagerSessions;
import com.benbenlaw.routers.screen.ExporterMenu;
import com.benbenlaw.routers.screen.ImporterExporterMenu;
import com.benbenlaw.routers.screen.ImporterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.Nullable;

// Sets the name of the router whose own screen the player has open. An empty name clears it.
public record RenameRouter(BlockPos pos, String name) implements CustomPacketPayload {

    public static final Type<RenameRouter> TYPE = new Type<>(Routers.identifier("rename_router"));

    public static final IPayloadHandler<RenameRouter> HANDLER = (packet, context) -> {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!packet.pos.equals(menuPos(player.containerMenu))) return;
            if (!ManagerSessions.allow(player, "rename", 4)) return;
            if (!(player.level().getBlockEntity(packet.pos) instanceof NamedRouter router)) return;

            // same cleanup an anvil does, so section signs and control characters can't sneak in
            router.setRouterName(StringUtil.filterText(packet.name).strip());
        });
    };

    // The block a router menu belongs to, or null if the menu isn't a router's own.
    @Nullable
    private static BlockPos menuPos(AbstractContainerMenu menu) {
        if (menu instanceof ExporterMenu exporter) return exporter.getBlockPos();
        if (menu instanceof ImporterMenu importer) return importer.getBlockPos();
        if (menu instanceof ImporterExporterMenu hybrid) return hybrid.getBlockPos();
        return null;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, RenameRouter> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RenameRouter::pos,
            ByteBufCodecs.stringUtf8(NamedRouter.MAX_NAME_LENGTH), RenameRouter::name,
            RenameRouter::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
