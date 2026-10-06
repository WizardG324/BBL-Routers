package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.screen.util.RouterNaming;
import com.benbenlaw.routers.screen.util.button.RenameButton;
import net.minecraft.client.input.KeyEvent;
import com.benbenlaw.core.screen.util.FluidRenderingUtils;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.screen.util.MousePositionManagerUtil;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.screen.util.button.FilterButton;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExporterScreen extends AbstractContainerScreen<ExporterMenu> {

    private static final Identifier TEXTURE = Routers.identifier("textures/gui/exporter_gui.png");

    private final Map<ButtonType, FilterButton> filterButtons = new HashMap<>();

    public ExporterScreen(ExporterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    private final RouterNaming naming = new RouterNaming(menu.blockEntity.getBlockPos(), this::addRenderableWidget, this::removeWidget, this::setFocused);

    @Override
    protected void init() {
        super.init();

        filterButtons.clear();

        if (MousePositionManagerUtil.lastMouseX != -1) {
            MousePositionManagerUtil.setLastKnownPosition();
        }

        naming.reset();
        addRenderableWidget(new RenameButton(leftPos + 160, topPos + 6, () -> naming.begin(leftPos + 8, topPos + 14, menu.blockEntity.getRouterName())));

        updateButtons();
    }

    private void updateButtons() {
        int baseX = (width - imageWidth) / 2;
        int baseY = (height - imageHeight) / 2;

        int BUTTON_SIZE = 20;
        int BUTTON_SPACING = 19;
        int BUTTON_Y = 30;

        long upgradeCount = RouterButtonTypes.all().stream()
                .filter(type -> menu.blockEntity.hasUpgrade(type))
                .count();

        int totalWidth = (int) (upgradeCount * BUTTON_SPACING - (upgradeCount > 0 ? (BUTTON_SPACING - BUTTON_SIZE) : 0));
        int startX = baseX + (imageWidth - totalWidth) / 2 + 1;

        int index = 0;

        for (ButtonType type : RouterButtonTypes.all()) {
            boolean hasUpgrade = menu.blockEntity.hasUpgrade(type);

            if (hasUpgrade) {
                int x = startX + index * BUTTON_SPACING;
                int y = baseY + BUTTON_Y;

                if (!filterButtons.containsKey(type)) {
                    FilterButton button = FilterButton.create(
                            x, y,
                            BUTTON_SIZE,
                            BUTTON_SIZE,
                            menu.blockEntity.getBlockPos(),
                            menu.blockEntity,
                            type
                    );

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
                Component buttonText = Component.translatable(button.getType().getButtonTooltip());

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

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        updateButtons();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        MousePositionManagerUtil.getLastKnownPosition();
        naming.mouseClicked(event);
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return naming.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        RouterNaming.drawLabels(guiGraphics, font, RouterNaming.title(title, menu.blockEntity, 148),
                titleLabelX, titleLabelY, playerInventoryTitle, inventoryLabelX, inventoryLabelY);
    }

    @Override
    public void onClose() {
        MousePositionManagerUtil.clear();
        super.onClose();
    }
}