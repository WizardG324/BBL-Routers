package com.benbenlaw.routers.networking;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.manager.ManagerSnapshot;
import com.benbenlaw.routers.networking.packets.*;
import com.benbenlaw.routers.screen.RouterManagerClientHandler;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RoutersNetworking {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Routers.MOD_ID);

        registrar.playToServer(OpenMenu.TYPE, OpenMenu.STREAM_CODEC, OpenMenu.HANDLER);
        registrar.playToServer(BackMenu.TYPE, BackMenu.STREAM_CODEC, BackMenu.HANDLER);
        registrar.playToServer(ToggleHybridSide.TYPE, ToggleHybridSide.STREAM_CODEC, ToggleHybridSide.HANDLER);
        registrar.playToServer(RequestManagerSnapshot.TYPE, RequestManagerSnapshot.STREAM_CODEC, RequestManagerSnapshot.HANDLER);
        registrar.playToServer(OpenRouterFromManager.TYPE, OpenRouterFromManager.STREAM_CODEC, OpenRouterFromManager.HANDLER);
        registrar.playToServer(RenameRouterFromManager.TYPE, RenameRouterFromManager.STREAM_CODEC, RenameRouterFromManager.HANDLER);
        registrar.playToServer(EditLinkFromManager.TYPE, EditLinkFromManager.STREAM_CODEC, EditLinkFromManager.HANDLER);
        registrar.playToClient(ManagerSnapshot.TYPE, ManagerSnapshot.STREAM_CODEC, RouterManagerClientHandler::handle);

        registrar.playToServer(SyncFilterValue.TYPE, SyncFilterValue.STREAM_CODEC, SyncFilterValue.HANDLER);
        registrar.playToServer(SyncStockFilter.TYPE, SyncStockFilter.STREAM_CODEC, SyncStockFilter.HANDLER);

    }
}
