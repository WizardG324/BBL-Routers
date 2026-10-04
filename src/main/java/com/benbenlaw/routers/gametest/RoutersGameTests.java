package com.benbenlaw.routers.gametest;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.manager.ManagerScanner;
import com.benbenlaw.routers.manager.ManagerSnapshot;
import com.benbenlaw.routers.config.StartupConfig;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.Consumer;

public class RoutersGameTests {

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, Routers.MOD_ID);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXPORTER_ITEM_FILTER =
            TEST_FUNCTIONS.register("exporter_item_filter", () -> RoutersGameTests::exporterItemFilterOnlyLetsFilteredItemThrough);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXPORTER_FINDS_SCATTERED_ITEM =
            TEST_FUNCTIONS.register("exporter_finds_scattered_item", () -> RoutersGameTests::exporterFindsItemPastFirstScanWindow);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> IMPORTER_EXPORTER_BOTH_ROLES =
            TEST_FUNCTIONS.register("importer_exporter_both_roles", () -> RoutersGameTests::importerExporterActsAsExporterAndImporterAtOnce);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTER_MANAGER_SCAN =
            TEST_FUNCTIONS.register("router_manager_scan", () -> RoutersGameTests::routerManagerChartsTheConnectedNetwork);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTER_MANAGER_SHARED_INVENTORY =
            TEST_FUNCTIONS.register("router_manager_shared_inventory", () -> RoutersGameTests::routerManagerChainsRoutersSharingAnInventory);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DISTRIBUTOR_SPREADS_ITEMS =
            TEST_FUNCTIONS.register("distributor_spreads_items", () -> RoutersGameTests::distributorSpreadsFilteredItemsToNearbyMachines);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DISTRIBUTOR_NEEDS_UPGRADE =
            TEST_FUNCTIONS.register("distributor_needs_upgrade", () -> RoutersGameTests::distributorWithoutUpgradeDistributesNothing);

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Routers.identifier("default"));

        TestData<Holder<TestEnvironmentDefinition<?>>> testData = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                120,
                1,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                8
        );

        event.registerTest(Routers.identifier("exporter_item_filter"),
                new FunctionGameTestInstance(EXPORTER_ITEM_FILTER.getKey(), testData));

        TestData<Holder<TestEnvironmentDefinition<?>>> longTestData = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                1000,
                1,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                8
        );

        event.registerTest(Routers.identifier("exporter_finds_scattered_item"),
                new FunctionGameTestInstance(EXPORTER_FINDS_SCATTERED_ITEM.getKey(), longTestData));

        event.registerTest(Routers.identifier("importer_exporter_both_roles"),
                new FunctionGameTestInstance(IMPORTER_EXPORTER_BOTH_ROLES.getKey(), longTestData));

        event.registerTest(Routers.identifier("router_manager_scan"),
                new FunctionGameTestInstance(ROUTER_MANAGER_SCAN.getKey(), testData));

        event.registerTest(Routers.identifier("distributor_spreads_items"),
                new FunctionGameTestInstance(DISTRIBUTOR_SPREADS_ITEMS.getKey(), longTestData));

        event.registerTest(Routers.identifier("distributor_needs_upgrade"),
                new FunctionGameTestInstance(DISTRIBUTOR_NEEDS_UPGRADE.getKey(), longTestData));

        event.registerTest(Routers.identifier("router_manager_shared_inventory"),
                new FunctionGameTestInstance(ROUTER_MANAGER_SHARED_INVENTORY.getKey(), testData));
    }

    private static void exporterItemFilterOnlyLetsFilteredItemThrough(GameTestHelper helper) {
        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourceChestPos = new BlockPos(1, 1, 2);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos destChestPos = new BlockPos(5, 1, 2);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourceChestPos, Blocks.CHEST);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(destChestPos, Blocks.CHEST);

        ChestBlockEntity sourceChest = helper.getBlockEntity(sourceChestPos, ChestBlockEntity.class);
        sourceChest.setItem(0, new ItemStack(Items.DIAMOND, 5));
        sourceChest.setItem(1, new ItemStack(Items.DIRT, 5));

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(importerPos)));

        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        exporter.getFilterItemHandler().set(0, ItemResource.of(Items.DIAMOND), 1);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertContainerContains(destChestPos, Items.DIAMOND))
                .thenExecute(() -> {
                    ChestBlockEntity dest = helper.getBlockEntity(destChestPos, ChestBlockEntity.class);
                    boolean hasDirt = false;
                    for (int i = 0; i < dest.getContainerSize(); i++) {
                        if (dest.getItem(i).is(Items.DIRT)) {
                            hasDirt = true;
                        }
                    }
                    helper.assertTrue(!hasDirt, "Dirt should have been blocked by the exporter's item filter");
                })
                .thenSucceed();
    }

    private static void exporterFindsItemPastFirstScanWindow(GameTestHelper helper) {
        int originalScanSize = StartupConfig.maxInventoryScanPerOperation.get();
        StartupConfig.maxInventoryScanPerOperation.set(3);
        helper.runBeforeTestEnd(() -> StartupConfig.maxInventoryScanPerOperation.set(originalScanSize));

        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourceChestPos = new BlockPos(1, 1, 2);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos destChestPos = new BlockPos(5, 1, 2);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourceChestPos, Blocks.CHEST);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(destChestPos, Blocks.CHEST);

        ChestBlockEntity sourceChest = helper.getBlockEntity(sourceChestPos, ChestBlockEntity.class);
        sourceChest.setItem(26, new ItemStack(Items.DIAMOND, 1));

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(importerPos)));
        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertContainerContains(destChestPos, Items.DIAMOND))
                .thenExecute(() -> StartupConfig.maxInventoryScanPerOperation.set(originalScanSize))
                .thenSucceed();
    }

    // Two Importer Exporters linked to each other. Each one exports its own chest's items to the other
    // (exporter side) while receiving the other's items into its chest (importer side), so both roles
    // are exercised on the same block at the same time. The exporter filters make the swap settle.
    private static void importerExporterActsAsExporterAndImporterAtOnce(GameTestHelper helper) {
        BlockPos firstPos = new BlockPos(1, 1, 1);
        BlockPos firstChestPos = new BlockPos(1, 1, 2);
        BlockPos secondPos = new BlockPos(5, 1, 1);
        BlockPos secondChestPos = new BlockPos(5, 1, 2);

        helper.setBlock(firstPos, RoutersBlocks.IMPORTER_EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(firstChestPos, Blocks.CHEST);
        helper.setBlock(secondPos, RoutersBlocks.IMPORTER_EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(secondChestPos, Blocks.CHEST);

        helper.getBlockEntity(firstChestPos, ChestBlockEntity.class).setItem(0, new ItemStack(Items.DIAMOND, 5));
        helper.getBlockEntity(secondChestPos, ChestBlockEntity.class).setItem(0, new ItemStack(Items.DIRT, 5));

        ImporterExporterBlockEntity first = helper.getBlockEntity(firstPos, ImporterExporterBlockEntity.class);
        ImporterExporterBlockEntity second = helper.getBlockEntity(secondPos, ImporterExporterBlockEntity.class);

        GlobalPos firstGlobal = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(firstPos));
        GlobalPos secondGlobal = GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(secondPos));

        first.toggleImporterPosition(secondGlobal);
        second.toggleImporterPosition(firstGlobal);

        first.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        first.getFilterItemHandler().set(0, ItemResource.of(Items.DIAMOND), 1);
        second.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        second.getFilterItemHandler().set(0, ItemResource.of(Items.DIRT), 1);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertContainerContains(secondChestPos, Items.DIAMOND);
                    helper.assertContainerContains(firstChestPos, Items.DIRT);
                })
                .thenExecute(() -> {
                    helper.assertTrue(first.getImporterCore().exporterPositions.size() == 1,
                            "First block's importer side should be linked to the second block's exporter side");
                    helper.assertTrue(second.getImporterCore().exporterPositions.size() == 1,
                            "Second block's importer side should be linked to the first block's exporter side");
                })
                .thenSucceed();
    }

    // An exporter feeding a plain importer and an Importer Exporter, with one extra link to a position
    // that holds no router at all. The manager should chart all of them from the exporter alone being
    // in range, and mark the missing one as unloaded/missing.
    private static void routerManagerChartsTheConnectedNetwork(GameTestHelper helper) {
        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos hybridPos = new BlockPos(9, 1, 1);
        BlockPos missingPos = new BlockPos(9, 1, 5);
        BlockPos managerPos = new BlockPos(1, 1, 5);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(hybridPos, RoutersBlocks.IMPORTER_EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(managerPos, RoutersBlocks.ROUTER_MANAGER.get());

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);

        var dimension = helper.getLevel().dimension();
        exporter.toggleImporterPosition(GlobalPos.of(dimension, helper.absolutePos(importerPos)));
        exporter.toggleImporterPosition(GlobalPos.of(dimension, helper.absolutePos(hybridPos)));
        exporter.importerPositions.add(GlobalPos.of(dimension, helper.absolutePos(missingPos)));

        helper.startSequence()
                .thenExecute(() -> {
                    ManagerSnapshot snapshot = ManagerScanner.scan(helper.getLevel(), helper.absolutePos(managerPos));

                    helper.assertTrue(snapshot.nodes().size() == 4, "Expected 4 charted routers but got " + snapshot.nodes().size());
                    helper.assertTrue(snapshot.edges().size() == 3, "Expected 3 links but got " + snapshot.edges().size());

                    long exporters = snapshot.nodes().stream().filter(n -> n.kind() == ManagerSnapshot.Kind.EXPORTER).count();
                    long importers = snapshot.nodes().stream().filter(n -> n.kind() == ManagerSnapshot.Kind.IMPORTER).count();
                    long hybrids = snapshot.nodes().stream().filter(n -> n.kind() == ManagerSnapshot.Kind.IMPORTER_EXPORTER).count();
                    long missing = snapshot.nodes().stream().filter(n -> n.kind() == ManagerSnapshot.Kind.UNLOADED).count();
                    helper.assertTrue(exporters == 1 && importers == 1 && hybrids == 1 && missing == 1,
                            "Wrong router kinds: " + exporters + "/" + importers + "/" + hybrids + "/" + missing);

                    helper.assertTrue(snapshot.edges().stream().allMatch(e -> (e.types() & ManagerSnapshot.ITEM) != 0),
                            "Every link from the item exporter should be marked as carrying items");
                })
                .thenSucceed();
    }

    // An importer and an unrelated exporter on opposite sides of one chest are not linked to each other,
    // but items pass through the chest, so the chart should chain importer -> exporter.
    private static void routerManagerChainsRoutersSharingAnInventory(GameTestHelper helper) {
        BlockPos importerPos = new BlockPos(1, 1, 1);
        BlockPos chestPos = new BlockPos(1, 1, 2);
        BlockPos exporterPos = new BlockPos(1, 1, 3);
        BlockPos managerPos = new BlockPos(5, 1, 1);

        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.NORTH);
        helper.setBlock(managerPos, RoutersBlocks.ROUTER_MANAGER.get());

        helper.startSequence()
                .thenExecute(() -> {
                    ManagerSnapshot snapshot = ManagerScanner.scan(helper.getLevel(), helper.absolutePos(managerPos));

                    helper.assertTrue(snapshot.nodes().size() == 2, "Expected 2 charted routers but got " + snapshot.nodes().size());
                    helper.assertTrue(snapshot.edges().size() == 1, "Expected 1 shared-inventory link but got " + snapshot.edges().size());

                    ManagerSnapshot.Edge edge = snapshot.edges().get(0);
                    helper.assertTrue(edge.viaInventory(), "The link should be marked as running through the shared inventory");
                    helper.assertTrue(snapshot.nodes().get(edge.from()).kind() == ManagerSnapshot.Kind.IMPORTER,
                            "The shared-inventory link should start at the importer");
                    helper.assertTrue(snapshot.nodes().get(edge.to()).kind() == ManagerSnapshot.Kind.EXPORTER,
                            "The shared-inventory link should end at the exporter");
                })
                .thenSucceed();
    }

    private static boolean containerHas(GameTestHelper helper, BlockPos pos, net.minecraft.world.item.Item item) {
        ChestBlockEntity chest = helper.getBlockEntity(pos, ChestBlockEntity.class);
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).is(item)) return true;
        }
        return false;
    }

    // An exporter pushes a diamond stack and a dirt stack into a Distributor. The Distributor's item upgrade lets
    // items through and its filter only lets diamonds through, so both chests in range should end up with diamonds
    // (an even spread) and neither with dirt. The source chest is out of range so it isn't a target itself.
    private static void distributorSpreadsFilteredItemsToNearbyMachines(GameTestHelper helper) {
        int originalRange = StartupConfig.distributorRange.get();
        StartupConfig.distributorRange.set(3);
        helper.runBeforeTestEnd(() -> StartupConfig.distributorRange.set(originalRange));

        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourceChestPos = new BlockPos(1, 1, 2);
        BlockPos distributorPos = new BlockPos(5, 1, 1);
        BlockPos firstChestPos = new BlockPos(7, 1, 1);
        BlockPos secondChestPos = new BlockPos(5, 1, 3);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourceChestPos, Blocks.CHEST);
        helper.setBlock(distributorPos, RoutersBlocks.DISTRIBUTOR.get(), Direction.SOUTH);
        helper.setBlock(firstChestPos, Blocks.CHEST);
        helper.setBlock(secondChestPos, Blocks.CHEST);

        ChestBlockEntity source = helper.getBlockEntity(sourceChestPos, ChestBlockEntity.class);
        source.setItem(0, new ItemStack(Items.DIAMOND, 16));
        source.setItem(1, new ItemStack(Items.DIRT, 16));

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(distributorPos)));

        DistributorBlockEntity distributor = helper.getBlockEntity(distributorPos, DistributorBlockEntity.class);
        distributor.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        distributor.getFilterItemHandler().set(0, ItemResource.of(Items.DIAMOND), 1);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertTrue(containerHas(helper, firstChestPos, Items.DIAMOND), "First machine should have received diamonds");
                    helper.assertTrue(containerHas(helper, secondChestPos, Items.DIAMOND), "Second machine should have received diamonds");
                })
                .thenExecute(() -> {
                    helper.assertTrue(!containerHas(helper, firstChestPos, Items.DIRT), "The Distributor's filter should have kept dirt out of the first machine");
                    helper.assertTrue(!containerHas(helper, secondChestPos, Items.DIRT), "The Distributor's filter should have kept dirt out of the second machine");
                    helper.assertTrue(containerHas(helper, sourceChestPos, Items.DIRT), "Dirt should still be in the source chest");
                })
                .thenSucceed();
    }

    // The Distributor only accepts a resource type it has an upgrade for, so with none installed nothing is passed on.
    private static void distributorWithoutUpgradeDistributesNothing(GameTestHelper helper) {
        int originalRange = StartupConfig.distributorRange.get();
        StartupConfig.distributorRange.set(3);
        helper.runBeforeTestEnd(() -> StartupConfig.distributorRange.set(originalRange));

        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourceChestPos = new BlockPos(1, 1, 2);
        BlockPos distributorPos = new BlockPos(5, 1, 1);
        BlockPos chestPos = new BlockPos(7, 1, 1);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourceChestPos, Blocks.CHEST);
        helper.setBlock(distributorPos, RoutersBlocks.DISTRIBUTOR.get(), Direction.SOUTH);
        helper.setBlock(chestPos, Blocks.CHEST);

        helper.getBlockEntity(sourceChestPos, ChestBlockEntity.class).setItem(0, new ItemStack(Items.DIAMOND, 16));

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.ITEM_UPGRADE_1.get()), 1);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(distributorPos)));

        helper.startSequence()
                .thenIdle(150)
                .thenExecute(() -> helper.assertTrue(!containerHas(helper, chestPos, Items.DIAMOND),
                        "A Distributor with no item upgrade should not have passed anything on"))
                .thenSucceed();
    }
}
