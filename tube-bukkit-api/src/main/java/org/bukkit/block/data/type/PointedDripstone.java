package org.bukkit.block.data.type;

public interface PointedDripstone extends org.bukkit.block.data.Waterlogged {

    org.bukkit.block.BlockFace getVerticalDirection();

    public enum Thickness {
        TIP_MERGE,
        TIP,
        FRUSTUM,
        MIDDLE,
        BASE
    }
}
