package com.benbenlaw.routers.gametest;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.block.entity.DistributorBlockEntity;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterExporterBlockEntity;
import com.benbenlaw.routers.manager.ManagerScanner;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.benbenlaw.routers.networking.packets.RenameRouterFromManager;
import com.benbenlaw.routers.networking.packets.EditLinkFromManager;
import com.benbenlaw.routers.manager.ManagerSnapshot;
import com.benbenlaw.routers.transfers.RoutersTransfers;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Map;
import java.util.WeakHashMap;
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

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENERGY_TRANSFER =
            TEST_FUNCTIONS.register("energy_transfer", () -> RoutersGameTests::energyMovesBetweenEnergyBlocks);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANAGER_EDIT_LINKS =
            TEST_FUNCTIONS.register("manager_edit_links", () -> RoutersGameTests::managerLinksAndUnlinksRouters);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANAGER_RENAME =
            TEST_FUNCTIONS.register("manager_rename", () -> RoutersGameTests::managerRenamesAndClampsNames);

    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MANAGER_SCAN_CACHE =
            TEST_FUNCTIONS.register("manager_scan_cache", () -> RoutersGameTests::managerReusesRecentScans);

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

        event.registerTest(Routers.identifier("energy_transfer"),
                new FunctionGameTestInstance(ENERGY_TRANSFER.getKey(), longTestData));

        event.registerTest(Routers.identifier("manager_edit_links"),
                new FunctionGameTestInstance(MANAGER_EDIT_LINKS.getKey(), testData));

        event.registerTest(Routers.identifier("manager_rename"),
                new FunctionGameTestInstance(MANAGER_RENAME.getKey(), testData));

        event.registerTest(Routers.identifier("manager_scan_cache"),
                new FunctionGameTestInstance(MANAGER_SCAN_CACHE.getKey(), testData));

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

                    helper.assertTrue(snapshot.edges().stream().allMatch(e -> (e.types() & RoutersTransfers.bit(Routers.identifier("item"))) != 0),
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

    // Vanilla chests have no energy, so while the energy test runs they're given a throwaway energy handler. It does nothing
    // otherwise.
    private static boolean energyTestActive;
    private static final Map<BlockEntity, SimpleEnergyHandler> TEST_ENERGY = new WeakHashMap<>();

    public static void registerTestCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, BlockEntityType.CHEST,
                (chest, side) -> energyTestActive ? TEST_ENERGY.computeIfAbsent(chest, key -> new SimpleEnergyHandler(100000, 100000)) : null);
    }

    // Energy goes through the same generic transfer a new resource type would use. The sink only has room for 300 of the 800
    // the exporter offers each time, so none of the rest may disappear from the source.
    private static void energyMovesBetweenEnergyBlocks(GameTestHelper helper) {
        energyTestActive = true;
        helper.runBeforeTestEnd(() -> energyTestActive = false);

        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos sourcePos = new BlockPos(1, 1, 2);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos sinkPos = new BlockPos(5, 1, 2);

        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(sourcePos, Blocks.CHEST);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(sinkPos, Blocks.CHEST);

        SimpleEnergyHandler source = new SimpleEnergyHandler(100000, 100000);
        source.set(5000);
        SimpleEnergyHandler sink = new SimpleEnergyHandler(300, 300);
        TEST_ENERGY.put(helper.getBlockEntity(sourcePos, ChestBlockEntity.class), source);
        TEST_ENERGY.put(helper.getBlockEntity(sinkPos, ChestBlockEntity.class), sink);

        ExporterBlockEntity exporter = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);
        exporter.getUpgradeItemHandler().set(0, ItemResource.of(RoutersItems.RF_UPGRADE_1.get()), 1);
        exporter.toggleImporterPosition(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(importerPos)));

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(sink.getAmountAsLong() == 300, "The sink should have been filled, but holds " + sink.getAmountAsLong()))
                .thenIdle(100)
                .thenExecute(() -> helper.assertTrue(source.getAmountAsLong() + sink.getAmountAsLong() == 5000,
                        "No energy should be lost: source " + source.getAmountAsLong() + " + sink " + sink.getAmountAsLong()))
                .thenSucceed();
    }

    // What the Router Manager's link packets do, minus the player: link, unlink, and the pairs it must refuse.
    private static void managerLinksAndUnlinksRouters(GameTestHelper helper) {
        BlockPos managerPos = new BlockPos(1, 1, 5);
        BlockPos exporterPos = new BlockPos(1, 1, 1);
        BlockPos importerPos = new BlockPos(5, 1, 1);
        BlockPos distributorPos = new BlockPos(9, 1, 1);

        helper.setBlock(managerPos, RoutersBlocks.ROUTER_MANAGER.get());
        helper.setBlock(exporterPos, RoutersBlocks.EXPORTER.get(), Direction.SOUTH);
        helper.setBlock(importerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(distributorPos, RoutersBlocks.DISTRIBUTOR.get(), Direction.SOUTH);

        var level = helper.getLevel();
        BlockPos manager = helper.absolutePos(managerPos);
        GlobalPos exporter = GlobalPos.of(level.dimension(), helper.absolutePos(exporterPos));
        GlobalPos importer = GlobalPos.of(level.dimension(), helper.absolutePos(importerPos));
        GlobalPos distributor = GlobalPos.of(level.dimension(), helper.absolutePos(distributorPos));

        ExporterBlockEntity exporterEntity = helper.getBlockEntity(exporterPos, ExporterBlockEntity.class);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertTrue(EditLinkFromManager.apply(level, manager, exporter, importer, true), "Linking an exporter to an importer should work");
                    helper.assertTrue(exporterEntity.importerPositions.contains(importer), "The exporter should now list the importer");
                    helper.assertTrue(ImporterCoreAt(level, importer).exporterPositions.contains(exporter), "The importer should list the exporter back");

                    helper.assertTrue(EditLinkFromManager.apply(level, manager, exporter, distributor, true), "Linking to a distributor should work");
                    helper.assertTrue(exporterEntity.importerPositions.size() == 2, "Both links should be there");

                    helper.assertTrue(EditLinkFromManager.apply(level, manager, exporter, importer, false), "Unlinking should work");
                    helper.assertTrue(!exporterEntity.importerPositions.contains(importer), "The importer should be gone from the exporter");
                    helper.assertTrue(!ImporterCoreAt(level, importer).exporterPositions.contains(exporter), "The exporter should be gone from the importer");
                    helper.assertTrue(exporterEntity.importerPositions.contains(distributor), "The distributor link should be untouched");

                    helper.assertTrue(!EditLinkFromManager.apply(level, manager, exporter, exporter, true), "A router can't be linked to itself");
                    helper.assertTrue(!EditLinkFromManager.apply(level, manager, importer, exporter, true), "An importer can't be the exporting end");
                    helper.assertTrue(!EditLinkFromManager.apply(level, manager, distributor, importer, true), "A distributor can't be the exporting end");
                })
                .thenSucceed();
    }

    private static com.benbenlaw.routers.block.entity.ImporterCore ImporterCoreAt(net.minecraft.server.level.ServerLevel level, GlobalPos pos) {
        return com.benbenlaw.routers.block.entity.ImporterCore.at(level, pos.pos());
    }

    // Names are cleaned, cut to length, and only accepted for routers in the manager's network. A too-long name that got
    // stored some other way must not stop the chart being sent.
    private static void managerRenamesAndClampsNames(GameTestHelper helper) {
        // far enough that the manager's starting radius never reaches it, and nothing links it in
        BlockPos managerPos = new BlockPos(1, 1, 1);
        BlockPos nearPos = new BlockPos(1, 1, 3);
        BlockPos farPos = new BlockPos(1 + StartupConfig.managerSeedRadius.get() + 10, 1, 1);

        helper.setBlock(managerPos, RoutersBlocks.ROUTER_MANAGER.get());
        helper.setBlock(nearPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);
        helper.setBlock(farPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);

        var level = helper.getLevel();
        BlockPos manager = helper.absolutePos(managerPos);
        BlockPos near = helper.absolutePos(nearPos);
        BlockPos far = helper.absolutePos(farPos);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertTrue(RenameRouterFromManager.apply(level, manager, near, "  Smelter  "), "Renaming a charted router should work");
                    helper.assertTrue(helper.getBlockEntity(nearPos, com.benbenlaw.routers.block.entity.ImporterBlockEntity.class).getRouterName().equals("Smelter"),
                            "The name should be trimmed");

                    helper.assertTrue(!RenameRouterFromManager.apply(level, manager, far, "Nope"), "A router outside the network must not be renamed");
                    helper.assertTrue(helper.getBlockEntity(farPos, com.benbenlaw.routers.block.entity.ImporterBlockEntity.class).getRouterName().isEmpty(),
                            "The far router should still be unnamed");

                    var near1 = helper.getBlockEntity(nearPos, com.benbenlaw.routers.block.entity.ImporterBlockEntity.class);
                    near1.setRouterName("x".repeat(100));
                    helper.assertTrue(near1.getRouterName().length() == com.benbenlaw.routers.api.NamedRouter.MAX_NAME_LENGTH, "Names are cut to the maximum length");

                    // the chart still encodes, and the name survives the trip
                    ManagerSnapshot snapshot = ManagerScanner.scanFresh(level, manager);
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
                    ManagerSnapshot.STREAM_CODEC.encode(buf, snapshot);
                    ManagerSnapshot decoded = ManagerSnapshot.STREAM_CODEC.decode(buf);
                    helper.assertTrue(decoded.nodes().stream().anyMatch(node -> node.name().length() == com.benbenlaw.routers.api.NamedRouter.MAX_NAME_LENGTH),
                            "The renamed router should come through the packet with its name");
                })
                .thenSucceed();
    }

    // Reads of the network reuse a recent scan; a fresh scan after a change replaces it.
    private static void managerReusesRecentScans(GameTestHelper helper) {
        BlockPos managerPos = new BlockPos(1, 1, 1);
        BlockPos routerPos = new BlockPos(3, 1, 1);

        helper.setBlock(managerPos, RoutersBlocks.ROUTER_MANAGER.get());
        helper.setBlock(routerPos, RoutersBlocks.IMPORTER.get(), Direction.SOUTH);

        var level = helper.getLevel();
        BlockPos manager = helper.absolutePos(managerPos);

        helper.startSequence()
                .thenExecute(() -> {
                    ManagerSnapshot first = ManagerScanner.scanCached(level, manager);
                    helper.assertTrue(ManagerScanner.scanCached(level, manager) == first, "A second read straight away should reuse the first scan");

                    RenameRouterFromManager.apply(level, manager, helper.absolutePos(routerPos), "Fresh");
                    ManagerSnapshot fresh = ManagerScanner.scanFresh(level, manager);
                    helper.assertTrue(fresh != first, "A fresh scan should replace the cached one");
                    helper.assertTrue(fresh.nodes().stream().anyMatch(node -> node.name().equals("Fresh")), "The fresh scan should show the new name");
                    helper.assertTrue(ManagerScanner.scanCached(level, manager) == fresh, "Reads after that should reuse the fresh scan");
                })
                .thenSucceed();
    }
}
