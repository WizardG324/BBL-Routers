package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.screen.upgrade.FilterMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RoutersMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Routers.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExporterMenu>> EXPORTER_MENU =
            MENUS.register("exporter_menu", () -> IMenuTypeExtension.create(ExporterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ImporterMenu>> IMPORTER_MENU =
            MENUS.register("importer_menu", () -> IMenuTypeExtension.create(ImporterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ImporterExporterMenu>> IMPORTER_EXPORTER_MENU =
            MENUS.register("importer_exporter_menu", () -> IMenuTypeExtension.create(ImporterExporterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RouterManagerMenu>> ROUTER_MANAGER_MENU =
            MENUS.register("router_manager_menu", () -> IMenuTypeExtension.create(RouterManagerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FilterMenu>> FILTER_MENU =
            MENUS.register("item_filter_menu", () -> IMenuTypeExtension.create(FilterMenu::new));





}
