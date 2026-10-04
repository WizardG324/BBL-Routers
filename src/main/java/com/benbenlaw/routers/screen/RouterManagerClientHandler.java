package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.manager.ManagerSnapshot;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RouterManagerClientHandler {

    public static void handle(ManagerSnapshot snapshot, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof RouterManagerScreen screen) {
                screen.setSnapshot(snapshot);
            }
        });
    }
}
