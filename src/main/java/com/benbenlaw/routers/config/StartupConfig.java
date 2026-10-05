package com.benbenlaw.routers.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class StartupConfig {


    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Integer> distributorRange;
    public static final ModConfigSpec.ConfigValue<Integer> distributorMaxTargets;
    public static final ModConfigSpec.ConfigValue<Integer> distributorRefreshTicks;

    public static final ModConfigSpec.ConfigValue<Integer> RFPerTick1;
    public static final ModConfigSpec.ConfigValue<Integer> RFPerTick2;
    public static final ModConfigSpec.ConfigValue<Integer> RFPerTick3;
    public static final ModConfigSpec.ConfigValue<Integer> RFPerTick4;
    public static final ModConfigSpec.ConfigValue<Integer> RFPerTick5;

    public static final ModConfigSpec.ConfigValue<Integer> itemPerOperation1;
    public static final ModConfigSpec.ConfigValue<Integer> itemPerOperation2;
    public static final ModConfigSpec.ConfigValue<Integer> itemPerOperation3;
    public static final ModConfigSpec.ConfigValue<Integer> itemPerOperation4;
    public static final ModConfigSpec.ConfigValue<Integer> itemPerOperation5;

    public static final ModConfigSpec.ConfigValue<Integer> fluidPerOperation1;
    public static final ModConfigSpec.ConfigValue<Integer> fluidPerOperation2;
    public static final ModConfigSpec.ConfigValue<Integer> fluidPerOperation3;
    public static final ModConfigSpec.ConfigValue<Integer> fluidPerOperation4;
    public static final ModConfigSpec.ConfigValue<Integer> fluidPerOperation5;

    public static final ModConfigSpec.ConfigValue<Integer> chemicalPerOperation1;
    public static final ModConfigSpec.ConfigValue<Integer> chemicalPerOperation2;
    public static final ModConfigSpec.ConfigValue<Integer> chemicalPerOperation3;
    public static final ModConfigSpec.ConfigValue<Integer> chemicalPerOperation4;

    public static final ModConfigSpec.ConfigValue<Integer> defaultSpeedPerOperation;
    public static final ModConfigSpec.ConfigValue<Integer> speedPerOperation1;
    public static final ModConfigSpec.ConfigValue<Integer> speedPerOperation2;
    public static final ModConfigSpec.ConfigValue<Integer> speedPerOperation3;
    public static final ModConfigSpec.ConfigValue<Integer> speedPerOperation4;

    public static final ModConfigSpec.ConfigValue<Integer> maxInventoryScanPerOperation;
    public static final ModConfigSpec.ConfigValue<Integer> minBackoffTicks;
    public static final ModConfigSpec.ConfigValue<Integer> maxBackoffTicks;

    public static final ModConfigSpec.ConfigValue<Integer> riftEnergyPerOperation1;
    public static final ModConfigSpec.ConfigValue<Integer> riftEnergyPerOperation2;
    public static final ModConfigSpec.ConfigValue<Integer> riftEnergyPerOperation3;
    public static final ModConfigSpec.ConfigValue<Integer> riftEnergyPerOperation4;
    public static final ModConfigSpec.ConfigValue<Integer> riftEnergyPerOperation5;

    public static final ModConfigSpec.ConfigValue<Integer> managerSeedRadius;
    public static final ModConfigSpec.ConfigValue<Integer> managerMaxRouters;

    public static final ModConfigSpec.ConfigValue<Integer> sourcePerOperation1;
    public static final ModConfigSpec.ConfigValue<Integer> sourcePerOperation2;
    public static final ModConfigSpec.ConfigValue<Integer> sourcePerOperation3;
    public static final ModConfigSpec.ConfigValue<Integer> sourcePerOperation4;





    static {

        BUILDER.comment("Distributor").push("Distributor");

        distributorRange = BUILDER
                .comment("The range of the distributor in blocks.")
                .defineInRange("Distributor Range", 10, 1, 64);

        distributorMaxTargets = BUILDER
                .comment("The most machines a single distributor will share resources between. If more are in range, the closest ones are used.")
                .defineInRange("Distributor Max Machines", 64, 1, 1024);

        distributorRefreshTicks = BUILDER
                .comment("How often, in ticks, a distributor looks again for the machines around it. A new machine can take up to this long to start receiving.")
                .defineInRange("Distributor Refresh Ticks", 100, 1, 1200);


        BUILDER.comment("Routers Config").push("RF Upgrades");

        RFPerTick1 = BUILDER
                .comment("The maximum RF per tick that tier 1 can provide.")
                .defineInRange("RF Per Operation 1",  800, 1, Integer.MAX_VALUE);

        RFPerTick2 = BUILDER
                .comment("The maximum RF per tick that tier 2 can provide.")
                .defineInRange("RF Per Operation 2", 32000, 1, Integer.MAX_VALUE);

        RFPerTick3 = BUILDER
                .comment("The maximum RF per tick that tier 3 can provide.")
                .defineInRange("RF Per Operation 3", 64000, 1, Integer.MAX_VALUE);

        RFPerTick4 = BUILDER
                .comment("The maximum RF per tick that tier 4 can provide.")
                .defineInRange("RF Per Operation 4", 128000, 1, Integer.MAX_VALUE);

        RFPerTick5 = BUILDER
                .comment("The maximum RF per tick that tier 5 can provide.")
                .defineInRange("RF Per Operation 5", 250000000, 1, Integer.MAX_VALUE);

        riftEnergyPerOperation1 = BUILDER
                .comment("The most Rift energy per operation that tier 1 can move (needs BBL Rifts).")
                .defineInRange("Rift Energy Per Operation 1", 800, 1, Integer.MAX_VALUE);

        riftEnergyPerOperation2 = BUILDER
                .comment("The most Rift energy per operation that tier 2 can move (needs BBL Rifts).")
                .defineInRange("Rift Energy Per Operation 2", 32000, 1, Integer.MAX_VALUE);

        riftEnergyPerOperation3 = BUILDER
                .comment("The most Rift energy per operation that tier 3 can move (needs BBL Rifts).")
                .defineInRange("Rift Energy Per Operation 3", 64000, 1, Integer.MAX_VALUE);

        riftEnergyPerOperation4 = BUILDER
                .comment("The most Rift energy per operation that tier 4 can move (needs BBL Rifts).")
                .defineInRange("Rift Energy Per Operation 4", 128000, 1, Integer.MAX_VALUE);

        riftEnergyPerOperation5 = BUILDER
                .comment("The most Rift energy per operation that tier 5 can move (needs BBL Rifts).")
                .defineInRange("Rift Energy Per Operation 5", 250000000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Item Upgrades");

        itemPerOperation1 = BUILDER
                .comment("The maximum items per operation that tier 1 can provide.")
                .defineInRange("Items Per Operation 1", 1, 1, Integer.MAX_VALUE);

        itemPerOperation2 = BUILDER
                .comment("The maximum items per operation that tier 2 can provide.")
                .defineInRange("Items Per Operation 2", 8, 1, Integer.MAX_VALUE);

        itemPerOperation3 = BUILDER
                .comment("The maximum items per operation that tier 3 can provide.")
                .defineInRange("Items Per Operation 3", 32, 1, Integer.MAX_VALUE);

        itemPerOperation4 = BUILDER
                .comment("The maximum items per operation that tier 4 can provide.")
                .defineInRange("Items Per Operation 4", 64, 1, Integer.MAX_VALUE);

        itemPerOperation5 = BUILDER
                .comment("The maximum items per operation that tier 5 can provide.")
                .defineInRange("Items Per Operation 5", 25000000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Fluid Upgrades");

        fluidPerOperation1 = BUILDER
                .comment("The maximum mb per operation that tier 1 can provide.")
                .defineInRange("Fluid Per Operation 1", 100, 1, Integer.MAX_VALUE);

        fluidPerOperation2 = BUILDER
                .comment("The maximum mb per operation that tier 2 can provide.")
                .defineInRange("Fluid Per Operation 2", 1000, 1, Integer.MAX_VALUE);

        fluidPerOperation3 = BUILDER
                .comment("The maximum mb per operation that tier 3 can provide.")
                .defineInRange("Fluid Per Operation 3", 10000, 1, Integer.MAX_VALUE);

        fluidPerOperation4 = BUILDER
                .comment("The maximum mb per operation that tier 4 can provide.")
                .defineInRange("Fluid Per Operation 4", 100000, 1, Integer.MAX_VALUE);

        fluidPerOperation5 = BUILDER
                .comment("The maximum mb per operation that tier 5 can provide.")
                .defineInRange("Fluid Per Operation 5", 250000000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Chemical Upgrades");
        chemicalPerOperation1 = BUILDER
                .comment("The maximum mB per operation that tier 1 can provide.")
                .defineInRange("Chemical Per Operation 1", 10, 1, Integer.MAX_VALUE);
        chemicalPerOperation2 = BUILDER
                .comment("The maximum mB per operation that tier 2 can provide.")
                .defineInRange("Chemical Per Operation 2", 100, 1, Integer.MAX_VALUE);
        chemicalPerOperation3 = BUILDER
                .comment("The maximum mB per operation that tier 3 can provide.")
                .defineInRange("Chemical Per Operation 3", 1000, 1, Integer.MAX_VALUE);
        chemicalPerOperation4 = BUILDER
                .comment("The maximum mB per operation that tier 4 can provide.")
                .defineInRange("Chemical Per Operation 4", 10000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Speed Upgrades");

        defaultSpeedPerOperation = BUILDER
                .comment("The default speed multiplier that is used when no speed upgrades are installed.")
                .defineInRange("Default Speed Per Operation", 40, 1, Integer.MAX_VALUE);
        speedPerOperation1 = BUILDER
                .comment("The speed multiplier that tier 1 can provide.")
                .defineInRange("Speed Per Operation 1", 30, 1, Integer.MAX_VALUE);
        speedPerOperation2 = BUILDER
                .comment("The speed multiplier that tier 2 can provide.")
                .defineInRange("Speed Per Operation 2", 20, 1, Integer.MAX_VALUE);
        speedPerOperation3 = BUILDER
                .comment("The speed multiplier that tier 3 can provide.")
                .defineInRange("Speed Per Operation 3", 10, 1, Integer.MAX_VALUE);
        speedPerOperation4 = BUILDER
                .comment("The speed multiplier that tier 4 can provide.")
                .defineInRange("Speed Per Operation 4", 1, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Performance");

        maxInventoryScanPerOperation = BUILDER
                .comment("The maximum number of slots an exporter/importer will scan in a single operation when looking for a match. ",
                        "Large storage inventories (hundreds or thousands of slots) get scanned across multiple operations instead of all at once, to keep any single tick cheap. ",
                        "Lower this if you have very large storages and are seeing lag from routers connected to them; raise it if you'd rather have routers find matches faster at the cost of a heavier tick.")
                .defineInRange("Max Inventory Scan Per Operation", 256, 1, Integer.MAX_VALUE);

        minBackoffTicks = BUILDER
                .comment("When an exporter/importer finds nothing to move for a resource type (nothing matches, or everything that ",
                        "matches gets rejected), it waits at least this many real ticks before checking that type again - regardless of ",
                        "speed tier, so a Speed 4 router backing off still gets a meaningful break instead of retrying almost immediately. ",
                        "This also doubles as the growth step: each consecutive miss adds another one of these, up to Max Backoff Ticks. ",
                        "Resets instantly to checking every operation the moment something actually moves. Set to 0 to disable backoff entirely.")
                .defineInRange("Min Backoff Ticks", 20, 0, Integer.MAX_VALUE);

        maxBackoffTicks = BUILDER
                .comment("The upper cap on how many ticks a persistently empty/blocked exporter or importer will wait between checks for one resource type.")
                .defineInRange("Max Backoff Ticks", 400, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Source Upgrades");

        sourcePerOperation1 = BUILDER
                .comment("The maximum sources per operation that tier 1 can provide.")
                .defineInRange("Source Per Operation 1", 10, 1, Integer.MAX_VALUE);
        sourcePerOperation2 = BUILDER
                .comment("The maximum sources per operation that tier 2 can provide.")
                .defineInRange("Source Per Operation 2", 100, 1, Integer.MAX_VALUE);
        sourcePerOperation3 = BUILDER
                .comment("The maximum sources per operation that tier 3 can provide.")
                .defineInRange("Source Per Operation 3", 500, 1, Integer.MAX_VALUE);
        sourcePerOperation4 = BUILDER
                .comment("The maximum sources per operation that tier 4 can provide.")
                .defineInRange("Source Per Operation 4", 1000, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.comment("Router Manager").push("Router Manager");

        managerSeedRadius = BUILDER
                .comment("Routers within this many blocks of a Router Manager are where its flow chart starts. ",
                        "From there it follows every link to chart the whole connected network, even far away or in other dimensions.")
                .defineInRange("Seed Radius", 16, 1, 128);

        managerMaxRouters = BUILDER
                .comment("The most routers a single Router Manager flow chart will include. Larger networks are cut off at this many.")
                .defineInRange("Max Routers Charted", 256, 1, 4096);

        BUILDER.pop();

        SPEC = BUILDER.build();

    }

}
