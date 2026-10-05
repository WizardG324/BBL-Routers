package com.benbenlaw.routers.api.screen;

import com.benbenlaw.core.screen.util.slot.FilterFluidSlot;
import com.benbenlaw.core.screen.util.slot.FilterSlot;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.RouterButtonTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RouterUIRegistries {
    public static final ResourceKey<Registry<ScreenModule>> SCREEN_MODULE_KEY =
            ResourceKey.createRegistryKey(Routers.identifier("screen_modules"));

    public static final DeferredRegister<ScreenModule> SCREEN_MODULES =
            DeferredRegister.create(SCREEN_MODULE_KEY, Routers.MOD_ID);

    public static final Registry<ScreenModule> SCREEN_MODULES_REGISTRY = SCREEN_MODULES.makeRegistry(builder ->
            builder.sync(false)
    );

    // Item Menu
    public static final DeferredHolder<ScreenModule, ScreenModule> ITEM_FILTER =
            SCREEN_MODULES.register("item_filter", () -> new ScreenModule(
                    RouterButtonTypes.ITEM,
                    (menu, entity, x) -> {
                        for (int i = 0; i < 18; i++) {
                            int row = i / 9;
                            int col = i % 9;
                            menu.addSlotPublic(new FilterSlot(entity.getFilterItemHandler(),
                                    entity.getFilterItemHandler()::set, i, 8 + col * 18, 36 + row * 18));
                        }
                    }
            ));

    // Fluid Menu
    public static final DeferredHolder<ScreenModule, ScreenModule> FLUID_FILTER =
            SCREEN_MODULES.register("fluid_filter", () -> new ScreenModule(
                    RouterButtonTypes.FLUID,
                    (menu, entity, x) -> {
                        SimpleContainer dummy = new SimpleContainer(18);
                        for (int i = 0; i < 18; i++) {
                            int row = i / 9;
                            int col = i % 9;
                            menu.addSlotPublic(new FilterFluidSlot(dummy,
                                    entity.getFilterFluidHandler(), i, 8 + col * 18, 36 + row * 18));
                        }
                    }
            ));

    // Energy Menu
    public static final DeferredHolder<ScreenModule, ScreenModule> ENERGY_FILTER =
            SCREEN_MODULES.register("energy_filter", () -> new ScreenModule(
                    RouterButtonTypes.ENERGY,
                    (menu, entity, x) -> {
                        // Keep empty if only rendering text on the client
                    }
            ));
}