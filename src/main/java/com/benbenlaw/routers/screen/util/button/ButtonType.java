package com.benbenlaw.routers.screen.util.button;

import com.benbenlaw.routers.item.RoutersItems;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ButtonType {
    private final Identifier id;
    private final int xOffset;
    private final int yOffset;
    private final String texture;
    private final String textureHover;
    private final String buttonTooltip;
    private final String lockedTooltip;
    private final String menuName;
    private final TagKey<Item> unlockedBy;
    private final float[] color;

    public ButtonType(Identifier id, int xOffset, int yOffset, String texture, String textureHover,
                      String buttonTooltip, String lockedTooltip, String menuName, TagKey<Item> unlockedBy, float[] color) {
        this.id = id;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.texture = texture;
        this.textureHover = textureHover;
        this.buttonTooltip = buttonTooltip;
        this.lockedTooltip = lockedTooltip;
        this.menuName = menuName;
        this.unlockedBy = unlockedBy;
        this.color = color;
    }

    public Identifier getId() { return id; }
    public int getOffsetX() { return xOffset; }
    public int getOffsetY() { return yOffset; }
    public String getTexture() { return texture; }
    public String getTextureHover() { return textureHover; }
    public String getButtonTooltip() { return buttonTooltip; }
    public String getLockedTooltip() { return lockedTooltip; }
    public String getMenuName() { return menuName; }
    public TagKey<Item> getUnlockedBy() { return unlockedBy; }
    public float[] getColor() { return color; }


    public static float[] hex(String hex) {
        int color = Integer.parseInt(hex, 16);
        return new float[]{
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        };
    }
}