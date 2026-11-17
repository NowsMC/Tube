package org.bukkit.event.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

public class EntityDeathEvent extends EntityEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final java.util.List<org.bukkit.inventory.ItemStack> drops;
    private int droppedExp;

    public EntityDeathEvent(LivingEntity entity) {
        this(entity, new java.util.ArrayList<>(), 0);
    }

    public EntityDeathEvent(LivingEntity entity, java.util.List<org.bukkit.inventory.ItemStack> drops, int droppedExp) {
        super(entity);
        this.drops = drops;
        this.droppedExp = droppedExp;
    }

    public java.util.List<org.bukkit.inventory.ItemStack> getDrops() {
        return drops;
    }

    public int getDroppedExp() {
        return droppedExp;
    }

    public void setDroppedExp(int exp) {
        this.droppedExp = exp;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
