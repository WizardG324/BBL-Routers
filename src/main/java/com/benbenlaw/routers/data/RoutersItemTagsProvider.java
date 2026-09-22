package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RoutersItemTagsProvider extends ItemTagsProvider {

    RoutersItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Routers.MOD_ID);
    }
    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {

        //Exporter Upgrades (everything the exporter's upgrade row accepts)
        this.tag(RoutersTags.Items.EXPORTER_UPGRADES)
                .addTag(RoutersTags.Items.ITEM_UPGRADES)
                .addTag(RoutersTags.Items.FLUID_UPGRADES)
                .addTag(RoutersTags.Items.RF_UPGRADES)
                .addTag(RoutersTags.Items.SPEED_UPGRADES)
                .addTag(RoutersTags.Items.DIMENSIONAL_UPGRADES)
                .addTag(RoutersTags.Items.ROUND_ROBIN_UPGRADES)

                .add(RoutersItems.BLACKLIST_UPGRADE.get())
                .add(RoutersItems.IGNORE_NBT_UPGRADE.get())
        ;

        //Importer Upgrades (everything the importer's upgrade row accepts)
        this.tag(RoutersTags.Items.IMPORTER_UPGRADES)
                .addTag(RoutersTags.Items.ROUND_ROBIN_UPGRADES)

                .add(RoutersItems.BLACKLIST_UPGRADE.get())
                .add(RoutersItems.IGNORE_NBT_UPGRADE.get())
        ;

        //All Upgrades (union, for general grouping/JEI)
        this.tag(RoutersTags.Items.UPGRADES)
                .addTag(RoutersTags.Items.EXPORTER_UPGRADES)
                .addTag(RoutersTags.Items.IMPORTER_UPGRADES)
        ;

        //Wrenches
        this.tag(Tags.Items.TOOLS_WRENCH)
                .add(RoutersItems.CONNECTOR.asItem());

        //Dimensional Upgrades
        this.tag(RoutersTags.Items.DIMENSIONAL_UPGRADES).add(
                RoutersItems.DIMENSIONAL_UPGRADE.get()
        );

        /*
        //Heat Upgrades PC
        this.tag(RoutersTags.Items.HEAT_UPGRADES_PC).add(
                RoutersItems.HEAT_UPGRADE_PC.get()
        );

        //Pressure Upgrades
        this.tag(RoutersTags.Items.PRESSURE_UPGRADES).add(
                RoutersItems.PRESSURE_UPGRADE.get(),
                RoutersItems.REINFORCED_PRESSURE_UPGRADE.get(),
                RoutersItems.ADVANCED_PRESSURE_UPGRADE.get()
        );

        //Soul Upgrades
        this.tag(RoutersTags.Items.SOUL_UPGRADES).add(
                RoutersItems.SOUL_UPGRADE.get()
        );

        //Source Upgrades
        this.tag(RoutersTags.Items.SOURCE_UPGRADES).add(
                RoutersItems.SOURCE_UPGRADE_1.get(),
                RoutersItems.SOURCE_UPGRADE_2.get(),
                RoutersItems.SOURCE_UPGRADE_3.get(),
                RoutersItems.SOURCE_UPGRADE_4.get()
        );

         */


        //RF Upgrades
        this.tag(RoutersTags.Items.RF_UPGRADES).add(
                RoutersItems.RF_UPGRADE_1.get(),
                RoutersItems.RF_UPGRADE_2.get(),
                RoutersItems.RF_UPGRADE_3.get(),
                RoutersItems.RF_UPGRADE_4.get(),
                RoutersItems.RF_UPGRADE_5.get()
        );

        //Item Upgrades
        this.tag(RoutersTags.Items.ITEM_UPGRADES).add(
                RoutersItems.ITEM_UPGRADE_1.get(),
                RoutersItems.ITEM_UPGRADE_2.get(),
                RoutersItems.ITEM_UPGRADE_3.get(),
                RoutersItems.ITEM_UPGRADE_4.get(),
                RoutersItems.ITEM_UPGRADE_5.get()
        );

        //Fluid Upgrades
        this.tag(RoutersTags.Items.FLUID_UPGRADES).add(
                RoutersItems.FLUID_UPGRADE_1.get(),
                RoutersItems.FLUID_UPGRADE_2.get(),
                RoutersItems.FLUID_UPGRADE_3.get(),
                RoutersItems.FLUID_UPGRADE_4.get(),
                RoutersItems.FLUID_UPGRADE_5.get()
        );

        /*

        //Chemical Upgrades
        this.tag(RoutersTags.Items.CHEMICAL_UPGRADES).add(
                RoutersItems.CHEMICAL_UPGRADE_1.get(),
                RoutersItems.CHEMICAL_UPGRADE_2.get(),
                RoutersItems.CHEMICAL_UPGRADE_3.get(),
                RoutersItems.CHEMICAL_UPGRADE_4.get()
        );

         */

        //Speed Upgrades
        this.tag(RoutersTags.Items.SPEED_UPGRADES).add(
                RoutersItems.SPEED_UPGRADE_1.get(),
                RoutersItems.SPEED_UPGRADE_2.get(),
                RoutersItems.SPEED_UPGRADE_3.get(),
                RoutersItems.SPEED_UPGRADE_4.get()
        );

        //Round Robin Upgrade
        this.tag(RoutersTags.Items.ROUND_ROBIN_UPGRADES).add(
                RoutersItems.ROUND_ROBIN_UPGRADE.get()
        );

    }

}