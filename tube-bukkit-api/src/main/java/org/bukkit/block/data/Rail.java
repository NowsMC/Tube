package org.bukkit.block.data;

public interface Rail extends org.bukkit.block.data.Waterlogged {

    org.bukkit.block.data.Rail.Shape getShape();

    public enum Shape {
        NORTH_SOUTH,
        EAST_WEST,
        ASCENDING_EAST,
        ASCENDING_WEST,
        ASCENDING_NORTH,
        ASCENDING_SOUTH,
        SOUTH_EAST,
        SOUTH_WEST,
        NORTH_WEST,
        NORTH_EAST
    }
}
