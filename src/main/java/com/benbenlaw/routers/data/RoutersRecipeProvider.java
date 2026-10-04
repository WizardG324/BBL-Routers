package com.benbenlaw.routers.data;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RoutersRecipeProvider extends RecipeProvider {

    public RoutersRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
            super(packOutput, provider);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider, @NotNull RecipeOutput recipeOutput) {
            return new RoutersRecipeProvider(provider, recipeOutput);
        }

        @Override
        public @NotNull String getName() {
            return Routers.MOD_ID + " Recipes";
        }
    }

    @Override
    protected void buildRecipes() {

        //Mod Filter
        shaped(RecipeCategory.MISC, RoutersItems.MOD_FILTER.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', ItemTags.LOGS)
                .define('C', Items.NAME_TAG)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Tag Filter
        shaped(RecipeCategory.MISC, RoutersItems.TAG_FILTER.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_GOLD)
                .define('B', ItemTags.LOGS)
                .define('C', Items.NAME_TAG)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Stock Filter
        shaped(RecipeCategory.MISC, RoutersItems.STOCK_FILTER.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.GEMS_DIAMOND)
                .define('B', ItemTags.LOGS)
                .define('C', Items.NAME_TAG)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Exporter
        shaped(RecipeCategory.MISC, RoutersBlocks.EXPORTER.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', ItemTags.LOGS)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Importer
        shaped(RecipeCategory.MISC, RoutersBlocks.IMPORTER.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', ItemTags.LOGS)
                .define('C', Items.HOPPER)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Exporter / Importer
        shapeless(RecipeCategory.MISC, RoutersBlocks.IMPORTER_EXPORTER.get())
                .requires(RoutersBlocks.EXPORTER.get())
                .requires(RoutersBlocks.IMPORTER.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_exporter", has(RoutersBlocks.EXPORTER.get()))
                .save(output);

        //Router Manager
        shaped(RecipeCategory.MISC, RoutersBlocks.ROUTER_MANAGER.get())
                .pattern("ABC")
                .pattern("DDD")
                .pattern("DDD")
                .define('A', RoutersBlocks.EXPORTER.get())
                .define('B', RoutersBlocks.IMPORTER.get())
                .define('C', RoutersBlocks.IMPORTER_EXPORTER.get())
                .define('D', Tags.Items.INGOTS_IRON)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Distributor
        shaped(RecipeCategory.MISC, RoutersBlocks.DISTRIBUTOR.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_GOLD)
                .define('B', ItemTags.LOGS)
                .define('C', RoutersBlocks.IMPORTER.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Connector
        shaped(RecipeCategory.MISC, RoutersItems.CONNECTOR.get())
                .pattern(" AA")
                .pattern(" BA")
                .pattern("A  ")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', Tags.Items.RODS_WOODEN)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Energy
        shaped(RecipeCategory.MISC, RoutersItems.RF_UPGRADE_1.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.DUSTS_REDSTONE)
                .define('B', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.STORAGE_BLOCKS_REDSTONE)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.RF_UPGRADE_2.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.DUSTS_REDSTONE)
                .define('B', Tags.Items.INGOTS_GOLD)
                .define('C', RoutersItems.RF_UPGRADE_1.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.RF_UPGRADE_3.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.DUSTS_REDSTONE)
                .define('B', Tags.Items.GEMS_DIAMOND)
                .define('C', RoutersItems.RF_UPGRADE_2.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.RF_UPGRADE_4.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.DUSTS_REDSTONE)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', RoutersItems.RF_UPGRADE_3.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.RF_UPGRADE_5.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.DUSTS_REDSTONE)
                .define('B', Tags.Items.NETHER_STARS)
                .define('C', RoutersItems.RF_UPGRADE_4.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHER_STAR))
                .save(output);

        //Item
        shaped(RecipeCategory.MISC, RoutersItems.ITEM_UPGRADE_1.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.CHESTS_WOODEN)
                .define('B', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.CHESTS_WOODEN)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.ITEM_UPGRADE_2.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.CHESTS_WOODEN)
                .define('B', Tags.Items.INGOTS_GOLD)
                .define('C', RoutersItems.ITEM_UPGRADE_1.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.ITEM_UPGRADE_3.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.CHESTS_WOODEN)
                .define('B', Tags.Items.GEMS_DIAMOND)
                .define('C', RoutersItems.ITEM_UPGRADE_2.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.ITEM_UPGRADE_4.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.CHESTS_WOODEN)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', RoutersItems.ITEM_UPGRADE_3.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.ITEM_UPGRADE_5.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.CHESTS_WOODEN)
                .define('B', Tags.Items.NETHER_STARS)
                .define('C', RoutersItems.ITEM_UPGRADE_4.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHER_STAR))
                .save(output);

        //Fluid
        shaped(RecipeCategory.MISC, RoutersItems.FLUID_UPGRADE_1.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.BUCKETS)
                .define('B', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.BUCKETS)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.FLUID_UPGRADE_2.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.BUCKETS)
                .define('B', Tags.Items.INGOTS_GOLD)
                .define('C', RoutersItems.FLUID_UPGRADE_1.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.FLUID_UPGRADE_3.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.BUCKETS)
                .define('B', Tags.Items.GEMS_DIAMOND)
                .define('C', RoutersItems.FLUID_UPGRADE_2.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.FLUID_UPGRADE_4.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.BUCKETS)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', RoutersItems.FLUID_UPGRADE_3.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.FLUID_UPGRADE_5.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.BUCKETS)
                .define('B', Tags.Items.NETHER_STARS)
                .define('C', RoutersItems.FLUID_UPGRADE_4.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHER_STAR))
                .save(output);

        //Speed
        shaped(RecipeCategory.MISC, RoutersItems.SPEED_UPGRADE_1.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.GEMS_QUARTZ)
                .define('B', Tags.Items.INGOTS_IRON)
                .define('C', Tags.Items.GEMS_QUARTZ)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.SPEED_UPGRADE_2.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.GEMS_QUARTZ)
                .define('B', Tags.Items.INGOTS_GOLD)
                .define('C', RoutersItems.SPEED_UPGRADE_1.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.SPEED_UPGRADE_3.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.GEMS_QUARTZ)
                .define('B', Tags.Items.GEMS_DIAMOND)
                .define('C', RoutersItems.SPEED_UPGRADE_2.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.MISC, RoutersItems.SPEED_UPGRADE_4.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.GEMS_QUARTZ)
                .define('B', Tags.Items.INGOTS_NETHERITE)
                .define('C', RoutersItems.SPEED_UPGRADE_3.get())
                .group(Routers.MOD_ID)
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
                .save(output);

        //Mod Filter
        //Tag Filter

        //Round Robin
        shaped(RecipeCategory.MISC, RoutersItems.ROUND_ROBIN_UPGRADE.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', Tags.Items.INGOTS_GOLD)
                .define('C', Tags.Items.GEMS_DIAMOND)
                .group(Routers.MOD_ID)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        //Dimensional
        shaped(RecipeCategory.MISC, RoutersItems.DIMENSIONAL_UPGRADE.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', Tags.Items.INGOTS_GOLD)
                .define('C', Tags.Items.ENDER_PEARLS)
                .group(Routers.MOD_ID)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        //Blacklist
        shaped(RecipeCategory.MISC, RoutersItems.BLACKLIST_UPGRADE.get())
                .pattern("ABA")
                .pattern("B B")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_IRON)
                .define('B', Items.PAPER)
                .group(Routers.MOD_ID)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        //Ignore NBT
        shaped(RecipeCategory.MISC, RoutersItems.IGNORE_NBT_UPGRADE.get())
                .pattern("ABA")
                .pattern("B B")
                .pattern("ABA")
                .define('A', Tags.Items.INGOTS_GOLD)
                .define('B', Items.PAPER)
                .group(Routers.MOD_ID)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);





    }
}