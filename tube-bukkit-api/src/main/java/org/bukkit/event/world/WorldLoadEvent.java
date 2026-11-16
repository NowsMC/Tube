package org.bukkit.event.world;

public class WorldLoadEvent extends org.bukkit.event.world.WorldEvent {
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public WorldLoadEvent(org.bukkit.World p0) {
        super(p0);
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
