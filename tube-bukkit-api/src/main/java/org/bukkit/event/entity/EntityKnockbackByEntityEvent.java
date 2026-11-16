package org.bukkit.event.entity;

public class EntityKnockbackByEntityEvent extends org.bukkit.event.entity.EntityKnockbackEvent {
    private org.bukkit.entity.Entity source;

    public EntityKnockbackByEntityEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.entity.Entity p1, org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause p2, double p3, org.bukkit.util.Vector p4, org.bukkit.util.Vector p5) {
        super(p0, p2, p3, p4, p5);
        this.source = p1;
    }

    public org.bukkit.entity.Entity getSourceEntity() {
        return source;
    }
}
