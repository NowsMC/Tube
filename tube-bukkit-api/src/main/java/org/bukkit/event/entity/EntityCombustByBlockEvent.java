package org.bukkit.event.entity;

public class EntityCombustByBlockEvent extends org.bukkit.event.entity.EntityCombustEvent {
    private org.bukkit.block.Block combuster;

    public EntityCombustByBlockEvent(org.bukkit.block.Block p0, org.bukkit.entity.Entity p1, int p2) {
        this(p0, p1, (float) p2);
    }

    public EntityCombustByBlockEvent(org.bukkit.block.Block p0, org.bukkit.entity.Entity p1, float p2) {
        super(p1, p2);
        this.combuster = p0;
    }

    public org.bukkit.block.Block getCombuster() {
        return combuster;
    }
}
