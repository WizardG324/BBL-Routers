package com.benbenlaw.routers.util;

import com.benbenlaw.core.block.entity.handler.item.SyncableItemHandler;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.screen.util.button.ButtonType;
import com.benbenlaw.routers.transfers.RoutersTransfers;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

public class UpgradeUtil {

    // Only one upgrade from each of these can be installed at a time: speed, plus one per registered resource.
    public static List<TagKey<Item>> getUpgradeTypeTags() {
        List<TagKey<Item>> tags = new ArrayList<>();
        tags.add(RoutersTags.Items.SPEED_UPGRADES);
        for (TransferModule<?> module : RoutersTransfers.TRANSFER_MODULES_REGISTRY) {
            tags.add(module.upgradeTag());
        }
        return tags;
    }

    public static boolean hasUpgradeTypeAlready(SyncableItemHandler upgradeItemHandler, ItemStack stack) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemStack existing = upgradeItemHandler.getResource(i).toStack();
            if (existing.isEmpty()) continue;

            for (TagKey<Item> tag : getUpgradeTypeTags()) {
                if (stack.is(tag) && existing.is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasUpgrade(SyncableItemHandler upgradeItemHandler, ButtonType type) {
        for (int i = 0; i < upgradeItemHandler.size(); i++) {
            ItemResource resource = upgradeItemHandler.getResource(i);
            if (resource.toStack().is(type.getUnlockedBy())) {
                return true;
            }
        }
        return false;
    }
}
