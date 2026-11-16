package org.bukkit.entity;

public interface Horse extends org.bukkit.entity.AbstractHorse {

    org.bukkit.entity.Horse.Color getColor();

    void setColor(org.bukkit.entity.Horse.Color p0);

    org.bukkit.entity.Horse.Style getStyle();

    void setStyle(org.bukkit.entity.Horse.Style p0);

    void setCarryingChest(boolean p0);

    org.bukkit.inventory.HorseInventory getInventory();

    public enum Style {
        NONE,
        WHITE,
        WHITEFIELD,
        WHITE_DOTS,
        BLACK_DOTS
    }

    public enum Color {
        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN
    }

    public enum Variant {
        HORSE,
        DONKEY,
        MULE,
        UNDEAD_HORSE,
        SKELETON_HORSE,
        LLAMA,
        CAMEL
    }
}
