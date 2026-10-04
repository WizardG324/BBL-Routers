package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SideToggleButton extends Button {

    private final boolean importerSide;
    private final ItemStack icon;

    public SideToggleButton(int x, int y, boolean importerSide, OnPress onPress) {
        super(x, y, 18, 18, Component.empty(), onPress, DEFAULT_NARRATION);
        this.importerSide = importerSide;
        this.icon = new ItemStack(importerSide ? RoutersBlocks.IMPORTER.get() : RoutersBlocks.EXPORTER.get());
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();
        Identifier frame = Routers.identifier(hovered ? "plain_hover" : "plain");

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, frame, this.getX(), this.getY(), this.width, this.height);
        guiGraphics.item(icon, this.getX() + 1, this.getY() + 1);

        if (hovered) {
            Component text = Component.translatable(importerSide
                    ? "tooltip.routers.importer_exporter.switch_to_exporter"
                    : "tooltip.routers.importer_exporter.switch_to_importer");

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
