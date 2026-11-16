package org.bukkit.event.world;

public abstract class WorldEvent extends org.bukkit.event.Event {
    private org.bukkit.World world;

    public WorldEvent(org.bukkit.World p0) {
        this(p0, false);
    }

    public WorldEvent(org.bukkit.World p0, boolean p1) {
        super(p1);
        this.world = p0;
    }

    public org.bukkit.World getWorld() {
        return world;
    }
}
