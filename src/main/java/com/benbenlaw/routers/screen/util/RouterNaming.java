package com.benbenlaw.routers.screen.util;

import com.benbenlaw.routers.api.NamedRouter;
import com.benbenlaw.routers.networking.packets.RenameRouter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

// The name box behind a router screen's rename button, and the title that shows the name: "Exporter (Smelter)".
// Enter or clicking away saves, Escape cancels. An empty name clears it.
public class RouterNaming {

    public static final int BOX_WIDTH = 100;
    public static final int BOX_HEIGHT = 12;

    private final BlockPos routerPos;
    private final Consumer<EditBox> addWidget;
    private final Consumer<EditBox> removeWidget;
    private final Consumer<EditBox> focus;

    private EditBox box;

    public RouterNaming(BlockPos routerPos, Consumer<EditBox> addWidget, Consumer<EditBox> removeWidget, Consumer<EditBox> focus) {
        this.routerPos = routerPos;
        this.addWidget = addWidget;
        this.removeWidget = removeWidget;
        this.focus = focus;
    }

    public boolean editing() {
        return box != null;
    }

    // The screen rebuilt its widgets, so the box is already gone.
    public void reset() {
        box = null;
    }

    public void begin(int x, int y, String current) {
        end(false);

        box = new EditBox(Minecraft.getInstance().font, x, y, BOX_WIDTH, BOX_HEIGHT, Component.empty());
        box.setMaxLength(NamedRouter.MAX_NAME_LENGTH);
        box.setHint(Component.translatable("gui.routers.rename_hint").withStyle(ChatFormatting.DARK_GRAY));
        box.setValue(current);
        addWidget.accept(box);
        focus.accept(box);
        box.setFocused(true);
    }

    public void end(boolean save) {
        if (box == null) return;

        if (save) ClientPacketDistributor.sendToServer(new RenameRouter(routerPos, box.getValue()));
        removeWidget.accept(box);
        box = null;
    }

    // True if the key was used up by the name box, which is every key while it's open so the inventory key can't close the screen.
    public boolean keyPressed(KeyEvent event) {
        if (box == null) return false;

        int key = event.key();
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            end(true);
        } else if (key == GLFW.GLFW_KEY_ESCAPE) {
            end(false);
        } else {
            box.keyPressed(event);
        }
        return true;
    }

    public void mouseClicked(MouseButtonEvent event) {
        if (box != null && !box.isMouseOver(event.x(), event.y())) end(true);
    }

    // "Exporter" or "Exporter (Smelter)", cut short with ... to fit the room beside the buttons.
    public static String title(Component base, NamedRouter router, int maxWidth) {
        String name = router.getRouterName();
        String full = name.isEmpty() ? base.getString() : base.getString() + " (" + name + ")";

        Font font = Minecraft.getInstance().font;
        if (font.width(full) <= maxWidth) return full;
        return font.plainSubstrByWidth(full, maxWidth - font.width("...")) + "...";
    }

    public static void drawLabels(GuiGraphicsExtractor guiGraphics, Font font, String title, int titleX, int titleY,
                                  Component inventoryTitle, int inventoryX, int inventoryY) {
        guiGraphics.text(font, title, titleX, titleY, 0xFF404040, false);
        guiGraphics.text(font, inventoryTitle, inventoryX, inventoryY, 0xFF404040, false);
    }
}
