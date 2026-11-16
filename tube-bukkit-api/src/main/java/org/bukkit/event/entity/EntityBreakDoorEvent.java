package org.bukkit.event.entity;

public class EntityBreakDoorEvent extends org.bukkit.event.entity.EntityChangeBlockEvent {

    public EntityBreakDoorEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.block.Block p1) {
        super(p0, p1, org.bukkit.Bukkit.createBlockData("minecraft:air"));
    }

    public org.bukkit.entity.LivingEntity getEntity() {
        return (org.bukkit.entity.LivingEntity) entity;
    }
}
