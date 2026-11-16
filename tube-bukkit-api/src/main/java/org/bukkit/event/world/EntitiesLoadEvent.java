package org.bukkit.event.world;

public class EntitiesLoadEvent extends org.bukkit.event.world.ChunkEvent {
    private java.util.List<org.bukkit.entity.Entity> entities;
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public EntitiesLoadEvent(org.bukkit.Chunk p0, java.util.List<org.bukkit.entity.Entity> p1) {
        super(p0);
        this.entities = p1;
    }

    public java.util.List<org.bukkit.entity.Entity> getEntities() {
        return entities;
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}
