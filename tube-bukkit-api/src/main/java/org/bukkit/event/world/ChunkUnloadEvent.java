package org.bukkit.event.world;

public class ChunkUnloadEvent extends org.bukkit.event.world.ChunkEvent {
    private boolean saveChunk;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public ChunkUnloadEvent(org.bukkit.Chunk p0) {
        this(p0, true);
    }

    public ChunkUnloadEvent(org.bukkit.Chunk p0, boolean p1) {
        super(p0);
        this.saveChunk = p1;
    }

    public boolean isSaveChunk() {
        return saveChunk;
    }

    public void setSaveChunk(boolean p0) {
        this.saveChunk = p0;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
