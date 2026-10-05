package com.benbenlaw.routers.api;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RouterButtonTypes {

    public static final Identifier ITEM = Routers.identifier("item");
    public static final Identifier FLUID = Routers.identifier("fluid");
    public static final Identifier ENERGY = Routers.identifier("energy");

    private static Map<Identifier, ButtonType> cache = Map.of();
    private static int builtFor = -1;

    private static synchronized Map<Identifier, ButtonType> buttons() {
        int size = RoutersTransfers.TRANSFER_MODULES_REGISTRY.size();

        if (size != builtFor) {
            Map<Identifier, ButtonType> built = new LinkedHashMap<>();
            for (TransferModule<?> module : RoutersTransfers.TRANSFER_MODULES_REGISTRY) {
                Identifier id = RoutersTransfers.TRANSFER_MODULES_REGISTRY.getKey(module);
                if (id != null) built.put(id, new ButtonType(id, module.upgradeTag()));
            }
            cache = built;
            builtFor = size;
        }

        return cache;
    }

    public static Collection<ButtonType> all() {
        return buttons().values();
    }

    @Nullable
    public static ButtonType get(Identifier id) {
        return buttons().get(id);
    }

    public static int count() {
        return buttons().size();
    }
}
