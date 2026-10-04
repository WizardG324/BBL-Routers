package com.benbenlaw.routers.screen.upgrade;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.screen.client.RouterUIRenderers; // Import the new renderer manager
import com.benbenlaw.routers.screen.util.MousePositionManagerUtil;
import com.benbenlaw.routers.screen.util.button.BackButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class FilterScreen extends AbstractContainerScreen<FilterMenu> {

    private static final Identifier TEXTURE = Routers.identifier("textures/gui/filter_gui.png");

    public FilterScreen(FilterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        MousePositionManagerUtil.setLastKnownPosition();
        addRenderableWidget(BackButton.create(getGuiLeft() + 151, getGuiTop() + 4, 20, 20, menu.blockPos));
    }

    public int getGuiLeft() { return (width - imageWidth) / 2; }
    public int getGuiTop() { return (height - imageHeight) / 2; }
    public int getXSize() { return imageWidth; }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight, 256, 256);

        RouterUIRenderers.get(menu.buttonType).ifPresent(renderer ->
                renderer.renderBackground(guiGraphics, this)
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        RouterUIRenderers.get(menu.buttonType).ifPresent(renderer ->
                renderer.renderExtra(guiGraphics, this, menu.blockEntity, mouseX, mouseY)
        );
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

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return mouseX >= leftPos + slot.x && mouseX < leftPos + slot.x + 16
                && mouseY >= topPos + slot.y && mouseY < topPos + slot.y + 16;
    }
}