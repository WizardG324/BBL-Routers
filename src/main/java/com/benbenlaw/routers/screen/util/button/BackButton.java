package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.networking.packets.BackMenu;
import com.benbenlaw.routers.networking.packets.OpenMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class BackButton extends Button {

    private ButtonType type;

    public BackButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.height = 18;
        this.width = 18;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();
        Identifier currentTexture = hovered ? Routers.identifier("back_hover") : Routers.identifier("back");

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, currentTexture, this.getX(), this.getY(), this.width, this.height);

        if (hovered) {
            Component buttonText = Component.translatable("tooltip.routers.button.back");

            List<ClientTooltipComponent> tooltipComponents = List.of(ClientTooltipComponent.create(buttonText.getVisualOrderText()));
            guiGraphics.tooltip(
                    Minecraft.getInstance().font,
                    tooltipComponents,
                    mouseX,
                    mouseY,
                    DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }

    public static BackButton create(int x, int y, int width, int height, BlockPos blockPos) {
        return new BackButton(x, y, width, height, button ->
                ClientPacketDistributor.sendToServer(new BackMenu(blockPos)));
    }
}
