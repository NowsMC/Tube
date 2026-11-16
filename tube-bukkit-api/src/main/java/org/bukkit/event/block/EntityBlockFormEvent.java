package org.bukkit.event.block;

public class EntityBlockFormEvent extends org.bukkit.event.block.BlockFormEvent {
    private org.bukkit.entity.Entity entity;

    public EntityBlockFormEvent(org.bukkit.entity.Entity p0, org.bukkit.block.Block p1, org.bukkit.block.BlockState p2) {
        super(p1, p2);
        this.entity = p0;
    }

    public org.bukkit.entity.Entity getEntity() {
        return entity;
    }
}
