package org.bukkit.block.data.type;

public interface Door extends org.bukkit.block.data.Bisected, org.bukkit.block.data.Directional, org.bukkit.block.data.Openable, org.bukkit.block.data.Powerable {

    void setHinge(org.bukkit.block.data.type.Door.Hinge p0);

    public enum Hinge {
        LEFT,
        RIGHT
    }
}
