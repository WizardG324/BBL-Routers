package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.api.RouterButtonTypes;
import com.benbenlaw.routers.api.screen.client.RouterUIRenderers;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.event.client.ManagerHighlight;
import com.benbenlaw.routers.manager.ManagerLayout;
import com.benbenlaw.routers.manager.ManagerSnapshot;
import com.benbenlaw.routers.manager.ManagerSnapshot.Edge;
import com.benbenlaw.routers.manager.ManagerSnapshot.Kind;
import com.benbenlaw.routers.manager.ManagerSnapshot.Node;
import com.benbenlaw.routers.api.NamedRouter;
import com.benbenlaw.routers.networking.packets.EditLinkFromManager;
import com.benbenlaw.routers.networking.packets.OpenRouterFromManager;
import com.benbenlaw.routers.networking.packets.RenameRouterFromManager;
import com.benbenlaw.routers.networking.packets.RequestManagerSnapshot;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

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
    private static final int COLOR_ISSUE = 0xFFE0A040;
    private static final int RENAME_WIDTH = 140;
    private static final int COLOR_LINKING = 0xFFFFFFFF;
    private static final int COLOR_LINK_TARGET = 0xFF60D060;

    private ManagerSnapshot snapshot;
    private ManagerLayout layout;
    private List<GlobalPos> layoutNodes = List.of();
    private List<Edge> layoutEdges = List.of();
    // per node, what looks wrong with it; rebuilt with every snapshot
    private List<List<Component>> issues = List.of();

    // the rename box opened with a middle click, and the router it names
    private EditBox nameBox;
    private GlobalPos renaming;

    // Ctrl + drag from a router: where the drag started, and where the cursor is now
    // where each edge leaves its source and meets its target: slot i of n down that router's side
    private int[] outSlot = new int[0];
    private int[] outCount = new int[0];
    private int[] inSlot = new int[0];
    private int[] inCount = new int[0];
    // x of each link's vertical run, its own lane across the gap after its exporter's column
    private int[] laneX = new int[0];

    private int linkFrom = -1;
    // Ctrl as seen through this screen's key events, alongside polling the keyboard
    private boolean ctrlHeld;
    // Tab held hides tooltips, for seeing the links underneath them
    private boolean tabHeld;
    private double linkX;
    private double linkY;

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

        if (layout == null || !nodes.equals(layoutNodes) || !newSnapshot.edges().equals(layoutEdges)) {
            layout = ManagerLayout.compute(nodes.size(), newSnapshot.edges());
            layoutNodes = nodes;
            layoutEdges = List.copyOf(newSnapshot.edges());
        }

        findIssues();
        assignPorts();

        if (!fitted && layout.width > 0) {
            fitToView();
            fitted = true;
        }
    }

    // Flags setups that can't move anything, so a broken link stands out without opening every router.
    private void findIssues() {
        List<Node> nodes = snapshot.nodes();
        int size = nodes.size();
        boolean[] linksOut = new boolean[size];
        boolean[] linksIn = new boolean[size];
        List<List<Component>> found = new ArrayList<>(size);
        for (int i = 0; i < size; i++) found.add(new ArrayList<>());

        for (Edge edge : snapshot.edges()) {
            if (edge.viaInventory()) continue;
            Node from = nodes.get(edge.from());
            Node to = nodes.get(edge.to());
            linksOut[edge.from()] = true;
            linksIn[edge.to()] = true;

            if (from.kind() == Kind.UNLOADED) addIssue(found, edge.to(), "gui.routers.manager.issue.unloaded_link");

            if (!from.pos().dimension().equals(to.pos().dimension()) && (from.exporterFlags() & ManagerSnapshot.DIMENSIONAL) == 0) {
                addIssue(found, edge.from(), "gui.routers.manager.issue.needs_dimensional");
            }
        }

        for (int i = 0; i < size; i++) {
            Node node = nodes.get(i);
            switch (node.kind()) {
                case EXPORTER -> { if (!linksOut[i]) addIssue(found, i, "gui.routers.manager.issue.no_importer"); }
                case IMPORTER -> { if (!linksIn[i]) addIssue(found, i, "gui.routers.manager.issue.no_exporter"); }
                case IMPORTER_EXPORTER -> { if (!linksOut[i] && !linksIn[i]) addIssue(found, i, "gui.routers.manager.issue.no_links"); }
                default -> {}
            }
        }

        issues = found;
    }

    // Gives every edge its own point on each router's side, ordered by where the other end sits so lines
    // don't cross. Without this, two exporters feeding one importer arrive at the same spot and read as one line.
    private void assignPorts() {
        List<Edge> edges = snapshot.edges();
        int nodeCount = snapshot.nodes().size();
        outSlot = new int[edges.size()];
        inSlot = new int[edges.size()];
        int[] outPerNode = new int[nodeCount];
        int[] inPerNode = new int[nodeCount];

        List<Integer> order = new ArrayList<>();
        for (int e = 0; e < edges.size(); e++) order.add(e);

        order.sort((a, b) -> Integer.compare(layout.y[edges.get(a).to()], layout.y[edges.get(b).to()]));
        for (int e : order) outSlot[e] = outPerNode[edges.get(e).from()]++;

        order.sort((a, b) -> Integer.compare(layout.y[edges.get(a).from()], layout.y[edges.get(b).from()]));
        for (int e : order) inSlot[e] = inPerNode[edges.get(e).to()]++;

        outCount = new int[edges.size()];
        inCount = new int[edges.size()];
        for (int e = 0; e < edges.size(); e++) {
            outCount[e] = outPerNode[edges.get(e).from()];
            inCount[e] = inPerNode[edges.get(e).to()];
        }

        // Every link leaving a column turns in the gap after it. Spread those turns across the gap in
        // order of where the link ends up, so they sit side by side instead of piling up in the middle.
        laneX = new int[edges.size()];
        java.util.Map<Integer, List<Integer>> byColumn = new java.util.HashMap<>();
        for (int e = 0; e < edges.size(); e++) {
            byColumn.computeIfAbsent(layout.x[edges.get(e).from()], key -> new ArrayList<>()).add(e);
        }
        for (java.util.Map.Entry<Integer, List<Integer>> column : byColumn.entrySet()) {
            List<Integer> lanes = column.getValue();
            lanes.sort((a, b) -> {
                int byTarget = Integer.compare(layout.y[edges.get(a).to()], layout.y[edges.get(b).to()]);
                return byTarget != 0 ? byTarget : Integer.compare(layout.y[edges.get(a).from()], layout.y[edges.get(b).from()]);
            });
            int start = column.getKey() + ManagerLayout.NODE_WIDTH + 8;
            int usable = ManagerLayout.GAP_X - 16;
            for (int j = 0; j < lanes.size(); j++) {
                laneX[lanes.get(j)] = start + (lanes.size() == 1 ? usable / 2 : Math.round(j * usable / (float) (lanes.size() - 1)));
            }
        }
    }

    // The y offset from a router's middle for slot i of n, spread down its side.
    private static int portOffset(int slot, int count) {
        if (count <= 1) return 0;
        float spacing = Math.min(10, (ManagerLayout.NODE_HEIGHT - 6) / (float) (count - 1));
        return Math.round((slot - (count - 1) / 2.0F) * spacing);
    }

    private static void addIssue(List<List<Component>> found, int index, String key) {
        Component issue = Component.translatable(key);
        if (!found.get(index).contains(issue)) found.get(index).add(issue);
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

        placeNameBox();
        drawChart(guiGraphics, font);
        // drawn after the chart rather than as a normal widget, which would render underneath it
        if (nameBox != null) nameBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
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

        boolean ctrl = ctrlDown();
        int hovered = hoveredNode(lastMouseX, lastMouseY);
        int hoveredLink = hovered < 0 ? hoveredEdge(lastMouseX, lastMouseY) : -1;
        int removable = ctrl && linkFrom < 0 ? hoveredLink : -1;

        // hovering a router or a link fades every other link, so the ones that matter can be followed
        boolean focusing = linkFrom < 0 && nameBox == null && (hovered >= 0 || hoveredLink >= 0);
        for (int e = 0; e < snapshot.edges().size(); e++) {
            Edge edge = snapshot.edges().get(e);
            boolean related = hoveredLink >= 0 ? e == hoveredLink : edge.from() == hovered || edge.to() == hovered;
            drawEdge(guiGraphics, e, e == removable, focusing && !related);
        }

        for (int i = 0; i < snapshot.nodes().size(); i++) {
            drawNode(guiGraphics, font, snapshot.nodes().get(i), layout.x[i], layout.y[i], i == hovered, !issues.get(i).isEmpty());
        }

        if (linkFrom >= 0) {
            int w = ManagerLayout.NODE_WIDTH;
            int h = ManagerLayout.NODE_HEIGHT;
            int fx = layout.x[linkFrom] + w / 2;
            int fy = layout.y[linkFrom] + h / 2;
            int mx = (int) ((linkX - chartLeft() - panX) / zoom);
            int my = (int) ((linkY - chartTop() - panY) / zoom);
            horizontal(guiGraphics, fx, mx, fy, COLOR_LINKING, true);
            vertical(guiGraphics, mx, fy, my, COLOR_LINKING, true);

            if (linkPair(linkFrom, hovered) != null) {
                guiGraphics.outline(layout.x[hovered] - 1, layout.y[hovered] - 1, w + 2, h + 2, COLOR_LINK_TARGET);
            }
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

    private void drawNode(GuiGraphicsExtractor guiGraphics, Font font, Node node, int nx, int ny, boolean hovered, boolean hasIssue) {
        int w = ManagerLayout.NODE_WIDTH;
        int h = ManagerLayout.NODE_HEIGHT;
        int kindColor = kindColor(node.kind());

        guiGraphics.fill(nx, ny, nx + w, ny + h, hovered ? 0xFF404040 : 0xFF2A2A2A);
        guiGraphics.outline(nx, ny, w, h, hovered ? 0xFFFFFFFF : kindColor);
        guiGraphics.fill(nx, ny, nx + 3, ny + h, kindColor);

        // a player-given name replaces the coordinates; those stay in the tooltip
        String title = node.name().isEmpty() ? node.pos().pos().toShortString() : node.name();
        guiGraphics.text(font, font.plainSubstrByWidth(title, w - 50), nx + 46, ny + 5, 0xFFFFFFFF, false);

        if (hasIssue) {
            drawWarningTriangle(guiGraphics, nx + w - 10, ny + 2);
        }

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

    // A warning sign: an 11 wide, 10 tall triangle with its tip at (cx, top) and a black ! inside.
    private static void drawWarningTriangle(GuiGraphicsExtractor guiGraphics, int cx, int top) {
        for (int row = 0; row < 10; row++) {
            int half = row * 5 / 9;
            guiGraphics.fill(cx - half, top + row, cx + half + 1, top + row + 1, COLOR_ISSUE);
        }
        guiGraphics.fill(cx, top + 3, cx + 1, top + 7, 0xFF000000);
        guiGraphics.fill(cx, top + 8, cx + 1, top + 9, 0xFF000000);
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
        float[] c = RouterUIRenderers.getColor(type);
        return 0xFF000000 | (Math.round(c[0] * 255) << 16) | (Math.round(c[1] * 255) << 8) | Math.round(c[2] * 255);
    }

    private static List<Integer> linkColors(int types) {
        List<Integer> colors = new ArrayList<>();
        for (ButtonType type : RouterButtonTypes.all()) {
            if ((types & RoutersTransfers.bit(type.getId())) != 0) colors.add(brighten(typeColor(type)));
        }
        if (colors.isEmpty()) colors.add(COLOR_NEUTRAL_LINK);
        return colors;
    }

    private static int brighten(int argb) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 60);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 60);
        int b = Math.min(255, (argb & 0xFF) + 60);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void drawEdge(GuiGraphicsExtractor guiGraphics, int e, boolean highlighted, boolean faded) {
        Edge edge = snapshot.edges().get(e);
        List<Integer> colors = linkColors(edge.types());
        int count = colors.size();
        boolean dashed = edge.viaInventory();

        for (int k = 0; k < count; k++) {
            int color = highlighted ? COLOR_LINKING : colors.get(k);
            if (faded) color = (color & 0x00FFFFFF) | 0x30000000;
            List<int[]> segments = edgeSegments(e, k, count);

            for (int[] seg : segments) {
                if (seg[1] == seg[3]) horizontal(guiGraphics, seg[0], seg[2], seg[1], color, dashed);
                else vertical(guiGraphics, seg[0], seg[1], seg[3], color, dashed);
            }

            int[] last = segments.get(segments.size() - 1);
            int tx = last[2];
            int ty = last[3];
            for (int i = 0; i < 5; i++) {
                int half = 4 - i;
                guiGraphics.fill(tx - 5 + i, ty - half, tx - 4 + i, ty + half + 1, color);
            }
        }
    }

    // The straight pieces of line k (of count) of an edge, each {x1, y1, x2, y2}, ending at the arrow.
    private List<int[]> edgeSegments(int e, int k, int count) {
        Edge edge = snapshot.edges().get(e);
        int w = ManagerLayout.NODE_WIDTH;
        int h = ManagerLayout.NODE_HEIGHT;
        // the item/fluid/energy lines of one link sit tighter when they share a router side with other links
        int typeGap = outCount[e] > 1 || inCount[e] > 1 ? 2 : 4;
        int offset = Math.round((k - (count - 1) / 2.0F) * typeGap);

        int fx = layout.x[edge.from()] + w;
        int fy = layout.y[edge.from()] + h / 2 + portOffset(outSlot[e], outCount[e]) + offset;
        int tx = layout.x[edge.to()];
        int ty = layout.y[edge.to()] + h / 2 + portOffset(inSlot[e], inCount[e]) + offset;

        if (tx - fx >= 12) {
            int mid = laneX[e] + offset;
            return List.of(new int[]{fx, fy, mid, fy}, new int[]{mid, fy, mid, ty}, new int[]{mid, ty, tx, ty});
        }

        int out = fx + 14 + offset;
        int in = tx - 14 - offset;
        int loop = Math.max(layout.y[edge.from()], layout.y[edge.to()]) + h + 8 + k * 4 + inSlot[e] * 8;
        return List.of(new int[]{fx, fy, out, fy}, new int[]{out, fy, out, loop}, new int[]{out, loop, in, loop},
                new int[]{in, loop, in, ty}, new int[]{in, ty, tx, ty});
    }

    // The direct link under the cursor, for Ctrl + right click to remove. Shared-inventory links aren't real links.
    private int hoveredEdge(int mouseX, int mouseY) {
        if (snapshot == null || layout == null || !inChart(mouseX, mouseY)) return -1;

        double worldX = (mouseX - chartLeft() - panX) / zoom;
        double worldY = (mouseY - chartTop() - panY) / zoom;
        double reach = 3 / Math.min(zoom, 1.0F);

        for (int e = 0; e < snapshot.edges().size(); e++) {
            Edge edge = snapshot.edges().get(e);
            if (edge.viaInventory()) continue;

            int count = linkColors(edge.types()).size();
            for (int k = 0; k < count; k++) {
                for (int[] seg : edgeSegments(e, k, count)) {
                    if (worldX >= Math.min(seg[0], seg[2]) - reach && worldX <= Math.max(seg[0], seg[2]) + reach
                            && worldY >= Math.min(seg[1], seg[3]) - reach && worldY <= Math.max(seg[1], seg[3]) + reach) {
                        return e;
                    }
                }
            }
        }
        return -1;
    }

    // Works out which end is the exporter, so a drag from an importer to an exporter links too.
    // Returns {exporter, importer}, or null when the pair can't be linked.
    private int[] linkPair(int a, int b) {
        if (a < 0 || b < 0 || a == b) return null;
        Kind kindA = snapshot.nodes().get(a).kind();
        Kind kindB = snapshot.nodes().get(b).kind();
        if (kindA.exports() && EditLinkFromManager.accepts(kindB)) return new int[]{a, b};
        if (kindB.exports() && EditLinkFromManager.accepts(kindA)) return new int[]{b, a};
        return null;
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

        for (ButtonType type : RouterButtonTypes.all()) {
            x = legendEntry(guiGraphics, font, x, y, brighten(typeColor(type)), type.getLegendKey(), false);
        }
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
        // tooltips would cover the rename box while typing
        // nothing should cover the rename box while typing, or the drop target while dragging a link
        if (nameBox != null || linkFrom >= 0 || tabDown()) return;

        int index = hoveredNode(mouseX, mouseY);
        if (index < 0) {
            drawLinkTooltip(guiGraphics, font, mouseX, mouseY);
            return;
        }

        Node node = snapshot.nodes().get(index);
        List<Component> lines = new ArrayList<>();

        lines.add(Component.translatable("gui.routers.manager.kind." + switch (node.kind()) {
            case EXPORTER -> "exporter";
            case IMPORTER -> "importer";
            case IMPORTER_EXPORTER -> "importer_exporter";
            case DISTRIBUTOR -> "distributor";
            case UNLOADED -> "unloaded";
        }).withStyle(ChatFormatting.WHITE));

        if (!node.name().isEmpty()) lines.add(Component.literal(node.name()).withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatable("gui.routers.manager.position", node.pos().pos().toShortString()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gui.routers.manager.dimension", node.pos().dimension().identifier().toString()).withStyle(ChatFormatting.GRAY));

        if (node.kind() != Kind.UNLOADED) {
            if (!node.adjacent().isEmpty()) {
                lines.add(Component.translatable("gui.routers.manager.adjacent", node.adjacent().getHoverName()).withStyle(ChatFormatting.GRAY));
            }
            if (node.kind().exports()) {
                lines.add(Component.translatable("gui.routers.manager.exporter_side", describeFlags(node.exporterTypes(), node.exporterFlags())).withStyle(ChatFormatting.RED));
            }
            if (node.kind().imports()) {
                lines.add(Component.translatable("gui.routers.manager.importer_side", describeFlags(node.importerTypes(), node.importerFlags())).withStyle(ChatFormatting.BLUE));
            }
            if (node.kind() == Kind.DISTRIBUTOR) {
                lines.add(Component.translatable("gui.routers.manager.distributor_side", describeFlags(node.exporterTypes(), node.exporterFlags())).withStyle(ChatFormatting.DARK_AQUA));
            }
            if (!node.working()) {
                lines.add(Component.translatable("gui.routers.manager.disabled").withStyle(ChatFormatting.DARK_RED));
            }
            List<String> sendsTo = new ArrayList<>();
            List<String> fedBy = new ArrayList<>();
            for (Edge edge : snapshot.edges()) {
                if (edge.viaInventory()) continue;
                if (edge.from() == index) sendsTo.add(label(snapshot.nodes().get(edge.to())));
                if (edge.to() == index) fedBy.add(label(snapshot.nodes().get(edge.from())));
            }
            if (!sendsTo.isEmpty()) {
                lines.add(Component.translatable("gui.routers.manager.sends_to", String.join(", ", sendsTo)).withStyle(ChatFormatting.GRAY));
            }
            if (!fedBy.isEmpty()) {
                lines.add(Component.translatable("gui.routers.manager.fed_by", String.join(", ", fedBy)).withStyle(ChatFormatting.GRAY));
            }
            for (Component issue : issues.get(index)) lines.add(issue.copy().withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("gui.routers.manager.click_to_open").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            lines.add(Component.translatable("gui.routers.manager.middle_click_to_rename").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            lines.add(Component.translatable("gui.routers.manager.ctrl_drag_to_link").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            lines.add(Component.translatable("gui.routers.manager.ctrl_right_click_link").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            for (Component issue : issues.get(index)) lines.add(issue.copy().withStyle(ChatFormatting.GOLD));
        }
        lines.add(Component.translatable("gui.routers.manager.right_click_to_locate").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        lines.add(Component.translatable("gui.routers.manager.hold_tab").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        List<ClientTooltipComponent> components = new ArrayList<>();
        for (Component line : lines) components.add(ClientTooltipComponent.create(line.getVisualOrderText()));

        guiGraphics.tooltip(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    // Names the link under the cursor, and with Ctrl held, that a right click removes it.
    private void drawLinkTooltip(GuiGraphicsExtractor guiGraphics, Font font, int mouseX, int mouseY) {
        if (linkFrom >= 0) return;
        int edge = hoveredEdge(mouseX, mouseY);
        if (edge < 0) return;

        Edge link = snapshot.edges().get(edge);
        List<ClientTooltipComponent> components = List.of(
                ClientTooltipComponent.create(Component.translatable("gui.routers.manager.link",
                        label(snapshot.nodes().get(link.from())), label(snapshot.nodes().get(link.to()))).getVisualOrderText()),
                ClientTooltipComponent.create(Component.translatable(ctrlDown()
                                ? "gui.routers.manager.right_click_to_unlink" : "gui.routers.manager.ctrl_right_click_to_unlink")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC).getVisualOrderText()),
                ClientTooltipComponent.create(Component.translatable("gui.routers.manager.hold_tab")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC).getVisualOrderText()));
        guiGraphics.tooltip(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    private static String label(Node node) {
        return node.name().isEmpty() ? node.pos().pos().toShortString() : node.name();
    }

    private static String describeFlags(int types, int flags) {
        List<String> parts = new ArrayList<>();

        if (types == 0) {
            parts.add(Component.translatable("gui.routers.manager.no_upgrades").getString());
        }
        for (ButtonType type : RouterButtonTypes.all()) {
            if ((types & RoutersTransfers.bit(type.getId())) != 0) parts.add(Component.translatable(type.getLegendKey()).getString());
        }
        if ((flags & ManagerSnapshot.ROUND_ROBIN) != 0) parts.add(Component.translatable("gui.routers.manager.flag.round_robin").getString());
        if ((flags & ManagerSnapshot.DIMENSIONAL) != 0) parts.add(Component.translatable("gui.routers.manager.flag.dimensional").getString());
        if ((flags & ManagerSnapshot.BLACKLIST) != 0) parts.add(Component.translatable("gui.routers.manager.flag.blacklist").getString());
        if ((flags & ManagerSnapshot.IGNORE_NBT) != 0) parts.add(Component.translatable("gui.routers.manager.flag.ignore_nbt").getString());
        if ((flags & ManagerSnapshot.FILTERED) != 0) parts.add(Component.translatable("gui.routers.manager.flag.filtered").getString());

        return String.join(", ", parts);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // resync, in case Ctrl was let go while another window had focus and the release never arrived
        ctrlHeld = event.hasControlDown();
        if (nameBox != null && !nameBox.isMouseOver(event.x(), event.y())) {
            finishRename(true);
        }

        if (ctrlDown()) {
            if (event.button() == 0) {
                int index = hoveredNode((int) event.x(), (int) event.y());
                if (index >= 0) {
                    linkFrom = index;
                    linkX = event.x();
                    linkY = event.y();
                    return true;
                }
            } else if (event.button() == 1) {
                unlinkAt((int) event.x(), (int) event.y());
                return true; // with Ctrl held a right click is never a locate
            }
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            int index = hoveredNode((int) event.x(), (int) event.y());
            if (index >= 0) {
                startRename(snapshot.nodes().get(index));
                return true;
            }
        }

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
        if (linkFrom >= 0 && event.button() == 0) {
            int[] pair = linkPair(linkFrom, hoveredNode((int) event.x(), (int) event.y()));
            if (pair != null) sendLink(pair[0], pair[1], true);
            linkFrom = -1;
            return true;
        }

        if (pressing && event.button() == 0) {
            pressing = false;

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

    private void startRename(Node node) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        if (node.kind() == Kind.UNLOADED) {
            minecraft.player.sendOverlayMessage(Component.translatable("gui.routers.manager.cannot_open_unloaded"));
            return;
        }
        if (!node.pos().dimension().equals(minecraft.level.dimension())) {
            minecraft.player.sendOverlayMessage(Component.translatable("gui.routers.manager.cannot_rename_dimension"));
            return;
        }

        if (nameBox != null) removeWidget(nameBox);
        nameBox = new EditBox(font, 0, 0, RENAME_WIDTH, 16, Component.empty());
        nameBox.setMaxLength(NamedRouter.MAX_NAME_LENGTH);
        nameBox.setHint(Component.translatable("gui.routers.manager.rename_hint").withStyle(ChatFormatting.DARK_GRAY));
        nameBox.setValue(node.name());
        addWidget(nameBox);
        setFocused(nameBox);
        nameBox.setFocused(true);
        renaming = node.pos();
        placeNameBox();
    }

    // Keeps the rename box over its router's title line, clamped inside the chart.
    private void placeNameBox() {
        if (nameBox == null) return;

        int index = -1;
        for (int i = 0; i < snapshot.nodes().size(); i++) {
            if (snapshot.nodes().get(i).pos().equals(renaming)) index = i;
        }
        if (index < 0) {
            finishRename(false); // the router left the network while it was being renamed
            return;
        }

        int x = Math.round(chartLeft() + panX + layout.x[index] * zoom);
        int y = Math.round(chartTop() + panY + layout.y[index] * zoom);
        nameBox.setX(Math.max(chartLeft(), Math.min(x, chartRight() - RENAME_WIDTH)));
        nameBox.setY(Math.max(chartTop(), Math.min(y, chartBottom() - 16)));
    }

    // Enter or clicking away saves, Escape cancels. An empty name clears it.
    private void finishRename(boolean save) {
        if (nameBox == null) return;
        if (save) {
            ClientPacketDistributor.sendToServer(new RenameRouterFromManager(menu.getBlockPos(), renaming.pos(), nameBox.getValue()));
        }
        removeWidget(nameBox);
        nameBox = null;
        renaming = null;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isCtrl(event.key())) ctrlHeld = true;
        if (nameBox == null) {
            if (event.key() == GLFW.GLFW_KEY_TAB) {
                tabHeld = true;
                return true; // taken for hiding tooltips rather than moving widget focus
            }
            return super.keyPressed(event);
        }

        int key = event.key();
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            finishRename(true);
        } else if (key == GLFW.GLFW_KEY_ESCAPE) {
            finishRename(false);
        } else {
            nameBox.keyPressed(event);
        }
        // swallow everything while typing, or the inventory key would close the screen
        return true;
    }

    // On a link: removes it. On a router: removes its link if it has exactly one, since otherwise
    // there's no telling which was meant.
    private void unlinkAt(int mouseX, int mouseY) {
        int edge = hoveredEdge(mouseX, mouseY);
        int node = hoveredNode(mouseX, mouseY);

        if (node >= 0) {
            edge = -1;
            int links = 0;
            for (int e = 0; e < snapshot.edges().size(); e++) {
                Edge candidate = snapshot.edges().get(e);
                if (candidate.viaInventory() || (candidate.from() != node && candidate.to() != node)) continue;
                edge = e;
                links++;
            }
            if (links > 1) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    minecraft.player.sendOverlayMessage(Component.translatable("gui.routers.manager.pick_a_link"));
                }
                return;
            }
        }

        if (edge >= 0) {
            Edge removed = snapshot.edges().get(edge);
            sendLink(removed.from(), removed.to(), false);
        }
    }

    private void sendLink(int exporter, int importer, boolean link) {
        ClientPacketDistributor.sendToServer(new EditLinkFromManager(menu.getBlockPos(),
                snapshot.nodes().get(exporter).pos(), snapshot.nodes().get(importer).pos(), link));
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (isCtrl(event.key())) ctrlHeld = false;
        if (event.key() == GLFW.GLFW_KEY_TAB) tabHeld = false;
        return super.keyReleased(event);
    }

    private static boolean isCtrl(int key) {
        return key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL;
    }

    private boolean tabDown() {
        return tabHeld || com.mojang.blaze3d.platform.InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_TAB);
    }

    private boolean ctrlDown() {
        return ctrlHeld || Minecraft.getInstance().hasControlDown();
    }

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
        if (linkFrom >= 0) {
            linkX = event.x();
            linkY = event.y();
            return true;
        }

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
