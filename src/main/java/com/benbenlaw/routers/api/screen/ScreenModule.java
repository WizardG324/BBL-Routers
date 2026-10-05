package com.benbenlaw.routers.api.screen;

import com.benbenlaw.routers.api.ConfigurableRouterBlockEntity;
import com.benbenlaw.routers.screen.upgrade.FilterMenu;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.function.TriConsumer;

public record ScreenModule(
        Identifier buttonType,
        TriConsumer<FilterMenu, ConfigurableRouterBlockEntity, Integer> slotAdder
) {}