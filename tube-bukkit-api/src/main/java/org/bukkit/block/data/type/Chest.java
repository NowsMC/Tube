package org.bukkit.block.data.type;

public interface Chest extends org.bukkit.block.data.Directional, org.bukkit.block.data.Waterlogged {

    org.bukkit.block.data.type.Chest.Type getType();

    void setType(org.bukkit.block.data.type.Chest.Type p0);

    public enum Type {
        SINGLE,
        LEFT,
        RIGHT
    }
}
