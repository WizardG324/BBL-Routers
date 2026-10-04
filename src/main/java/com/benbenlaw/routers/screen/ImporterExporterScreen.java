package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.networking.packets.ToggleHybridSide;
import com.benbenlaw.routers.screen.util.MousePositionManagerUtil;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.screen.util.button.FilterButton;
import com.benbenlaw.routers.screen.util.button.SideToggleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImporterExporterScreen extends AbstractContainerScreen<ImporterExporterMenu> {

    private static final Identifier EXPORTER_TEXTURE = Routers.identifier("textures/gui/exporter_gui.png");
    private static final Identifier IMPORTER_TEXTURE = Routers.identifier("textures/gui/importer_gui.png");

    private final Map<ButtonType, FilterButton> filterButtons = new HashMap<>();

    public ImporterExporterScreen(ImporterExporterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, Component.translatable(menu.importerSide
                ? "gui.routers.importer_exporter.title.importer"
                : "gui.routers.importer_exporter.title.exporter"));
    }

    @Override
    protected void init() {
        super.init();

        filterButtons.clear();

        if (MousePositionManagerUtil.lastMouseX != -1) {
            MousePositionManagerUtil.setLastKnownPosition();
        }

        addRenderableWidget(new SideToggleButton(guiLeft() + 151, guiTop() + 4, menu.importerSide,
                button -> ClientPacketDistributor.sendToServer(new ToggleHybridSide(menu.getBlockEntity().getBlockPos()))));

        updateButtons();
    }

    private int guiLeft() {
        return (width - imageWidth) / 2;
    }

    private int guiTop() {
        return (height - imageHeight) / 2;
    }

    private void updateButtons() {
        int baseX = guiLeft();
        int baseY = guiTop();

        int BUTTON_SIZE = 20;
        int BUTTON_SPACING = 19;
        int BUTTON_Y = 30;

        ConfigurableRouterBlockEntity configurable = menu.getBlockEntity().getActiveConfigurable();
        boolean importerSide = menu.importerSide;

        long visibleCount = RouterButtonTypes.BUTTONS.values().stream()
                .filter(type -> importerSide || configurable.hasUpgrade(type))
                .count();

        int totalWidth = (int) (visibleCount * BUTTON_SPACING - (visibleCount > 0 ? (BUTTON_SPACING - BUTTON_SIZE) : 0));
        int startX = baseX + (imageWidth - totalWidth) / 2 + 1;

        int index = 0;

        for (ButtonType type : RouterButtonTypes.BUTTONS.values()) {
            boolean visible = importerSide || configurable.hasUpgrade(type);

            if (visible) {
                int x = startX + index * BUTTON_SPACING;
                int y = baseY + BUTTON_Y;

                if (!filterButtons.containsKey(type)) {
                    FilterButton button = importerSide
                            ? FilterButton.createAlwaysVisible(x, y, BUTTON_SIZE, BUTTON_SIZE, menu.getBlockEntity().getBlockPos(), configurable, type)
                            : FilterButton.create(x, y, BUTTON_SIZE, BUTTON_SIZE, menu.getBlockEntity().getBlockPos(), configurable, type);

                    if (button != null) {
                        addRenderableWidget(button);
                        filterButtons.put(type, button);
                    }
                } else {
                    filterButtons.get(type).setPosition(x, y);
                }
                index++;
            } else if (filterButtons.containsKey(type)) {
                removeWidget(filterButtons.get(type));
                filterButtons.remove(type);
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);

        for (FilterButton button : filterButtons.values()) {
            if (button.isHovered()) {
                String tooltipKey = menu.importerSide && button.isLocked() ? button.getType().getLockedTooltip() : button.getType().getButtonTooltip();
                Component buttonText = Component.translatable(tooltipKey);

                List<ClientTooltipComponent> tooltipComponents = List.of(ClientTooltipComponent.create(buttonText.getVisualOrderText()));
                guiGraphics.tooltip(
                        Minecraft.getInstance().font,
                        tooltipComponents,
                        mouseX,
                        mouseY,
                        DefaultTooltipPositioner.INSTANCE,
                        null
                );
                break;
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, menu.importerSide ? IMPORTER_TEXTURE : EXPORTER_TEXTURE,
                guiLeft(), guiTop(), 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        updateButtons();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        MousePositionManagerUtil.getLastKnownPosition();
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void onClose() {
        MousePositionManagerUtil.clear();
        super.onClose();
    }
}
