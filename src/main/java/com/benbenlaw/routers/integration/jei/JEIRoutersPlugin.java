package com.benbenlaw.routers.integration.jei;

import com.benbenlaw.core.integration.jei.GhostFilter;
import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.block.RoutersBlocks;
import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.screen.upgrade.FilterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;

@JeiPlugin
public class JEIRoutersPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Routers.MOD_ID, "jei_plugin");
    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(FilterScreen.class, new GhostFilter());
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersItems.CONNECTOR.get())), "wrench");

        Collection<String> exporterAliases = List.of("exporter", "cable", "wireless");
        Collection<String> importerAliases = List.of("importer", "cable", "wireless");

        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersBlocks.EXPORTER.get())),  exporterAliases);
        registration.addAliases(VanillaTypes.ITEM_STACK, List.of(new ItemStack(RoutersBlocks.IMPORTER.get())),  importerAliases);
    }
}
