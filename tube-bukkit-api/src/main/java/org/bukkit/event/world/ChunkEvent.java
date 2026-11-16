package org.bukkit.event.world;

public abstract class ChunkEvent extends org.bukkit.event.world.WorldEvent {
    protected org.bukkit.Chunk chunk;

    protected ChunkEvent(org.bukkit.Chunk p0) {
        super((org.bukkit.World) null);
        this.chunk = p0;
    }

    public org.bukkit.Chunk getChunk() {
        return chunk;
    }
}
