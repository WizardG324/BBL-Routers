package com.benbenlaw.routers.manager;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.custom.RouterManagerBlock;
import com.benbenlaw.routers.screen.ImporterExporterMenu;
import com.benbenlaw.routers.screen.ExporterMenu;
import com.benbenlaw.routers.screen.ImporterMenu;
import com.benbenlaw.routers.screen.RouterManagerMenu;
import com.benbenlaw.routers.screen.upgrade.FilterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Tracks a player who opened a router's menu from a Router Manager. While the session lasts the router's
// menus stay open even out of normal reach (as long as the player is still near the manager), and when the
// player closes the router's screens the manager screen is opened again.
@EventBusSubscriber(modid = Routers.MOD_ID)
public class ManagerSessions {

    private record Session(BlockPos manager, BlockPos target) {}

    private record Reopen(ServerPlayer player, BlockPos manager) {}

    private static final double MAX_MANAGER_DISTANCE_SQR = 64;

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final Set<UUID> SWITCHING = ConcurrentHashMap.newKeySet();
    private static final List<Reopen> PENDING_REOPEN = new ArrayList<>();

    private static final Map<UUID, Map<String, Integer>> LAST_ACTION = new ConcurrentHashMap<>();

    // The manager's packets can each cost a scan or a screen's worth of data, so a player can only send each kind so often.
    // Plenty fast for anyone using the screen by hand.
    public static boolean allow(ServerPlayer player, String action, int cooldownTicks) {
        int now = player.level().getServer().getTickCount();
        Map<String, Integer> actions = LAST_ACTION.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());

        Integer last = actions.get(action);
        if (last != null && now >= last && now - last < cooldownTicks) return false;

        actions.put(action, now);
        return true;
    }

    public static void begin(ServerPlayer player, BlockPos manager, BlockPos target) {
        SESSIONS.put(player.getUUID(), new Session(manager, target));
    }

    // Opens one of the mod's menus for a player. Use this (or switchMenu) instead of calling player.openMenu
    // directly so that moving between a router's own screens (main menu, filters, back) isn't mistaken for
    // the player closing them.
    public static void openMenu(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        switchMenu(player, () -> player.openMenu(new SimpleMenuProvider(provider, provider.getDisplayName()), pos));
    }

    public static void switchMenu(ServerPlayer player, Runnable action) {
        UUID id = player.getUUID();
        boolean alreadySwitching = !SWITCHING.add(id);
        try {
            action.run();
        } finally {
            if (!alreadySwitching) SWITCHING.remove(id);
        }
    }

    public static boolean allowsRemote(Player player, BlockPos menuPos) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !session.target.equals(menuPos)) return false;

        Level level = player.level();
        if (!(level.getBlockState(session.manager).getBlock() instanceof RouterManagerBlock)) return false;
        if (!(level.getBlockState(menuPos).getBlock() instanceof RouterBlock)) return false;

        return player.distanceToSqr(Vec3.atCenterOf(session.manager)) <= MAX_MANAGER_DISTANCE_SQR;
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID id = player.getUUID();
        Session session = SESSIONS.get(id);
        if (session == null || SWITCHING.contains(id)) return;

        AbstractContainerMenu closed = event.getContainer();

        if (closed instanceof RouterManagerMenu) {
            SESSIONS.remove(id);
            return;
        }

        boolean routerMenu = closed instanceof ExporterMenu || closed instanceof ImporterMenu
                || closed instanceof ImporterExporterMenu || closed instanceof FilterMenu;
        if (!routerMenu) return;

        SESSIONS.remove(id);

        if (!player.isAlive() || player.hasDisconnected()) return;
        if (!(player.level().getBlockState(session.manager).getBlock() instanceof RouterManagerBlock)) return;
        if (player.distanceToSqr(Vec3.atCenterOf(session.manager)) > MAX_MANAGER_DISTANCE_SQR) return;

        // Not opened here: this event fires partway through the close, and opening a menu now would have it
        // torn down again as soon as the close finishes. It's opened at the end of the tick instead.
        PENDING_REOPEN.add(new Reopen(player, session.manager));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_REOPEN.isEmpty()) return;

        List<Reopen> reopen = new ArrayList<>(PENDING_REOPEN);
        PENDING_REOPEN.clear();

        for (Reopen entry : reopen) {
            ServerPlayer player = entry.player;
            if (!player.isAlive() || player.hasDisconnected()) continue;
            if (!(player.level().getBlockState(entry.manager).getBlock() instanceof RouterManagerBlock)) continue;
            RouterManagerBlock.openFor(player, entry.manager);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID());
        SWITCHING.remove(event.getEntity().getUUID());
        LAST_ACTION.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SESSIONS.clear();
        SWITCHING.clear();
        LAST_ACTION.clear();
        PENDING_REOPEN.clear();
        ManagerScanner.clearCache();
    }
}
