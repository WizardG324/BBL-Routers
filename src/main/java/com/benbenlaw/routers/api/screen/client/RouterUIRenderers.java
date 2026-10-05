package com.benbenlaw.routers.api.screen.client;

import com.benbenlaw.core.screen.util.FluidRenderingUtils;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RouterUIRenderers {
    private static final Map<Identifier, Renderer> RENDERERS = new HashMap<>();
    private static final float[] DEFAULT_COLOR = ButtonType.hex("A0A0A0");
    private static final Identifier SLOTS_9 = Routers.identifier("inventory_slots_9");

    public interface Renderer {
        default float[] color() {
            return DEFAULT_COLOR;
        }

        default void renderBackground(GuiGraphicsExtractor gui, FilterScreen screen) {}

        default void renderExtra(GuiGraphicsExtractor gui, FilterScreen screen, ConfigurableRouterBlockEntity entity, int mouseX, int mouseY) {}
    }

    public static void register(Identifier resource, Renderer renderer) {
        RENDERERS.put(resource, renderer);
    }

    public static float[] getColor(ButtonType type) {
        Renderer renderer = RENDERERS.get(type.getId());
        return renderer != null ? renderer.color() : DEFAULT_COLOR;
    }

    public static void init() {
        // Item Screen
        register(RouterButtonTypes.ITEM, new Renderer() {
            @Override public float[] color() { return ButtonType.hex("818181"); }

            @Override
            public void renderBackground(GuiGraphicsExtractor gui, FilterScreen screen) {
                gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_9, screen.getGuiLeft() + 7, screen.getGuiTop() + 35, 162, 18);
                gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_9, screen.getGuiLeft() + 7, screen.getGuiTop() + 53, 162, 18);
            }
            @Override public void renderExtra(GuiGraphicsExtractor g, FilterScreen s, ConfigurableRouterBlockEntity e, int mx, int my) {}
        });

        // Fluid Screen
        register(RouterButtonTypes.FLUID, new Renderer() {
            @Override public float[] color() { return ButtonType.hex("0A61B8"); }

            @Override
            public void renderBackground(GuiGraphicsExtractor gui, FilterScreen screen) {
                gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_9, screen.getGuiLeft() + 7, screen.getGuiTop() + 35, 162, 18);
                gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_9, screen.getGuiLeft() + 7, screen.getGuiTop() + 53, 162, 18);
            }

            @Override
            public void renderExtra(GuiGraphicsExtractor gui, FilterScreen screen, ConfigurableRouterBlockEntity entity, int mouseX, int mouseY) {
                for (int i = 0; i < 18; i++) {
                    int slotX = screen.getGuiLeft() + 8 + ((i % 9) * 18);
                    int slotY = screen.getGuiTop() + 36 + ((i / 9) * 18);
                    FluidRenderingUtils.renderFluidStack(gui, entity.getFilterFluidHandler().getFilter(i), slotX, slotY, 16, 16, mouseX, mouseY);

                    FluidRenderingUtils.renderFluidStackTooltip(gui, entity.getFilterFluidHandler().getFilter(i),
                            entity.getFilterFluidHandler(), i, slotX, slotY, 16, 16, mouseX, mouseY);
                }
            }
        });

        // Energy Screen
        register(RouterButtonTypes.ENERGY, new Renderer() {
            @Override public float[] color() { return ButtonType.hex("B70000"); }

            @Override public void renderBackground(GuiGraphicsExtractor gui, FilterScreen screen) {}

            @Override
            public void renderExtra(GuiGraphicsExtractor gui, FilterScreen screen, ConfigurableRouterBlockEntity entity, int mouseX, int mouseY) {
                if (!(entity instanceof ExporterBlockEntity exporter)) return;

                int rate = exporter.getUpgradeValue(RoutersTags.Items.RF_UPGRADES);
                int speed = exporter.getUpgradeValue(RoutersTags.Items.SPEED_UPGRADES);
                if (speed == 0) speed = StartupConfig.defaultSpeedPerOperation.get();

                Component text = Component.literal(rate + " FE / " + speed + " ticks").withStyle(ChatFormatting.DARK_GREEN);
                int centerX = screen.getGuiLeft() + (screen.getXSize() / 2) - (Minecraft.getInstance().font.width(text) / 2);
                gui.text(Minecraft.getInstance().font, text, centerX, screen.getGuiTop() + 45, 0xFFFFFFFF, false);
            }
        });
    }

    public static Optional<Renderer> get(ButtonType type) {
        return Optional.ofNullable(RENDERERS.get(type.getId()));
    }
}