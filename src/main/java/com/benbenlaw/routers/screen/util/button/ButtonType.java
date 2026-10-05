package com.benbenlaw.routers.screen.util.button;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

// The filter button for one resource type. Everything but the upgrade tag follows from the id, so for a resource
// registered as "namespace:name" the files and lang keys are:
//   sprite          namespace:filter_buttons/name (and name_hover)
//   tooltips        tooltip.namespace.button.name, tooltip.namespace.button.name.locked, tooltip.namespace.menu.name
//   chart legend    gui.namespace.manager.legend.name
// The colour used for beams and chart links is set on the client in RouterUIRenderers.
public class ButtonType {
    private final Identifier id;
    private final TagKey<Item> unlockedBy;

    public ButtonType(Identifier id, TagKey<Item> unlockedBy) {
        this.id = id;
        this.unlockedBy = unlockedBy;
    }

    public Identifier getId() { return id; }
    public TagKey<Item> getUnlockedBy() { return unlockedBy; }

    public Identifier getTexture() {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "filter_buttons/" + id.getPath());
    }

    public Identifier getTextureHover() {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "filter_buttons/" + id.getPath() + "_hover");
    }

    public String getButtonTooltip() { return "tooltip." + id.getNamespace() + ".button." + id.getPath(); }
    public String getLockedTooltip() { return getButtonTooltip() + ".locked"; }
    public String getMenuName() { return "tooltip." + id.getNamespace() + ".menu." + id.getPath(); }
    public String getLegendKey() { return "gui." + id.getNamespace() + ".manager.legend." + id.getPath(); }

    public static float[] hex(String hex) {
        int color = Integer.parseInt(hex, 16);
        return new float[]{
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        };
    }
}
