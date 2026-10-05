package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class RoutersLangProvider extends LanguageProvider {

    public RoutersLangProvider(PackOutput output) {
        super(output, Routers.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        //Creative Tab
        add("itemGroup.routers", "Routers");

        //Items
        add("item.routers.connector", "Connector");

        add("item.routers.rf_upgrade_1", "Energy Upgrade I");
        add("item.routers.rf_upgrade_2", "Energy Upgrade II");
        add("item.routers.rf_upgrade_3", "Energy Upgrade III");
        add("item.routers.rf_upgrade_4", "Energy Upgrade IV");
        add("item.routers.rf_upgrade_5", "Energy Upgrade V");

        add("item.routers.item_upgrade_1", "Item Upgrade I");
        add("item.routers.item_upgrade_2", "Item Upgrade II");
        add("item.routers.item_upgrade_3", "Item Upgrade III");
        add("item.routers.item_upgrade_4", "Item Upgrade IV");
        add("item.routers.item_upgrade_5", "Item Upgrade V");

        add("item.routers.fluid_upgrade_1", "Fluid Upgrade I");
        add("item.routers.fluid_upgrade_2", "Fluid Upgrade II");
        add("item.routers.fluid_upgrade_3", "Fluid Upgrade III");
        add("item.routers.fluid_upgrade_4", "Fluid Upgrade IV");
        add("item.routers.fluid_upgrade_5", "Fluid Upgrade V");

        add("item.routers.speed_upgrade_1", "Speed Upgrade I");
        add("item.routers.speed_upgrade_2", "Speed Upgrade II");
        add("item.routers.speed_upgrade_3", "Speed Upgrade III");
        add("item.routers.speed_upgrade_4", "Speed Upgrade IV");

        add("item.routers.round_robin_upgrade", "Round Robin Upgrade");
        add("item.routers.dimensional_upgrade", "Dimensional Upgrade");
        add("item.routers.blacklist_upgrade", "Blacklist Upgrade");
        add("item.routers.ignore_nbt_upgrade", "Ignore NBT/Components Upgrade");
        add("item.routers.mod_filter", "Mod Filter Upgrade");
        add("item.routers.tag_filter", "Tag Filter Upgrade");
        add("item.routers.stock_filter", "Stock Filter Upgrade");

        //Client Screen
        add("screen.routers.config", "Config Menu");

        //Blocks
        add("block.routers.exporter", "Exporter");
        add("block.routers.importer", "Importer");
        add("block.routers.importer_exporter", "Importer Exporter (BETA)");
        add("block.routers.router_manager", "Router Manager (BETA)");
        add("block.routers.distributor", "Distributor (BETA)");

        //Tooltips
        add("tooltip.routers.connector", "Shift Right Click to set Exporter / Importer target. Right Click to connect to target");
        add("tooltip.routers.exporter", "Exports resources from a connected block to Importer, Exporters extract every %s ticks");
        add("tooltip.routers.importer", "Receives resources from connected Exporters");
        add("tooltip.routers.importer_exporter", "Exports and Imports from the same block, toggle mode inside the GUI");

        add("gui.routers.importer_exporter.title.exporter", "Configuring Exporter");
        add("gui.routers.importer_exporter.title.importer", "Configuring Importer");
        add("tooltip.routers.router_manager", "Shows a flow chart of every Router connected to the Routers around it. Scroll to zoom, drag to pan, hover a Router for details");

        add("gui.routers.manager.title", "Router Manager");
        add("gui.routers.manager.routers", "%s Routers");
        add("gui.routers.manager.truncated", "Showing first %s Routers");
        add("gui.routers.manager.empty", "No Routers found near the Router Manager");
        add("gui.routers.manager.hint", "Scroll to zoom, drag to pan");
        add("gui.routers.manager.legend.item", "Items");
        add("gui.routers.manager.legend.fluid", "Fluids");
        add("gui.routers.manager.legend.energy", "Energy");
        add("gui.routers.manager.legend.inventory", "Via shared inventory");
        add("gui.routers.manager.kind.exporter", "Exporter");
        add("gui.routers.manager.kind.importer", "Importer");
        add("gui.routers.manager.kind.distributor", "Distributor");
        add("gui.routers.manager.kind.importer_exporter", "Importer Exporter");
        add("gui.routers.manager.kind.unloaded", "Unloaded or missing Router");
        add("gui.routers.manager.position", "Position: %s");
        add("gui.routers.manager.dimension", "Dimension: %s");
        add("gui.routers.manager.adjacent", "Connected to: %s");
        add("gui.routers.manager.exporter_side", "Exporter: %s");
        add("gui.routers.manager.importer_side", "Importer: %s");
        add("gui.routers.manager.distributor_side", "Distributes: %s");
        add("gui.routers.manager.no_upgrades", "No transfer upgrades");
        add("gui.routers.manager.flag.round_robin", "Round Robin");
        add("gui.routers.manager.flag.dimensional", "Dimensional");
        add("gui.routers.manager.flag.blacklist", "Blacklist");
        add("gui.routers.manager.flag.ignore_nbt", "Ignore NBT");
        add("gui.routers.manager.flag.filtered", "Filtered");
        add("gui.routers.manager.disabled", "Disabled by redstone");
        add("gui.routers.manager.click_to_open", "Click to open");
        add("gui.routers.manager.right_click_to_locate", "Right click to locate");
        add("gui.routers.manager.cannot_locate_dimension", "That Router is in another dimension, so it cannot be highlighted");
        add("gui.routers.manager.cannot_open_unloaded", "That Router is not loaded");
        add("gui.routers.manager.cannot_open_dimension", "That Router is in another dimension");
        add("gui.routers.manager.cannot_rename_dimension", "That Router is in another dimension, so it cannot be renamed from here");
        add("gui.routers.manager.middle_click_to_rename", "Middle click to rename");
        add("gui.routers.manager.ctrl_drag_to_link", "Ctrl + drag to another Router to link");
        add("gui.routers.manager.ctrl_right_click_link", "Ctrl + right click a link to remove it");
        add("gui.routers.manager.hold_tab", "Hold TAB to hide tooltips");
        add("gui.routers.manager.link", "%s -> %s");
        add("gui.routers.manager.right_click_to_unlink", "Right click to remove this link");
        add("gui.routers.manager.ctrl_right_click_to_unlink", "Ctrl + right click to remove this link");
        add("gui.routers.manager.sends_to", "Sends to: %s");
        add("gui.routers.manager.fed_by", "Fed by: %s");
        add("gui.routers.manager.pick_a_link", "That Router has several links, Ctrl + right click the link itself to remove it");
        add("gui.routers.manager.rename_hint", "Router name, Enter to save");
        add("gui.routers.manager.issue.no_importer", "Not linked to any Importer");
        add("gui.routers.manager.issue.no_exporter", "Not linked to any Exporter");
        add("gui.routers.manager.issue.no_links", "Not linked to anything");
        add("gui.routers.manager.issue.unloaded_link", "Linked to an unloaded or missing Router");
        add("gui.routers.manager.issue.needs_dimensional", "Linked to another dimension without a Dimensional upgrade");

        add("tooltip.routers.importer_exporter.switch_to_importer", "Switch to Importer settings");
        add("tooltip.routers.importer_exporter.switch_to_exporter", "Switch to Exporter settings");
        add("tooltip.routers.distributor", "Link Exporters to it like an Importer. Whatever they send is shared out between every machine in range, depending on this Distributor's own upgrades and filters. Has no storage");

        add("tooltip.routers.item_upgrade", "Allows the Extraction of Items from an Exporter at %s Per Operation");
        add("tooltip.routers.fluid_upgrade", "Allows the Extraction of Fluids from an Exporter at %smb Per Operation");
        add("tooltip.routers.energy_upgrade", "Allows the Extraction of Energy from an Exporter at %sRF Per Operation");
        add("tooltip.routers.speed_upgrade", "Allows the Extractor to extract every %s ticks");

        add("tooltip.routers.round_robin_upgrade", "After each operation, the Exporter will try to insert into the next connected Importer");
        add("tooltip.routers.dimensional_upgrade", "Allows the Exporter to send resources to an Importer in a different dimension");
        add("tooltip.routers.blacklist_upgrade", "Changes the filtering to be a Blacklist. Effects all resource types that can be filtered");
        add("tooltip.routers.ignore_nbt_upgrade", "Ignores NBT/Data when filtering resources. Effects all resource types that can be filtered");

        add("tooltip.routers.menu.item", "Item Filter");
        add("tooltip.routers.button.item", "Item Filter");
        add("tooltip.routers.button.item.locked", "No connected Exporter with an Item Upgrade yet - filter can still be set");

        add("tooltip.routers.menu.fluid", "Fluid Filter");
        add("tooltip.routers.button.fluid", "Fluid Filter");
        add("tooltip.routers.button.fluid.locked", "No connected Exporter with a Fluid Upgrade yet - filter can still be set");

        add("tooltip.routers.menu.energy", "Energy");
        add("tooltip.routers.button.energy", "Energy");
        add("tooltip.routers.button.energy.locked", "No connected Exporter with an Energy Upgrade yet - filter can still be set");

        add("tooltip.routers.button.back", "Back");

        add("tooltip.routers.empty_item_filter_slot", "Empty Item Filter Slot");
        add("tooltip.routers.empty_fluid_filter_slot", "Empty Fluid Filter Slot");

        add("tooltip.routers.wrench_exporter", "Linked Exporter at %s");
        add("tooltip.routers.wrench_importer", "Linked Importer at %s");

        add("tooltip.routers.tag_filter", "Set to %s");
        add("tooltip.routers.tag_filter_info", "Used to set a Tag as a filter");
        add("tooltip.routers.tag_filter_tooltip", "Enter tag...");

        add("tooltip.routers.mod_filter", "Set to %s");
        add("tooltip.routers.mod_filter_info", "Used to set a Mod as a filter");
        add("tooltip.routers.mod_filter_tooltip", "Enter mod name...");

        add("tooltip.routers.stock_filter", "Set to %s with %s amount");
        add("tooltip.routers.stock_filter_info", "Used in Importers to only allow up to the amount and item set inside");
        add("tooltip.routers.stock_filter_tooltip", "Enter item...");

        add("tooltip.routers.amount", "Max Amount");



        //Client Messages
        add("message.routers.exporter_selected", "Exporter target set to %s");
        add("message.routers.importer_selected", "Importer target set to %s");
        add("message.routers.distributor_selected", "Distributor target set to %s");
        add("message.routers.connected_exporter_to_importer", "Connected Exporter to Importer at %s");
        add("message.routers.connected_exporter_to_distributor", "Connected Exporter to Distributor at %s");
        add("message.routers.connected_importer_to_exporter", "Connected Importer to Exporter at %s");
        add("message.routers.cannot_connect_to_self", "A block cannot be connected to itself!");
        add("message.routers.not_loaded", "Area not loaded to connect routers!");
        add("message.routers.no_exporter_importer_selected", "No Exporter / Importer selected. Shift right click to set main connection !");
        add("message.routers.disconnected_exporter_from_importer", "Unlinked Exporter from Importer!");
        add("message.routers.disconnected_exporter_from_distributor", "Unlinked Exporter from Distributor!");


    }


}

