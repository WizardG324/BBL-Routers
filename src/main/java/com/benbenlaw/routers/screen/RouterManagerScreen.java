package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.event.client.ManagerHighlight;
import com.benbenlaw.routers.manager.ManagerLayout;
import com.benbenlaw.routers.manager.ManagerSnapshot;
import com.benbenlaw.routers.manager.ManagerSnapshot.Edge;
import com.benbenlaw.routers.manager.ManagerSnapshot.Kind;
import com.benbenlaw.routers.manager.ManagerSnapshot.Node;
import com.benbenlaw.routers.networking.packets.OpenRouterFromManager;
import com.benbenlaw.routers.networking.packets.RequestManagerSnapshot;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class RouterManagerScreen extends AbstractContainerScreen<RouterManagerMenu> {

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 224;
    private static final int REFRESH_TICKS = 40;

    private static final float MIN_ZOOM = 0.25F;
    private static final float MAX_ZOOM = 2.0F;

    private static final int COLOR_EXPORTER = 0xFFD04040;
    private static final int COLOR_IMPORTER = 0xFF4070D0;
    private static final int COLOR_BOTH = 0xFFA050D0;
    private static final int COLOR_DISTRIBUTOR = 0xFF40B0A0;
    private static final int COLOR_UNLOADED = 0xFF808080;
    private static final int COLOR_NEUTRAL_LINK = 0xFF909090;

    private ManagerSnapshot snapshot;
    private ManagerLayout layout;
    private List<GlobalPos> layoutNodes = List.of();
    private List<Edge> layoutEdges = List.of();

    private float panX;
    private float panY;
    private float zoom = 1.0F;
    private boolean fitted;
    private int ticks;

    private final ItemStack exporterIcon = new ItemStack(RoutersBlocks.EXPORTER.get());
    private final ItemStack importerIcon = new ItemStack(RoutersBlocks.IMPORTER.get());
    private final ItemStack bothIcon = new ItemStack(RoutersBlocks.IMPORTER_EXPORTER.get());
    private final ItemStack distributorIcon = new ItemStack(RoutersBlocks.DISTRIBUTOR.get());

    public RouterManagerScreen(RouterManagerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        requestSnapshot();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (++ticks % REFRESH_TICKS == 0) requestSnapshot();
    }

    private void requestSnapshot() {
        ClientPacketDistributor.sendToServer(new RequestManagerSnapshot(menu.getBlockPos()));
    }

    public void setSnapshot(ManagerSnapshot newSnapshot) {
        this.snapshot = newSnapshot;

        List<GlobalPos> nodes = new ArrayList<>(newSnapshot.nodes().size());
        for (Node node : newSnapshot.nodes()) nodes.add(node.pos());

        // only re-run the layout when the shape of the network changed, so the chart doesn't jump on refresh
        if (layout == null || !nodes.equals(layoutNodes) || !newSnapshot.edges().equals(layoutEdges)) {
            layout = ManagerLayout.compute(nodes.size(), newSnapshot.edges());
            layoutNodes = nodes;
            layoutEdges = List.copyOf(newSnapshot.edges());
        }

        if (!fitted && layout.width > 0) {
            fitToView();
            fitted = true;
        }
    }

    private int chartLeft() {
        return leftPos + 8;
    }

    private int chartTop() {
        return topPos + 22;
    }

    private int chartRight() {
        return leftPos + imageWidth - 8;
    }

    private int chartBottom() {
        return topPos + imageHeight - 22;
    }

    private boolean inChart(double x, double y) {
        return x >= chartLeft() && x < chartRight() && y >= chartTop() && y < chartBottom();
    }

    private void fitToView() {
        float areaWidth = chartRight() - chartLeft();
        float areaHeight = chartBottom() - chartTop();

        zoom = Math.max(MIN_ZOOM, Math.min(1.0F, Math.min(areaWidth / (layout.width + 24), areaHeight / (layout.height + 24))));
        panX = (areaWidth - layout.width * zoom) / 2;
        panY = (areaHeight - layout.height * zoom) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);

        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF3B3B3B);
        guiGraphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF000000);
        guiGraphics.fill(chartLeft(), chartTop(), chartRight(), chartBottom(), 0xFF171717);
        guiGraphics.outline(chartLeft() - 1, chartTop() - 1, chartRight() - chartLeft() + 2, chartBottom() - chartTop() + 2, 0xFF000000);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        Font font = Minecraft.getInstance().font;
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        guiGraphics.text(font, Component.translatable("gui.routers.manager.title"), leftPos + 8, topPos + 8, 0xFFFFFFFF, false);

        if (snapshot != null) {
            Component count = Component.translatable("gui.routers.manager.routers", snapshot.nodes().size());
            guiGraphics.text(font, count, chartRight() - font.width(count), topPos + 8, 0xFFA0A0A0, false);
        }

        drawChart(guiGraphics, font);
        drawLegend(guiGraphics, font);
        drawHoverTooltip(guiGraphics, font, mouseX, mouseY);
    }

    private void drawChart(GuiGraphicsExtractor guiGraphics, Font font) {
        if (snapshot == null || layout == null) return;

        if (snapshot.nodes().isEmpty()) {
            Component empty = Component.translatable("gui.routers.manager.empty");
            guiGraphics.text(font, empty, (chartLeft() + chartRight() - font.width(empty)) / 2, (chartTop() + chartBottom()) / 2 - 4, 0xFF808080, false);
            return;
        }

        guiGraphics.enableScissor(chartLeft(), chartTop(), chartRight(), chartBottom());
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(chartLeft() + panX, chartTop() + panY);
        guiGraphics.pose().scale(zoom, zoom);

        for (Edge edge : snapshot.edges()) drawEdge(guiGraphics, edge);

        int hovered = hoveredNode(lastMouseX, lastMouseY);
        for (int i = 0; i < snapshot.nodes().size(); i++) {
            drawNode(guiGraphics, font, snapshot.nodes().get(i), layout.x[i], layout.y[i], i == hovered);
        }

        guiGraphics.pose().popMatrix();
        guiGraphics.disableScissor();
    }

    private double pressX;
    private double pressY;
    private boolean pressing;

    private int lastMouseX = -1;
    private int lastMouseY = -1;

    private int hoveredNode(int mouseX, int mouseY) {
        if (snapshot == null || layout == null || !inChart(mouseX, mouseY)) return -1;

        double worldX = (mouseX - chartLeft() - panX) / zoom;
        double worldY = (mouseY - chartTop() - panY) / zoom;

        for (int i = 0; i < snapshot.nodes().size(); i++) {
            if (worldX >= layout.x[i] && worldX < layout.x[i] + ManagerLayout.NODE_WIDTH
                    && worldY >= layout.y[i] && worldY < layout.y[i] + ManagerLayout.NODE_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    private void drawNode(GuiGraphicsExtractor guiGraphics, Font font, Node node, int nx, int ny, boolean hovered) {
        int w = ManagerLayout.NODE_WIDTH;
        int h = ManagerLayout.NODE_HEIGHT;
        int kindColor = kindColor(node.kind());

        guiGraphics.fill(nx, ny, nx + w, ny + h, hovered ? 0xFF404040 : 0xFF2A2A2A);
        guiGraphics.outline(nx, ny, w, h, hovered ? 0xFFFFFFFF : kindColor);
        guiGraphics.fill(nx, ny, nx + 3, ny + h, kindColor);

        String coordinates = node.pos().pos().toShortString();
        guiGraphics.text(font, coordinates, nx + 46, ny + 5, 0xFFFFFFFF, false);

        if (node.kind() == Kind.UNLOADED) {
            guiGraphics.text(font, "?", nx + 14, ny + 10, 0xFF808080, false);
            guiGraphics.text(font, font.plainSubstrByWidth(Component.translatable("gui.routers.manager.kind.unloaded").getString(), w - 50),
                    nx + 46, ny + 16, 0xFF808080, false);
            return;
        }

        guiGraphics.item(iconFor(node.kind()), nx + 6, ny + 6);

        if (node.kind() == Kind.DISTRIBUTOR) {
            guiGraphics.text(font, font.plainSubstrByWidth(Component.translatable("gui.routers.manager.kind.distributor").getString(), w - 50),
                    nx + 46, ny + 16, 0xFFA0A0A0, false);
        } else if (!node.adjacent().isEmpty()) {
            guiGraphics.item(node.adjacent(), nx + 26, ny + 6);
            guiGraphics.text(font, font.plainSubstrByWidth(node.adjacent().getHoverName().getString(), w - 50),
                    nx + 46, ny + 16, 0xFFA0A0A0, false);
        }

        if (!node.working()) {
            guiGraphics.fill(nx + w - 3, ny, nx + w, ny + h, 0xFFB04040);
        }
    }

    private ItemStack iconFor(Kind kind) {
        return switch (kind) {
            case EXPORTER -> exporterIcon;
            case IMPORTER -> importerIcon;
            case DISTRIBUTOR -> distributorIcon;
            default -> bothIcon;
        };
    }

    private static int kindColor(Kind kind) {
        return switch (kind) {
            case EXPORTER -> COLOR_EXPORTER;
            case IMPORTER -> COLOR_IMPORTER;
            case IMPORTER_EXPORTER -> COLOR_BOTH;
            case DISTRIBUTOR -> COLOR_DISTRIBUTOR;
            case UNLOADED -> COLOR_UNLOADED;
        };
    }

    private static int typeColor(ButtonType type) {
        float[] c = type.getColor();
        return 0xFF000000 | (Math.round(c[0] * 255) << 16) | (Math.round(c[1] * 255) << 8) | Math.round(c[2] * 255);
    }

    private static List<Integer> linkColors(int types) {
        List<Integer> colors = new ArrayList<>();
        if ((types & ManagerSnapshot.ITEM) != 0) colors.add(brighten(typeColor(RouterButtonTypes.ITEM_FILTER)));
        if ((types & ManagerSnapshot.FLUID) != 0) colors.add(brighten(typeColor(RouterButtonTypes.FLUID_FILTER)));
        if ((types & ManagerSnapshot.ENERGY) != 0) colors.add(brighten(typeColor(RouterButtonTypes.ENERGY_FILTER)));
        if (colors.isEmpty()) colors.add(COLOR_NEUTRAL_LINK);
        return colors;
    }

    // the filter button colours are fairly dark; lift them so they read against the dark chart
    private static int brighten(int argb) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 60);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 60);
        int b = Math.min(255, (argb & 0xFF) + 60);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void drawEdge(GuiGraphicsExtractor guiGraphics, Edge edge) {
        int w = ManagerLayout.NODE_WIDTH;
        int h = ManagerLayout.NODE_HEIGHT;

        List<Integer> colors = linkColors(edge.types());
        int count = colors.size();
        boolean dashed = edge.viaInventory();

        for (int k = 0; k < count; k++) {
            int offset = Math.round((k - (count - 1) / 2.0F) * 4);
            int color = colors.get(k);

            int fx = layout.x[edge.from()] + w;
            int fy = layout.y[edge.from()] + h / 2 + offset;
            int tx = layout.x[edge.to()];
            int ty = layout.y[edge.to()] + h / 2 + offset;

            if (tx - fx >= 12) {
                int mid = fx + (tx - fx) / 2 + offset;
                horizontal(guiGraphics, fx, mid, fy, color, dashed);
                vertical(guiGraphics, mid, fy, ty, color, dashed);
                horizontal(guiGraphics, mid, tx, ty, color, dashed);
            } else {
                int out = fx + 14 + offset;
                int in = tx - 14 - offset;
                int loop = Math.max(layout.y[edge.from()], layout.y[edge.to()]) + h + 8 + k * 4;
                horizontal(guiGraphics, fx, out, fy, color, dashed);
                vertical(guiGraphics, out, fy, loop, color, dashed);
                horizontal(guiGraphics, in, out, loop, color, dashed);
                vertical(guiGraphics, in, loop, ty, color, dashed);
                horizontal(guiGraphics, in, tx, ty, color, dashed);
            }

            for (int i = 0; i < 5; i++) {
                int half = 4 - i;
                guiGraphics.fill(tx - 5 + i, ty - half, tx - 4 + i, ty + half + 1, color);
            }
        }
    }

    private static void horizontal(GuiGraphicsExtractor guiGraphics, int x1, int x2, int y, int color, boolean dashed) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2) + 1;
        if (!dashed) {
            guiGraphics.fill(start, y - 1, end, y + 1, color);
            return;
        }
        for (int x = start; x < end; x += 6) guiGraphics.fill(x, y - 1, Math.min(x + 3, end), y + 1, color);
    }

    private static void vertical(GuiGraphicsExtractor guiGraphics, int x, int y1, int y2, int color, boolean dashed) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2) + 1;
        if (!dashed) {
            guiGraphics.fill(x - 1, start, x + 1, end, color);
            return;
        }
        for (int y = start; y < end; y += 6) guiGraphics.fill(x - 1, y, x + 1, Math.min(y + 3, end), color);
    }

    private void drawLegend(GuiGraphicsExtractor guiGraphics, Font font) {
        int y = chartBottom() + 7;
        int x = chartLeft();

        x = legendEntry(guiGraphics, font, x, y, brighten(typeColor(RouterButtonTypes.ITEM_FILTER)), "gui.routers.manager.legend.item", false);
        x = legendEntry(guiGraphics, font, x, y, brighten(typeColor(RouterButtonTypes.FLUID_FILTER)), "gui.routers.manager.legend.fluid", false);
        x = legendEntry(guiGraphics, font, x, y, brighten(typeColor(RouterButtonTypes.ENERGY_FILTER)), "gui.routers.manager.legend.energy", false);
        legendEntry(guiGraphics, font, x, y, COLOR_NEUTRAL_LINK, "gui.routers.manager.legend.inventory", true);

        boolean truncated = snapshot != null && snapshot.truncated();
        Component hint = Component.translatable(truncated ? "gui.routers.manager.truncated" : "gui.routers.manager.hint",
                snapshot != null ? snapshot.nodes().size() : 0);
        int hintX = leftPos + 8 + font.width(Component.translatable("gui.routers.manager.title")) + 14;
        guiGraphics.text(font, hint, hintX, topPos + 8, truncated ? 0xFFE0A040 : 0xFF808080, false);
    }

    private int legendEntry(GuiGraphicsExtractor guiGraphics, Font font, int x, int y, int color, String key, boolean dashed) {
        horizontal(guiGraphics, x, x + 9, y + 4, color, dashed);
        Component label = Component.translatable(key);
        guiGraphics.text(font, label, x + 14, y, 0xFFC0C0C0, false);
        return x + 14 + font.width(label) + 12;
    }

    private void drawHoverTooltip(GuiGraphicsExtractor guiGraphics, Font font, int mouseX, int mouseY) {
        int index = hoveredNode(mouseX, mouseY);
        if (index < 0) return;

        Node node = snapshot.nodes().get(index);
        List<Component> lines = new ArrayList<>();

        lines.add(Component.translatable("gui.routers.manager.kind." + switch (node.kind()) {
            case EXPORTER -> "exporter";
            case IMPORTER -> "importer";
            case IMPORTER_EXPORTER -> "importer_exporter";
            case DISTRIBUTOR -> "distributor";
            case UNLOADED -> "unloaded";
        }).withStyle(ChatFormatting.WHITE));

        lines.add(Component.translatable("gui.routers.manager.position", node.pos().pos().toShortString()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gui.routers.manager.dimension", node.pos().dimension().identifier().toString()).withStyle(ChatFormatting.GRAY));

        if (node.kind() != Kind.UNLOADED) {
            if (!node.adjacent().isEmpty()) {
                lines.add(Component.translatable("gui.routers.manager.adjacent", node.adjacent().getHoverName()).withStyle(ChatFormatting.GRAY));
            }
            if (node.kind().exports()) {
                lines.add(Component.translatable("gui.routers.manager.exporter_side", describeFlags(node.exporterFlags())).withStyle(ChatFormatting.RED));
            }
            if (node.kind().imports()) {
                lines.add(Component.translatable("gui.routers.manager.importer_side", describeFlags(node.importerFlags())).withStyle(ChatFormatting.BLUE));
            }
            if (node.kind() == Kind.DISTRIBUTOR) {
                lines.add(Component.translatable("gui.routers.manager.distributor_side", describeFlags(node.exporterFlags())).withStyle(ChatFormatting.DARK_AQUA));
            }
            if (!node.working()) {
                lines.add(Component.translatable("gui.routers.manager.disabled").withStyle(ChatFormatting.DARK_RED));
            }
            lines.add(Component.translatable("gui.routers.manager.click_to_open").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        lines.add(Component.translatable("gui.routers.manager.right_click_to_locate").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        List<ClientTooltipComponent> components = new ArrayList<>();
        for (Component line : lines) components.add(ClientTooltipComponent.create(line.getVisualOrderText()));

        guiGraphics.tooltip(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    private static String describeFlags(int flags) {
        List<String> parts = new ArrayList<>();

        if ((flags & ManagerSnapshot.TYPE_MASK) == 0) {
            parts.add(Component.translatable("gui.routers.manager.no_upgrades").getString());
        }
        if ((flags & ManagerSnapshot.ITEM) != 0) parts.add(Component.translatable("gui.routers.manager.legend.item").getString());
        if ((flags & ManagerSnapshot.FLUID) != 0) parts.add(Component.translatable("gui.routers.manager.legend.fluid").getString());
        if ((flags & ManagerSnapshot.ENERGY) != 0) parts.add(Component.translatable("gui.routers.manager.legend.energy").getString());
        if ((flags & ManagerSnapshot.ROUND_ROBIN) != 0) parts.add(Component.translatable("gui.routers.manager.flag.round_robin").getString());
        if ((flags & ManagerSnapshot.DIMENSIONAL) != 0) parts.add(Component.translatable("gui.routers.manager.flag.dimensional").getString());
        if ((flags & ManagerSnapshot.BLACKLIST) != 0) parts.add(Component.translatable("gui.routers.manager.flag.blacklist").getString());
        if ((flags & ManagerSnapshot.IGNORE_NBT) != 0) parts.add(Component.translatable("gui.routers.manager.flag.ignore_nbt").getString());
        if ((flags & ManagerSnapshot.FILTERED) != 0) parts.add(Component.translatable("gui.routers.manager.flag.filtered").getString());

        return String.join(", ", parts);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 1) {
            int index = hoveredNode((int) event.x(), (int) event.y());
            if (index >= 0) {
                locateNode(snapshot.nodes().get(index));
                return true;
            }
        }

        if (event.button() == 0 && inChart(event.x(), event.y())) {
            pressX = event.x();
            pressY = event.y();
            pressing = true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (pressing && event.button() == 0) {
            pressing = false;

            // a click, not the end of a pan
            if (Math.abs(event.x() - pressX) < 4 && Math.abs(event.y() - pressY) < 4) {
                int index = hoveredNode((int) event.x(), (int) event.y());
                if (index >= 0) {
                    openNode(snapshot.nodes().get(index));
                    return true;
                }
            }
        }
        return super.mouseReleased(event);
    }

    // closes the screen and outlines the router in the world for a few seconds
    private void locateNode(Node node) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        if (!node.pos().dimension().equals(minecraft.level.dimension())) {
            minecraft.player.sendOverlayMessage(Component.translatable("gui.routers.manager.cannot_locate_dimension"));
            return;
        }

        ManagerHighlight.show(node.pos());
        onClose();
    }

    private void openNode(Node node) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        if (node.kind() == Kind.UNLOADED) {
            minecraft.player.sendOverlayMessage(Component.translatable("gui.routers.manager.cannot_open_unloaded"));
        } else if (!node.pos().dimension().equals(minecraft.level.dimension())) {
            minecraft.player.sendOverlayMessage(Component.translatable("gui.routers.manager.cannot_open_dimension"));
        } else {
            ClientPacketDistributor.sendToServer(new OpenRouterFromManager(menu.getBlockPos(), node.pos().pos()));
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == 0 && inChart(event.x(), event.y())) {
            panX += (float) dragX;
            panY += (float) dragY;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0 && inChart(mouseX, mouseY)) {
            float newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * (scrollY > 0 ? 1.15F : 1 / 1.15F)));

            // keep whatever is under the cursor under the cursor while zooming
            float localX = (float) mouseX - chartLeft();
            float localY = (float) mouseY - chartTop();
            panX = localX - (localX - panX) * (newZoom / zoom);
            panY = localY - (localY - panY) * (newZoom / zoom);
            zoom = newZoom;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
