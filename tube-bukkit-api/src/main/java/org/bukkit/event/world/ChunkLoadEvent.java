package org.bukkit.event.world;

public class ChunkLoadEvent extends org.bukkit.event.world.ChunkEvent {
    private boolean newChunk;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ChunkLoadEvent(org.bukkit.Chunk p0, boolean p1) {
        super(p0);
        this.newChunk = p1;
    }

    public boolean isNewChunk() {
        return newChunk;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
