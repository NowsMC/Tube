package org.bukkit.event.world;

public class ChunkPopulateEvent extends org.bukkit.event.world.ChunkEvent {
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ChunkPopulateEvent(org.bukkit.Chunk p0) {
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
