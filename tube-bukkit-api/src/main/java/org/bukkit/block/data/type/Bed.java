package org.bukkit.block.data.type;

public interface Bed extends org.bukkit.block.data.Directional {

    org.bukkit.block.data.type.Bed.Part getPart();

    void setPart(org.bukkit.block.data.type.Bed.Part p0);

    boolean isOccupied();

    public enum Part {
        HEAD,
        FOOT
    }
}
