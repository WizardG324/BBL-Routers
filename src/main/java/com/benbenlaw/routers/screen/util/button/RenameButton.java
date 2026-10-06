package com.benbenlaw.routers.screen.util.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

// A small name tag beside the title, with no frame: click it to name the router.
public class RenameButton extends Button {

    public static final int SIZE = 9;

    private final ItemStack icon = new ItemStack(Items.NAME_TAG);

    public RenameButton(int x, int y, Runnable onRename) {
        super(x, y, SIZE, SIZE, Component.empty(), button -> onRename.run(), DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();

        if (hovered) guiGraphics.fill(getX() - 1, getY() - 1, getX() + SIZE + 1, getY() + SIZE + 1, 0x40FFFFFF);

        // the item is 16 wide, so it's drawn at half size
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(getX() + 0.5F, getY() + 0.5F);
        guiGraphics.pose().scale(0.5F, 0.5F);
        guiGraphics.item(icon, 0, 0);
        guiGraphics.pose().popMatrix();

        if (hovered) {
            Component text = Component.translatable("tooltip.routers.rename");

            guiGraphics.tooltip(
                    Minecraft.getInstance().font,
                    List.of(ClientTooltipComponent.create(text.getVisualOrderText())),
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }
}
