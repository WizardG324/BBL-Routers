package com.benbenlaw.routers.screen;

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

public class ImporterScreen extends AbstractContainerScreen<ImporterMenu> {

    private static final Identifier TEXTURE = Routers.identifier("textures/gui/importer_gui.png");

    private final Map<ButtonType, FilterButton> filterButtons = new HashMap<>();

    public ImporterScreen(ImporterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();

        filterButtons.clear();

        if (MousePositionManagerUtil.lastMouseX != -1) {
            MousePositionManagerUtil.setLastKnownPosition();
        }

        updateButtons();
    }

    // Unlike the Exporter, every filter button is always shown here - the upgrade it needs lives
    // on a linked exporter rather than this block, so hiding the button when nothing's linked yet
    // would just look like the feature doesn't exist. A locked button stays visible, dimmed, with
    // a tooltip explaining what it's waiting on (see FilterButton.isLocked/createAlwaysVisible).
    private void updateButtons() {
        int baseX = (width - imageWidth) / 2;
        int baseY = (height - imageHeight) / 2;

        int BUTTON_SIZE = 20;
        int BUTTON_SPACING = 19;
        int BUTTON_Y = 30;

        int buttonCount = RouterButtonTypes.BUTTONS.size();

        int totalWidth = buttonCount * BUTTON_SPACING - (buttonCount > 0 ? (BUTTON_SPACING - BUTTON_SIZE) : 0);
        int startX = baseX + (imageWidth - totalWidth) / 2 + 1;

        int index = 0;

        for (ButtonType type : RouterButtonTypes.BUTTONS.values()) {
            int x = startX + index * BUTTON_SPACING;
            int y = baseY + BUTTON_Y;

            if (!filterButtons.containsKey(type)) {
                FilterButton button = FilterButton.createAlwaysVisible(
                        x, y,
                        BUTTON_SIZE,
                        BUTTON_SIZE,
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
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);

        for (FilterButton button : filterButtons.values()) {
            if (button.isHovered()) {
                String tooltipKey = button.isLocked() ? button.getType().getLockedTooltip() : button.getType().getButtonTooltip();
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
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public void onClose() {
        MousePositionManagerUtil.clear();
        super.onClose();
    }
}
