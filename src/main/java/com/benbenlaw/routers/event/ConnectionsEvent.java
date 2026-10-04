package com.benbenlaw.routers.event;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.item.RoutersDataComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Routers.MOD_ID)
public class ConnectionsEvent {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getLevel();
        GlobalPos clickedPos = GlobalPos.of(level.dimension(), event.getPos());
        BlockState blockState = level.getBlockState(clickedPos.pos());

        if (!(blockState.getBlock() instanceof RouterBlock)) return;
        if (level.isClientSide()) return;

        ItemStack heldItem = event.getItemStack();
        Player player = event.getEntity();
        String clickedPosStr = clickedPos.pos().toShortString();

        if (heldItem.is(Tags.Items.TOOLS_WRENCH)) {

           GlobalPos mainExporterPos = heldItem.get(RoutersDataComponents.EXPORTER_POSITION.value());
           GlobalPos mainImporterPos = heldItem.get(RoutersDataComponents.IMPORTER_POSITION.value());

            boolean hybridImporterMode = level.getBlockEntity(clickedPos.pos()) instanceof ImporterExporterBlockEntity hybrid && hybrid.isViewingImporterSide();
            boolean isHybrid = blockState.is(RoutersBlocks.IMPORTER_EXPORTER);
            boolean actsAsExporter = blockState.is(RoutersBlocks.EXPORTER) || (isHybrid && !hybridImporterMode);
            boolean actsAsImporter = blockState.is(RoutersBlocks.IMPORTER) || blockState.is(RoutersBlocks.DISTRIBUTOR) || (isHybrid && hybridImporterMode);

            if (player.isShiftKeyDown()) {
                player.swing(event.getHand(), true);

                if (actsAsExporter) {
                    player.sendSystemMessage(
                            Component.translatable("message.routers.exporter_selected", clickedPosStr)
                    );
                    if (clickedPos.equals(mainImporterPos)) {
                        heldItem.remove(RoutersDataComponents.IMPORTER_POSITION.value());
                    }
                    heldItem.set(RoutersDataComponents.EXPORTER_POSITION, clickedPos);

                } else if (actsAsImporter) {
                    player.sendSystemMessage(
                            Component.translatable(blockState.is(RoutersBlocks.DISTRIBUTOR)
                                    ? "message.routers.distributor_selected"
                                    : "message.routers.importer_selected", clickedPosStr)
                    );
                    if (clickedPos.equals(mainExporterPos)) {
                        heldItem.remove(RoutersDataComponents.EXPORTER_POSITION.value());
                    }
                    heldItem.set(RoutersDataComponents.IMPORTER_POSITION, clickedPos);
                }

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            if (mainExporterPos != null && actsAsImporter) {
                connectExporterToImporter(level, player, mainExporterPos, clickedPos);
                return;
            }

            if (mainImporterPos != null && actsAsExporter) {
                connectExporterToImporter(level, player, clickedPos, mainImporterPos);
                return;
            }

            player.sendSystemMessage(
                    Component.translatable("message.routers.no_exporter_importer_selected")
            );
        }
    }

    private static void connectExporterToImporter(Level level, Player player, GlobalPos exporterPos, GlobalPos importerPos) {
        if (exporterPos.equals(importerPos)) {
            player.sendSystemMessage(
                    Component.translatable("message.routers.cannot_connect_to_self")
            );
            return;
        }

        ServerLevel exporterLevel = level.getServer().getLevel(exporterPos.dimension());

        if (exporterLevel == null || !exporterLevel.isLoaded(exporterPos.pos())) {
            player.sendSystemMessage(
                    Component.translatable("message.routers.not_loaded")
            );
            return;
        }

        if (!(exporterLevel.getBlockEntity(exporterPos.pos()) instanceof ExporterBlockEntity exporter)) {
            player.sendSystemMessage(
                    Component.translatable("message.routers.not_loaded")
            );
            return;
        }

        ServerLevel importerLevel = level.getServer().getLevel(importerPos.dimension());
        String target = importerLevel != null && importerLevel.getBlockState(importerPos.pos()).is(RoutersBlocks.DISTRIBUTOR)
                ? "distributor" : "importer";

        boolean connected = exporter.toggleImporterPosition(importerPos);

        if (connected) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.routers.connected_exporter_to_" + target,
                            importerPos.pos().toShortString()
                    )
            );
        } else {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.routers.disconnected_exporter_from_" + target,
                            importerPos.pos().toShortString()
                    )
            );
        }
    }
}