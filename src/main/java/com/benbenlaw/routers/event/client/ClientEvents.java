package com.benbenlaw.routers.event.client;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlockEntities;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterCore;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.*;
import com.benbenlaw.routers.util.RoutersTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Routers.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String moreInfo = "";

        if (stack.getItem() instanceof UpgradeItem upgradeItem) {
            moreInfo = String.valueOf(upgradeItem.getExtractAmount());
        }

        addShiftTooltip(stack, event, RoutersBlocks.EXPORTER.get().asItem(), "tooltip.routers.exporter",
                String.valueOf(StartupConfig.defaultSpeedPerOperation.get()));

        addShiftTooltip(stack, event, RoutersBlocks.IMPORTER.get().asItem(), "tooltip.routers.importer");
        addShiftTooltip(stack, event, RoutersBlocks.IMPORTER_EXPORTER.get().asItem(), "tooltip.routers.importer_exporter");
        addShiftTooltip(stack, event, RoutersBlocks.ROUTER_MANAGER.get().asItem(), "tooltip.routers.router_manager");
        addShiftTooltip(stack, event, RoutersBlocks.DISTRIBUTOR.get().asItem(), "tooltip.routers.distributor");

        addShiftTooltip(stack, event, RoutersItems.CONNECTOR.get(), "tooltip.routers.connector");

        addShiftTooltip(stack, event, RoutersTags.Items.ITEM_UPGRADES, "tooltip.routers.item_upgrade", moreInfo);
        addShiftTooltip(stack, event, RoutersTags.Items.FLUID_UPGRADES, "tooltip.routers.fluid_upgrade", moreInfo);
        addShiftTooltip(stack, event, RoutersTags.Items.RF_UPGRADES, "tooltip.routers.energy_upgrade", moreInfo);
        addShiftTooltip(stack, event, RoutersTags.Items.SPEED_UPGRADES, "tooltip.routers.speed_upgrade", moreInfo);

        addShiftTooltip(stack, event, RoutersItems.ROUND_ROBIN_UPGRADE.get(), "tooltip.routers.round_robin_upgrade");
        addShiftTooltip(stack, event, RoutersItems.DIMENSIONAL_UPGRADE.get(), "tooltip.routers.dimensional_upgrade");
        addShiftTooltip(stack, event, RoutersItems.BLACKLIST_UPGRADE.get(), "tooltip.routers.blacklist_upgrade");
        addShiftTooltip(stack, event, RoutersItems.IGNORE_NBT_UPGRADE.get(), "tooltip.routers.ignore_nbt_upgrade");

        //Connectors
        GlobalPos exporterPos = stack.get(RoutersDataComponents.EXPORTER_POSITION.value());
        GlobalPos importerPos = stack.get(RoutersDataComponents.IMPORTER_POSITION.value());

        if (exporterPos != null) {
            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().add(Component.translatable("tooltip.routers.wrench_exporter", exporterPos.pos().toShortString()).withStyle(ChatFormatting.BLUE));
            }
        }
        if (importerPos != null) {
            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().add(Component.translatable("tooltip.routers.wrench_importer", importerPos.pos().toShortString()).withStyle(ChatFormatting.BLUE));
            }
        }

        if (stack.getItem() instanceof FilterItem filterItem) {
            FilterType mode = filterItem.filterType;

            if (mode == FilterType.TAG) {
                Identifier tagInfo = stack.get(RoutersDataComponents.TAG_FILTER.value());
                if (tagInfo != null) {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.tag_filter", tagInfo.toString());
                } else {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.tag_filter_info");
                }
            }
            if (mode == FilterType.MOD) {
                String modInfo = stack.get(RoutersDataComponents.MOD_FILTER.value());
                if (modInfo != null) {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.mod_filter", modInfo);
                } else {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.mod_filter_info");
                }
            }
            if (mode == FilterType.STOCK) {
                StockFilter stockInfo = stack.get(RoutersDataComponents.STOCK_FILTER.value());
                if (stockInfo != null) {
                    ItemStack stockStack = stockInfo.stack();
                    int stockCount = stockInfo.amount();
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.stock_filter", stockStack.getHoverName().getString(), String.valueOf(stockCount));
                } else {
                    addShiftTooltip(stack, event, filterItem, "tooltip.routers.stock_filter_info");
                }
            }
        }
    }


    public static void addShiftTooltip(ItemStack stack, ItemTooltipEvent event, Item item, String tooltipText, String additionalInfo) {
        if (!stack.is(item)) return;

        if (Minecraft.getInstance().hasShiftDown()) {
            event.getToolTip().add(
                    Component.translatable(tooltipText, additionalInfo).withStyle(ChatFormatting.BLUE)
            );
        } else {
            event.getToolTip().add(
                    Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    public static void addShiftTooltip(ItemStack stack, ItemTooltipEvent event, TagKey<Item> item, String tooltipText, String... additionalInfo) {
        if (!stack.is(item)) return;

        if (Minecraft.getInstance().hasShiftDown()) {
            event.getToolTip().add(
                    Component.translatable(tooltipText, (Object[]) additionalInfo).withStyle(ChatFormatting.BLUE)
            );
        } else {
            event.getToolTip().add(
                    Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    public static void addShiftTooltip(ItemStack stack, ItemTooltipEvent event, Item item, String tooltipText, String... additionalInfo) {
        if (!stack.is(item)) return;

        if (Minecraft.getInstance().hasShiftDown()) {
            event.getToolTip().add(
                    Component.translatable(tooltipText, (Object[]) additionalInfo).withStyle(ChatFormatting.BLUE)
            );
        } else {
            event.getToolTip().add(
                    Component.translatable("tooltip.bblcore.shift").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    private static final RenderType LINES_NO_DEPTH_TEST = RenderType.create("routers_lines_no_depth", RenderSetup.builder(RoutersRenderPipelines.LINES_NO_DEPTH).createRenderSetup());
    private static final int COLOR_RED   = ARGB.colorFromFloat(0.4F, 0.96F, 0.2F, 0.2F);
    private static final int COLOR_BLUE  = ARGB.colorFromFloat(0.4F, 0.26F, 0.53F, 0.96F);
    private static final int COLOR_GREEN = ARGB.colorFromFloat(0.4F, 0.2F, 0.96F, 0.3F);

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;

        ItemStack heldItem = player.getMainHandItem();
        boolean holdingWrench = heldItem.is(Tags.Items.TOOLS_WRENCH);
        GlobalPos highlighted = ManagerHighlight.active();
        if (!holdingWrench && highlighted == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        VertexConsumer lineBuilder = buffer.getBuffer(LINES_NO_DEPTH_TEST);

        boolean drewAnything = false;
        int pulseGreen = pulse(0.2F, 0.96F, 0.3F);

        GlobalPos exporterGlobalPos = holdingWrench ? heldItem.get(RoutersDataComponents.EXPORTER_POSITION.value()) : null;
        if (exporterGlobalPos != null) {
            BlockEntity be = level.getBlockEntity(exporterGlobalPos.pos());

            if (be instanceof ExporterBlockEntity exporterBlockEntity && exporterBlockEntity.importerPositions != null && !exporterBlockEntity.importerPositions.isEmpty()) {

                drawBlockOutline(poseStack, lineBuilder, level, exporterGlobalPos.pos(), cam, pulseGreen);
                drewAnything = true;

                for (GlobalPos importerGlobalPos : exporterBlockEntity.importerPositions) {
                    if (!importerGlobalPos.dimension().equals(level.dimension())) continue;

                    drawBlockOutline(poseStack, lineBuilder, level, importerGlobalPos.pos(), cam, COLOR_BLUE);
                }
            }
        }

        GlobalPos importerGlobalPos = holdingWrench ? heldItem.get(RoutersDataComponents.IMPORTER_POSITION.value()) : null;
        if (importerGlobalPos != null) {
            ImporterCore importerCore = ImporterCore.at(level, importerGlobalPos.pos());

            if (importerCore != null && importerCore.exporterPositions != null && !importerCore.exporterPositions.isEmpty()) {

                drawBlockOutline(poseStack, lineBuilder, level, importerGlobalPos.pos(), cam, pulseGreen);
                drewAnything = true;

                for (GlobalPos linkedExporterPos : importerCore.exporterPositions) {
                    if (!linkedExporterPos.dimension().equals(level.dimension())) continue;

                    drawBlockOutline(poseStack, lineBuilder, level, linkedExporterPos.pos(), cam, COLOR_RED);
                }
            }
        }

        if (highlighted != null && highlighted.dimension().equals(level.dimension())) {
            drawBlockOutline(poseStack, lineBuilder, level, highlighted.pos(), cam, pulse(1.0F, 1.0F, 1.0F));
            drewAnything = true;
        }

        if (drewAnything) {
            buffer.endBatch(LINES_NO_DEPTH_TEST);
        }
    }

    private static void drawBlockOutline(PoseStack poseStack, VertexConsumer lineBuilder, Level level, BlockPos pos, Vec3 cam, int color) {

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos, CollisionContext.empty());
        AABB bounds = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);

        AABB shifted = bounds.inflate(0.002).move(-cam.x, -cam.y, -cam.z);
        ShapeRenderer.renderShape(poseStack, lineBuilder, Shapes.create(shifted), 0, 0, 0, color, 10f);
    }

    private static int pulse(float r, float g, float b) {
        float time = (System.currentTimeMillis() % 1000L) / 1000.0f;
        float alpha = 0.25F + 0.35F * (float) (Math.sin(time * Math.PI * 2) * 0.5 + 0.5);
        return ARGB.colorFromFloat(alpha, r, g, b);
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(RoutersRenderPipelines.LINES_NO_DEPTH);
    }
}
