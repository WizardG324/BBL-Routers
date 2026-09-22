package com.benbenlaw.routers.item;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.config.StartupConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RoutersItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Routers.MOD_ID);

    public static final DeferredItem<Item> CONNECTOR = ITEMS.registerItem("connector",
            properties -> new ConnectorItem(new Item.Properties().setId(createID("connector"))));

    public static final DeferredItem<Item> RF_UPGRADE_1 = ITEMS.registerItem("rf_upgrade_1",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("rf_upgrade_1")), StartupConfig.RFPerTick1.get()));

    public static final DeferredItem<Item> RF_UPGRADE_2 = ITEMS.registerItem("rf_upgrade_2",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("rf_upgrade_2")),  StartupConfig.RFPerTick2.get()));

    public static final DeferredItem<Item> RF_UPGRADE_3 = ITEMS.registerItem("rf_upgrade_3",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("rf_upgrade_3")), StartupConfig.RFPerTick3.get()));

    public static final DeferredItem<Item> RF_UPGRADE_4 = ITEMS.registerItem("rf_upgrade_4",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("rf_upgrade_4")), StartupConfig.RFPerTick4.get()));

    public static final DeferredItem<Item> RF_UPGRADE_5 = ITEMS.registerItem("rf_upgrade_5",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("rf_upgrade_5")), StartupConfig.RFPerTick5.get()));

    public static final DeferredItem<Item> ITEM_UPGRADE_1 = ITEMS.registerItem("item_upgrade_1",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("item_upgrade_1")) , StartupConfig.itemPerOperation1.get()));

    public static final DeferredItem<Item> ITEM_UPGRADE_2 = ITEMS.registerItem("item_upgrade_2",
            properties-> new UpgradeItem(new Item.Properties().setId(createID("item_upgrade_2")) , StartupConfig.itemPerOperation2.get()));

    public static final DeferredItem<Item> ITEM_UPGRADE_3 = ITEMS.registerItem("item_upgrade_3",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("item_upgrade_3")) , StartupConfig.itemPerOperation3.get()));

    public static final DeferredItem<Item> ITEM_UPGRADE_4 = ITEMS.registerItem("item_upgrade_4",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("item_upgrade_4")) , StartupConfig.itemPerOperation4.get()));

    public static final DeferredItem<Item> ITEM_UPGRADE_5 = ITEMS.registerItem("item_upgrade_5",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("item_upgrade_5")) , StartupConfig.itemPerOperation5.get()));


    public static final DeferredItem<Item> FLUID_UPGRADE_1 = ITEMS.registerItem("fluid_upgrade_1",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("fluid_upgrade_1")) , StartupConfig.fluidPerOperation1.get()));

    public static final DeferredItem<Item> FLUID_UPGRADE_2 = ITEMS.registerItem("fluid_upgrade_2",
            properties-> new UpgradeItem(new Item.Properties().setId(createID("fluid_upgrade_2")) , StartupConfig.fluidPerOperation2.get()));

    public static final DeferredItem<Item> FLUID_UPGRADE_3 = ITEMS.registerItem("fluid_upgrade_3",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("fluid_upgrade_3")) , StartupConfig.fluidPerOperation3.get()));

    public static final DeferredItem<Item> FLUID_UPGRADE_4 = ITEMS.registerItem("fluid_upgrade_4",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("fluid_upgrade_4")) , StartupConfig.fluidPerOperation4.get()));

    public static final DeferredItem<Item> FLUID_UPGRADE_5 = ITEMS.registerItem("fluid_upgrade_5",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("fluid_upgrade_5")) , StartupConfig.fluidPerOperation5.get()));



    public static final DeferredItem<Item> SPEED_UPGRADE_1 = ITEMS.registerItem("speed_upgrade_1",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("speed_upgrade_1")), StartupConfig.speedPerOperation1.get()));

    public static final DeferredItem<Item> SPEED_UPGRADE_2 = ITEMS.registerItem("speed_upgrade_2",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("speed_upgrade_2")), StartupConfig.speedPerOperation2.get()));

    public static final DeferredItem<Item> SPEED_UPGRADE_3 = ITEMS.registerItem("speed_upgrade_3",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("speed_upgrade_3")), StartupConfig.speedPerOperation3.get()));

    public static final DeferredItem<Item> SPEED_UPGRADE_4 = ITEMS.registerItem("speed_upgrade_4",
            properties -> new UpgradeItem(new Item.Properties().setId(createID("speed_upgrade_4")), StartupConfig.speedPerOperation4.get()));

    public static final DeferredItem<Item> MOD_FILTER = ITEMS.registerItem("mod_filter",
            properties -> new FilterItem(new Item.Properties().setId(createID("mod_filter")), FilterType.MOD));

    public static final DeferredItem<Item> TAG_FILTER = ITEMS.registerItem("tag_filter",
            properties -> new FilterItem(new Item.Properties().setId(createID("tag_filter")), FilterType.TAG));

    public static final DeferredItem<Item> STOCK_FILTER = ITEMS.registerItem("stock_filter",
            properties -> new FilterItem(new Item.Properties().setId(createID("stock_filter")), FilterType.STOCK));
    
    public static final DeferredItem<Item> ROUND_ROBIN_UPGRADE = ITEMS.register("round_robin_upgrade",
            () -> new UpgradeItem(new Item.Properties().setId(createID("round_robin_upgrade")), 0));

    public static final DeferredItem<Item> DIMENSIONAL_UPGRADE = ITEMS.register("dimensional_upgrade",
            () -> new UpgradeItem(new Item.Properties().setId(createID("dimensional_upgrade")), 1));

    public static final DeferredItem<Item> BLACKLIST_UPGRADE = ITEMS.register("blacklist_upgrade",
            () -> new UpgradeItem(new Item.Properties().setId(createID("blacklist_upgrade")), 0));

    public static final DeferredItem<Item> IGNORE_NBT_UPGRADE = ITEMS.register("ignore_nbt_upgrade",
            () -> new UpgradeItem(new Item.Properties().setId(createID("ignore_nbt_upgrade")), 0));



    public static ResourceKey<Item> createID(String name) {
        return ResourceKey.create(Registries.ITEM, Routers.identifier(name));
    }

}
