package com.benbenlaw.routers;

import com.benbenlaw.routers.api.screen.RouterUIRegistries;
import com.benbenlaw.routers.api.screen.client.RouterUIRenderers;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.renderer.ExporterBlockEntityRenderer;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.gametest.RoutersGameTests;
import com.benbenlaw.routers.integration.rifts.RiftsIntegration;
import com.benbenlaw.routers.item.RoutersCreativeTab;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.networking.RoutersNetworking;
import com.benbenlaw.routers.screen.*;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Routers.MOD_ID)
public class Routers {
    public static final String MOD_ID = "routers";
    public static final Logger LOGGER = LogManager.getLogger();

    public Routers(final IEventBus eventBus, final ModContainer modContainer) {

        RoutersBlocks.BLOCKS.register(eventBus);
        RoutersBlockEntities.BLOCK_ENTITIES.register(eventBus);
        RoutersItems.ITEMS.register(eventBus);
        RoutersDataComponents.COMPONENTS.register(eventBus);
        RoutersCreativeTab.CREATIVE_MODE_TABS.register(eventBus);
        RoutersMenuTypes.MENUS.register(eventBus);
        RoutersTransfers.TRANSFER_MODULES.register(eventBus);
        RouterUIRegistries.SCREEN_MODULES.register(eventBus);
        RoutersGameTests.TEST_FUNCTIONS.register(eventBus);

        eventBus.addListener(this::commonSetup);
        eventBus.addListener(RoutersGameTests::registerTests);
        eventBus.addListener(RoutersGameTests::registerTestCapabilities);

        if (ModList.get().isLoaded("rifts")) {
            RiftsIntegration.register(eventBus);
        }

        if (Dist.CLIENT.isClient()) {
            eventBus.addListener(this::onClientSetup);
        }

        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC, "bbl/routers-startup.toml");
    }

    public void commonSetup(RegisterPayloadHandlersEvent event) {
        RoutersNetworking.registerNetworking(event);

    }

    @EventBusSubscriber(modid = Routers.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(RoutersBlockEntities.EXPORTER_BLOCK_ENTITY.get(), ExporterBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(RoutersBlockEntities.IMPORTER_EXPORTER_BLOCK_ENTITY.get(), ExporterBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(RoutersMenuTypes.EXPORTER_MENU.get(), ExporterScreen::new);
            event.register(RoutersMenuTypes.IMPORTER_MENU.get(), ImporterScreen::new);
            event.register(RoutersMenuTypes.IMPORTER_EXPORTER_MENU.get(), ImporterExporterScreen::new);
            event.register(RoutersMenuTypes.ROUTER_MANAGER_MENU.get(), RouterManagerScreen::new);
            event.register(RoutersMenuTypes.FILTER_MENU.get(), FilterScreen::new);
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(RouterUIRenderers::init);

        if (ModList.get().isLoaded("rifts")) {
            event.enqueueWork(RiftsIntegration::clientInit);
        }
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
