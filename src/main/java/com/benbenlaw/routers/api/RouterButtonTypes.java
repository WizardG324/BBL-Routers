package com.benbenlaw.routers.api;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class RouterButtonTypes {
    public static final Map<Identifier, ButtonType> BUTTONS = new LinkedHashMap<>();

    public static final ButtonType ITEM_FILTER = register(new ButtonType(
            Routers.identifier("item"), 7, 31,
            "filter_buttons/item", "filter_buttons/item_hover",
            "tooltip.routers.button.item", "tooltip.routers.button.item.locked", "tooltip.routers.menu.item",
            RoutersTags.Items.ITEM_UPGRADES, ButtonType.hex("818181")));

    public static final ButtonType FLUID_FILTER = register(new ButtonType(
            Routers.identifier("fluid"), 25, 31,
            "filter_buttons/fluid", "filter_buttons/fluid_hover",
            "tooltip.routers.button.fluid", "tooltip.routers.button.fluid.locked", "tooltip.routers.menu.fluid",
            RoutersTags.Items.FLUID_UPGRADES, ButtonType.hex("0A61B8")));

    public static final ButtonType ENERGY_FILTER = register(new ButtonType(
            Routers.identifier("energy"), 43, 31,
            "filter_buttons/energy", "filter_buttons/energy_hover",
            "tooltip.routers.button.energy", "tooltip.routers.button.energy.locked", "tooltip.routers.menu.energy",
            RoutersTags.Items.RF_UPGRADES, ButtonType.hex("B70000")));

    public static ButtonType register(ButtonType type) {
        BUTTONS.put(type.getId(), type);
        return type;
    }

}