package org.bukkit.event.entity;

public class EntityCombustByEntityEvent extends org.bukkit.event.entity.EntityCombustEvent {
    private org.bukkit.entity.Entity combuster;

    public EntityCombustByEntityEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Entity p1, int p2) {
        this(p0, p1, (float) p2);
    }

    public EntityCombustByEntityEvent(org.bukkit.entity.Entity p0, org.bukkit.entity.Entity p1, float p2) {
        super(p1, p2);
        this.combuster = p0;
    }

    public org.bukkit.entity.Entity getCombuster() {
        return combuster;
    }
}
