package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.networking.packets.OpenMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;

public class FilterButton extends Button {

    private final ButtonType type;
    @Nullable
    private final ConfigurableRouterBlockEntity configurable;

    private FilterButton(int x, int y, int width, int height, OnPress onPress, ButtonType type,
                          @Nullable ConfigurableRouterBlockEntity configurable) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.type = type;
        this.configurable = configurable;
        this.height = 18;
        this.width = 18;
    }

    // True when the block backing this button doesn't currently satisfy the upgrade requirement -
    // for an Exporter that means "no such upgrade in its own slots"; for an Importer (which
    // inherits filter buttons from linked exporters) it means "no linked exporter has one".
    public boolean isLocked() {
        return configurable == null || !configurable.hasUpgrade(type);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = this.isHovered();
        Identifier currentTexture = hovered ? Routers.identifier(type.getTextureHover()) : Routers.identifier(type.getTexture());

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, currentTexture, this.getX(), this.getY(), this.width, this.height);
    }

    // Hides the button entirely when locked - used by the Exporter, where the upgrade the button
    // needs is just a slot on the same block the player is already looking at.
    @Nullable
    public static FilterButton create(int x, int y, int width, int height, BlockEntity blockEntity, ButtonType type) {
        if (blockEntity instanceof ConfigurableRouterBlockEntity configurable) {
            if (!configurable.hasUpgrade(type)) return null;

            return new FilterButton(x, y, width, height,
                    button -> openMenu(blockEntity, type), type, configurable);
        }

        return null;
    }

    // Always visible AND always clickable, even when locked - used by the Importer, where the
    // requirement lives on a linked exporter rather than this block itself. The filter can still
    // be configured ahead of a connection existing; "locked" here is purely informational (it
    // won't actually do anything until a linked exporter provides the matching upgrade), not a
    // block on editing it.
    @Nullable
    public static FilterButton createAlwaysVisible(int x, int y, int width, int height, BlockEntity blockEntity, ButtonType type) {
        if (blockEntity instanceof ConfigurableRouterBlockEntity configurable) {
            return new FilterButton(x, y, width, height,
                    button -> openMenu(blockEntity, type), type, configurable);
        }

        return null;
    }

    private static void openMenu(BlockEntity blockEntity, ButtonType type) {
        ClientPacketDistributor.sendToServer(new OpenMenu(blockEntity.getBlockPos(), type));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
    }

    public ButtonType getType() {
        return type;
    }
}
