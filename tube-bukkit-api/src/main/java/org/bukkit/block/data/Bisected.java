package org.bukkit.block.data;

public interface Bisected extends org.bukkit.block.data.BlockData {

    org.bukkit.block.data.Bisected.Half getHalf();

    void setHalf(org.bukkit.block.data.Bisected.Half p0);

    public enum Half {
        TOP,
        BOTTOM
    }
}
