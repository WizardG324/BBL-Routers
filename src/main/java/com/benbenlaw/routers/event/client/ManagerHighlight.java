package com.benbenlaw.routers.event.client;

import net.minecraft.core.GlobalPos;

import javax.annotation.Nullable;

// The router picked with a right click in the Router Manager, outlined in the world for a few seconds.
public class ManagerHighlight {

    private static final long DURATION_MILLIS = 5000;

    private static GlobalPos pos;
    private static long expiresAt;

    public static void show(GlobalPos position) {
        pos = position;
        expiresAt = System.currentTimeMillis() + DURATION_MILLIS;
    }

    @Nullable
    public static GlobalPos active() {
        if (pos != null && System.currentTimeMillis() >= expiresAt) pos = null;
        return pos;
    }
}
