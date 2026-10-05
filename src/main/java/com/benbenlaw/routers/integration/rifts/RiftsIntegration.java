package com.benbenlaw.routers.integration.rifts;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.api.screen.client.RouterUIRenderers;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.item.UpgradeItem;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class RiftsIntegration {

    public static final Identifier RIFT_ENERGY = Routers.identifier("rift_energy");

    private static final DeferredRegister<TransferModule<?>> TRANSFER_MODULES =
            DeferredRegister.create(RoutersTransfers.TRANSFER_MODULE_KEY, Routers.MOD_ID);

    public static final DeferredHolder<TransferModule<?>, RiftEnergyTransfer> RIFT_ENERGY_TRANSFER =
            TRANSFER_MODULES.register("rift_energy", RiftEnergyTransfer::new);

    public static final List<DeferredItem<Item>> RIFT_ENERGY_UPGRADES = List.of(
            riftEnergyUpgrade(1, StartupConfig.riftEnergyPerOperation1),
            riftEnergyUpgrade(2, StartupConfig.riftEnergyPerOperation2),
            riftEnergyUpgrade(3, StartupConfig.riftEnergyPerOperation3),
            riftEnergyUpgrade(4, StartupConfig.riftEnergyPerOperation4),
            riftEnergyUpgrade(5, StartupConfig.riftEnergyPerOperation5)
    );

    public static void register(IEventBus eventBus) {
        TRANSFER_MODULES.register(eventBus);
    }

    public static void clientInit() {
        RouterUIRenderers.register(RIFT_ENERGY, new RouterUIRenderers.Renderer() {
            @Override
            public float[] color() {
                return ButtonType.hex("B070F0");
            }

            @Override
            public void renderExtra(GuiGraphicsExtractor gui, FilterScreen screen, ConfigurableRouterBlockEntity entity, int mouseX, int mouseY) {
                if (!(entity instanceof ExporterBlockEntity exporter)) return;

                int rate = exporter.getUpgradeValue(RoutersTags.Items.RIFT_ENERGY_UPGRADES);
                int speed = exporter.getUpgradeValue(RoutersTags.Items.SPEED_UPGRADES);
                if (speed == 0) speed = StartupConfig.defaultSpeedPerOperation.get();

                Component text = Component.literal(rate + " Rift / " + speed + " ticks").withStyle(ChatFormatting.DARK_PURPLE);
                int centerX = screen.getGuiLeft() + (screen.getXSize() / 2) - (Minecraft.getInstance().font.width(text) / 2);
                gui.text(Minecraft.getInstance().font, text, centerX, screen.getGuiTop() + 45, 0xFFFFFFFF, false);
            }
        });
    }

    private static DeferredItem<Item> riftEnergyUpgrade(int tier, ModConfigSpec.ConfigValue<Integer> amount) {
        String name = "rift_energy_upgrade_" + tier;
        return RoutersItems.ITEMS.registerItem(name,
                properties -> new UpgradeItem(new Item.Properties().setId(RoutersItems.createID(name)), amount.get()));
    }
}
