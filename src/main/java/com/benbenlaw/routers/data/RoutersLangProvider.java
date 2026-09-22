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
        add("block.routers.distributor", "Distributor (BETA)");

        //Tooltips
        add("tooltip.routers.connector", "Shift Right Click to set Exporter / Importer target. Right Click to connect to target");
        add("tooltip.routers.exporter", "Exports resources from a connected block to Importer, Exporters extract every %s ticks");
        add("tooltip.routers.importer", "Receives resources from connected Exporters");
        add("tooltip.routers.distributor", "Distributes energy from this to other energy receiving blocks in the area");

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
        add("message.routers.connected_exporter_to_importer", "Connected Exporter to Importer at %s");
        add("message.routers.connected_importer_to_exporter", "Connected Importer to Exporter at %s");
        add("message.routers.not_loaded", "Area not loaded to connect routers!");
        add("message.routers.no_exporter_importer_selected", "No Exporter / Importer selected. Shift right click to set main connection !");
        add("message.routers.disconnected_exporter_from_importer", "Unlinked Exporter from Importer!");


    }


}

